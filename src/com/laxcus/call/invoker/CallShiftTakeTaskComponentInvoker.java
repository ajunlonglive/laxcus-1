/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.command.task.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.conduct.balance.*;
import com.laxcus.task.conduct.init.*;
import com.laxcus.task.contact.fork.*;
import com.laxcus.task.contact.merge.*;
import com.laxcus.task.establish.assign.*;
import com.laxcus.task.establish.issue.*;
import com.laxcus.util.naming.*;

/**
 * 获取分布任务组件包调用器
 * 
 * @author scott.liang
 * @version 1.0 3/11/2013
 * @since laxcus 1.0
 */
public class CallShiftTakeTaskComponentInvoker extends CommonShiftTakeTaskComponentInvoker {

	/**
	 * 构造分布任务组件包调用器，指定转发命令
	 * @param shift 转发获得分布任务组件命令
	 */
	public CallShiftTakeTaskComponentInvoker(ShiftTakeTaskComponent shift) {
		super(shift);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.CommonShiftTakeTaskComponentInvoker#deploy(com.laxcus.task.archive.TaskComponent)
	 */
	@Override
	protected boolean deploy(TaskComponent component) {
		boolean success = false;
		
		TaskTag tag = component.getTag();
		switch (tag.getFamily()) {
		case PhaseTag.INIT:
			success = InitTaskPool.getInstance().deploy(component);
			break;
		case PhaseTag.BALANCE:
			success = BalanceTaskPool.getInstance().deploy(component);
			break;
		case PhaseTag.ISSUE:
			success = IssueTaskPool.getInstance().deploy(component);
			break;
		case PhaseTag.ASSIGN:
			success = AssignTaskPool.getInstance().deploy(component);
			break;
		case PhaseTag.FORK:
			success = ForkTaskPool.getInstance().deploy(component);
			break;
		case PhaseTag.MERGE:
			success = MergeTaskPool.getInstance().deploy(component);
			break;
		}
		// 返回结果
		return success;
	}

}