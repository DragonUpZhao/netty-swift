package com.letv.psp.swift.common.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * java反射工具类
 */
public class ReflectUtils {
	private static final Logger logger = LoggerFactory.getLogger(ReflectUtils.class);
	// 主要用来缓存Method对象，提升反射效率
	private static final Map<MethodDescriptor, Method> methodCachePool = new HashMap<ReflectUtils.MethodDescriptor, Method>();

	public static Object newInstance(String className) {
		try {
			return ReflectUtils.class.getClassLoader().loadClass(className).newInstance();
		} catch (InstantiationException ex) {
			logger.error("", ex);
		} catch (IllegalAccessException ex) {
			logger.error("", ex);
		} catch (ClassNotFoundException ex) {
			logger.error("", ex);
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Method getMethod(Class clazz, String methodName, Class[] paramTypes) {
		MethodDescriptor md = new MethodDescriptor(clazz, methodName, paramTypes);
		Method method = getCachedMethod(md);

		if (method != null) {
			return method;
		}
		try {
			method = clazz.getMethod(methodName, paramTypes);
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			cacheMethod(md, method);
			return method;
		} catch (Exception ex) {
			logger.error("", ex);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static Object invokeMethod(Object obj, String methodName, Object[] parameters, Class[] parameterTypes) {
		Method invokeMethod = getMethod(obj.getClass(), methodName, parameterTypes);
		try {
			if (invokeMethod != null) {
				return invokeMethod.invoke(obj, parameters);
			}
		} catch (Exception ex) {
			logger.error("", ex);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static Object invokeMethod(Object obj, String methodName, Object[] parameters) {
		Class[] parameterTypes = new Class[parameters.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypes[i] = parameters[i].getClass();
		}
		Method invokeMethod = getMethod(obj.getClass(), methodName, parameterTypes);
		try {
			if (invokeMethod != null) {
				return invokeMethod.invoke(obj, parameters);
			}
		} catch (Exception ex) {
			logger.error("", ex);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static Object invokeMethodThrowExceptionOnError(Object obj, String methodName, Object[] parameters)
			throws Exception {
		Class[] parameterTypes = new Class[parameters.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypes[i] = parameters[i].getClass();
		}
		Method invokeMethod = getMethod(obj.getClass(), methodName, parameterTypes);
		try {
			if (invokeMethod != null) {
				return invokeMethod.invoke(obj, parameters);
			}
			throw new NullPointerException(StringUtils.buildString("在类{0}中不存在方法{1}", obj.getClass().getName(),
					methodName));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@SuppressWarnings("rawtypes")
	public static Object invokeMethodThrowExceptionOnError(Object obj, String methodName, Object[] parameters,
			Class[] parameterTypes) throws Exception {
		Method invokeMethod = getMethod(obj.getClass(), methodName, parameterTypes);
		try {
			if (invokeMethod != null) {
				return invokeMethod.invoke(obj, parameters);
			}
			throw new NullPointerException(StringUtils.buildString("在类{0}中不存在方法{1}", obj.getClass().getName(),
					methodName));
		} catch (Exception ex) {
			throw ex;
		}
	}

	private static Method getCachedMethod(MethodDescriptor md) {
		Method method = methodCachePool.get(md);
		if (method != null) {
			return method;
		}
		return null;
	}

	private static void cacheMethod(MethodDescriptor md, Method method) {
		if (method != null) {
			methodCachePool.put(md, method);
		}
	}

	public static void clearMethodCache() {
		methodCachePool.clear();
	}

	private static class MethodDescriptor {
		@SuppressWarnings("rawtypes")
		private Class clazz;
		private String methodName;
		@SuppressWarnings("rawtypes")
		private Class[] parameterTypes;

		private int hashCode;

		@SuppressWarnings("rawtypes")
		public MethodDescriptor(Class clazz, String methodName, Class[] parameterTypes) {
			this.clazz = clazz;
			this.methodName = methodName;
			this.parameterTypes = parameterTypes;
			this.hashCode = methodName.length();
		}

		public int hashCode() {
			return this.hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof MethodDescriptor)) {
				return false;
			}

			MethodDescriptor md = (MethodDescriptor) obj;
			return methodName.equals(md.methodName) && clazz.equals(md.clazz)
					&& Arrays.equals(parameterTypes, md.parameterTypes);
		}
	}
}
