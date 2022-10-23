/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.site.bank.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 获得关联用户签名的HASH站点转发调用器。<br><br>
 * 
 * BANK/GATE/ENTRANCE站点发出，目标是HASH站点。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2018
 * @since laxcus 1.0
 */
public class GateShiftTakeAccountSiteInvoker extends GateInvoker {

	/**
	 * 构造获得关联用户签名的HASH站点转发调用器，指定命令
	 * @param shift 转发获得关联用户签名的HASH站点
	 */
	public GateShiftTakeAccountSiteInvoker(ShiftTakeAccountSite shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeAccountSite getCommand() {
		return (ShiftTakeAccountSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeAccountSite shift = getCommand();
		TakeAccountSite cmd = shift.getCommand();
		Siger siger = cmd.getSiger();
		// 根据签名定位HASH站点
		Node hash = locate(siger);
		// 发送到HASH站点
		boolean success = (hash != null);
		if (success) {
			success = launchTo(hash, cmd);
		}
		// 失败，通知命令钩子
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
		int index = findEchoKey(0);
		TakeAccountSiteProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeAccountSiteProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null);
		
		ShiftTakeAccountSite shift = getCommand();
		if (success) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		return useful(success);
	}

}
