/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.invoker;

import com.laxcus.command.task.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.establish.sift.*;

/**
 * 分布任务组件包调用器
 * 
 * @author scott.liang
 * @version 1.0 3/11/2013
 * @since laxcus 1.0
 */
public class BuildShiftTakeTaskComponentInvoker extends CommonShiftTakeTaskComponentInvoker {

	/**
	 * 构造分布任务组件包调用器，指定命令
	 * @param shift
	 */
	public BuildShiftTakeTaskComponentInvoker(ShiftTakeTaskComponent shift) {
		super(shift);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.CommonShiftTakeTaskComponentInvoker#deploy(com.laxcus.task.archive.TaskComponent)
	 */
	@Override
	protected boolean deploy(TaskComponent component) {
		return SiftTaskPool.getInstance().deploy(component);
	}

}