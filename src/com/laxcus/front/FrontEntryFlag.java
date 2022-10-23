/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front;

/**
 * FRONT节点登录状态
 * 
 * @author scott.liang
 * @version 1.0 11/26/2018
 * @since laxcus 1.0
 */
public class FrontEntryFlag {
	
	/** 版本号不一致 **/
	public final static int VERSION_NOTMATCH = -12;
	
	/** 许可证限制在NAT网络中使用 **/
	public final static int LICENCE_NAT_REFUSE = -11;
	
	/** 最大重试同错 **/
	public final static int MAX_RETRY = -10;
	
	/** FRONT发送UDP包到ENTRANCE节点，定位自己的IP地址，这个过程超时失败 **/
	public final static int REFLECT_FAULT = -9;
	
	/** 服务不足，由服务端造成 **/
	public final static int SERVICE_MISSING = -8;
	
	/** 登录等待达到最大时间，超时！ **/
	public final static int LOGIN_TIMEOUT = -7;
	
	/** 达到最大在线用户数目 **/
	public final static int MAX_USER = -6;
	
	/** 使用shine命令时发生检测失败 **/
	public final static int NAT_FAULT = -5;

	/** 网络故障 **/
	public final static int NETWORK_FAULT = -4;
	
	/** ENTRANCE重定向GATE主机失败 **/
	public final static int CANNOT_REDIRECT = -3;

	/** GATE节点故障 **/
	public final static int GATE_FAULT = -2;

	/** ENTRANCE节点故障 **/
	public final static int ENTRANCE_FAULT = -1;

	/** 登录成功 **/
	public final static int SUCCESSFUL = 1;
	
	/**
	 * 判断是登录成功
	 * @param who
	 * @return
	 */
	public static boolean isSuccessful(int who) {
		return who == FrontEntryFlag.SUCCESSFUL;
	}

}