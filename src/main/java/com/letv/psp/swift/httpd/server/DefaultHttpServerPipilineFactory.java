package com.letv.psp.swift.httpd.server;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import com.letv.psp.swift.core.server.DefaultChannelPipelineFactory;
import com.letv.psp.swift.httpd.handler.HttpServiceDispatcherServerHandler;

public class DefaultHttpServerPipilineFactory extends DefaultChannelPipelineFactory {
	private static final HttpServiceDispatcherServerHandler handler = new HttpServiceDispatcherServerHandler();

	@Override
	protected ChannelUpstreamHandler getDecoder() {
		return new HttpRequestDecoder();
	}

	@Override
	protected ChannelDownstreamHandler getEncoder() {
		return new HttpResponseEncoder();
	}

	@Override
	protected ChannelUpstreamHandler getWorker() {
		return handler;
	}

}
