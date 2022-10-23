/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved. 
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.thread;

/**
 * 线程基础类，所有基于线程运行的类全部由此派生。<br>
 *
 * @author scott.liang
 * @version 1.0 2/1/2009
 * @since laxcus 1.0
 */
public abstract class VirtualThread implements Runnable {

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

	/** 线程运行标记 */
	private volatile boolean running;

	/** 线程终止标记 **/
	private volatile boolean interrupted;

	/** 线程延时时间，单位:毫秒 */
	private long sleep;

	/** 打印错误日志，默认是假 **/
	private boolean printFault;

	/** 消息通知接口 **/
	private ThreadStick threadStick;

	/** 线程堆栈尺寸，默认是0，由系统分配 **/
	private long stackSize; 

	/**
	 * 构造默认的JAVA线程
	 */
	public VirtualThread() {
		super();
		setSleepTime(5);
		interrupted = false;
		thread = null;
		running = false;
		printFault = false;
		threadStick = null;
		stackSize = 0;
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
	 * 设置打印日志标记。当出现错误时，在退出的控制台打印日志。
	 * @param b 打印标记
	 */
	public void setPrintFault(boolean b) {
		printFault = b;
	}

	/**
	 * 判断打印日志
	 * @return 返回真或者假
	 */
	public boolean isPrintFault() {
		return printFault;
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
	 * 返回线程静默时间。单位：秒
	 * @return 整型的线程静默时间
	 */
	public int getSleepTime() {
		return (int) (sleep / 1000L);
	}

	/**
	 * 返回线程静默时间。单位：毫秒
	 * @return 长整型的线程静默时间
	 */
	public long getSleepTimeMillis() {
		return sleep;
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
		// 初始化服务
		boolean success = init();
		if (!success) {
			// 如果不成功，输出日志到控制台
			if (printFault) {
				com.laxcus.log.client.Logger.gushing();
			}
			return false;
		}

		// 类名称
		String name = getClass().getSimpleName();
		name = String.format("%s_%d", name, VirtualThread.nextThreadNumber());

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
	 * 停止线程运行并且通知调用接口
	 * @param e ThreadInductor句柄
	 */
	public void stop(ThreadStick e) {
		threadStick = e;
		stop();
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
	 * 输出线程句柄
	 * 
	 * @return Thread实例
	 */
	protected final Thread getThread() {
		return thread;
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
	 * 线程运行
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		running = true;

		// 如果线程被要求中断，退出循环
		while(!isInterrupted()) {
			process();
		}
		// 释放和结束
		finish();

		// 通知调用接口线程已经退出
		if (threadStick != null) {
			threadStick.wakeup();
		}

		running = false;
		thread = null;
	}

	/**
	 * 执行线程启动前的任务初始化工作
	 * @return 初始化成功返回真，否则假
	 */
	public abstract boolean init();

	/**
	 * 执行线程运行过程中的任务处理
	 */
	public abstract void process();

	/**
	 * 执行线程结束后的清理释放工作
	 */
	public abstract void finish();
}