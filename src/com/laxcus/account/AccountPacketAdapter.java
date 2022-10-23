/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * ACCOUNT站点数据包适配器
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class AccountPacketAdapter extends PacketAdapter {

	/**
	 * 构造ACCOUNT站点数据包适配器
	 */
	public AccountPacketAdapter() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#apply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet apply(Packet packet) {
		Packet resp = null;
		Mark cmd = packet.getMark();

		if (Assert.isShutdown(cmd)) {
			shutdown(AccountLauncher.getInstance(), packet);
		} else if (Assert.isComeback(cmd)) {
			AccountLauncher.getInstance().hurry();
		}

		return resp;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#reply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet reply(Packet packet) {
		Packet reply = null;
		Mark cmd = packet.getMark();
		short code = cmd.getAnswer();

		switch (code) {
		case Answer.ISEE:
			AccountLauncher.getInstance().refreshEndTime(); // 来自TOP的确认通知
			break;
		case Answer.NOTLOGIN:
			Logger.error(this, "reply", "from %s", packet.getRemote());
			AccountLauncher.getInstance().kiss(); //.doLogin();
			break;
		}

		return reply;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#active(com.laxcus.site.Node)
	 */
	@Override
	protected boolean active(Node node) {
		return false;
	}

}