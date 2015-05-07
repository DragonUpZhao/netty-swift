package com.letv.psp.swift.common.cfg;

import java.util.Properties;
import java.util.regex.Matcher;

import com.letv.psp.swift.common.util.StringUtils;

public abstract class PropertiesConfiguration implements Configuration {
	static final Properties EMPTY_PROPERTIES = new Properties();

	protected Properties properties;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qiyi.vrs.vis.commons.configuration.Configuration#getString(java.lang
	 * .String)
	 */
	@Override
	public String getString(String key) {
		String _value = getAndProcessValue(key);
		return _value;
	}

	private String getAndProcessValue(String key) {
		String _value = properties.getProperty(key);
		if (StringUtils.isBlank(_value)) {
			return StringUtils.EMPTY;
		}
		StringBuffer result = new StringBuffer();
		Matcher matcher = Configuration.VALUE_RESOLVER_PATTERN.matcher(_value);

		String resolveKey = null;
		String matchStr = null;

		while (matcher.find()) {
			matchStr = matcher.group();
			resolveKey = StringUtils.substringBetween(matchStr, "${", "}");
			matcher.appendReplacement(result, properties.getProperty(resolveKey));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qiyi.vrs.vis.commons.configuration.Configuration#getInt(java.lang
	 * .String)
	 */
	@Override
	public int getInt(String key) {
		String value=getAndProcessValue(key);
		return StringUtils.toInt(value, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qiyi.vrs.vis.commons.configuration.Configuration#getStringArray(java
	 * .lang.String)
	 */
	@Override
	public String[] getStringArray(String key) {
		String value=getAndProcessValue(key);
		if(value==null){
			return null;
		}
		return value.split(",");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qiyi.vrs.vis.commons.configuration.Configuration#getLong(java.lang
	 * .String)
	 */
	@Override
	public long getLong(String key) {
		String value=getAndProcessValue(key);
		return StringUtils.toLong(value, -1L);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qiyi.vrs.vis.commons.configuration.Configuration#getFloat(java.lang
	 * .String)
	 */
	@Override
	public float getFloat(String key) {
		String _value = getAndProcessValue(key);
		if (StringUtils.isBlank(_value)) {
			return -1F;
		}
		try {
			return Float.parseFloat(_value);
		} catch (Exception ex) {
			// ignore this ex
		}
		return -1F;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qiyi.vrs.vis.commons.configuration.Configuration#getDouble(java.lang
	 * .String)
	 */
	@Override
	public double getDouble(String key) {
		String _value = getAndProcessValue(key);
		if (StringUtils.isBlank(_value)) {
			return -1D;
		}
		try {
			return Double.parseDouble(_value);
		} catch (Exception ex) {
			// ignore this ex
		}
		return -1D;
	}

	@Override
	public boolean containsKey(String key) {
		return properties.containsKey(key);
	}
}
