/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.command.*;

/**
 * 转发云端应用组件调用器。
 * 是AccountShiftPublishXXXXComponentInvoker的父类
 * 
 * @author scott.liang
 * @version 1.0 3/29/2020
 * @since laxcus 1.0
 */
public abstract class AccountShiftPublishComponentInvoker extends AccountInvoker {

	/**
	 * 构造转发云端应用组件调用器，指定命令
	 * @param cmd
	 */
	protected AccountShiftPublishComponentInvoker(Command cmd) {
		super(cmd);
		// 注意！不绑定资源，否则会出错！
		setShackle(false);
	}

}