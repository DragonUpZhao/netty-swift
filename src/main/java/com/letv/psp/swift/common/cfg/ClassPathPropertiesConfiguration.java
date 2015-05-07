package com.letv.psp.swift.common.cfg;

import com.letv.psp.swift.common.util.ClassLoaderUtils;

public class ClassPathPropertiesConfiguration extends PropertiesConfiguration {
	public ClassPathPropertiesConfiguration(String propertiesFile) {
		this.properties = ClassLoaderUtils.getProperties(propertiesFile);
	}
}
