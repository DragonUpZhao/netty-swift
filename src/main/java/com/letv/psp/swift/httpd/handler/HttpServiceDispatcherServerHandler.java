package com.letv.psp.swift.httpd.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.letv.psp.swift.common.util.ClassLoaderUtils;
import com.letv.psp.swift.common.util.IOUtils;
import com.letv.psp.swift.common.util.PropertyGetter;
import com.letv.psp.swift.common.util.ReflectUtils;
import com.letv.psp.swift.common.util.StringUtils;
import com.letv.psp.swift.core.service.MessageContext;
import com.letv.psp.swift.core.service.ServiceLocator;
import com.letv.psp.swift.httpd.SwiftHttpRequest;
import com.letv.psp.swift.httpd.service.ContentTypeResolver;
import com.letv.psp.swift.httpd.service.Result;
import com.letv.psp.swift.httpd.service.ResultRenderer;
import com.letv.psp.swift.httpd.service.ResultRendererFactory;

@Sharable
public class HttpServiceDispatcherServerHandler extends SimpleChannelUpstreamHandler {
	private static final Logger logger = LoggerFactory.getLogger("swift");

	private static final String defaultContentType = "text/html; charset=UTF-8";
	private static final String INVALID_SERVICE_URL_PREFIX = "invalid service url: ";
	private static final String SERVICE_NOT_FOUND = "service not found,serviceName: ";

	private static final String HTTP_HEADER_X_FORWARD_FOR = "X-Forwarded-For";
	private static final String HTTP_HEADER_X_REAL_IP = "X-Real-IP";

	private static final String SERVICE_KEY = "service";
	private static final String METHOD_KEY = "method";

	private static ServiceLocator serviceLocator;
	private static final Map<String, String> contentTypeMapper = new HashMap<String, String>();

	static {
		contentTypeMapper.put("html", "text/html; charset=UTF-8");
		contentTypeMapper.put("textplain", "text/plain; charset=UTF-8");
		contentTypeMapper.put("stream", "application/octet-stream");
		contentTypeMapper.put("json", "text/plain; charset=UTF-8");
		contentTypeMapper.put("xml", "text/xml; charset=UTF-8");

	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, defaultContentType);

