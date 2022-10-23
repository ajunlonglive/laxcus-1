/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;
import com.laxcus.site.*;

/**
 * DATA节点数据包适配器
 * 
 * @author scott.liang
 * @version 1.3 6/29/2012
 * @since laxcus 1.0
 */
public class DataPacketAdapter extends PacketAdapter {

	/**
	 * 构造DATA节点数据包适配器
	 */
	public DataPacketAdapter() {
		super();
	}

	/**
	 * 请求操作
	 * @see com.laxcus.invoke.PacketAdapter#apply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet apply(Packet packet) {
		Packet resp = null;

		Mark cmd = packet.getMark();

		if (Assert.isShutdown(cmd)) {
			this.shutdown(DataLauncher.getInstance(), packet);
		} else if (Assert.isComeback(cmd)) {
			DataLauncher.getInstance().hurry();
		}
		
		return resp;
	}

	/**
	 * DATA节点应答操作
	 * @see com.laxcus.invoke.PacketAdapter#reply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet reply(Packet packet) {
		Packet resp = null;
		Mark cmd = packet.getMark();
		short code = cmd.getAnswer();
		
		switch (code) {
		case Answer.ISEE:
			DataLauncher.getInstance().refreshEndTime();
			break;
		case Answer.NOTLOGIN:
			DataLauncher.getInstance().kiss();
			break;
		}
		
		return resp;
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