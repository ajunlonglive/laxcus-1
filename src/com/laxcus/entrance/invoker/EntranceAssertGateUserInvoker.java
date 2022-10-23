/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.entrance.invoker;

import java.util.*;

import com.laxcus.command.site.gate.*;
import com.laxcus.entrance.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 判断账号在GATE站点存在调用器
 * 
 * @author scott.liang
 * @version 1.0 7/19/2019
 * @since laxcus 1.0
 */
public class EntranceAssertGateUserInvoker extends EntranceInvoker {

	/**
	 * 构造判断账号在GATE站点存在调用器，指定命令
	 * @param cmd 判断账号在GATE站点存在
	 */
	public EntranceAssertGateUserInvoker(AssertGateUser cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AssertGateUser getCommand() {
		return (AssertGateUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AssertGateUser cmd = getCommand();

		// 取出全部GATE站点地址
		List<Node> slaves = StaffOnEntrancePool.getInstance().getPrivateSites();
		if (slaves.isEmpty()) {
			FrontOnEntrancePool.getInstance().touch(cmd.getUsername(), null);
			return false;
		}

		// 发送给全部GATE站点，不允许出错
		boolean success = launchTo(slaves, cmd);
		// 不成功，通知处于等待中的
		if (!success) {
			FrontOnEntrancePool.getInstance().touch(cmd.getUsername(), null);
		}
		
		Logger.debug(this, "launch", success, "send count %d", slaves.size());

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		List<Integer> keys = getEchoKeys();
		int count = 0;
		for (int index : keys) {
			try {
				AssertGateUserProduct e = getObject(AssertGateUserProduct.class, index);
				if (e.isSuccessful()) {
					FrontOnEntrancePool.getInstance().touch(e.getSiger(), e.getSite());
					count++;
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		// 成功
		boolean success = (count > 0);
		// 不成功，采用HASH定位方式，取得一个GATE站点
		if (!success) {
			AssertGateUser cmd = getCommand();
			Siger siger = cmd.getUsername();
			boolean fromWide = cmd.isWide();
			// 公网地址
			Node gate = StaffOnEntrancePool.getInstance().locate(siger, fromWide);
			// 保存地址
			FrontOnEntrancePool.getInstance().touch(cmd.getUsername(), gate);
		}
		
		Logger.debug(this, "ending", success, "count %d", count);
		
		// 退出
		return useful(success);
	}

}