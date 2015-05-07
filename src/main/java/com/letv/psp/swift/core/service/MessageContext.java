package com.letv.psp.swift.core.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class MessageContext {
	private static final Logger logger = LoggerFactory.getLogger(MessageContext.class);

	private Map<String, String[]> parameterMap;
	private Map<String, Object> sessionMap;
	private Map<String, String> cookieMap;
	private Map<String, String> headerMap;

	private final Map<String, Object> context;
	private InputStream inputStream;

	private String reffer;
	private String remoteAddr;
	private String requestUri;

	public MessageContext() {
		this.parameterMap = new HashMap<String, String[]>();
		this.sessionMap = new HashMap<String, Object>();
		this.cookieMap = new HashMap<String, String>();
		this.context = new HashMap<String, Object>();
		this.headerMap = new HashMap<String, String>();
	}

	public MessageContext(Map<String, String[]> parameters, Map<String, Object> session, Map<String, String> cookieMap) {
		this.parameterMap = parameters;
		this.sessionMap = session;
		this.cookieMap = cookieMap;
		this.context = new HashMap<String, Object>();
	}

	public MessageContext(Map<String, String[]> parameters, Map<String, Object> session, Map<String, String> cookieMap,
			Map<String, String> headerMap) {
		this.parameterMap = parameters;
		this.sessionMap = session;
		this.cookieMap = cookieMap;
		this.context = new HashMap<String, Object>();
		this.headerMap = headerMap;
	}

	public MessageContext(Map<String, String[]> parameters, Map<String, Object> session, Map<String, String> cookieMap,
			InputStream in,Map<String, String> headerMap) {
		this.parameterMap = parameters;
		this.sessionMap = session;
		this.cookieMap = cookieMap;
		this.context = new HashMap<String, Object>();
		this.inputStream = in;
		this.headerMap=headerMap;
	}

	public MessageContext(Map<String, String[]> parameters, Map<String, Object> session, Map<String, String> cookieMap,
			InputStream in) {
		this.parameterMap = parameters;
		this.sessionMap = session;
		this.cookieMap = cookieMap;
		this.context = new HashMap<String, Object>();
		this.inputStream = in;
	}
	
	/**
	 * @return the reffer
	 */
	public String getReffer() {
		return reffer;
	}

	/**
	 * @param reffer
	 *            the reffer to set
	 */
	public void setReffer(String reffer) {
		this.reffer = reffer;
	}

	/**
	 * @return the remoteAddr
	 */
	public String getRemoteAddr() {
		return remoteAddr;
	}

	/**
	 * @param remoteAddr
	 *            the remoteAddr to set
	 */
	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	/**
	 * @return the requestUri
	 */
	public String getRequestUri() {
		return requestUri;
	}

	/**
	 * @param requestUri
	 *            the requestUri to set
	 */
	public void setRequestUri(String requestUri) {
		this.requestUri = requestUri;
	}

	/**
	 * @return the sessionMap
	 */
	public Map<String, Object> getSessionMap() {
		return sessionMap;
	}

	/**
	 * @return the cookieMap
	 */
	public Map<String, String> getCookieMap() {
		return cookieMap;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qiyi.vrs.netty.http.service.MessageContext#getContext()
	 */
	public Map<String, Object> getContext() {
		return this.context;
	}

	public String getParameter(String name) {
		if (parameterMap == null) {
			return null;
		}
		String[] parameterValues = parameterMap.get(name);
		if (parameterValues != null && parameterValues.length > 0) {
			return parameterValues[0];
		}
		return null;
	}

	public Integer getParameterInt(String name) {
		try {
			return Integer.valueOf(getParameter(name));
		} catch (Exception ex) {
			// DO NOTHING
		}
		return null;
	}

	public Integer getParameterInt(String name, Integer defaultValue) {
		try {
			return Integer.valueOf(getParameter(name));
		} catch (Exception ex) {
			// DO NOTHING
		}
		return defaultValue;
	}

	public Long getParameterLong(String name) {
		try {
			return Long.valueOf(getParameter(name));
		} catch (Exception ex) {
			// DO NOTHING
		}
		return null;
	}

	public Long getParameterLong(String name, Long defaultValue) {
		try {
			return Long.valueOf(getParameter(name));
		} catch (Exception ex) {
			// DO NOTHING
		}
		return defaultValue;
	}

	public String[] getParameterValues(String name) {
		if (parameterMap == null) {
			return null;
		}
		return parameterMap.get(name);
	}

	public Object getSessionAttribute(String attributeName) {
		if (sessionMap == null) {
			return null;
		}
		return sessionMap.get(attributeName);
	}

	public String getCookie(String name) {
		if (cookieMap == null) {
			return null;
		}
		return cookieMap.get(name);
	}

	public Object getContentParameter(String name) {
		return this.context.get(name);
	}

	public Map<String, String[]> getParameterMap() {
		return parameterMap;
	}

	/**
	 * @param parameterMap
	 *            the parameterMap to set
	 */
	public void setParameterMap(Map<String, String[]> parameterMap) {
		this.parameterMap = parameterMap;
	}

	/**
	 * @param sessionMap
	 *            the sessionMap to set
	 */
	public void setSessionMap(Map<String, Object> sessionMap) {
		this.sessionMap = sessionMap;
	}

	/**
	 * @param cookieMap
	 *            the cookieMap to set
	 */
	public void setCookieMap(Map<String, String> cookieMap) {
		this.cookieMap = cookieMap;
	}

	public Map<String,String> getHeaderMap(){
		return this.headerMap;
	}
	
	public String getHeader(String headerName) {
		if (headerMap == null) {
			return null;
		}
		return headerMap.get(headerName);
	}

}
