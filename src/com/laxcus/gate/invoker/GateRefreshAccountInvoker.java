/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.command.access.account.*;
import com.laxcus.gate.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 构造刷新账号调用器。<br>
 * GATE站点接收BANK/GATE站点发来的命令，如果本地有这个账号，则更新它，否则忽略。
 * 
 * @author scott.liang
 * @version 1.0 6/30/2018
 * @since laxcus 1.0
 */
public class GateRefreshAccountInvoker extends GateInvoker {

	/**
	 * 构造刷新账号调用器，指定命令
	 * @param cmd 刷新账号
	 */
	public GateRefreshAccountInvoker(RefreshAccount cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshAccount getCommand() {
		return (RefreshAccount) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		RefreshAccount cmd = getCommand();
		Siger siger = cmd.getSiger();

		// 如果有这个账号，则更新，否则忽略。资源管理池或者被授权引用管理池有二者有其一即可！

		// 判断资源管理池有这个账号
		boolean success = StaffOnGatePool.getInstance().contains(siger);
		// 以上不成立，被授权资源引用管理池有这个账号
		if (!success) {
			success = ConferrerStaffOnGatePool.getInstance().contains(siger);
		}
		// 以上不成立，忽略它
		if (!success) {
			return useful();
		}
		
		// 账号中的参数已经更新，现在需要重新加载这个账号。发送命令给ACCOUNT站点
		Node account = cmd.getLocal();// ACOUNT站点
		TakeAccount sub = new TakeAccount(siger);
		success = launchTo(account, sub);

		Logger.debug(this, "launch", success, "refresh %s & %s", siger, account);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		Account account = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				TakeAccountProduct e = getObject(TakeAccountProduct.class, index);
				account = e.getAccount();
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 不成功，忽略！
		if(account == null) {
			return useful(false);
		}

		// 用户账号
		Siger siger = account.getUsername();
		// 判断账号失效
		boolean disabled = account.getUser().isDisabled();

		// 判断被授权资源管理池有这个签名
		boolean success = ConferrerStaffOnGatePool.getInstance().contains(siger);
		if (success) {
			Refer refer = new Refer(account);
			ConferrerStaffOnGatePool.getInstance().create(refer);
		}

		// 判断管理池有这个账号
		success = StaffOnGatePool.getInstance().contains(siger);
		// 账号存在且失效，删除它！
		if (success && disabled) {
			StaffOnGatePool.getInstance().drop(siger);
			FrontOnGatePool.getInstance().drop(siger);
			Logger.info(this, "ending", "%s is disabled! drop it!", siger);
		}
		// 建立/更新账号
		else if (success) {
			StaffOnGatePool.getInstance().create(account);
		}
		
		Logger.debug(this, "ending", success, "update: %s", getCommand().getSeat());

		// 退出
		return useful();
	}

}