/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.access.user.*;

/**
 * 设置账号最大在线用户数目调用器
 * 
 * @author scott.liang
 * @version 1.0 05/30/2021
 * @since laxcus 1.0
 */
public class DesktopSetMaxMembersInvoker extends DesktopSetMultiUserParameterInvoker {

	/**
	 * 构造设置账号最大在线用户数目调用器，指定命令 
	 * @param cmd 设置账号最大在线用户数目
	 */
	public DesktopSetMaxMembersInvoker(SetMaxMembers cmd) {
		super(cmd);
	}

}