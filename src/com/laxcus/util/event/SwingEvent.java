/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.event;

import java.util.*;

/**
 * SWING事件委托线程 <br><br>
 * 
 * SwingDispatcher调用SwingEvent分两种情况：<br>
 * 1. 如果SwingEvent.waiting=true，SwingDispatcher调用“SwingUtilities.invokeAndWait”，直到线程完成后退出。<br>
 * 2. 如果SwingEvent.waiting=false（这是大多数情况）， SwingDispatcher将调用“SwingUtilities.invokeLater”，放入队列后退出，不等待它完成。<br>
 * 如果有多个线程竞用同一个组件时，这时要使用“同步”方案。比如多个线程竞用状态栏的JLable显示文本时，为避免竞用，这时需要“waiting=true”。<br><br>
 * 
 * 开发者使用第1种情况时，注意不能用一个SwingEvent线程中调用另一个SwingEvent线程，否则将卡死无法退出。切记！切记！切记！<br>
 * 
 * @author scott.liang
 * @version 1.0 1/3/2020
 * @since laxcus 1.0
 */
public abstract class SwingEvent implements Runnable {
	
	/** 就绪 **/
	public static final int READY = 1;

	/** 执行 **/
	public static final int LAUNCH = 2;
	
	/** 空闲 **/
	public static final int EXIT = 3;

	/** 状态 **/
	private volatile int state;

	/** 等待直到最后离开... **/
	private boolean waiting = false;

	/** 最终触发时间，默认是0 **/
	private long touchTime;
	
	/** 事件监听器 **/
	private SwingStageListener listener;

	/**
	 * 构造默认的SWING事件委托线程
	 */
	public SwingEvent() {
		super();
		state = SwingEvent.EXIT;
		waiting = false;
		// 触发时间默认是0，不需要
		touchTime = 0;
	}

	/**
	 * 构造SWING事件委托线程
	 * 
	 * @param waiting
	 * 			waiting=true，调用"SwingUtilities.invokeAndWait"方法，等待直到本线程完成。
	 * 			否则将调用"SwingUtilities.invokeLater"方法
	 */
	public SwingEvent(boolean waiting) {
		this();
		setWaiting(waiting);
	}

	/**
	 * 事件监听器
	 * @param e
	 */
	public void setSwingStageListener(SwingStageListener e) {
		listener = e;
	}

	/**
	 * 返回事件监听器
	 * @return
	 */
	public SwingStageListener getSwingStageListener() {
		return listener;
	}

	/**
	 * 延时
	 * @param ms 毫秒
	 */
	protected synchronized void sleep(long ms) {
		try {
			if (ms > 0L) {
				wait(ms);
			}
		} catch (InterruptedException e) {
			
		}
	}
	
	/**
	 * 设置最终触发时间
	 * @param ms 毫秒
	 */
	public void setTouchTime(long ms) {
		touchTime = ms;
	}

	/**
	 * 返回最终触发时间
	 * @return 毫秒
	 */
	public long getTouchTime() {
		return touchTime;
	}

	/**
	 * 判断达到触发时间
	 * @return 返回真或者
	 */
	public boolean isTouched() {
		if (touchTime < 1) {
			return true;
		}
		return System.currentTimeMillis() >= touchTime;
	}

	/**
	 * 进入就绪状态
	 */
	protected void doReady() {
		state = SwingEvent.READY;
		// 分派事件
		if (listener != null) {
			listener.callReady(new EventObject(this));
		}
	}

	/**
	 * 判断是就绪状态
	 * @return 返回真或者假
	 */
	public boolean isReady() {
		return state == SwingEvent.READY;
	}

	/**
	 * 判断启动进入线程
	 * @return 返回真或者假
	 */
	public boolean isLaunched() {
		return state == SwingEvent.LAUNCH;
	}

	/**
	 * 判断是空闲状态
	 * @return
	 */
	public boolean isExit() {
		return state == SwingEvent.EXIT;
	}

	/**
	 * 设置呼叫
	 * @param b 真或者假
	 */
	protected void setWaiting(boolean b) {
		waiting = b;
	}

	/**
	 * 等待直到最后离开
	 * @return 返回真或者假
	 */
	protected boolean isWaiting() {
		return waiting;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		state = SwingEvent.LAUNCH;
		// 分派事件
		if (listener != null) {
			listener.callLaunch(new EventObject(this));
		}

		// 处理SWING界面操作
		process();

		// 退出...
		state = SwingEvent.EXIT;
		// 分派事件
		if (listener != null) {
			listener.callExit(new EventObject(this));
		}
	}

	/**
	 * 处理线程操作
	 */
	public abstract void process();
	
}