package com.letv.psp.swift.httpd.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.letv.psp.swift.common.util.ClassLoaderUtils;
import com.letv.psp.swift.common.util.PropertyGetter;
import com.letv.psp.swift.common.util.StringUtils;

public class ResultRendererFactory {
	private static final Logger logger = LoggerFactory.getLogger(ResultRendererFactory.class);
	private static final Map<String, ResultRenderer> resultRendererRegister = new HashMap<String, ResultRenderer>();

	static {
		// TODO 初始化ResultRenderer注册
	}

	
	public static ResultRenderer getResultRenderer(ResultType type) {
		String resultTypeName = type.getName();
		ResultRenderer resultRenderer = resultRendererRegister.get(resultTypeName);

		if (resultRenderer != null) {
			return resultRenderer;
		}
		String rendererClass = PropertyGetter.getString(resultTypeName);
		if (StringUtils.isBlank(rendererClass)) {
			if (logger.isDebugEnabled()) {
				logger.debug("找不到ResultType为{}的render", resultTypeName);
			}
			// logger.info("找不到ResultType为{}的render",resultTypeName);
			return ResultRenderer.NOOP;
		}

		try {
			resultRenderer = (ResultRenderer) ClassLoaderUtils.getInstance(rendererClass);
			resultRendererRegister.put(resultTypeName, resultRenderer);
			return resultRenderer;
		} catch (Exception ex) {
			logger.error("初始化ResultRenderer失败, resultRendererImpl: " + rendererClass, ex);
			resultRenderer = ResultRenderer.NOOP;
		}
		return resultRenderer;
	}

}
