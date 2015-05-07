package com.letv.psp.swift.core.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.letv.psp.swift.common.util.NamedThreadFactory;
import com.letv.psp.swift.core.NettyConfiguration;

public class Server {
	private static final Logger logger = LoggerFactory.getLogger(Server.class);

	private final NettyConfiguration configuration;
	private ServerBootstrap bootstrap;

	public Server(NettyConfiguration configuration) {
		this.configuration = configuration;
	}

	public void start(int port) {
		bootstrap.setFactory(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(new NamedThreadFactory(
				"Netty-Boss")), Executors.newCachedThreadPool(new NamedThreadFactory("Netty-I/O worker")),
				configuration.getWorkerCount()));

		bootstrap.setPipelineFactory(configuration.getServerPipelineFactory());

		bootstrap.setOption("reuseAddress", configuration.isReuseAddress());
		bootstrap.setOption("backlog", configuration.getBacklog());

		if (configuration.getSendBufferSize() > 0) {
			bootstrap.setOption("sendBufferSize", configuration.getSendBufferSize());
			bootstrap.setOption("child.sendBufferSize", configuration.getSendBufferSize());
		}

		if (configuration.getReceiveBufferSize() > 0) {
			bootstrap.setOption("receiveBufferSize", configuration.getReceiveBufferSize());
			bootstrap.setOption("child.receiveBufferSize", configuration.getReceiveBufferSize());
		}

		bootstrap.setOption("child.reuseAddress", configuration.isReuseAddress());
		bootstrap.setOption("child.tcpNoDelay", configuration.isTcpNoDelay());
		bootstrap.setOption("child.keepAlive", configuration.isKeepAlive());

		bootstrap.bind(new InetSocketAddress(port));
		logger.info("Server start in {}", port);

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				bootstrap.releaseExternalResources();
			}
		}));
	}
}
