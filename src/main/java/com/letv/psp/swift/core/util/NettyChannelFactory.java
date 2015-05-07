package com.letv.psp.swift.core.util;

import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class NettyChannelFactory {
	public static Channel createChannel(String host, int port) {
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("connectTimeoutMillis", 10000);

		ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(host, port));
		channelFuture.awaitUninterruptibly();
		return null;
	}
}
