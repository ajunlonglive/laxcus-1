/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.thread;

/**
 * 工作者线程
 * 处理一些简单的工作
 * 
 * @author scott.liang
 * @version 1.0 2/22/2022
 * @since laxcus 1.0
 */
public abstract class WorkThread implements Runnable {

	/** 顺序线程号 **/
	private static volatile int threadNumber = 1;

	/**
	 * 返回下一个线程编号
	 * @return 线程编号
	 */
	private static synchronized int nextThreadNumber() {
		if (threadNumber >= Integer.MAX_VALUE) {
			threadNumber = 1;
		}
		return threadNumber++;
	}

	/** 线程句柄 */
	private Thread thread;

	/** 线程堆栈尺寸，默认是0，由系统分配 **/
	private long stackSize; 

	/** 线程延时时间，单位:毫秒 */
	private long sleep;

	/** 线程运行标记 */
	private volatile boolean running;

	/** 线程终止标记 **/
	private volatile boolean interrupted;

	/**
	 * 构造工作者线程
	 */
	public WorkThread() {
		super();
		interrupted = false;
		thread = null;
		running = false;
		stackSize = 0;
	}
	
	/**
	 * 设置线程静默时间，在这个时间里，在没有外部唤醒情况下，保持睡眠状态。单位：秒
	 * @param second 线程静默时间
	 */
	public void setSleepTime(int second) {
		setSleepTimeMillis(second * 1000L);
	}

	/**
	 * 设置线程静默时间，在这个时间里，在没有外部唤醒情况下，保持睡眠状态。单位：毫秒
	 * @param ms 线程静默时间
	 */
	public void setSleepTimeMillis(long ms) {
		if (ms >= 1000) {
			sleep = ms;
		}
	}

	/**
	 * 设置线程堆栈尺寸，必须大于等于1M。<br>
	 * 堆栈设置是否生效，由当前操作系统决定。<br><br>
	 * 
	 * @param size 堆栈尺寸
	 * @return 返回设置后的堆栈尺寸，设置失败返回0。
	 */
	public long setStackSize(long size) {
		if (size >= 0x100000) {
			stackSize = size;
		}
		return stackSize;
	}

	/**
	 * 返回线程堆栈尺寸，没有定义是0
	 * @return 堆栈尺寸
	 */
	public long getStackSize() {
		return stackSize;
	}
	
	/**
	 * 判断线程被要求中断。中断标记为“真”时，即是要求中断
	 * @return 返回真或者假
	 */
	public final boolean isInterrupted() {
		return interrupted;
	}

	/**
	 * 设置中断标记
	 * @param b 中断标记
	 */
	protected void setInterrupted(boolean b) {
		interrupted = b;
	}
	
	/**
	 * 停止线程运行
	 */
	public void stop() {
		if (interrupted) {
			return;
		}
		interrupted = true;
		wakeupAll();
	}
	
	/**
	 * 线程延时等待。单位：毫秒。
	 * @param timeout 超时时间
	 */
	public synchronized void delay(long timeout) {
		try {
			if (timeout > 0L) {
				wait(timeout);
			}
		} catch (InterruptedException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}

	/**
	 * 进入默认的延时
	 */
	protected void sleep() {
		delay(sleep);
	}

	/**
	 * 唤醒线程
	 */
	public synchronized void wakeup() {
		try {
			notify();
		}catch(IllegalMonitorStateException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}

	/**
	 * 唤醒全部
	 */
	protected synchronized void wakeupAll() {
		try {
			notifyAll();
		}catch(IllegalMonitorStateException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}
	
	/**
	 * 判断线程处于运行状态
	 * @return 返回真或者假
	 */
	public final boolean isRunning() {
		return running && thread != null;
	}

	/**
	 * 判断线程处于停止状态
	 * @return 返回真或者假
	 */
	public boolean isStopped() {
		return !isRunning();
	}

	/**
	 * 启动线程，在启动线程前调用"init"方法
	 * @param priority 线程优化级，见Thread中的定义
	 * @return 成功返回“真”，失败“假”。
	 */
	public boolean start(int priority) {
		// 检测线程
		synchronized (this) {
			if (thread != null) {
				return false;
			}
		}

		// 类名称
		String name = getClass().getSimpleName();
		name = String.format("%s_%d", name, WorkThread.nextThreadNumber());

		// 启动线程
		if (stackSize >= 0x100000) {
			thread = new Thread(null, this, name, stackSize);
		} else {
			thread = new Thread(this, name);
		}
		// 设置线程优先级
		thread.setPriority(priority);
		// 启动线程
		thread.start();

		return true;
	}

	/**
	 * 使用线程较小优先级启动线程
	 * @return 成功返回“真”，失败“假”。
	 */
	public boolean start() {
		return start(Thread.NORM_PRIORITY);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		running = true;

		// 如果线程被要求中断，退出循环
		while (!isInterrupted()) {
			process();
		}

		running = false;
		thread = null;
	}

	/**
	 * 执行线程运行过程中的任务处理
	 */
	public abstract void process();

}