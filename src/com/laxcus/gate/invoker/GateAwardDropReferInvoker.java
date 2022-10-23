/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.command.refer.*;
import com.laxcus.gate.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 构造删除用户资源引用调用器。<br>
 * GATE站点接收BANK站点发来的命令。
 * 
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public class GateAwardDropReferInvoker extends GateInvoker {

	/**
	 * 构造删除用户资源引用调用器，指定命令
	 * @param cmd 删除用户资源引用
	 */
	public GateAwardDropReferInvoker(AwardDropRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardDropRefer getCommand() {
		return (AwardDropRefer) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardDropRefer cmd = getCommand();
		Siger siger = cmd.getUsername();

		// 删除与账号关联的参数
		boolean b1 = StaffOnGatePool.getInstance().drop(siger);
		boolean b2 = CallOnGatePool.getInstance().remove(siger);
		boolean b3 = ConferrerFrontOnGatePool.getInstance().removeAuthorizer(siger);
		boolean b4 = FrontOnGatePool.getInstance().drop(siger);
		
		// 任何一个成功即成功
		boolean success = (b1 || b2 || b3 || b4);

		// 如果要求反馈，提供一个应答
		if (cmd.isReply()) {
			DropUserProduct product = new DropUserProduct(siger, success);
			replyProduct(product);
		}

		Logger.debug(this, "launch", success, "remove %s", siger);

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

}
