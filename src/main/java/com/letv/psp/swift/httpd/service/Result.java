package com.letv.psp.swift.httpd.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * html,xml,textplain,stream,velocity,freemarker,json
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Result {
	ResultType type() default ResultType.HTML;
	String contentType() default "";
}
