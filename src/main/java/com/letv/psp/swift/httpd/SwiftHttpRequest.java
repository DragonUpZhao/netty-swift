package com.letv.psp.swift.httpd;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.letv.psp.swift.common.util.PropertyGetter;
import com.letv.psp.swift.common.util.StringUtils;
import com.letv.psp.swift.common.util.UrlRewrite;

public class SwiftHttpRequest {
	private static final Logger logger = LoggerFactory.getLogger(SwiftHttpRequest.class);
	private static final String POST_FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

	private static final boolean COOKIEDECODE = PropertyGetter.getInt("cookieDecode", 1) == 1 ? true : false;
	private static final boolean HEADERDECODE = PropertyGetter.getInt("headerDecode", 1) == 1 ? true : false;

	private final HttpRequest wrapperHttpRequest;
	private boolean requestParametersParsed;
	private boolean requestBodyParsed;
	private boolean requestHeaderParsed;

	private Map<String, String[]> parameterMap;
	private Map<String, String> cookieMap;
	private InputStream inputStream;
	private Map<String, String> headerMap;

	private String requestUrl;

	public SwiftHttpRequest(HttpRequest wrapperHttpRequest) {
		this.wrapperHttpRequest = wrapperHttpRequest;
		this.parameterMap = new HashMap<String, String[]>();
		this.cookieMap = new HashMap<String, String>();
		this.headerMap = new HashMap<String, String>();
	}

	public Map<String, String> getHeaderMap() {
		if (!HEADERDECODE) {
			return Collections.emptyMap();
		}
		if (!requestHeaderParsed) {
			parseRequestHeaders();
		}
		return headerMap;
	}

	private void parseRequestHeaders() {
		if (requestHeaderParsed) {
			return;
		}
		List<Entry<String, String>> headers = wrapperHttpRequest.getHeaders();
		for (Entry<String, String> header : headers) {
			headerMap.put(header.getKey(), header.getValue());
		}
		this.requestHeaderParsed=true;
	}

	public String getParameter(String name) {
		if (!requestParametersParsed) {
			parseRequestParameters();
		}
		String[] values = parameterMap.get(name);
		if (values != null && values.length > 0) {
			return values[0];
		}
		return null;
	}

	private void parseRequestParameters() {
		if (requestParametersParsed) {
			return;
		}
		this.requestParametersParsed = true;
		HttpMethod _method = wrapperHttpRequest.getMethod();

		QueryStringDecoder queryStringDecoder = null;
		QueryStringDecoder postParameterDecoder = null;

		String canonicalizeUrl = canonicalizeUrl(wrapperHttpRequest.getUri());
		String origQueryString = getQueryString(canonicalizeUrl);

		String requestUri = StringUtils.substringBefore(canonicalizeUrl, "?");
		String url = UrlRewrite.rewriteUrl(requestUri);

		if (!url.equals(canonicalizeUrl) && !StringUtils.isBlank(origQueryString)) {
			if (url.contains("?")) {
				url = StringUtils.concat(url, "&", origQueryString);
			} else {
				url = StringUtils.concat(url, "?", origQueryString);
			}
		}

		this.requestUrl = url;

		Map<String, List<String>> _parameterMap = new HashMap<String, List<String>>();
		queryStringDecoder = new QueryStringDecoder(url);
		_parameterMap.putAll(queryStringDecoder.getParameters());
		if (_method == HttpMethod.POST) {
			String contentType = HttpHeaders.getHeader(wrapperHttpRequest, HttpHeaders.Names.CONTENT_TYPE);
			if (POST_FORM_CONTENT_TYPE.equalsIgnoreCase(contentType)
					&& (HttpHeaders.getContentLength(wrapperHttpRequest) > 0 || wrapperHttpRequest.getContent()
							.readableBytes() > 0)) {
				// FIXME 修复解析post参数的bug,必须将第二个参数hashPath设置为false才可以
				postParameterDecoder = new QueryStringDecoder(wrapperHttpRequest.getContent().toString(
						CharsetUtil.UTF_8), false);
				_parameterMap.putAll(postParameterDecoder.getParameters());
			}
		}
		try {
			for (Entry<String, List<String>> entry : _parameterMap.entrySet()) {
				parameterMap.put(entry.getKey(), entry.getValue().toArray(new String[] {}));
			}

			if (COOKIEDECODE) {
				// FIXME
				// 增加一个开关来决定是否需要解析cookie,在配置文件中增加cookieDecode属性，1-代表开启,0表示关闭
				CookieDecoder cookieDecoder = new CookieDecoder();
				String cookieHeaderValue = HttpHeaders.getHeader(wrapperHttpRequest, HttpHeaders.Names.COOKIE);
				if (!StringUtils.isBlank(cookieHeaderValue)) {
					Set<Cookie> _cookies = cookieDecoder.decode(cookieHeaderValue);
					for (Cookie _cookie : _cookies) {
						cookieMap.put(_cookie.getName(), _cookie.getValue());
					}
				}
			}
		} catch (Throwable ex) {
			// FIXME 这里捕获Throwable异常，由于Netty本身自带的CookieDecoder中采用正则表达式来解析Cookie
			// 如果cookie值太长会导致StackOverFlowError错误
			logger.error("parse cookie error", ex);
		}
	}

	private String getQueryString(String canonicalizeUrl) {
		return StringUtils.trim(StringUtils.substringAfter(canonicalizeUrl, "?"));
	}

	public String[] getParameterValues(String name) {
		if (!requestParametersParsed) {
			parseRequestParameters();
		}
		return parameterMap.get(name);
	}

	public Map<String, String[]> getParameterMap() {
		if (!requestParametersParsed) {
			parseRequestParameters();
		}
		return parameterMap;
	}

	public HttpRequest getWrapperHttpRequest() {
		return this.wrapperHttpRequest;
	}

	public String getHeader(String name) {
		return HttpHeaders.getHeader(wrapperHttpRequest, name);
	}

	public Map<String, String> getCookieMap() {
		if (!requestParametersParsed) {
			parseRequestParameters();
		}
		return this.cookieMap;
	}

	public InputStream getInputStream() {
		if (requestBodyParsed) {
			return this.inputStream;
		}
		this.requestBodyParsed = true;
		HttpMethod _method = wrapperHttpRequest.getMethod();
		String contentType = wrapperHttpRequest.getHeader(HttpHeaders.Names.CONTENT_TYPE);
		if (_method == HttpMethod.POST
				&& !"multipart/form-data".equalsIgnoreCase(contentType)
				&& (HttpHeaders.getContentLength(wrapperHttpRequest) > 0 || wrapperHttpRequest.getContent()
						.readableBytes() > 0)) {
			return new SwiftInputStream(wrapperHttpRequest.getContent());
		}
		return null;
	}

	private String canonicalizeUrl(String uri) {
		// TODO url规范化，默认直接原样返回
		if (uri.endsWith("?")) {
			uri = uri.substring(0, uri.lastIndexOf("?"));
		}
		return uri;
	}

	public String getRequestUrl() {
		return requestUrl;
	}
}
