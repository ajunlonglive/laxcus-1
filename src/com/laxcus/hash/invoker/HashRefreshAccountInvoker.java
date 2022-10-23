/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.hash.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.hash.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.site.*;

/**
 * 构造刷新账号调用器。<br>
 * HASH站点接收BANK站点发来的命令。
 * 
 * @author scott.liang
 * @version 1.0 6/30/2018
 * @since laxcus 1.0
 */
public class HashRefreshAccountInvoker extends HashInvoker {

	/**
	 * 构造刷新账号调用器，指定命令
	 * @param cmd 刷新账号
	 */
	public HashRefreshAccountInvoker(RefreshAccount cmd) {
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
		Node remote = cmd.getLocal(); // ACCOUNT站点

		// 取出站点坐标，判断匹配
		SiteAxes axes = getLauncher().getAxes();

		// 判断包含
		boolean success = axes.allow(siger);
		if (success) {
			// 判断是不存在
			boolean exists = StaffOnHashPool.getInstance().hasSiger(siger);
			
			success = StaffOnHashPool.getInstance().push(siger, remote);
			
			// 投递给BANK节点
			if (!exists && success) {
				castToWatch(siger);
			}
		}

		Logger.debug(this, "launch", success, "save %s / %s", siger, remote);

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 投递给BANK节点，转发给BANK.WATCH/ TOP.WATCH节点
	 * @param siger 用户签名
	 */
	private void castToWatch(Siger siger) {
		Seat seat = new Seat(siger, getLocal());
		PushRegisterMember sub = new PushRegisterMember(seat);
		directToHub(sub);
	}
}