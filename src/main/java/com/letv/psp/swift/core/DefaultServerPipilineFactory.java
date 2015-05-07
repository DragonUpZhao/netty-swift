package com.letv.psp.swift.core;

import java.util.List;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

public class DefaultServerPipilineFactory implements ChannelPipelineFactory {
	private final NettyConfiguration configuration;
	
	public DefaultServerPipilineFactory(NettyConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();

		List<ChannelHandler> encoders = configuration.getEncoders();
		for (int i = 0, size = encoders.size(); i < size; i++) {
			ChannelHandler encoder = encoders.get(i);
			addToPipeline("encoder-" + i, encoder, pipeline);
		}

		List<ChannelHandler> decoders = configuration.getDecoders();
		for (int i = 0, size = decoders.size(); i < size; i++) {
			ChannelHandler decoder = decoders.get(i);
			addToPipeline("decoder-" + i, decoder, pipeline);
		}

		return pipeline;
	}

	private void addToPipeline(String name, ChannelHandler handler, ChannelPipeline pipeline) {
		ChannelHandler _handler = handler;
		if (handler instanceof ChannelHandlerFactory) {
			_handler = ((ChannelHandlerFactory) handler).newChannelHandler();
		}
		pipeline.addLast(name, _handler);
	}

}
