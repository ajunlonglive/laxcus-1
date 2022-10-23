/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.talk;

/**
 * 分布任务组件当前状态。<br>
 * 
 * 包括：没有找到、命令状态（没有运行）、调用器状态（已经运行）。
 * 
 * @author scott.liang
 * @version 1.0 6/13/2018
 * @since laxcus 1.0
 */
public class TaskStatus {

	/** 没有找到 **/
	public static final int NOTFOUND = -1;

	/** 命令等待状态 **/
	public static final int COMMAND = 1;

	/** 运行的异步调用器状态**/
	public static final int INVOKER = 2;

	/**
	 * 判断是没有找到
	 * @param who
	 * @return 真或者假
	 */
	public static boolean isNotFound(int who) {
		return TaskStatus.NOTFOUND == who;
	}

	/**
	 * 判断是等待状态
	 * @param who
	 * @return 真或者假
	 */
	public static boolean isCommand(int who) {
		return TaskStatus.COMMAND == who;
	}

	/**
	 * 判断是运行状态
	 * @param who
	 * @return 真或者假
	 */
	public static boolean isInvoker(int who) {
		return TaskStatus.INVOKER == who;
	}
	
	/**
	 * 判断是有效的状态
	 * @param who 状态码
	 * @return 匹配返回真，否则假
	 */
	public static boolean isStatus(int who) {
		switch (who) {
		case TaskStatus.NOTFOUND:
		case TaskStatus.COMMAND:
		case TaskStatus.INVOKER:
			return true;
		}
		return false;
	}

	/**
	 * 转义
	 * @param who
	 * @return 字符串
	 */
	public static String translate(int who) {
		switch (who) {
		case TaskStatus.NOTFOUND:
			return "NOT FOUND";
		case TaskStatus.COMMAND:
			return "WAITING";
		case TaskStatus.INVOKER:
			return "RUNNING";
		}
		return "NONE";
	}
}