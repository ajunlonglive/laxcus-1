/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.dict;

/**
 * 账号配置
 * 
 * @author scott.liang
 * @version 1.0 7/13/2020
 * @since laxcus 1.0
 */
public class AccountConfig {

	/** 最大用户数目，社区版本默认是2个 **/
	private static int maxUsers = 2;

	/**
	 * 设置最大用户数目。<br>
	 * 社区版本最大用户数目是2，商业版由许可证中的参数决定。<br><br>
	 * 
	 * @param what 整数
	 */
	public static void setMaxUsers(int what) {
		AccountConfig.maxUsers = what;
	}

	/**
	 * 返回最大用户数目
	 * @return 整数
	 */
	public static int getMaxUsers() {
		return AccountConfig.maxUsers;
	}

	/**
	 * 判断达到最大用户数目
	 * @return 返回真或者假
	 */
	public static boolean isMaxUsers(int size) {
		return size >= AccountConfig.maxUsers;
	}

	/** 一个用户并行成员数目，默认是3个 **/
	private static int maxMembers = 3;

	/**
	 * 设置一个用户并行成员数目。<br>
	 * 社区版本一个用户并行成员数目是3，商业版本由许可证决定。<br><br>
	 * 
	 * @param what 整数
	 */
	public static void setMaxMembers(int what) {
		AccountConfig.maxMembers = what;
	}

	/**
	 * 返回一个用户并行成员数目
	 * @return 整数
	 */
	public static int getMaxMembers() {
		return AccountConfig.maxMembers;
	}

	/**
	 * 返回允许的用户并行成员数目
	 * @param input 输入的
	 * @return 返回真或者假
	 */
	public static int getPreferredMembers(int input) {
		return input >= AccountConfig.maxMembers ? AccountConfig.maxMembers : input;
	}
	
	/** 一个用户并行任务数目，默认是10个 **/
	private static int maxJobs = 10;

	/**
	 * 设置一个用户并行任务数目。<br>
	 * 社区版本一个用户并行任务数目是10，商业版本由许可证决定。<br><br>
	 * 
	 * @param what 整数
	 */
	public static void setMaxJobs(int what) {
		AccountConfig.maxJobs = what;
	}

	/**
	 * 返回一个用户并行任务数目
	 * @return 整数
	 */
	public static int getMaxJobs() {
		return AccountConfig.maxJobs;
	}

	/**
	 * 返回允许的用户并行任务数目
	 * @param input 输入的
	 * @return 返回真或者假
	 */
	public static int getPreferredJobs(int input) {
		return input >= AccountConfig.maxJobs ? AccountConfig.maxJobs : input;
	}
}