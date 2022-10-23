/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.command.task.*;
import com.laxcus.data.*;
import com.laxcus.data.pool.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.site.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.conduct.from.*;
import com.laxcus.task.establish.rise.*;
import com.laxcus.task.establish.scan.*;
import com.laxcus.util.naming.*;

/**
 * 新分布组件标识获取调用器。<br>
 * 
 * @author scott.liang
 * @version 1.1 3/09/2015
 * @since laxcus 1.0
 */
public class DataTakeTaskTagInvoker extends VirtualTakeTaskTagInvoker {

	/**
	 * 构造分布组件调用器，指定命令
	 * @param cmd TakeTaskTag实例
	 */
	public DataTakeTaskTagInvoker(TakeTaskTag cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.VirtualTakeTaskTagInvoker#check(int, com.laxcus.echo.product.task.TaskTagProduct)
	 */
	@Override
	protected boolean check(int index, TakeTaskTagProduct product) {
		TaskTag tag = product.getTag();
		boolean existed = true;
		switch (tag.getFamily()) {
		case PhaseTag.FROM:
			existed = FromTaskPool.getInstance().match(tag);
			break;
		case PhaseTag.SCAN:
			// 只能是MASTER节点，SLAVE节点忽略
			boolean master = DataLauncher.getInstance().isMaster();
			if (master) {
				existed = ScanTaskPool.getInstance().match(tag);
			}
			break;
		case PhaseTag.RISE:
			existed = RiseTaskPool.getInstance().match(tag);
			break;
		}
		// 如果不存在，发出新命令
		if (!existed) {
			Node hub = getBufferHub(index);
			TakeTaskComponent cmd = new TakeTaskComponent(tag);
			ShiftTakeTaskComponent shift = new ShiftTakeTaskComponent(cmd, hub);
			return DataCommandPool.getInstance().press(shift);
		}
		return false;
	}

}