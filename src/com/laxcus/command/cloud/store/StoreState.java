/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

/**
 * 云存储处理状态
 * 
 * @author scott.liang
 * @version 1.0 10/24/2021
 * @since laxcus 1.0
 */
public class StoreState {

	/** 没有找到 **/
	public static final int NOT_FOUND = -3;

	/** 磁盘空间不足 **/
	public static final int DISK_MISSING = -2;

	/** 失败 **/
	public static final int FAILED = -1;

	/** 未定义 **/
	public static final int NONE = 0;

	/** 成功 **/
	public static final int SUCCESSFUL = 1;

	/** 存在 **/
	public static final int EXISTS = 2;

	/**
	 * 判断是没有找到
	 * @param who
	 * @return
	 */
	public static boolean isNotFound(int who) {
		return who == StoreState.NOT_FOUND;
	}

	/**
	 * 判断是磁盘空间不足
	 * @param who
	 * @return
	 */
	public static boolean isDiskMissing(int who) {
		return who == StoreState.DISK_MISSING;
	}

	/**
	 * 判断是失败
	 * @param who
	 * @return
	 */
	public static boolean isFailed(int who) {
		return who == StoreState.FAILED;
	}

	public static boolean isNone(int who) {
		return who == StoreState.NONE;
	}

	/**
	 * 判断是成功
	 * @param who
	 * @return
	 */
	public static boolean isSuccessful(int who) {
		return who == StoreState.SUCCESSFUL;
	}

	/**
	 * 判断是存在
	 * @param who
	 * @return
	 */
	public static boolean isExists(int who) {
		return who == StoreState.EXISTS;
	}

}