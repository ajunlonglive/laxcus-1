/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

/**
 * 事务规则提交结果
 * 
 * @author scott.liang
 * @version 1.0 4/27/2015
 * @since laxcus 1.0
 */
public class RuleSubmit {

	/** 提交拒绝 **/
	public static final int REFUSE = -1;

	/** 接受提交 **/
	public static final int ACCEPTED = 1;

	/** 提交进入等待 **/
	public static final int WAITING = 2;

	/**
	 * 接受提交
	 * @param who 状态码
	 * @return 匹配返回真，否则假
	 */
	public static boolean isAccepted(int who) {
		return who == RuleSubmit.ACCEPTED;
	}

	/**
	 * 提交进行等待
	 * @param who 状态码
	 * @return 匹配返回真，否则假
	 */
	public static boolean isWaiting(int who) {
		return who == RuleSubmit.WAITING;
	}

	/**
	 * 提交拒绝，通常是发生了其他故障！
	 * @param who 状态码
	 * @return 匹配返回真，否则假
	 */
	public static boolean isRefuse(int who) {
		return who == RuleSubmit.REFUSE;
	}
	
	/**
	 * 解释参数
	 * @param who
	 * @return 返回文字说明
	 */
	public static String translate(int who) {
		switch (who) {
		case RuleSubmit.ACCEPTED:
			return "Submit Accepted";
		case RuleSubmit.WAITING:
			return "Submit Waiting";
		case RuleSubmit.REFUSE:
			return "Submit Refused";
		default:
			return "Invalid symbol";
		}
	}
	
}
