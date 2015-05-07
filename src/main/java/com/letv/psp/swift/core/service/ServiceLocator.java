package com.letv.psp.swift.core.service;

import java.util.HashMap;
import java.util.Map;

import com.letv.psp.swift.common.util.ClassLoaderUtils;
import com.letv.psp.swift.common.util.PropertyGetter;
import com.letv.psp.swift.common.util.StringUtils;

/**
 * 服务定位器，根据请求服务地址上的service参数来获得具体的服务实例
 */
public interface ServiceLocator {
	Map<String, Object> servicePool = new HashMap<String, Object>();
	
	Object getService(String serviceName);

	ServiceLocator SIMPLE = new ServiceLocator() {
		@Override
		public Object getService(String serviceName) {
			Object serviceInstance=servicePool.get(serviceName);
			if(serviceInstance!=null){
				return serviceInstance;
			}
			String serviceImpl = PropertyGetter.getString(serviceName);
			//if(serviceImpl.)
			if (StringUtils.isNotBlank(serviceImpl)) {
				serviceInstance = ClassLoaderUtils.getInstance(serviceImpl);
				if (serviceInstance != null) {
					servicePool.put(serviceName, serviceInstance);
				}
				return serviceInstance;
			}
			return null;
		}
	};
}
