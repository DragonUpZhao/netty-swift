package com.letv.psp.swift.httpd.service;

public class BaseHttpService implements ContentTypeResolver {
	
	private static final String DEFAULT_CONTENT_TYPE = "text/html;charset=UTF-8";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qiyi.swift.http.service.ContentTypeResolver#getContentType(java.lang
	 * .String)
	 */
	@Override
	public String getContentType(String method) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qiyi.swift.http.service.ContentTypeResolver#getDefaultContentType()
	 */
	@Override
	public String getDefaultContentType() {
		return DEFAULT_CONTENT_TYPE;
	}

}
