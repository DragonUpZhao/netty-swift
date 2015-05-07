package com.letv.psp.swift.core;

import java.nio.charset.Charset;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

public final class ChannelHandlerFactoryBuilder {
	private ChannelHandlerFactoryBuilder() {
	}

	public static ChannelHandlerFactory newStringEncoder(Charset charset) {
		return new ShareableChannelHandlerFactory(new StringEncoder(charset));
	}

	public static ChannelHandlerFactory newStringDecoder(Charset charset) {
		return new ShareableChannelHandlerFactory(new StringDecoder(charset));
	}

	public static ChannelHandlerFactory newIdleStateHandler(int keepAliveTimeout) {
		Timer timer = new HashedWheelTimer();
		return new ShareableChannelHandlerFactory(new IdleStateHandler(timer, 0, 0, keepAliveTimeout));
	}

	public static ChannelHandlerFactory newLengthFieldBasedFrameDecoder(final int maxFrameLength,
			final int lengthFieldOffset, final int lengthFieldLength, final int lengthAdjustment,
			final int initialBytesToStrip) {
		return new ChannelHandlerFactory() {
			@Override
			public ChannelHandler newChannelHandler() {
				return new LengthFieldBasedFrameDecoder(maxFrameLength, lengthFieldOffset, lengthFieldLength,
						lengthAdjustment, initialBytesToStrip);
			}
		};
	}
}
