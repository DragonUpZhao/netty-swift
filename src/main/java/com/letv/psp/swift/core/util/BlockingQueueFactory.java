package com.letv.psp.swift.core.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * 线程池阻塞队列创建工厂
 */
public class BlockingQueueFactory {
	private BlockingQueueFactory() {
	}

	public static <T> BlockingQueue<T> getBlockingQueue(Class<T> queueItemType, int queueType) {
		switch (queueType) {
		case 1:
			return new LinkedBlockingQueue<T>(5000000);
		case 2:
			return new SynchronousQueue<T>();
		case 3:
			return new SynchronousQueue<T>();
		default:
			return new LinkedBlockingQueue<T>(5000000);
		}
	}
}
