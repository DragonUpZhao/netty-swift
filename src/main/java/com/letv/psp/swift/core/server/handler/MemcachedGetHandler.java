package com.letv.psp.swift.core.server.handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.letv.psp.swift.common.util.ClassLoaderUtils;
import com.letv.psp.swift.common.util.PropertyGetter;
import com.letv.psp.swift.common.util.ReflectUtils;
import com.letv.psp.swift.common.util.StringUtils;
import com.letv.psp.swift.common.util.UrlRewrite;
import com.letv.psp.swift.core.service.MessageContext;
import com.letv.psp.swift.core.service.ServiceLocator;

/**
 * 兼容memcached协议的get命令实现，支持nginx-memcached模块通过长连接和后端server进行通讯
 */
public class MemcachedGetHandler extends SimpleChannelUpstreamHandler {
	private static final Logger logger = LoggerFactory.getLogger(MemcachedGetHandler.class);

	private static final String SPACE = " ";
	private static final String LINE_SEPARATOR = "\r\n";
	private static final String VALUE_END = "END";
	private static final String GET_COMMAND_PREFIX = "get ";
	private static final String COMMAND_ERROR = "ERROR";
	private static final String VALUE_PREFIX = "VALUE ";

	// 是否启用urlrewrite功能，默认启用，如果不需要此功能的话，建议在属性文件中增加urlrewrite.enable=0
	private static final boolean URLREWRITE_ENABLE = PropertyGetter.getInt("urlrewrite.enable", 0) == 1;
	private static ServiceLocator serviceLocator;

	static {
		initServiceLocatorIfNecessary();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		ChannelBuffer channelBuffer = (ChannelBuffer) e.getMessage();
		String command = channelBuffer.toString(CharsetUtil.UTF_8);
		
		if(!command.startsWith(GET_COMMAND_PREFIX)){
			sendErrorResponse(ctx, e);
			return;
		}
		try{
			handleRequest(command, ctx, e);
		}catch(Throwable ex){
			sendResponse(null, command, ctx);
		}
	}

	private void handleRequest(String command, ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		int SPACEIDX = command.indexOf(SPACE);
		String requestUrl = command.substring(SPACEIDX + 1);

		String serviceUrl = requestUrl;
		// 如果启用urlrewrite的话，执行urlrewrite
		if (URLREWRITE_ENABLE) {
			serviceUrl = UrlRewrite.rewriteUrl(requestUrl);
		}
		String serviceName = null;
		String methodName = null;
		try {
			// 从服务url中构造自定义的MessageContext实例
			MessageContext msgCtx = buildMessageContext(serviceUrl);
			serviceName = msgCtx.getParameter("service");
			methodName = msgCtx.getParameter("method");

			if (StringUtils.isBlank(serviceName) || StringUtils.isBlank(methodName)) {
				logger.warn("serviceName or methodName is blank");
				throw new IllegalArgumentException("Invalid request, service or method is null");
			}

			// 从服务定位器中查找具体服务实例并利用反射调用对应的服务方法
			Object invokeService = serviceLocator.getService(serviceName);
			if (invokeService == null) {
				logger.error("invokeService is not exists, serviceName: " + serviceName);
				throw new RuntimeException("Service not found, service: " + serviceName);
			}

			Method invokeMethod = ReflectUtils.getMethod(invokeService.getClass(), methodName,
					new Class[] { MessageContext.class });
			if (invokeMethod == null) {
				logger.error("invokeMethod not found, method: " + methodName);
				throw new RuntimeException("Method not found, method: " + methodName);
			}

			Object invokeResult = invokeMethod.invoke(invokeService, new Object[] { msgCtx });
			if (invokeResult == null) {
				throw new NullPointerException("Data not found");
			}
			sendResponse(invokeResult, requestUrl, ctx);
		} catch (Exception ex) {
			throw ex;
		} finally {
			// TODO 记录日志采用异步方式进行记录，避免采用阻塞IO同步写入，如果必须需要同步写入的话，
			// 务必增大I/O buffer
		}
	}

	private void sendErrorResponse(ChannelHandlerContext ctx, MessageEvent e) {
		StringBuilder sb = new StringBuilder();
		sb.append(COMMAND_ERROR).append(LINE_SEPARATOR);
		e.getChannel().write(ChannelBuffers.copiedBuffer(sb, CharsetUtil.UTF_8));
	}

	private MessageContext buildMessageContext(String serviceUrl) {
		QueryStringDecoder decoder = new QueryStringDecoder(serviceUrl);
		Map<String, List<String>> parameters = decoder.getParameters();

		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		// 这里采用初始化固定大小的数组来实现List.toArray功能，避免反射调用List.toArray(String[]{})提高性能
		// String[] _parameters = new String[];
		for (Entry<String, List<String>> entry : parameters.entrySet()) {
			String[] _parameters = new String[entry.getValue().size()];
			parameterMap.put(entry.getKey(), entry.getValue().toArray(_parameters));
		}

		return new MessageContext(parameterMap, null, null);
	}

	private void sendResponse(Object response, String key, ChannelHandlerContext ctx) {
		StringBuilder sb = new StringBuilder();
		if (response == null) {
			sb.append(VALUE_END).append(LINE_SEPARATOR);
			ctx.getChannel().write(ChannelBuffers.copiedBuffer(sb, CharsetUtil.UTF_8));
			return;
		}

		String resp = response.toString();
		// 这里按照memcached协议拼接响应内容
		String returnValue = VALUE_PREFIX + key + SPACE + 0 + SPACE + resp.length() + LINE_SEPARATOR + resp
				+ LINE_SEPARATOR + VALUE_END + LINE_SEPARATOR;
		ctx.getChannel().write(ChannelBuffers.copiedBuffer(returnValue, CharsetUtil.UTF_8));
	}

	private static void initServiceLocatorIfNecessary() {
		if (serviceLocator != null) {
			return;
		}
		String serviceLocatorClazz = PropertyGetter.getString("serviceLocatorImpl");
		logger.info("serviceLocatorImpl: {}", serviceLocatorClazz);
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
}
