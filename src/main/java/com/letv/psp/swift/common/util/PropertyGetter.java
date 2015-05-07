package com.letv.psp.swift.common.util;

import com.letv.psp.swift.common.cfg.Configuration;
import com.letv.psp.swift.common.cfg.ConfigurationFactory;

public class PropertyGetter {
	private static final Configuration cfg = ConfigurationFactory.getConfiguration();

	private PropertyGetter() {
	}

	public static String getString(String key) {
		return cfg.getString(key);
	}

	public static String getString(String key, String defaultVal) {
		String cfgValue = cfg.getString(key);
		return (cfgValue == null || cfgValue.trim().length() == 0) ? defaultVal : cfgValue;
	}

	public static String[] getStringArray(String key) {
		return cfg.getStringArray(key);
	}

	public static int getInt(String key) {
		return cfg.getInt(key);
	}

	public static long getLong(String key) {
		return cfg.getLong(key);
	}

	public static int getInt(String key, int defaultVal) {
		int value = cfg.getInt(key);
		return value == -1 ? defaultVal : value;
	}
}
