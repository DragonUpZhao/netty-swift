package com.letv.psp.swift.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipelineFactory;

public class NettyConfiguration {

	private boolean tcpNoDelay = true;
	private boolean keepAlive = true;
	private boolean reuseAddress;

	private long sendBufferSize = -1L;
	private long receiveBufferSize = -1L;
	private long connectTimeout = 10000;

	private int workerCount = Runtime.getRuntime().availableProcessors() * 2;
	private int backlog = 65535;

	private int connectionPoolSize;
	private int idleConnectionTestPeriodInSeconds;
	private int keepAliveThreads = 1;

	private ExecutorService asyncExecutor;

	private List<ChannelHandler> encoders = new ArrayList<ChannelHandler>();
	private List<ChannelHandler> decoders = new ArrayList<ChannelHandler>();

	private ChannelPipelineFactory serverPipelineFactory;
	private ChannelPipelineFactory clientPipelineFactory;

	public boolean isTcpNoDelay() {
		return tcpNoDelay;
	}

	public NettyConfiguration setTcpNoDelay(boolean tcpNoDelay) {
		this.tcpNoDelay = tcpNoDelay;
		return this;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public NettyConfiguration setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
		return this;
	}

	public boolean isReuseAddress() {
		return reuseAddress;
	}

	public NettyConfiguration setReuseAddress(boolean reuseAddress) {
		this.reuseAddress = reuseAddress;
		return this;
	}

	public long getSendBufferSize() {
		return sendBufferSize;
	}

	public int getKeepAliveThreads() {
		return keepAliveThreads;
	}

	public NettyConfiguration setKeepAliveThreads(int keepAliveThreads) {
		this.keepAliveThreads = keepAliveThreads;
		return this;
	}

	public NettyConfiguration setSendBufferSize(long sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
		return this;
	}

	public long getReceiveBufferSize() {
		return receiveBufferSize;
	}

	public NettyConfiguration setReceiveBufferSize(long receiveBufferSize) {
		this.receiveBufferSize = receiveBufferSize;
		return this;
	}

	public long getConnectTimeout() {
		return connectTimeout;
	}

	public NettyConfiguration setConnectTimeout(long connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	public int getWorkerCount() {
		return workerCount;
	}

	public NettyConfiguration setWorkerCount(int workerCount) {
		this.workerCount = workerCount;
		return this;
	}

	public int getBacklog() {
		return backlog;
	}

	public NettyConfiguration setBacklog(int backlog) {
		this.backlog = backlog;
		return this;
	}

	public List<ChannelHandler> getEncoders() {
		return encoders;
	}

	public NettyConfiguration setEncoders(List<ChannelHandler> encoders) {
		this.encoders = encoders;
		return this;
	}

	public List<ChannelHandler> getDecoders() {
		return decoders;
	}

	public NettyConfiguration setDecoders(List<ChannelHandler> decoders) {
		this.decoders = decoders;
		return this;
	}

	public NettyConfiguration addDecoder(ChannelHandler decoder) {
		if (!decoders.contains(decoder)) {
			decoders.add(decoder);
		}
		return this;
	}

	public NettyConfiguration addEncoder(ChannelHandler encoder) {
		if (!encoders.contains(encoder)) {
			encoders.add(encoder);
		}
		return this;
	}

	public int getConnectionPoolSize() {
		return connectionPoolSize;
	}

	public NettyConfiguration setConnectionPoolSize(int connectionPoolSize) {
		this.connectionPoolSize = connectionPoolSize;
		return this;
	}

	public int getIdleConnectionTestPeriodInSeconds() {
		return idleConnectionTestPeriodInSeconds;
	}

	public NettyConfiguration setIdleConnectionTestPeriodInSeconds(int idleConnectionTestPeriodInSeconds) {
		this.idleConnectionTestPeriodInSeconds = idleConnectionTestPeriodInSeconds;
		return this;
	}

	public ExecutorService getAsyncExecutor() {
		return asyncExecutor;
	}

	public NettyConfiguration setAsyncExecutor(ExecutorService asyncExecutor) {
		this.asyncExecutor = asyncExecutor;
		return this;
	}

	public ChannelPipelineFactory getServerPipelineFactory() {
		return serverPipelineFactory;
	}

	public NettyConfiguration setServerPipelineFactory(ChannelPipelineFactory serverPipelineFactory) {
		this.serverPipelineFactory = serverPipelineFactory;
		return this;
	}

	public ChannelPipelineFactory getClientPipelineFactory() {
		return clientPipelineFactory;
	}

	public NettyConfiguration setClientPipelineFactory(ChannelPipelineFactory clientPipelineFactory) {
		this.clientPipelineFactory = clientPipelineFactory;
		return this;
	}

}
