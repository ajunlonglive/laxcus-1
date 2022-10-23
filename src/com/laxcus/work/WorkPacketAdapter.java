/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;
import com.laxcus.site.*;

/**
 * WORK节点数据包适配器。<br>
 * 
 * @author scott.liang
 * @version 1.0 10/8/2009
 * @since laxcus 1.0
 */
public class WorkPacketAdapter extends PacketAdapter { 

	/**
	 * 构造WORK节点数据包适配器
	 */
	public WorkPacketAdapter() {
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
			shutdown(WorkLauncher.getInstance(), packet);
		} else if (Assert.isComeback(cmd)) {
			WorkLauncher.getInstance().hurry();
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
			WorkLauncher.getInstance().refreshEndTime();
			break;
		case Answer.NOTLOGIN:
			WorkLauncher.getInstance().kiss();
			break;
		}
		
		return reply;
	}

	@Override
	protected boolean active(Node node) {
		return false;
	}

}