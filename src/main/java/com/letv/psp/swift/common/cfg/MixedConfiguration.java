package com.letv.psp.swift.common.cfg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.letv.psp.swift.common.util.StringUtils;

public class MixedConfiguration implements Configuration {
	private static final Logger logger=LoggerFactory.getLogger(MixedConfiguration.class);
	
	private static final String PREFIX_FILE = "file:";
	private static final String PREFIX_CLASSPATH = "classpath:";

	private Configuration[] configurations;

	public MixedConfiguration(String[] configPropertiesFiles) {
		initConfigurations(configPropertiesFiles);
	}

	private void initConfigurations(String[] configPropertiesFiles) {
		if(configPropertiesFiles==null||configPropertiesFiles.length==0){
			return;
		}
		configurations = new Configuration[configPropertiesFiles.length];
		for (int i = 0; i < configurations.length; i++) {
			logger.info("loading properties file: {}",configPropertiesFiles[i]);
			if (configPropertiesFiles[i].startsWith(PREFIX_FILE)) {
				configurations[i] = new FilePropertiesConfiguration(StringUtils.substringAfter(
						configPropertiesFiles[i], PREFIX_FILE));
			} else if (configPropertiesFiles[i].startsWith(PREFIX_CLASSPATH)) {
				configurations[i] = new ClassPathPropertiesConfiguration(StringUtils.substringAfter(
						configPropertiesFiles[i], PREFIX_CLASSPATH));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qiyi.vrs.vis.commons.configuration.Configuration#getString(java.lang
	 * .String)
	 */
	@Override
	public String getString(String key) {
		for(Configuration configuration:configurations){
			if(configuration.containsKey(key)){
				return configuration.getString(key);
			}
		}
		return StringUtils.EMPTY;
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
		for(Configuration cfg:configurations){
			if(cfg.containsKey(key)){
				return cfg.getInt(key);
			}
		}
		return -1;
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
		for(Configuration cfg:configurations){
			if(cfg.containsKey(key)){
				return cfg.getStringArray(key);
			}
		}
		return null;
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
		for(Configuration cfg:configurations){
			if(cfg.containsKey(key)){
				return cfg.getLong(key);
			}
		}
		return -1L;
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
		for(Configuration cfg:configurations){
			if(cfg.containsKey(key)){
				return cfg.getFloat(key);
			}
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
		for(Configuration cfg:configurations){
			if(cfg.containsKey(key)){
				return cfg.getDouble(key);
			}
		}
		return 0;
	}

	@Override
	public boolean containsKey(String key) {
		for(Configuration cfg:configurations){
			if(cfg.containsKey(key)){
				return true;
			}
		}
		return false;
	}
}
