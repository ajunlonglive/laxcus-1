/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import com.laxcus.util.net.*;

/**
 * 工具类
 * 
 * @author scott.liang
 * @version 1.0 12/6/2020
 * @since laxcus 1.0
 */
public class ReplyUtil {

	/**
	 * 判断是公网地址，两个条件：<br>
	 * 1. 必须是公网IP。<br>
	 * 2. 本地没有这个公网IP。<br><br>
	 * 
	 * @param from 来源地址
	 * @return 返回真或者假。
	 */
	public static boolean isWideAddress(Address from) {
		return from.isWideAddress() && !Address.contains(from);
	}

}