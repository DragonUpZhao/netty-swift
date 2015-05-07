package com.letv.psp.swift.core.component;

import com.letv.psp.swift.common.util.UrlRewrite;

/**
 * urlrewrite规则初始化组件
 * 
 */
public class UrlRewriter extends DefaultComponent {
	
	private static final String DEFAULT_URL_REWRITE_FILE = "urlrewrite.xml";

	private String urlRewriteFile = DEFAULT_URL_REWRITE_FILE;

	public UrlRewriter(String urlRewriteFile) {
		if (urlRewriteFile != null && urlRewriteFile.length() > 0) {
			this.urlRewriteFile = urlRewriteFile;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qiyi.swift.core.component.Lifecycle#init()
	 */
	@Override
	public void init() {
		UrlRewrite.init(this.urlRewriteFile);
	}
}
