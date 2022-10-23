/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.hash.invoker;

import java.util.*;

import com.laxcus.hash.pool.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 检索LAXCUS集群成员调用器
 * 
 * @author scott.liang
 * @version 1.0 1/18/2020
 * @since laxcus 1.0
 */
public class HashAskClusterMemberInvoker extends HashInvoker {

	/**
	 * 构造检索LAXCUS集群成员调用器，指定命令
	 * @param cmd 检索LAXCUS集群成员调用器
	 */
	public HashAskClusterMemberInvoker(AskClusterMember cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AskClusterMember getCommand() {
		return (AskClusterMember) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AskClusterMember cmd = getCommand();
		Node hub = cmd.getRemote();
		if (hub == null) {
			hub = getHub();
		}

		// 取出全部注册用户
		List<Siger> sigers = StaffOnHashPool.getInstance().getSigers();
		if (sigers != null && sigers.size() > 0) {
			PushRegisterMember sub = new PushRegisterMember();
			for (Siger siger : sigers) {
				Seat seat = new Seat(siger, getLocal());
				// 保存
				sub.add(seat);
			}
			// 投递给WATCH站点
			directTo(hub, sub);
		}

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}