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
import com.laxcus.util.naming.*;
import com.laxcus.visit.*;

/**
 * 转发获取保存系统组件ACCOUNT站点命令调用器。
 * CALL/DATA/WORK/BUILD节点使用。
 * 
 * @author scott.liang
 * @version 1.0 10/11/2019
 * @since laxcus 1.0
 */
public class SubCommonShiftLoadSystemTaskInvoker extends CommonInvoker {

	/**
	 * 构造转发获取保存系统组件ACCOUNT站点命令，指定命令
	 * @param shift 转发获取保存系统组件ACCOUNT站点命令
	 */
	public SubCommonShiftLoadSystemTaskInvoker(ShiftLoadSystemTask shift) {
		super(shift);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftLoadSystemTask getCommand() {
		return (ShiftLoadSystemTask) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftLoadSystemTask shift = getCommand();
		TakeSystemTaskSite cmd = shift.getCommand();
		// 找到注册的HOME节点地址
		Node hub = getHub();
		boolean success = (hub != null);
		if (success) {
			success = launchTo(hub, cmd);
		}
		if (!success) {
			shift.getHook().done();
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		Node site = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				TakeSystemTaskSiteProduct product = getObject(TakeSystemTaskSiteProduct.class, index);
				site = product.getSite();
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (site != null);
		if (success) {
			ShiftLoadSystemTask shift = getCommand();
			// 取出类型，生成命令，交给命令管理池处理！
			for (int family : shift.getFamilies()) {
				TaskPart part = new TaskPart(family);
				TakeTaskTag sub = new TakeTaskTag(site, part);
				getCommandPool().admit(sub);
				
				Logger.info(this, "ending", "load system task! type: %s", PhaseTag.translate(family));
			}
		}

		return useful(success);
	}

}