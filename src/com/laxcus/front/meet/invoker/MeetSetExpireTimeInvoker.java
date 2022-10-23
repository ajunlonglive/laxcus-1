/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.access.user.*;

/**
 * 设置用户账号到期调用器
 * 
 * @author scott.liang
 * @version 1.0 01/04/2018
 * @since laxcus 1.0
 */
public class MeetSetExpireTimeInvoker extends MeetSetMultiUserParameterInvoker {

	/**
	 * 构造设置用户账号到期调用器，指定命令 
	 * @param cmd 设置用户账号到期
	 */
	public MeetSetExpireTimeInvoker(SetExpireTime cmd) {
		super(cmd);
	}

}