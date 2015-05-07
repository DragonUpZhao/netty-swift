package com.letv.psp.swift.core;

import org.jboss.netty.channel.ChannelHandler;

public class ShareableChannelHandlerFactory implements ChannelHandlerFactory {
	private final ChannelHandler channelHandler;

	public ShareableChannelHandlerFactory(ChannelHandler channelHandler) {
		this.channelHandler = channelHandler;
	}

	@Override
	public ChannelHandler newChannelHandler() {
		return this.channelHandler;
	}

}
