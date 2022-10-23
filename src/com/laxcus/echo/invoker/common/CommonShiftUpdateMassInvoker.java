/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.stub.transfer.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 更新数据块命令调用器
 * 
 * @author scott.liang
 * @version 1.0 4/23/2013
 * @since laxcus 1.0
 */
public class CommonShiftUpdateMassInvoker extends CommonInvoker {

	/**
	 * 构造更新数据块命令调用器，指定命令
	 * @param shift 转发命令
	 */
	public CommonShiftUpdateMassInvoker(ShiftUpdateMass shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftUpdateMass getCommand() {
		return (ShiftUpdateMass) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftUpdateMass shift = this.getCommand();
		UpdateMass cmd = shift.getCommand();
		Node site = shift.getSite();

		// 发送到指定的目标地址
		boolean success = launchTo(site, cmd);
		if (!success) {
			UpdateMassHook hook = shift.getHook();
			hook.done();
		}
		
		Logger.debug(this, "launch", "send to %s", site);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftUpdateMass shift = this.getCommand();
		UpdateMassHook hook = shift.getHook();

		UpdateMassProduct product = null;
		try {
			if (isSuccessObjectable(0)) {
				product = getObject(UpdateMassProduct.class, 0);
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