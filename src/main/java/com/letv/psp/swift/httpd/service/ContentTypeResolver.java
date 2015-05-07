package com.letv.psp.swift.httpd.service;

public interface ContentTypeResolver {
	
	String getContentType(String method);

	String getDefaultContentType();
}
