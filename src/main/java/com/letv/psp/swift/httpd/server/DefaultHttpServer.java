package com.letv.psp.swift.httpd.server;

import java.util.Map.Entry;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import com.letv.psp.swift.common.util.PropertyGetter;
import com.letv.psp.swift.core.server.AbstractServer;

/**
 * 默认httpserver实现
 */
public abstract class DefaultHttpServer extends AbstractServer {
	protected static final boolean httpChunkEnable = PropertyGetter.getInt("httpChunk.enable", 0) == 1;
	protected static final int httpMaxChunksize = PropertyGetter.getInt("maxChunksize", 1024 * 1024);

	@Override
	protected ChannelPipelineFactory getPipelineFactory() {
		return new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				// 对于无状态的ChannelUpstreamHandler，直接采用共享实例方式添加到pipeline中
				pipeline.addLast("idleChannelTester", idleStateHandler);
				pipeline.addLast("keepaliveTimeoutHandler", keepaliveTimeoutHandler);
				pipeline.addLast("decoder", new HttpRequestDecoder());

				if (httpChunkEnable) {
					pipeline.addLast("chunkAggregator", new HttpChunkAggregator(httpMaxChunksize));
				}
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("asyncExecutor", asyncExecutor);
				
				for (Entry<String, ChannelHandler> entry : channelHandlers.entrySet()) {
					pipeline.addLast(entry.getKey(), entry.getValue());
				}
				return pipeline;
			}
		};
	}

}
