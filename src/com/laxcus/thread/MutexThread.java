/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.thread;

import com.laxcus.util.lock.*;

/**
 * 互斥锁线程。
 * 
 * @author scott.liang
 * @version 1.0 2/2/2009
 * @since laxcus 1.0
 */
public abstract class MutexThread extends VirtualThread {

	/** 互斥锁(多读/单写模式) */
	private MutexLock lock = new MutexLock();

	/**
	 * 构造互斥锁线程
	 */
	protected MutexThread() {
		super();
	}

	/**
	 * 单向锁定，如果要求持续等待直到锁定，那么将直到锁定成功。<br>
	 * @param keep 持续等待
	 * @return 锁定成功返回true，失败或者线程退出返回false
	 */
	protected boolean lockSingle(boolean keep) {
		return lock.lockSingle(keep);
	}

	/**
	 * 执行单向锁定，直到锁定返回
	 * @return 总是返回“真”
	 */
	protected boolean lockSingle() {
		return lock.lockSingle(true);
	}

	/**
	 * 执行解除单向锁定
	 * @return 解除锁定返回真，否则假
	 */
	protected boolean unlockSingle() {
		return lock.unlockSingle();
	}

	/**
	 * 执行多向锁定。
	 * @return 锁定成功返回真，否则假
	 */
	protected boolean lockMulti() {
		return lock.lockMulti();
	}

	/**
	 * 执行解除多向锁定
	 * @return 解除锁定返回真，否则假
	 */
	protected boolean unlockMulti() {
		return lock.unlockMulti();
	}

}