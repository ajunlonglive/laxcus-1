/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.lock;

/**
 * 互斥锁句柄。<br><br>
 * 
 * 互斥锁句柄将互斥锁操作封装，使用时可以直接从这个类派生和调用它的方法，这样就不必再去考虑互斥锁的操作细节。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/17/2008
 * @since laxcus 1.0
 */
public class MutexHandler {

	/** 互斥锁(多读/单写模式) */
	private MutexLock lock = new MutexLock();

	/**
	 * 构造互斥锁句柄
	 */
	public MutexHandler() {
		super();
	}

	/**
	 * 设置锁定超时间隔时间
	 * @param ms 为毫秒为单位的超时时间
	 */
	public void setLockTimeout(long ms) {
		lock.setTimeout(ms);
	}

	/**
	 * 任务延时
	 * @param ms 等待时间，单位：毫秒
	 */
	public synchronized void delay(long ms) {
		try {
			if (ms > 0L) {
				super.wait(ms);
			}
		} catch (InterruptedException e) {
			com.laxcus.log.client.Logger.error(e);
		}
	}

	/**
	 * 唤醒
	 */
	public synchronized void wakeup() {
		try {
			super.notify();
		} catch (IllegalMonitorStateException e) {
			com.laxcus.log.client.Logger.error(e);
		}
	}

	//	/**
	//	 * 单向锁定，如果要求持续等待直到锁定，那么将一直到锁定成功。<br>
	//	 * @param keep 持续等待
	//	 * @return 锁定成功返回true，失败返回false
	//	 */
	//	public boolean lockSingle(boolean keep) {
	//		// 单向锁定做为“非保持”状态尝试进行锁定，如果锁定不成功，进入后续检查，直到最后锁定！
	//		while (!lock.lockSingle(false)) {
	//			if (keep) {
	//				delay(1L);
	//			} else {
	//				return false;
	//			}
	//		}
	//		return true;
	//	}
	//
	//	/**
	//	 * 单向锁定，直到锁定返回
	//	 * @return 总是返回真
	//	 */
	//	public boolean lockSingle() {
	//		return lockSingle(true);
	//	}

	/**
	 * 单向锁定，如果要求持续等待直到锁定，那么将一直到锁定成功。<br>
	 * @param keep 持续等待
	 * @return 锁定成功返回true，失败返回false
	 */
	public boolean lockSingle(boolean keep) {
		return lock.lockSingle(keep);
	}

	/**
	 * 单向锁定，直到锁定返回
	 * @return 总是返回真
	 */
	public boolean lockSingle() {
		return lock.lockSingle(true);
	}

	/**
	 * 解除单向锁定
	 * @return 解除锁定返回真，否则假
	 */
	public boolean unlockSingle() {
		return lock.unlockSingle();
	}

	/**
	 * 多向锁定
	 * @return 锁定返回真，否则假
	 */
	public boolean lockMulti() {
		return lock.lockMulti();
	}

	/**
	 * 解除多向锁定
	 * @return 解除锁定返回真，否则假
	 */
	public boolean unlockMulti() {
		return lock.unlockMulti();
	}
}