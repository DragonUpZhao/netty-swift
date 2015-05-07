package com.letv.psp.swift.common.httpclient;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.letv.psp.swift.common.util.IOUtils;
import com.letv.psp.swift.common.util.StringUtils;

/**
 * 基于{@link java.net.HttpURLConnection}实现的简单的Http客户端
 * 
 */
public class SimpleHttpClient implements HttpClient {
	private static final Logger logger = LoggerFactory.getLogger(SimpleHttpClient.class);

	private static final String GET = "GET";
	private static final String POST = "POST";

	private static final String DEFAULT_CHARSET = "UTF-8";

	private int connectionTimeout = 30000;
	private int soTimeout = 30000;
	private boolean debug = false;
	private boolean lineFeed = true;
	private boolean trustAny = false;

	private boolean followRedirects = true;
	private boolean keepAlive = false;

	private String contentType = null;
	private String url = null;
	private String rawStream = null;
	private String clientCertAlias = null;
	private String basicAuthUsername = null;
	private String basicAuthPassword = null;

	private Map<String, Object> parameters = null;
	private Map<String, String> headers = null;
	private String requestBody;

	private URL requestUrl = null;
	private URLConnection con = null;

	public SimpleHttpClient() {
	}

	public SimpleHttpClient(URL url) {
		this.url = url.toExternalForm();
	}

	public SimpleHttpClient(String url) {
		this.url = url;
	}

	public SimpleHttpClient(URL url, Map<String, Object> parameters) {
		this.url = url.toExternalForm();
		this.parameters = parameters;
	}

	public SimpleHttpClient(String url, Map<String, Object> parameters) {
		this.url = url;
		this.parameters = parameters;
	}

	public SimpleHttpClient(String url, Map<String, Object> parameters, Map<String, String> headers) {
		this(url, parameters);
		this.headers = headers;
	}

