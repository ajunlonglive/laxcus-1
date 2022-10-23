/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.invoker;

import com.laxcus.build.pool.*;
import com.laxcus.command.task.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.site.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.establish.sift.*;

/**
 * 根据组件部件，获得分布任务组件标识。<br>
 * 
 * 命令从BUILD站点发出，目标是ACCOUNT站点
 * 
 * @author scott.liang
 * @version 1.1 3/09/2015
 * @since laxcus 1.0
 */
public class BuildTakeTaskTagInvoker extends VirtualTakeTaskTagInvoker {

	/**
	 * 构造分布组件加载调用器，指定命令
	 * @param cmd
	 */
	public BuildTakeTaskTagInvoker(TakeTaskTag cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.VirtualTakeTaskTagInvoker#check(int, com.laxcus.echo.product.task.TaskTagProduct)
	 */
	@Override
	protected boolean check(int index, TakeTaskTagProduct product) {
		TaskTag tag = product.getTag();
		boolean existed = SiftTaskPool.getInstance().match(tag);
		// 如果不存在，发出新命令
		if (!existed) {
			Node hub = getBufferHub(index);
			TakeTaskComponent cmd = new TakeTaskComponent(tag);
			ShiftTakeTaskComponent shift = new ShiftTakeTaskComponent(cmd, hub);
			return BuildCommandPool.getInstance().press(shift);
		}
		return false;
	}

}
