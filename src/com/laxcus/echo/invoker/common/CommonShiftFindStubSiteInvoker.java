/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.stub.find.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 转发查询站点数据块命令 <br><br>
 * 
 * ShiftFindStubSite命令被多个接口使用，查询目标站点的数据块编号。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public class CommonShiftFindStubSiteInvoker extends CommonInvoker {

	/**
	 * 构造ShiftFindStubSite命令调用器，指定命令
	 * @param shift ShiftFindStubSite命令
	 */
	public CommonShiftFindStubSiteInvoker(ShiftFindStubSite shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftFindStubSite getCommand() {
		return (ShiftFindStubSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftFindStubSite shift = getCommand();

		Node hub = shift.getHub();
		FindStubSite cmd = shift.getCommand();

		// 发送命令，返回数据选择保存到内存或者硬盘
		CommandItem item = new CommandItem(hub, cmd);
		boolean success = completeTo(item);

		// 不成功，退出
		if (!success) {
			FindStubSiteHook hook = shift.getHook();
			hook.setFault(new EchoException("send failed! to %s", hub));
		}

		Logger.debug(this, "launch", success, "send to %s", hub);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftFindStubSite shift = getCommand();
		FindStubSiteHook hook = shift.getHook();
		
		FindStubSiteProduct product = null;
		try {
			if (isSuccessObjectable(0)) {
				product = getObject(FindStubSiteProduct.class, 0);
			}
		} catch (VisitException e) {
			Logger.error(e);
			hook.setFault(e);
			return false; // 错误，退出
		}

		boolean success = (product != null);
		if (success) {
			hook.setResult(product);
		}
		// 唤醒钩子
		hook.done();

		Logger.debug(this, "ending", success, "result is");

		return useful(success);
	}

}