	public SimpleHttpClient(URL url, Map<String, Object> parameters, Map<String, String> headers) {
		this(url, parameters);
		this.headers = headers;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isLineFeed() {
		return lineFeed;
	}

	public void setLineFeed(boolean lineFeed) {
		this.lineFeed = lineFeed;
	}

	public boolean isTrustAny() {
		return trustAny;
	}

	public void setTrustAny(boolean trustAny) {
		this.trustAny = trustAny;
	}

	public boolean isFollowRedirects() {
		return followRedirects;
	}

	public void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRawStream() {
		return rawStream;
	}

	public void setRawStream(String rawStream) {
		this.rawStream = rawStream;
	}

	public String getClientCertAlias() {
		return clientCertAlias;
	}

	public void setClientCertAlias(String clientCertAlias) {
		this.clientCertAlias = clientCertAlias;
	}

	public String getBasicAuthUsername() {
		return basicAuthUsername;
	}

	public void setBasicAuthUsername(String basicAuthUsername) {
		this.basicAuthUsername = basicAuthUsername;
	}

	public String getBasicAuthPassword() {
		return basicAuthPassword;
	}

	public void setBasicAuthPassword(String basicAuthPassword) {
		this.basicAuthPassword = basicAuthPassword;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public URL getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(URL requestUrl) {
		this.requestUrl = requestUrl;
	}

	public URLConnection getCon() {
		return con;
	}

	public void setCon(URLConnection con) {
		this.con = con;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qiyi.vrs.vis.commons.httpclient.HttpClient#get(java.lang.String)
	 */
	@Override
	public String get() {
		return sendHttpRequest("get");
	}

	private String resolveContentEncoding(String contentType) {
		if (logger.isDebugEnabled()) {
			logger.debug("contentType: {}", contentType);
		}
		if (StringUtils.isBlank(contentType)) {
			return null;
		}
		// text/html;charset=GB2312
		String[] contentTypeInfos = contentType.split(";");
		if (contentTypeInfos.length > 1) {
			return contentTypeInfos[1].split("=")[1].trim();
		}
		return null;
	}

	@Override
	public String post() {
		return sendHttpRequest("post");
	}

	public int getResponseCode() {
		if (con == null) {
			throw new HttpClientException("Connection not yet established");
		}
		if (!(con instanceof HttpURLConnection)) {
			throw new HttpClientException("Connection is not HTTP; no response code");
		}
		try {
			return ((HttpURLConnection) con).getResponseCode();
		} catch (Exception ex) {
			throw new HttpClientException(ex.getMessage(), ex);
		}
	}

	public static void main(String[] args) throws Exception {
		SimpleHttpClient httpClient = new SimpleHttpClient();
		httpClient.setUrl("http://10.1.30.74:8080/url/?");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("c", "片花");
		parameters.put("l", "");
		parameters.put("y", "null");
		parameters.put("domain", "");
		httpClient.setParameters(parameters);
		// System.out.println(System.getProperties());
		String str = httpClient.get();
		System.out.println(str);
	}

	private String sendHttpRequest(String method) {
		InputStream is = null;
		try {
			is = sendHttpRequestStream(method);
			int statusCode = getResponseCode();
			if (statusCode != HttpURLConnection.HTTP_OK) {
				logger.error("==获取响应内容失败，responseCode: {}==", statusCode);
				return null;
			}
			String charset = null;
			String contentType = con.getContentType();
			// FIXME 这个地方不适用URLConnection的guessContentTypeFromStream方法，如果需要猜码
			// 的话使用juniversalchardet(http://code.google.com/p/juniversalchardet)或者http://sourceforge.net/projects/jchardet
			/*
			 * if(StringUtils.isBlank(contentType)){
			 * contentType=URLConnection.guessContentTypeFromStream(is); }
			 */
			charset = resolveContentEncoding(contentType);
			return IOUtils.toString(is, charset);
		} catch (Exception ex) {
			logger.error("", ex);
		} finally {
			IOUtils.closeQuietly(is);
			if (this.con != null) {
				if (this.con instanceof HttpURLConnection) {
					if (logger.isDebugEnabled()) {
						logger.debug("close remote http connection");
					}
					((HttpURLConnection) con).disconnect();
				}
			}
		}
		return null;
	}

	@Override
	public InputStream getStream() {
		return sendHttpRequestStream("get");
	}

	@Override
	public InputStream postStream() {
		return sendHttpRequestStream("post");
	}

	private InputStream sendHttpRequestStream(String method) {
		String arguments = null;
		InputStream is = null;
		OutputStreamWriter oss = null;

		if (url == null) {
			throw new HttpClientException("Cannot process a null URL");
		}
		if (rawStream != null) {
			arguments = rawStream;
		} else if (parameters != null && parameters.size() > 0) {
			arguments = urlEncodeParameters(parameters);
		}
		if (method.equalsIgnoreCase(GET) && !StringUtils.isBlank(arguments)) {
			url = buildRequestURL(url, arguments);
		} else if (method.equals(POST) && !StringUtils.isBlank(arguments)) {
			// 如果既包括请求参数又包括请求主体，需要将请求参数作为queryString附加到url后面
			if (!StringUtils.isBlank(requestBody)) {
				url = buildRequestURL(url, arguments);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("请求url: {}", url);
		}
		try {
			requestUrl = new URL(url);
			con = requestUrl.openConnection();
			con.setConnectTimeout(this.connectionTimeout);
			con.setReadTimeout(this.soTimeout);
			HttpURLConnection httpURLConnection = (HttpURLConnection) con;
			httpURLConnection.setInstanceFollowRedirects(followRedirects);
			if (contentType != null) {
				httpURLConnection.setRequestProperty("Content-type", contentType);
			}
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);

			if (keepAlive) {
				con.setRequestProperty("Connection", "Keep-Alive");
			} else {
				con.setRequestProperty("Connection", "close");
			}
			if (method.equalsIgnoreCase(POST)) {
				if (contentType == null) {
					con.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
				}
			}
			if (headers != null && headers.size() > 0) {
				for (Entry<String, String> headEntry : headers.entrySet()) {
					con.setRequestProperty(headEntry.getKey(), headEntry.getValue());
				}
			}
			if (method.equalsIgnoreCase(POST)) {
				oss = new OutputStreamWriter(con.getOutputStream(), DEFAULT_CHARSET);
				if (StringUtils.isNotBlank(requestBody)) {
					oss.write(requestBody);
				} else {
					if (StringUtils.isNotBlank(arguments)) {
						oss.write(arguments);
					}
				}
				oss.flush();
				oss.close();
			}
			is = con.getInputStream();
			return is;
		} catch (Exception ex) {
			logger.error("", ex);
			throw new HttpClientException("", ex);
		}
	}

	private String buildRequestURL(String url, String arguments) {
		if (url.endsWith("?")) {
			url = StringUtils.concat(url, arguments);
		} else {
			if (url.contains("?")) {
				url = StringUtils.concat(url, "&", arguments);
			}else{
				url=StringUtils.concat(url,"?",arguments);
			}
		}
		return url;
	}

	private String urlEncodeParameters(Map<String, Object> parameters) {
		if (parameters == null || parameters.size() == 0) {
			return StringUtils.EMPTY;
		}
		StringBuilder argumentsBuffer = new StringBuilder();

		for (Entry<String, Object> parameterEntry : parameters.entrySet()) {
			String parameterName = parameterEntry.getKey();
			Object parameterValue = parameterEntry.getValue();

			String valueStr = null;
			if (parameterName == null)
				continue;
			if (parameterValue instanceof String) {
				valueStr = (String) parameterValue;
			} else {
				if (parameterValue != null) {
					valueStr = parameterValue.toString();
				} else {
					valueStr = StringUtils.EMPTY;
				}
			}
			if (StringUtils.isNotBlank(argumentsBuffer)) {
				argumentsBuffer.append("&");
			}
			try {
				// argumentsBuffer.append(URLEncoder.encode(parameterName,
				// DEFAULT_CHARSET));
				argumentsBuffer.append(parameterName);
				argumentsBuffer.append("=");
				argumentsBuffer.append(URLEncoder.encode(valueStr, DEFAULT_CHARSET));
			} catch (UnsupportedEncodingException ex) {
				// ignore this ex
			}
		}
		return argumentsBuffer.toString();
	}
}
