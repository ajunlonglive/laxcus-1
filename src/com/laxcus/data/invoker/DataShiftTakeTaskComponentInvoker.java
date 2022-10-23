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
import com.laxcus.echo.invoker.common.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.conduct.from.*;
import com.laxcus.task.establish.rise.*;
import com.laxcus.task.establish.scan.*;
import com.laxcus.util.naming.*;

/**
 * 分布任务组件包调用器
 * 
 * @author scott.liang
 * @version 1.0 3/11/2013
 * @since laxcus 1.0
 */
public class DataShiftTakeTaskComponentInvoker extends CommonShiftTakeTaskComponentInvoker {

	/**
	 * 构造分布任务组件包调用器，指定命令
	 * @param shift ShiftTakeTaskComponent实例
	 */
	public DataShiftTakeTaskComponentInvoker(ShiftTakeTaskComponent shift) {
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
		case PhaseTag.FROM:
			success = FromTaskPool.getInstance().deploy(component);
			break;
		case PhaseTag.SCAN:
			boolean master = DataLauncher.getInstance().isMaster();
			if (master) {
				success = ScanTaskPool.getInstance().deploy(component);
			}
			break;
		case PhaseTag.RISE:
			success = RiseTaskPool.getInstance().deploy(component);
			break;
		}
		// 返回结果
		return success;
	}

}
