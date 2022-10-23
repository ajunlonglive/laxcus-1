/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.hub;

/**
 * FRONT登录状态 <br>
 * 
 * @author scott.liang
 * @version 1.0 7/19/2019
 * @since laxcus 1.0
 */
public class FrontStatus {

	/** 重定向 **/
	public final static int REDIRECT = 1;

	/** 逗留中 ... */
	public final static int LINGER = 2;

	/** 登录成功 **/
	public final static int LOGINED = 3;

	/** 失败 **/
	public final static int FAILED = 4;
	
	/** 在线用户数目达到最大，不能登录  **/
	public final static int MAXUSER = 5;
	
	/** 登录失败，原因是服务器的某些服务不足！ **/
	public final static int SERVICE_MISSING = 6;
	
	/** 最大重试错误 **/
	public final static int MAX_RETRY = 7;

	/**
	 * 判断是合法的登录状态
	 * @param who 登录状态
	 * @return 返回真或者假
	 */
	public static boolean isStatus(int who) {
		switch(who) {
		case FrontStatus.REDIRECT:
		case FrontStatus.LINGER:
		case FrontStatus.LOGINED:
		case FrontStatus.FAILED:
		case FrontStatus.MAXUSER:
		case FrontStatus.SERVICE_MISSING:
		case FrontStatus.MAX_RETRY:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 判断是重定向
	 * @param who 登录状态
	 * @return 返回真或者假
	 */
	public static boolean isDirect(int who) {
		return who == FrontStatus.REDIRECT;
	}
	
	/**
	 * 判断是逗留状态
	 * @param who 登录状态
	 * @return 返回真或者假
	 */
	public static boolean isLinger(int who) {
		return who == FrontStatus.LINGER;
	}
	
	/**
	 * 判断是已经登录
	 * @param who 登录状态
	 * @return 返回真或者假
	 */
	public static boolean isLogined(int who) {
		return who == FrontStatus.LOGINED;
	}
	
	/**
	 * 判断是失败。失败是一个通用性错误，以上条件不符合，都是失败。
	 * @param who 登录状态
	 * @return 返回真或者假
	 */
	public static boolean isFailed(int who) {
		return who == FrontStatus.FAILED;
	}
	
	/**
	 * 判断达到最大用户数目，这是一种确定性的登录失败！
	 * @param who 登录状态
	 * @return 返回真或者假
	 */
	public static boolean isMaxUser(int who) {
		return who == FrontStatus.MAXUSER;
	}

	/**
	 * 判断是服务不足，原来在服务端
	 * @param who 登录状态
	 * @return 返回真或者假
	 */
	public static boolean isServiceMissing(int who) {
		return who == FrontStatus.SERVICE_MISSING;
	}

	/**
	 * 达到最大重试次数
	 * @param who
	 * @return
	 */
	public static boolean isMaxRetry(int who) {
		return who == FrontStatus.MAX_RETRY;
	}
}