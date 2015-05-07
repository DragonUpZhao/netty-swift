package com.letv.psp.swift.core.server.handler;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeepaliveTimeoutHandler extends IdleStateAwareChannelHandler {
	private static final Logger logger=LoggerFactory.getLogger(KeepaliveTimeoutHandler.class);
	
	private final int keepaliveTimeout;

	public KeepaliveTimeoutHandler(int keepaliveTimeout) {
		this.keepaliveTimeout = keepaliveTimeout;
	}

	@Override
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
		String remoteAddress = ((InetSocketAddress) e.getChannel().getRemoteAddress()).getAddress().getHostAddress();

		if (e.getState() == IdleState.ALL_IDLE) {
			logger.warn("channel {} over keepAliveTimeout: {},close it", remoteAddress, this.keepaliveTimeout);
			e.getChannel().close();
		}
	}

}
