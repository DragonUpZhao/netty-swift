package com.letv.psp.swift.core.server;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;

import com.letv.psp.swift.common.util.PropertyGetter;
import com.letv.psp.swift.common.util.StringUtils;
import com.letv.psp.swift.common.util.UrlRewrite;
import com.letv.psp.swift.core.server.handler.MemcachedGetHandler;

public class MemcServer extends AbstractServer {
	private static final MemcachedGetHandler memcHandler = new MemcachedGetHandler();
	private static final boolean KEEP_ALIVE_ENABLE = PropertyGetter.getInt("server.keepalive.enable", 0) == 1;

	public void setUrlRewriteFile(String urlRewriteFile) {
		if (StringUtils.isNotBlank(urlRewriteFile)) {
			UrlRewrite.init(urlRewriteFile);
		}
	}

	@Override
	protected ChannelPipelineFactory getPipelineFactory() {
		return new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				if (!KEEP_ALIVE_ENABLE) {
					pipeline.addLast("idleChannelTester", idleStateHandler);
					pipeline.addLast("keepaliveTimeoutHandler", keepaliveTimeoutHandler);
				}
				pipeline.addLast("decoder", new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()));
				pipeline.addLast("asyncExecutor", asyncExecutor);
				pipeline.addLast("memcHandler", memcHandler);
				return pipeline;
			}
		};
	}
}
