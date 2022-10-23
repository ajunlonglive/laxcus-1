/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray;

/**
 * FRONT节点登录状态
 * 
 * @author scott.liang
 * @version 1.0 01/18/2020
 * @since laxcus 1.0
 */
public class RayEntryFlag {
	
	/** 登录HUB节点故障 **/
	public final static int LOGIN_FAULT = -5;

	/** 版本号不一致 **/
	public final static int VERSION_NOTMATCH = -4;
	
	/** socket连接失败 **/
	public final static int CONNECT_FAULT = -3;

	/** UDP通信定位HOME/BANK/TOP失败 **/
	public final static int REFLECT_FAULT = -2;
	
	/** 启动时，通过socket tcp/udp定位失败 **/
	public final static int CHECK_FAULT = -1;
	
	/** 登录成功 **/
	public final static int SUCCESSFUL = 1;
	
	/**
	 * 判断是登录成功
	 * @param who 状态码
	 * @return 返回真或者假
	 */
	public static boolean isSuccessful(int who) {
		return who == RayEntryFlag.SUCCESSFUL;
	}

}