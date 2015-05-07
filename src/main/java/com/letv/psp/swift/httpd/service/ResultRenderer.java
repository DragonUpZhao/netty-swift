package com.letv.psp.swift.httpd.service;

import com.letv.psp.swift.core.service.MessageContext;

public interface ResultRenderer {
	ResultRenderer NOOP = new ResultRenderer() {
		@Override
		public Object renderResult(Object result, MessageContext msgCtx) {
			return result;
		}
	};

	Object renderResult(Object result, MessageContext msgCtx);
}
