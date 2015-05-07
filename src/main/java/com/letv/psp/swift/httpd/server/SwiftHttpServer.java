package com.letv.psp.swift.httpd.server;

import com.letv.psp.swift.common.util.StringUtils;
import com.letv.psp.swift.common.util.UrlRewrite;
import com.letv.psp.swift.httpd.handler.HttpServiceDispatcherServerHandler;

public class SwiftHttpServer extends DefaultHttpServer {

	public SwiftHttpServer(String urlrewriteConfigFile) {
		if (StringUtils.isBlank(urlrewriteConfigFile)) {
			urlrewriteConfigFile = "urlrewrite.xml";
		}
		UrlRewrite.init(urlrewriteConfigFile);
		this.addChannelHandler(new HttpServiceDispatcherServerHandler());
	}
}
