/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call;

import com.laxcus.call.pool.*;
import com.laxcus.fixp.*;
import com.laxcus.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.net.*;

/**
 * CALL节点数据包适配器。
 * 
 * @author scott.liang
 * @version 1.2 5/10/2013
 * @since laxcus 1.0
 */
public class CallPacketAdapter extends PacketAdapter { 

	/**
	 * 构造CALL节点数据包适配器
	 */
	public CallPacketAdapter() {
		super();
	}

	/**
	 * 处理请求任务
	 * @see com.laxcus.invoke.PacketAdapter#apply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet apply(Packet packet) {
		Packet resp = null;
		Mark cmd = packet.getMark();

		if (Assert.isShutdown(cmd)) {
			shutdown(CallLauncher.getInstance(), packet); // 请求关闭
		} else if (Assert.isHelo(cmd)) {
			resp = refresh(packet); // 激活注册节点
		} else if (Assert.isComeback(cmd)) {
			CallLauncher.getInstance().hurry(); // 请求重新注册
		}

		return resp;
	}

	/**
	 * 处理应答任务
	 * @see com.laxcus.invoke.PacketAdapter#reply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet reply(Packet reply) {
		Packet resp = null;
		Mark cmd = reply.getMark();
		short code = cmd.getAnswer();

		switch (code) {
		case Answer.ISEE:
			doISee(reply);
			break;
		case Answer.NOTLOGIN:
			CallLauncher.getInstance().kiss();
			break;
		}

		return resp;
	}

	/**
	 * @param packet
	 */
	private void doISee(Packet packet) {
		SocketHost source = packet.getRemote();
		SiteHost hub = CallLauncher.getInstance().getHubHost();
		if (hub.getPacketHost().compareTo(source) == 0) {
			CallLauncher.getInstance().refreshEndTime();
		} else {
			HomeOnCallPool.getInstance().refresh(source);
		}
	}
	
	/**
	 * 激活FRONT站点
	 * @see com.laxcus.invoke.PacketAdapter#active(com.laxcus.site.Node)
	 */
	@Override
	protected boolean active(Node node) {
		// 只接受FRONT激活
		if (!node.isFront()) {
			Logger.error(this, "active", "refuse! from %s", node);
			return false;
		}

		// 激活FRONT节点
		boolean success = FrontOnCallPool.getInstance().refresh(node);

		return success;
	}
	

//	/**
//	 * 激活FRONT站点
//	 * @see com.laxcus.invoke.PacketAdapter#active(com.laxcus.site.Node)
//	 */
//	@Override
//	protected boolean active(Node node) {
//		boolean success = false;
//		if (node.isFront()) {
//			success = FrontOnCallPool.getInstance().refresh(node);
//		}
//		return success;
//	}

}