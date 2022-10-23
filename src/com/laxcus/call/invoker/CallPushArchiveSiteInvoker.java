/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.call.pool.*;
import com.laxcus.command.account.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 主动推送ACCOUNT站点命令调用器。
 * 
 * 收到HOME站点推送的ACCOUNT站点后，系统将启动TakeTaskTag命令，去检查分布组件被更新。
 * 大多数情况下，这是一个冗余操作，但是为了保证及时更新分布组件，这种冗余操作是必须的。
 * 
 * @author scott.liang
 * @version 1.1 8/4/2015
 * @since laxcus 1.0
 */
public class CallPushArchiveSiteInvoker extends CallInvoker {

	/**
	 * 构造主动推送ACCOUNT站点命令调用器，指定命令
	 * @param cmd 主动推送ACCOUNT站点命令
	 */
	public CallPushArchiveSiteInvoker(PushAccountSite cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PushAccountSite getCommand() {
		return (PushAccountSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		PushAccountSite cmd = getCommand();
		// ACCOUNT站点
		Node node = cmd.getNode();
		
		Logger.debug(this, "launch", "user size %d", cmd.size());
		
		// 有效的签名
		ArrayList<Siger> array = new ArrayList<Siger>();

		// 检查账号存在，则要保存成功
		for (Siger siger : cmd.list()) {
			// 签名有效
			boolean success = StaffOnCallPool.getInstance().allow(siger);
			// 保存账号和ACCOUNT站点地址
			if (success) {
				AccountOnCommonPool.getInstance().add(siger, node);
				array.add(siger);
			}
		}
		
		// 检查系统级分布任务组件
		StaffOnCallPool.getInstance().loadTasks(null);
		
		// 检查用户级分布任务组件和码位计算器
		boolean success = (array.size() > 0);
		if (success) {
			for (Siger siger : array) {
				StaffOnCallPool.getInstance().loadTasks(siger);
//				StaffOnCallPool.getInstance().loadScaler(siger);
			}
		}

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}