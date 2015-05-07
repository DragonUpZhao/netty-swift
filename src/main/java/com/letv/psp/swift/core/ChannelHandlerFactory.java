package com.letv.psp.swift.core;

import org.jboss.netty.channel.ChannelHandler;

public interface ChannelHandlerFactory extends ChannelHandler {
	ChannelHandler newChannelHandler();
}
