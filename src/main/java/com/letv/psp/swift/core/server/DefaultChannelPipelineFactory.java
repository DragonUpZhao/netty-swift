package com.letv.psp.swift.core.server;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import com.letv.psp.swift.common.util.PropertyGetter;
import com.letv.psp.swift.core.server.handler.KeepaliveTimeoutHandler;

/**
 * 默认http服务调用server的{@link ChannelPipelineFactory}实现
 * 
 */
public abstract class DefaultChannelPipelineFactory implements ChannelPipelineFactory {
	// 读写超时时间设置为30s, 如果超过30s服务器主动断开客户端连接回收资源
	private static final int keepAliveTimeout = PropertyGetter.getInt("server.keepalive.timeout", 30);

	// 设置线程池最小为200,最大为4096且超过2分钟会空闲自动回收多余线程的线程池，队列采用LinkedTransferQueue,超过队列
	// 采用调用方线程执行策略进行处理
	private static final Timer timer = new HashedWheelTimer();

	// 空闲连接保活时长，默认30s，如需其他配置请修改或增加keepalive-timeout参数
	protected static final IdleStateHandler idleStateHandler = new IdleStateHandler(timer, 0, 0, keepAliveTimeout);
	protected static final KeepaliveTimeoutHandler keepaliveTimeoutHandler = new KeepaliveTimeoutHandler(
			keepAliveTimeout);

	// 为了channelHandlers的顺序，需要使用LinkedHashMap来保持
	protected Map<String, ChannelHandler> channelHandlers = new LinkedHashMap<String, ChannelHandler>();

	protected abstract ChannelUpstreamHandler getDecoder();

	protected abstract ChannelDownstreamHandler getEncoder();

	protected abstract ChannelUpstreamHandler getWorker();

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("idleTest", idleStateHandler);
		pipeline.addLast("keepAlive", keepaliveTimeoutHandler);

		pipeline.addLast("decoder", getDecoder());
		pipeline.addLast("encoder", getEncoder());

		pipeline.addLast("worker", getWorker());
		return pipeline;
	}

}
