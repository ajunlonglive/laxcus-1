/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.lock;

/**
 * 单向锁。<br><br>
 * 
 * 单向锁的特点是当一个锁定操作进入状态后，后续的锁定操作请求必须等待，直到前一个锁定解除。<br>
 * 这是一个非常重要的锁操作处理类，用于资源的顺序调用环境。<br>
 *
 * @author scott.liang 
 * @version 1.0 8/17/2008 
 * @since laxcus 1.0
 */
public final class SingleLock {

	//	/** 锁定标记 */
	//	private boolean locked;

	/** 锁定标记 */
	private volatile boolean locked;

	/** 锁定超时时间(毫秒) */
	private long timeout;

	/**
	 * 构造一个单向锁
	 */
	public SingleLock() {
		super();
		locked = false;
		setTimeout(5L);
	}

	/**
	 * 构造单向锁，指定锁定等待时间
	 * 
	 * @param timeout 毫秒
	 */
	public SingleLock(long timeout) {
		this();
		setTimeout(timeout);
	}

	/**
	 * 设置锁定等待时间
	 * 
	 * @param ms 毫秒
	 */
	public void setTimeout(long ms) {
		if (ms > 0L) {
			timeout = ms;
		}
	}

	/**
	 * 返回锁定等待时间
	 * 
	 * @return 等待时间
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * 判断是否处于锁定状态
	 * 
	 * @return 返回真或者假
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * 进入锁定状态，直到完成
	 */
	public synchronized void lock() {
		// 如果锁操作已经处于锁定状态，直到它完成
		while (locked) {
			try {
				wait(timeout);
			} catch (InterruptedException e) {

			}
		}
		locked = true;
	}

	/**
	 * 解除锁定
	 * 
	 * @return 返回真或者假
	 */
	public synchronized boolean unlock() {
		// 没有锁定不处理，返回假
		if (!locked) {
			return false;
		}
		// 解除锁定，并且唤醒后一个锁
		locked = false;
		try {
			notify();
		} catch (IllegalMonitorStateException e) {
		}
		return true;
	}
}