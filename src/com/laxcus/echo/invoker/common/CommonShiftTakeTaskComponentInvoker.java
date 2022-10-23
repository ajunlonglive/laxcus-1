/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.task.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.archive.*;
import com.laxcus.visit.*;

/**
 * 分布任务组件包调用器 <br>
 * 按照参数要求，从网络上获得分布任务组件包，然后在本地发布。
 * 
 * @author scott.liang
 * @version 1.0 3/11/2013
 * @since laxcus 1.0
 */
public abstract class CommonShiftTakeTaskComponentInvoker extends CommonInvoker {

	/**
	 * 构造分布任务组件包调用器，指定转发命令
	 * @param shift 获取分布任务组件的转发命令
	 */
	protected CommonShiftTakeTaskComponentInvoker(ShiftTakeTaskComponent shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeTaskComponent getCommand() {
		return (ShiftTakeTaskComponent) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeTaskComponent shift = getCommand();

		Node hub = shift.getRemote();
		TakeTaskComponent cmd = shift.getCommand();

		// 发送到目标站点
		boolean success = completeTo(hub, cmd);

		Logger.debug(this, "launch", success, "%s send to %s", cmd.getTag(), hub);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		TaskComponentProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TaskComponentProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null);

		Logger.debug(this, "ending", success, "take %s", getCommand().getCommand().getTag());

		// 发布任务组件
		if (success) {
			TaskComponent component = product.getComponent();
			// 发布到本地的组件管理池
			success = deploy(component);
		}

		Logger.debug(this, "ending", success, "deploy %s", getCommand().getCommand().getTag());

		// 以上成功后，采用异步方式（INVOKE/PRODUCE），要求所属组件站点重新注册。
		if (success) {
			getLauncher().checkin(false);
		}
		
		return useful(success);
	}

	/**
	 * 发送分布任务组件到任务组件管理池。这个方法由子类，包括DATA/WORK/BUILD/CALL去分别实现。
	 * 
	 * @param component 分布任务组件
	 * @return 成功返回真，否则假
	 */
	protected abstract boolean deploy(TaskComponent component);
}
