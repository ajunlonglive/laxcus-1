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
 * 设置用户命令权级调用器
 * 
 * @author scott.liang
 * @version 1.0 10/6/2022
 * @since laxcus 1.0
 */
public class DesktopSetUserPriorityInvoker extends DesktopSetMultiUserParameterInvoker {

	/**
	 * 构造设置用户命令权级调用器，指定命令 
	 * @param cmd 设置用户命令权级
	 */
	public DesktopSetUserPriorityInvoker(SetUserPriority cmd) {
		super(cmd);
	}

}