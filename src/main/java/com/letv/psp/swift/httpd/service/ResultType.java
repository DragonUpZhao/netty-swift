package com.letv.psp.swift.httpd.service;

public enum ResultType {
	HTML("html"), // text/html
	JSON("json"), // application/json
	TEXTPLAIN("textplain"), // text/plain
	VELOCITY("velocity"), // velocity
	FREEMARKER("freemarker"), // freemarker
	XML("xml"), //text/xml
	STREAM("stream");
	
	private final String name;

	private ResultType(String name) {
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
}
