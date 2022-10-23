/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.stub.reflex.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 转发删除缓存映像块命令调用器
 * 
 * @author scott.liang
 * @version 1.0 4/25/2013
 * @since laxcus 1.0
 */
public class CommonShiftDeleteCacheReflexInvoker extends CommonInvoker {

	/**
	 * 构造转发删除缓存映像块命令调用器，指定转发命令
	 * @param shift ShiftDeleteCacheReflex命令
	 */
	public CommonShiftDeleteCacheReflexInvoker(ShiftDeleteCacheReflex shift) {
		super(shift);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftDeleteCacheReflex getCommand() {
		return (ShiftDeleteCacheReflex) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftDeleteCacheReflex shift = getCommand();
		Node hub = shift.getSite();
		DeleteCacheReflex cmd = shift.getCommand();

		// 发送命令，数据写入本地磁盘
		boolean success = launchTo(hub, cmd);
		// 不成功，唤醒钩子
		if (!success) {
			DeleteCacheReflexHook hook = shift.getHook();
			hook.done();
		}

		Logger.debug(this, "launch", success, "send to %s", hub);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftDeleteCacheReflex shift = getCommand();
		DeleteCacheReflexHook hook = shift.getHook();

		DeleteCacheReflexProduct product = null;
		try {
			if (isSuccessObjectable(0)) {
				product = getObject(DeleteCacheReflexProduct.class, 0);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			hook.setResult(product);
		}
		hook.done();

		return useful(success);
	}

}