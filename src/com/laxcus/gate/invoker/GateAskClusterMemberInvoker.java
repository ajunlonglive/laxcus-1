/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.gate.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 检索LAXCUS集群成员调用器
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public class GateAskClusterMemberInvoker extends GateInvoker {

	/**
	 * 构造检索LAXCUS集群成员调用器，指定命令
	 * @param cmd 检索LAXCUS集群成员调用器
	 */
	public GateAskClusterMemberInvoker(AskClusterMember cmd) {
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

		// 在线用户签名
		List<Siger> sigers = FrontOnGatePool.getInstance().getSigers();
		if (sigers != null && sigers.size() > 0) {
			// 删除管理员账号
			Administrator admin = StaffOnGatePool.getInstance().getAdministrator();
			if (admin != null) {
				sigers.remove(admin.getUsername());
			}
			// 有签名，输出！
			if (sigers.size() > 0) {
				PushOnlineMember sub = new PushOnlineMember();
				for (Siger siger : sigers) {
					
					// 取账号明文
					String plainText = null;
					Account account = StaffOnGatePool.getInstance().findAccount(siger);
					if(account != null) {
						plainText = account.getUser().getPlainText();
					}
					
					// 找到全部匹配的FRONT节点
					List<Node> fronts =	FrontOnGatePool.getInstance().findFronts(siger);
					for(Node front : fronts) {
						FrontSeat seat = new FrontSeat(siger, getLocal(), front);
						seat.setPlainText(plainText);
						// 保存！
						sub.add(seat);
					}
				}
				// 投递给WATCH站点
				directTo(hub, sub);
			}
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