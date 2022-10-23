/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate;

import com.laxcus.fixp.*;
import com.laxcus.gate.pool.*;
import com.laxcus.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * GATE站点数据包适配器
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class GatePacketAdapter extends PacketAdapter {

	/**
	 * 构造GATE站点数据包适配器
	 */
	public GatePacketAdapter() {
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
			shutdown(GateLauncher.getInstance(), packet);
		} else if (Assert.isComeback(cmd)) {
			GateLauncher.getInstance().hurry();
		} else if (Assert.isHelo(cmd)) {
			resp = refresh(packet);// 激活注册节点地址
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
			//			GateLauncher.getInstance().refreshEndTime(); // 来自BANK的确认通知
			saw(packet);
			break;
		case Answer.NOTLOGIN:
			Logger.error(this, "reply", "cannot be login! from %s", packet.getRemote());
			GateLauncher.getInstance().kiss(); 
			break;
		}

		return reply;
	}

	/**
	 * 判断是来自BANK站点
	 * @param packet 来源数据包
	 * @return 成功返回真，否则假
	 */
	private boolean saw(Packet packet) {
		// 为源SOCKET地址
		SocketHost remote = packet.getRemote();
		// 判断来自注册的BANK站点，刷新它
		Node hub = GateLauncher.getInstance().getHub();
		boolean success = (Laxkit.compareTo(hub.getPacketHost(), remote) == 0);
		if (success) {
			GateLauncher.getInstance().refreshEndTime();
		}
		return success;
	}

	/**
	 * 激活FRONT站点
	 * @see com.laxcus.invoke.PacketAdapter#active(com.laxcus.site.Node)
	 */
	@Override
	protected boolean active(Node node) {
		// 只接受FRONT站点刷新
		if (!node.isFront()) {
			Logger.error(this, "active", "refuse! from %s", node);
			return false;
		}

		// 刷新普通的注册用户和被授权用户
		boolean b1 = false;
		boolean b2 = false;

		// 判断节点存在和刷新它
		if (FrontOnGatePool.getInstance().contains(node)) {
			b1 = FrontOnGatePool.getInstance().refresh(node);
		}
		if (ConferrerFrontOnGatePool.getInstance().contains(node)) {
			b2 = ConferrerFrontOnGatePool.getInstance().refresh(node);
		}

		// 任何一个存在都成立
		return (b1 || b2);
	}

	//	/**
	//	 * 激活FRONT站点
	//	 * @see com.laxcus.invoke.PacketAdapter#active(com.laxcus.site.Node)
	//	 */
	//	@Override
	//	protected boolean active(Node node) {
	//		// 只接受FRONT站点刷新
	//		if (!node.isFront()) {
	//			Logger.error(this, "active", "refuse! from %s", node);
	//			return false;
	//		}
	//
	//		// 判断在主管理池注册（是正常的注册用户）
	//		boolean success = FrontOnGatePool.getInstance().refresh(node);
	//		// 如果不成立，判断注册在被授权管理池（以上不成立，判断是被授权用户）
	//		if (!success) {
	//			success = ConferrerFrontOnGatePool.getInstance().refresh(node);
	//		}
	//		// 返回结果
	//		return success;
	//
	//		// 刷新普通的注册用户和被授权用户
	//		//		boolean b1 = false;
	//		//		boolean b2 = false;
	//		//
	//		//		// 判断节点存在和刷新它
	//		//		if (FrontOnGatePool.getInstance().contains(node)) {
	//		//			b1 = FrontOnGatePool.getInstance().refresh(node);
	//		//		}
	//		//		if (ConferrerFrontOnGatePool.getInstance().contains(node)) {
	//		//			b2 = ConferrerFrontOnGatePool.getInstance().refresh(node);
	//		//		}
	//		//
	//		//		// 任何一个存在都成立
	//		//		return (b1 || b2);
	//	}

}