/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

/**
 * CommandDispatcher.submit提交执行结果
 *
 * @author scott.liang
 * @version 1.0 11/10/2019
 * @since laxcus 1.0
 */
public final class CommandSubmit {
	
	/** 受理 **/
	public final static int ACCEPTED = 1;
	
	/** 撤销 **/
	public final static int CANCELED = 2;
	
	/** 发生故障 **/
	public final static int FAULTED = 3;

	/**
	 * 判断是标准参数
	 * @param who
	 * @return 返回真或者假
	 */
	public static boolean isSubmit(int who) {
		switch(who) {
		case CommandSubmit.ACCEPTED:
		case CommandSubmit.CANCELED:
		case CommandSubmit.FAULTED:
			return true;
		}
		return false;
	}

	/**
	 * 判断操作被接受
	 * @param who
	 * @return
	 */
	public static boolean isAccpeted(int who) {
		return who == CommandSubmit.ACCEPTED;
	}

	/**
	 * 判断操作被取消
	 * @param who
	 * @return
	 */
	public static boolean isCanceled(int who) {
		return who == CommandSubmit.CANCELED;
	}

	/**
	 * 判断操作发生故障
	 * @param who 
	 * @return
	 */
	public static boolean isFaulted(int who) {
		return who == CommandSubmit.FAULTED;
	}
	
}