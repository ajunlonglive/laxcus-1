/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.lock;

/**
 * 单向锁句柄 <br><br>
 * 
 * 单向锁句柄封装了“SingleLock”，每次只允许一个操作获得锁定权利，其它都要等待。直到锁定操作解锁后，其它操作才有继续锁定的权利。单向锁不区分读写操作。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/17/2008
 * @since laxcus 1.0
 */
public class SingleHandler {

	/** 单向锁实例 */
	private SingleLock lock = new SingleLock();

	/**
	 * 构造单向锁句柄
	 */
	public SingleHandler() {
		super();
	}

	/**
	 * 设置锁定等待时间。单位：毫秒
	 * @param ms 毫秒
	 */
	public void setTimeout(long ms) {
		lock.setTimeout(ms);
	}
	
	/**
	 * 单向锁定，直到获得锁定权
	 */
	public void lock() {
		lock.lock();
	}

	/**
	 * 解除单向锁定
	 * @return 返回真或者假
	 */
	public boolean unlock() {
		return lock.unlock();
	}
	
}