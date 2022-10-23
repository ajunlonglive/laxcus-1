/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.invoker;

import com.laxcus.util.lock.*;

/**
 * FRONT资源刷新锁定。<br>
 * 由资源管理池定时触发，去GATE/ENTRANCE节点获取新的参数，更新到本地和显示，同时修正与CALL节点/授权人GATE节点的连接。
 * 
 * @author scott.liang
 * @version 1.0 11/9/2020
 * @since laxcus 1.0
 */
public class ScheduleLock extends MutexHandler {

	/** 实例 **/
	private static ScheduleLock selfHandle = new ScheduleLock();

	/** 锁定标记，全局有效！ **/
	private boolean lockin = false;

	/**
	 * 构造实例！
	 */
	private ScheduleLock() {
		super();
		lockin = false;
	}

	/**
	 * 返回调用实例
	 * @return
	 */
	public static ScheduleLock getInstance() {
		return ScheduleLock.selfHandle;
	}

	/**
	 * 判断被锁定
	 * @return
	 */
	private boolean __isLock() {
		super.lockSingle();
		try {
			return lockin;
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 执行锁定
	 * @return 成功返回真，否则假
	 */
	private boolean __lock() {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			// 没有锁定，才能进行锁定！
			if (!lockin) {
				lockin = true;
				success = true;
			}
		} catch (Throwable e) {

		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 解除锁定
	 * @return 成功返回真，否则假
	 */
	private boolean __unlock() {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			// 只有锁定时，解锁才有效
			if (lockin) {
				lockin = false;
				success = true;
			}
		} catch (Throwable e) {

		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 判断被锁定
	 * @return 返回真或者假
	 */
	public static boolean isLock() {
		return ScheduleLock.getInstance().__isLock();
	}

	/**
	 * 锁定
	 * @return
	 */
	public static boolean lock() {
		return ScheduleLock.getInstance().__lock();
	}

	/**
	 * 解析锁定!
	 * @return
	 */
	public static boolean unlock() {
		return ScheduleLock.getInstance().__unlock();
	}


	//	public static void main(String[] args) {
	//		boolean lock = ScheduleLock.isLock();
	//		System.out.printf("lock is %s\n", lock);
	//
	//		boolean success = ScheduleLock.lock();
	//		System.out.printf("lock is %s\n", success);
	//		if (success) {
	//			success = ScheduleLock.unlock();
	//			System.out.printf("unlock is %s\n", success);
	//		}
	//
	//		boolean unlock = ScheduleLock.isLock();
	//		System.out.printf("unlock is %s\n", unlock);
	//	}


	//	/** 锁定标记，全局有效！ **/
	//	private static volatile boolean lockin = false;
	//	
	//	/**
	//	 * 锁定
	//	 * @return
	//	 */
	//	public static boolean lock() {
	//		// 没有锁定时，锁定有效
	//		if (!ScheduleLock.lockin) {
	//			ScheduleLock.lockin = true;
	//			return true;
	//		}
	//		return false;
	//	}
	//
	//	/**
	//	 * 解析锁定!
	//	 * @return
	//	 */
	//	public static boolean unlock() {
	//		// 锁定时，解决锁定有效
	//		if (ScheduleLock.lockin) {
	//			ScheduleLock.lockin = false;
	//			return true;
	//		}
	//		return false;
	//	}


}