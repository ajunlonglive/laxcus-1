/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.command.task.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 转发删除分布应用调用器。<br><br>
 * 
 * 根据目标地址，分发给DATA/WORK/BUILD/CALL节点。
 * 
 * @author scott.liang
 * @version 1.0 6/21/2020
 * @since laxcus 1.0
 */
public class AccountShiftDropTaskApplicationInvoker extends AccountInvoker {

	/**
	 * 构造转发删除分布应用，指定命令
	 * @param shift 转发删除分布应用
	 */
	public AccountShiftDropTaskApplicationInvoker(ShiftDropTaskApplication shift) {
		super(shift);
		// 注意！不绑定资源，否则会出错！因为DropCloudPackage已经绑定了!
		setShackle(false);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftDropTaskApplication getCommand() {
		return (ShiftDropTaskApplication) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftDropTaskApplication shift = getCommand();
		Node remote = shift.getRemote();
		DropTaskApplication cmd = shift.getCommand();

		// 投递到DATA/WORK/BUILD/CALL站点的任意一个
		boolean success = launchTo(remote, cmd);
		// 不成功，唤醒线程
		if (!success) {
			shift.getHook().done();
		}

		Logger.debug(this, "launch", success, "send to %s", remote);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftDropTaskApplication shift = getCommand();
		DropTaskApplicationHook hook = shift.getHook();

		// 反馈结果
		DropTaskApplicationProduct product = readProduct();

		// 判断有成功的记录
		boolean success = (product != null);
		if (success) {
			hook.setResult(product);
		} else {
			hook.done();
		}

		Logger.debug(this, "ending", success, "finished!");

		return useful(success);
	}

	/**
	 * 读取反馈结果
	 * @return
	 */
	private DropTaskApplicationProduct readProduct() {
		// 反馈结果
		int index = findEchoKey(0);
		try {
			if (isSuccessCompleted(index)) {
				return getObject(DropTaskApplicationProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		return null;
	}

}