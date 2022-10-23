/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.refer.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 转获得账号资源引用调用器。
 * 
 * @author scott.liang
 * @version 1.0 7/03/2017
 * @since laxcus 1.0
 */
public class GateShiftTakeReferInvoker extends GateSeekAccountSiteInvoker {

	/** 操作步骤，从1开始 **/
	private int step;

	/**
	 * 构造转发获得账号资源引用调用器，指定转发命令
	 * @param cmd 转发获得账号资源引用命令
	 */
	public GateShiftTakeReferInvoker(ShiftTakeRefer cmd) {
		super(cmd);
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeRefer getCommand() {
		return (ShiftTakeRefer) super.getCommand();
	}

	/**
	 * 返回被授权人签名
	 * @return
	 */
	private Siger getConferrer(){
		ShiftTakeRefer shift = getCommand();
		TakeRefer cmd = shift.getCommand();
		return cmd.getSiger();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return todo();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return todo();
	}

	/**
	 * 执行处理流程
	 * @return 成功返回真，否则假
	 */
	private boolean todo() {
		boolean success = false;
		switch(step) {
		case 1:
			success = doFirst();
			break;
		case 2:
			success = doSecond();
			break;
		case 3:
			success = doThird();
			break;
		}
		// 自增1
		step++;
		// 失败或者完成
		if (!success || step > 3) {
			setQuit(true);
			getCommand().getHook().done();
		}
		return success;
	}

	/**
	 * 第一步，获取ACCOUNT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		Siger siger = getConferrer();
		return seekSite(siger);
	}

	/**
	 * 第二步：发送命令给ACCOUNT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		Node account = super.replySite();
		ShiftTakeRefer shift = getCommand();
		TakeRefer cmd = shift.getCommand();
		// 发送到ACCOUNT站点
		return launchTo(account, cmd);
	}

	/**
	 * 第三步：接收报告 
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		int index = findEchoKey(0);
		TakeReferProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeReferProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		ShiftTakeRefer shift = getCommand();
		TakeReferHook hook = shift.getHook();

		// 成功，设置参数
		boolean success = (product != null);
		if (success) {
			hook.setResult(product);
		}
		hook.done();
		
		return success;
	}

}