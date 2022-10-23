/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.entrance;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * ENTRANCE站点数据包适配器
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class EntrancePacketAdapter extends PacketAdapter {

	/**
	 * 构造ENTRANCE站点数据包适配器
	 */
	public EntrancePacketAdapter() {
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
			shutdown(EntranceLauncher.getInstance(), packet);
		} else if (Assert.isComeback(cmd)) {
			EntranceLauncher.getInstance().hurry();
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
			EntranceLauncher.getInstance().refreshEndTime(); // 来自TOP的确认通知
			break;
		case Answer.NOTLOGIN:
			Logger.debug(this, "reply", "not loging");
			EntranceLauncher.getInstance().kiss(); //.doLogin();
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