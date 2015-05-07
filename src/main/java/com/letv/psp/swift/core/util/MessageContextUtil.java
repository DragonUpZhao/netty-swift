package com.letv.psp.swift.core.util;

import java.util.ArrayList;
import java.util.List;

import com.letv.psp.swift.common.util.StringUtils;
import com.letv.psp.swift.core.service.MessageContext;

public class MessageContextUtil {
	private MessageContextUtil() {
	}

	public static Integer getIntegerParameter(MessageContext msgCtx, String name) {
		try {
			return Integer.valueOf(msgCtx.getParameter(name));
		} catch (Exception ex) {
			// DO NOTHING
		}
		return null;
	}

	public static int getIntParameter(MessageContext msgCtx, String name) {
		return StringUtils.toInt(msgCtx.getParameter(name));
	}

	public static Integer[] getIntegerParameterValues(MessageContext msgCtx, String name) {
		String[] parameterValues = msgCtx.getParameterValues(name);
		if (parameterValues == null) {
			return null;
		}
		List<Integer> parameterValuesAsList = new ArrayList<Integer>();
		for (String parameterValue : parameterValues) {
			parameterValuesAsList.add(StringUtils.toInt(parameterValue));
		}
		return parameterValuesAsList.toArray(new Integer[] {});
	}
}
