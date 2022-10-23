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
import com.laxcus.site.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.contact.distant.*;
import com.laxcus.util.naming.*;
import com.laxcus.work.pool.*;

/**
 * 分布任务组件标识调用器。<br>
 * 
 * WORK站点向ACCOUNT站点申请分布任务组件。
 * 
 * @author scott.liang
 * @version 1.1 3/09/2015
 * @since laxcus 1.0
 */
public class WorkTakeTaskTagInvoker extends VirtualTakeTaskTagInvoker {

	/**
	 * 构造分布任务组件标识调用器，指定命令
	 * @param cmd 获得组件标识命令
	 */
	public WorkTakeTaskTagInvoker(TakeTaskTag cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.VirtualTakeTaskTagInvoker#check(int, com.laxcus.echo.product.task.TaskTagProduct)
	 */
	@Override
	protected boolean check(int index, TakeTaskTagProduct product) {
		TaskTag tag = product.getTag();
		boolean exists = false;
		
		switch (tag.getFamily()) {
		case PhaseTag.TO:
			exists = ToTaskPool.getInstance().match(tag);
			break;
		case PhaseTag.DISTANT:
			exists = DistantTaskPool.getInstance().match(tag);
			break;
		}
		
		// 如果不存在，发出新命令
		if (!exists) {
			Node hub = getBufferHub(index);
			TakeTaskComponent cmd = new TakeTaskComponent(tag);
			ShiftTakeTaskComponent shift = new ShiftTakeTaskComponent(cmd, hub);
			return WorkCommandPool.getInstance().press(shift);
		}
		return false;
	}

}