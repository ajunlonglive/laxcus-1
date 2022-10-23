/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.server;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;
import com.laxcus.site.*;

/**
 * LOG节点数据包适配器
 * 
 * @author scott.liang
 * @version 1.1 3/22/2012
 * @since laxcus 1.0
 */
public class LogPacketAdapter extends PacketAdapter { 

	/**
	 * 构造LOG节点数据包适配器
	 */
	public LogPacketAdapter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#apply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet apply(Packet request) {
		Packet resp = null;
		Mark cmd = request.getMark();

		if (Assert.isShutdown(cmd)) {
			this.shutdown(LogLauncher.getInstance(), request);
		} else if (Assert.isComeback(cmd)) {
			LogLauncher.getInstance().hurry();
		}

		return resp;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#reply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet reply(Packet resp) {
		Packet reply = null;
		Mark cmd = resp.getMark();
		short code = cmd.getAnswer();

		switch (code) {
		case Answer.ISEE:
			LogLauncher.getInstance().refreshEndTime();
			break;
		case Answer.NOTLOGIN:
			LogLauncher.getInstance().kiss();
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