		try {
			service(ctx, e, request, response);
		} catch (Throwable ex) {
			StringWriter stringWriter = new StringWriter();
			String errorMsg = ex.getMessage() == null ? StringUtils.EMPTY : ex.getMessage();
			logger.error(errorMsg, ex);
			response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain;charset=UTF-8");
			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			ex.getCause().printStackTrace(new PrintWriter(stringWriter));
			response.setContent(ChannelBuffers.copiedBuffer(stringWriter.toString(), CharsetUtil.UTF_8));
		} finally {
			closeChannelIfNecessary(ctx.getChannel(), request, response);
		}
	}

	private static String getLocalHostIp() throws Exception {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("windows")) {
			return InetAddress.getLocalHost().getHostAddress();
		} else {
			InetAddress ip = null;
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				String interfaceName = networkInterface.getName();
				if (!interfaceName.equals("eth0")) {
					continue;
				}
				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					ip = inetAddresses.nextElement();
					if (!ip.isLoopbackAddress()) {
						return ip.getHostAddress();
					}
				}
			}
		}
		return StringUtils.EMPTY;
	}

	private void service(ChannelHandlerContext ctx, MessageEvent e, HttpRequest request, HttpResponse response)
			throws Throwable {
		String url = canonicalizeUrl(request.getUri());
		request.setUri(url);

		boolean success = true;
		MessageContext msgCtx = null;
		long start = System.nanoTime();

		String serviceName = null, methodName = null;
		Object invokeService = null;

		try {
			msgCtx = buildMessageContext(ctx, request);
			if (msgCtx == null) {
				throw new NullPointerException("Null MessageContext object");
			}
			serviceName = msgCtx.getParameter(SERVICE_KEY);
			methodName = msgCtx.getParameter(METHOD_KEY);

			if (StringUtils.isBlank(serviceName) || StringUtils.isBlank(methodName)) {
				response.setStatus(HttpResponseStatus.NOT_FOUND);
				response.setContent(ChannelBuffers.copiedBuffer(INVALID_SERVICE_URL_PREFIX.concat(request.getUri()),
						CharsetUtil.UTF_8));
				return;
			}

			initServiceLocatorIfNecessary();
			invokeService = serviceLocator.getService(serviceName);
			if (invokeService == null) {
				throw new NullPointerException(SERVICE_NOT_FOUND.concat(serviceName));
			}

			Method _invokeMethod = ReflectUtils.getMethod(invokeService.getClass(), methodName,
					new Class[] { MessageContext.class });
			Object invokeResult = _invokeMethod.invoke(invokeService, new Object[] { msgCtx });
			if (invokeResult == null) {
				throw new NullPointerException("Null invoked result");
			}

			if (invokeService instanceof ContentTypeResolver) {
				// 兼容原来旧有的service实现
				sendResponse(response, invokeResult, getContentType(invokeService, methodName));
			} else {
				Result responseResult = _invokeMethod.getAnnotation(Result.class);
				if (responseResult == null) {
					responseResult = invokeService.getClass().getAnnotation(Result.class);
				}
				if (responseResult == null) {
					// 如果不存在responseResult注解的话，兼容原来逻辑
					sendResponse(response, invokeResult, getContentType(invokeService, methodName));
				} else {
					sendResponse(response, invokeResult, responseResult, msgCtx);
				}
			}
		} catch (Throwable ex) {
			success = false;
			throw ex;
		} finally {
			if (url.endsWith("/favicon.ico")) {
				return;
			} else {
				long now = System.nanoTime();
				long elapsed = (now - start) / 1000000;
				logger.info("from: {}, serviceUrl: {}, service: {}, method: {}, elapsed: {}ms, success: {}", 
						new Object[] {getLocalHostIp(), url, serviceName, methodName, elapsed, success});
			}
		}
	}

	private void sendResponse(HttpResponse response, Object invokeResult, Result result, MessageContext msgCtx) {
		String resultTypeName = result.type().getName();
		String contentType = result.contentType();

		// 如果contentType不空的话，忽略resultType
		if (StringUtils.isNotBlank(contentType)) {
			// HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE,
			// contentType);
		} else {
			contentType = StringUtils.isBlank(contentTypeMapper.get(resultTypeName)) ? defaultContentType
					: contentTypeMapper.get(resultTypeName);
		}
		ResultRenderer renderer = ResultRendererFactory.getResultRenderer(result.type());
		Object renderOut = renderer.renderResult(invokeResult, msgCtx);
		sendResponse(response, renderOut, contentType);
	}

	private static void initServiceLocatorIfNecessary() {
		if (serviceLocator != null) {
			return;
		}
		String serviceLocatorClazz = PropertyGetter.getString("serviceLocatorImpl");
		if (StringUtils.isBlank(serviceLocatorClazz)) {
			serviceLocator = ServiceLocator.SIMPLE;
		} else {
			serviceLocator = (ServiceLocator) ClassLoaderUtils.getInstance(serviceLocatorClazz);
			if (serviceLocator == null) {
				logger.info("实例化类{}失败", serviceLocatorClazz);
			}
		}
		if (serviceLocator == null) {
			serviceLocator = ServiceLocator.SIMPLE;
		}
	}

	private String getRemoteAddress(ChannelHandlerContext ctx, HttpRequest request) {
		String remoteAddr = HttpHeaders.getHeader(request, HTTP_HEADER_X_FORWARD_FOR);
		if (StringUtils.isBlank(remoteAddr)) {
			remoteAddr = HttpHeaders.getHeader(request, HTTP_HEADER_X_REAL_IP);
		}
		if (StringUtils.isBlank(remoteAddr)) {
			remoteAddr = ((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getAddress().getHostAddress();
		}
		return remoteAddr;
	}

	private String getContentType(Object invokeService, String methodName) {
		String contentType = null;
		if (invokeService instanceof ContentTypeResolver) {
			ContentTypeResolver contentTypeResolver = (ContentTypeResolver) invokeService;
			contentType = contentTypeResolver.getContentType(methodName);
			if (StringUtils.isBlank(contentType)) {
				contentType = contentTypeResolver.getDefaultContentType();
			}
		}
		if (StringUtils.isBlank(contentType)) {
			contentType = defaultContentType;
		}
		return contentType;
	}

	private void sendResponse(HttpResponse response, Object invokeResult, String contentType) {
		HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, contentType);
		if (invokeResult instanceof String) {
			response.setContent(ChannelBuffers.copiedBuffer((String) invokeResult, CharsetUtil.UTF_8));
		} else if (invokeResult instanceof InputStream) {
			response.setContent(ChannelBuffers.copiedBuffer(toByteArray((InputStream) invokeResult)));
		} else if (invokeResult instanceof byte[]) {
			response.setContent(ChannelBuffers.copiedBuffer((byte[]) invokeResult));
		}
	}

	private byte[] toByteArray(InputStream result) {
		if (result == null) {
			return new byte[] {};
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] byteBuffer = new byte[1024 * 8];
		int count = 0;
		try {
			while ((count = result.read(byteBuffer)) > 0) {
				baos.write(byteBuffer, 0, count);
			}
			return baos.toByteArray();
		} catch (IOException ex) {
			logger.error("", ex);
		} finally {
			IOUtils.closeQuietly(baos);
		}
		return new byte[] {};
	}

	private MessageContext buildMessageContext(ChannelHandlerContext ctx, HttpRequest request) {
		SwiftHttpRequest _request = new SwiftHttpRequest(request);
		MessageContext msgCtx = new MessageContext(_request.getParameterMap(), null, _request.getCookieMap(),
				_request.getInputStream(),_request.getHeaderMap());
		String remoteAddress = getRemoteAddress(ctx, request);

		msgCtx.getContext().put("remoteAddress", remoteAddress);
		msgCtx.getContext().put("referer", _request.getHeader(HttpHeaders.Names.REFERER));
		return msgCtx;
	}

	private String canonicalizeUrl(String uri) {
		// TODO url规范化，默认直接原样返回
		if (uri.endsWith("?")) {
			uri = uri.substring(0, uri.lastIndexOf("?"));
		}
		return uri;
	}

	private void closeChannelIfNecessary(Channel channel, HttpRequest request, HttpResponse response) {
		try {
			boolean isKeepAlive = isKeepAlive(request);
			if (isKeepAlive) {
				if (logger.isDebugEnabled()) {
					logger.debug("KeepAlived connection, keep channel open until keepalive-timeout");
				}
				HttpHeaders.setContentLength(response, response.getContent().readableBytes());
				// 如果http协议版本不是默认keepalive且请求时keepalive请求，需要在http响应头部增加connection:keep-alive
				if (!request.getProtocolVersion().isKeepAliveDefault()) {
					HttpHeaders.setKeepAlive(response, true);
				}
			}
            if(channel.isOpen() || channel.isConnected()){
                ChannelFuture channelFuture = channel.write(response);
                if (!isKeepAlive) {
                    if (logger.isDebugEnabled()) {
                        logger.info("Not keepAlived connection, close it");
                    }
                    channelFuture.addListener(ChannelFutureListener.CLOSE);
                }
            }
		} catch (Throwable ex) {
			//logger.error("", ex);
			channel.close();
		}
	}

	private boolean isKeepAlive(HttpRequest request) {
		String connection = request.getHeader(HttpHeaders.Names.CONNECTION);
		if (Values.CLOSE.equalsIgnoreCase(connection)) {
			return false;
		}
		if (request.getProtocolVersion().isKeepAliveDefault()) {
			return !Values.CLOSE.equalsIgnoreCase(connection);
		} else {
			return Values.KEEP_ALIVE.equalsIgnoreCase(connection);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		logger.error("Netty based httpserver exceptionCaught, close channel", e.getCause());
		e.getChannel().close();
	}
}
