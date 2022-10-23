/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.call.pool.*;
import com.laxcus.command.task.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.site.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.conduct.balance.*;
import com.laxcus.task.conduct.init.*;
import com.laxcus.task.contact.fork.*;
import com.laxcus.task.contact.merge.*;
import com.laxcus.task.establish.assign.*;
import com.laxcus.task.establish.issue.*;
import com.laxcus.util.naming.*;

/**
 * 获得分布组件标识调用器。<br>
 * 
 * @author scott.liang
 * @version 1.1 3/09/2015
 * @since laxcus 1.0
 */
public class CallTakeTaskTagInvoker extends VirtualTakeTaskTagInvoker {

	/**
	 * 构造获得分布组件标识调用器，指定命令
	 * @param cmd 获得分布组件标识命令
	 */
	public CallTakeTaskTagInvoker(TakeTaskTag cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.VirtualTakeTaskTagInvoker#check(int, com.laxcus.echo.product.task.TaskTagProduct)
	 */
	@Override
	protected boolean check(int index, TakeTaskTagProduct product) {
		TaskTag tag = product.getTag();
		boolean existed = true; // 假定存在
		switch (tag.getFamily()) {
		case PhaseTag.INIT:
			existed = InitTaskPool.getInstance().match(tag);
			break;
		case PhaseTag.BALANCE:
			existed = BalanceTaskPool.getInstance().match(tag);
			break;
		case PhaseTag.ISSUE:
			existed = IssueTaskPool.getInstance().match(tag);
			break;
		case PhaseTag.ASSIGN:
			existed = AssignTaskPool.getInstance().match(tag);
			break;
		case PhaseTag.FORK:
			existed = ForkTaskPool.getInstance().match(tag);
			break;
		case PhaseTag.MERGE:
			existed = MergeTaskPool.getInstance().match(tag);
			break;
		}

		// 如果不存在，向ACCOUNT站点发出命令，去获取新的组件
		if (!existed) {
			Node hub = getBufferHub(index);
			TakeTaskComponent cmd = new TakeTaskComponent(tag);
			ShiftTakeTaskComponent shift = new ShiftTakeTaskComponent(cmd, hub);
			return CallCommandPool.getInstance().press(shift);
		}
		return false;
	}

}