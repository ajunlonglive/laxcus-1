/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch;

/**
 * WATCH定位主机结果码
 * 
 * @author scott.liang
 * @version 1.0 9/15/2020
 * @since laxcus 1.0
 */
public class WatchPitch {

	/** 定位成功 **/
	public static final int SUCCESSFUL = 0;

	/** 没有找到主机地址 **/
	public static final int NOT_FOUND = -1;

	/** 位于NAT网络中 **/
	public static final int NAT_ERROR = -2;

	/** PTICH返回地址与本地不匹配 **/
	public static final int ADDRESS_NOTMATCH = -3;

}
