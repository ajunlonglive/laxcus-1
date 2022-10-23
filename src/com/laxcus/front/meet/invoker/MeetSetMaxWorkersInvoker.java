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
 * 设置账号WORK节点数目调用器
 * 
 * @author scott.liang
 * @version 1.0 03/24/2018
 * @since laxcus 1.0
 */
public class MeetSetMaxWorkersInvoker extends MeetSetMultiUserParameterInvoker {

	/**
	 * 构造设置账号WORK节点数目调用器，指定命令 
	 * @param cmd 设置账号WORK节点数目
	 */
	public MeetSetMaxWorkersInvoker(SetMaxWorkers cmd) {
		super(cmd);
	}

}