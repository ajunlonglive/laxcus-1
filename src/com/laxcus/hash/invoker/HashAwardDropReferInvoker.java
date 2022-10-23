/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.hash.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.hash.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 构造删除用户资源引用调用器。<br>
 * HASH站点接收BANK站点发来的命令。
 * 
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public class HashAwardDropReferInvoker extends HashInvoker {

	/**
	 * 构造删除用户资源引用调用器，指定命令
	 * @param cmd 删除用户资源引用
	 */
	public HashAwardDropReferInvoker(AwardDropRefer cmd) {
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

		// 判断包含
		boolean success = StaffOnHashPool.getInstance().remove(siger);

		// 如果要求反馈，提供一个应答
		if (cmd.isReply()) {
			DropUserProduct product = new DropUserProduct(siger, success);
			replyProduct(product);
		}
		
		// 通知BANK节点，删除一个注册成员
		if (success) {
			castToWatch(siger);
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

	/**
	 * 投递给BANK节点，转发给BANK.WATCH/ TOP.WATCH节点
	 * @param siger 用户签名
	 */
	private void castToWatch(Siger siger) {
		Seat seat = new Seat(siger, getLocal());
		DropRegisterMember sub = new DropRegisterMember(seat);
		directToHub(sub);
	}

}