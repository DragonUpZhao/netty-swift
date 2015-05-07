package com.letv.psp.swift.core.server.handler;

import java.util.concurrent.ThreadPoolExecutor;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class ThreadPoolableChannelUpstreamHandler extends SimpleChannelUpstreamHandler {
	
	protected final ThreadPoolExecutor workerThreadPool;

	public ThreadPoolableChannelUpstreamHandler(ThreadPoolExecutor workerThreadPool) {
		this.workerThreadPool = workerThreadPool;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		try {
			if (workerThreadPool == null) {
				handleRequest(ctx, e);
			} else {
				workerThreadPool.submit(new InnerRequestProcessor(ctx, e));
			}
		} catch (Exception ex) {
			sendError(ctx, e, ex);
		}
	}

	private class InnerRequestProcessor implements Runnable {
		private final ChannelHandlerContext ctx;
		private final MessageEvent e;

		public InnerRequestProcessor(ChannelHandlerContext ctx, MessageEvent e) {
			this.ctx = ctx;
			this.e = e;
		}

		@Override
		public void run() {
			try {
				handleRequest(ctx, e);
			} catch (Exception ex) {
				sendError(ctx, e, ex);
			}
		}
	}

	private void sendError(ChannelHandlerContext ctx, MessageEvent e, Exception ex) {
		// TODO Auto-generated method stub
	}

	// protected void sendError(Chann)
	protected void handleRequest(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, e);
	}
}
