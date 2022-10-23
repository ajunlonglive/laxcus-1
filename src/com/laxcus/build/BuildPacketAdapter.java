/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;
import com.laxcus.site.*;

/**
 * BUILD节点数据包适配器 
 * 
 * @author scott.liang
 * @version 1.1 8/19/2012
 * @since laxcus 1.0
 */
public class BuildPacketAdapter extends PacketAdapter { 

	/**
	 * 构造BUILD节点数据包适配器
	 */
	public BuildPacketAdapter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#apply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet apply(Packet packet) {
		Packet resp = null;
		Mark cmd = packet.getMark();

		if (Assert.isShutdown(cmd)) {
			shutdown(BuildLauncher.getInstance(), packet);
		} else if (Assert.isComeback(cmd)) {
			BuildLauncher.getInstance().hurry();
		}

		return resp;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#reply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet reply(Packet packet) {
		Packet reply = null;
		Mark cmd = packet.getMark();
		short code = cmd.getAnswer();

		switch (code) {
		case Answer.ISEE:
			BuildLauncher.getInstance().refreshEndTime();
			break;
		case Answer.NOTLOGIN:
			// HOME节点要求重新注册
			BuildLauncher.getInstance().kiss();
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