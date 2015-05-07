package com.letv.psp.swift.core.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.letv.psp.swift.common.util.NamedThreadFactory;
import com.letv.psp.swift.common.util.PropertyGetter;
import com.letv.psp.swift.core.component.Component;
import com.letv.psp.swift.core.server.handler.KeepaliveTimeoutHandler;
import com.letv.psp.swift.core.util.BlockingQueueFactory;

/**
 * Server基类
 * 
 */
public class AbstractServer {
	private static final Logger logger = LoggerFactory.getLogger(AbstractServer.class);

	// 将初始化线程大小设置为200, 最大线程数设置为4096
	private static final int workerPoolCoreSize = PropertyGetter.getInt("server.workpool.minSize", 200);
	private static final int workerPoolMaxSize = PropertyGetter.getInt("server.workpool.maxSize", 4096);
	// 读写超时时间设置为30s, 如果超过30s服务器主动断开客户端连接回收资源
	private static final int keepAliveTimeout = PropertyGetter.getInt("server.keepalive.timeout", 30);

	// 设置线程池最小为200,最大为4096且超过2分钟会空闲自动回收多余线程的线程池，队列采用LinkedTransferQueue,超过队列
	// 采用调用方线程执行策略进行处理
	private static final Timer timer = new HashedWheelTimer();
	private static BlockingQueue<Runnable> blockingQueue = BlockingQueueFactory.getBlockingQueue(Runnable.class,
			PropertyGetter.getInt("server.queue.type", 1));

	private static final ExecutorService executor = new ThreadPoolExecutor(workerPoolCoreSize, workerPoolMaxSize, 120L,
			TimeUnit.SECONDS, blockingQueue, new NamedThreadFactory("swift-http-worker", true),
			new ThreadPoolExecutor.CallerRunsPolicy());

	protected static final ExecutionHandler asyncExecutor = new ExecutionHandler(executor);
	// 空闲连接保活时长，默认30s，如需其他配置请修改或增加keepalive-timeout参数
	protected static final IdleStateHandler idleStateHandler = new IdleStateHandler(timer, 0, 0, keepAliveTimeout);
	protected static final KeepaliveTimeoutHandler keepaliveTimeoutHandler = new KeepaliveTimeoutHandler(
			keepAliveTimeout);
	
	// 为了channelHandlers的顺序，需要使用LinkedHashMap来保持
	protected Map<String, ChannelHandler> channelHandlers = new LinkedHashMap<String, ChannelHandler>();
	protected List<Component> components = new ArrayList<Component>();

	protected ChannelPipelineFactory getPipelineFactory() {
		return new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("idleChannelTester", idleStateHandler);
				pipeline.addLast("keepaliveTimeoutHandler", keepaliveTimeoutHandler);
				pipeline.addLast("asyncExecutor", asyncExecutor);
				// 这里只允许增加无状态允许共享的ChannelHandler，对于有状态的channelHandler需要重写
				// getPipelineFactory()方法
				for (Entry<String, ChannelHandler> channelHandler : channelHandlers.entrySet()) {
					pipeline.addLast(channelHandler.getKey(), channelHandler.getValue());
				}
				return pipeline;
			}
		};
	}

	public void addChannelHandler(ChannelHandler handler) {
		channelHandlers.put(handler.getClass().getSimpleName(), handler);
	}

	public void addComponent(Component component) {
		this.components.add(component);
	}

	public void listen(int port) {
		for (Component component : components) {
			component.init();
		}
		final ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(new NamedThreadFactory("swift-server-boss")),
				Executors.newCachedThreadPool(new NamedThreadFactory("swift-server I/O worker"))));

		bootstrap.setPipelineFactory(getPipelineFactory());
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("backlog", 1024000);
		bootstrap.setOption("child.reuseAddress", true);
		bootstrap.setOption("child.tcpNoDelay", true);
		// 非常重要的参数，一定要开启
		bootstrap.setOption("child.keepAlive", true);

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				bootstrap.releaseExternalResources();
				for (Component component : components) {
					component.destroy();
				}
			}
		}));
		bootstrap.bind(new InetSocketAddress(port));
		logger.info("server start in {}", port);
	}
}
