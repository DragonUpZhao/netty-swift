package com.letv.psp.swift.common.httpclient;

import java.io.InputStream;

/**
 * HttpClient接口定义
 */
public interface HttpClient {
	/**
	 * 执行HTTP get请求，返回原始响应流
	 * 
	 * @return
	 */
	InputStream getStream();

	/**
	 * 执行HTTP post请求，返回原始响应流
	 * 
	 * @return
	 */
	InputStream postStream();

	/**
	 * 发送Http GET请求并返回响应
	 * 
	 * @param url
	 * @return
	 */
	String get();

	/**
	 * 发送http POST请求并返回响应
	 * 
	 * @param url
	 *            请求url
	 * @param parameters
	 *            请求参数
	 * @param body
	 *            请求报文体
	 * @param contentType
	 *            报文类型
	 * @return
	 */
	String post();
}
