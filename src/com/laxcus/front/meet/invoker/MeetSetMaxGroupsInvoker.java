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
 * 设置账号最大HOME子域集群数目调用器
 * 
 * @author scott.liang
 * @version 1.0 03/24/2018
 * @since laxcus 1.0
 */
public class MeetSetMaxGroupsInvoker extends MeetSetMultiUserParameterInvoker {

	/**
	 * 构造设置账号最大HOME子域集群数目调用器，指定命令 
	 * @param cmd 设置账号最大HOME子域集群数目
	 */
	public MeetSetMaxGroupsInvoker(SetMaxGroups cmd) {
		super(cmd);
	}

}