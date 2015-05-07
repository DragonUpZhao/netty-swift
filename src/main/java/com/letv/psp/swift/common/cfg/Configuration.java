package com.letv.psp.swift.common.cfg;

import java.util.regex.Pattern;

/**
 * 系统配置接口
 */
public interface Configuration {
	Pattern VALUE_RESOLVER_PATTERN = Pattern.compile("\\$\\{\\w+\\}", Pattern.CASE_INSENSITIVE);

	String getString(String key);

	int getInt(String key);

	String[] getStringArray(String key);

	long getLong(String key);

	float getFloat(String key);

	double getDouble(String key);
	
	boolean containsKey(String key);
}
