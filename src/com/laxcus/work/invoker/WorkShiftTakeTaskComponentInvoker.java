/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.invoker;

import com.laxcus.command.task.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.contact.distant.*;
import com.laxcus.util.naming.*;

/**
 * 分布任务组件包调用器
 * 
 * @author scott.liang
 * @version 1.0 3/11/2013
 * @since laxcus 1.0
 */
public class WorkShiftTakeTaskComponentInvoker extends CommonShiftTakeTaskComponentInvoker {

	/**
	 * 构造分布任务组件包调用器，指定命令
	 * @param shift 转发命令
	 */
	public WorkShiftTakeTaskComponentInvoker(ShiftTakeTaskComponent shift) {
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
		case PhaseTag.TO:
			success = ToTaskPool.getInstance().deploy(component);
			break;
		case PhaseTag.DISTANT:
			success = DistantTaskPool.getInstance().deploy(component);
			break;
		}

		Logger.note(this, "deploy", success, "deploy %s", tag);

		// 返回结果
		return success;
	}

}