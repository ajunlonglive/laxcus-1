/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.net.*;

/**
 * TOP节点数据包适配器
 * 
 * @author scott.liang
 * @version 1.0 11/12/2009
 * @since laxcus 1.0
 */
public class TopPacketAdapter extends PacketAdapter { 

	/**
	 * 构造TOP节点数据包适配器
	 */
	public TopPacketAdapter() {
		super();
	}
	
	/**
	 * 执行回答任务
	 * @see com.laxcus.invoke.PacketAdapter#reply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet reply(Packet packet) {
		Packet resp = null;
		Mark cmd = packet.getMark();
		short code = cmd.getAnswer();
		
		switch (code) {
		case Answer.ISEE:
			this.doISee(packet);
			break;
		case Answer.NOTLOGIN:
			doNotLogin(packet);
			break;
		}
		return resp;
	}

	/**
	 * 执行请求任务
	 * @see com.laxcus.invoke.PacketAdapter#apply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet apply(Packet request) {
		Packet resp = null;
		Mark cmd = request.getMark();

		if (Assert.isHelo(cmd)) {
			resp = refresh(request); // 激活注册站点
		} else if (Assert.isComeback(cmd)) {
			TopLauncher.getInstance().hurry();
		} else if (Assert.isShutdown(cmd)) {
			this.shutdown(TopLauncher.getInstance(), request);
		}

		return resp;
	}
	
	/**
	 * 判断是来自TOP管理站点的应答
	 * @param from 来源地址
	 * @return 判断成立返回“真”，否则“假”。
	 */
	private boolean isFromRunSite(SocketHost from) {
		Node site = TopLauncher.getInstance().getManager();
		boolean success = (site != null);
		if (success) {
			success = (site.getPacketHost().compareTo(from) == 0);
		}
		return success;
	}
	
	/**
	 * 执行ISEE应答
	 * @param packet
	 */
	private void doISee(Packet packet) {
		SocketHost from = packet.getRemote();
		Logger.debug(this, "doISee", "from %s", from);

		if(isFromRunSite(from)) {
			TopMonitor.getInstance().refreshEndTime();
		}
	}
	
	private void doNotLogin(Packet packet) {
		SocketHost from = packet.getRemote();
		Logger.debug(this, "doNotLogin", "from %s", from);

		if(isFromRunSite(from)) {
			TopMonitor.getInstance().kiss();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#active(com.laxcus.site.Node)
	 */
	@Override
	protected boolean active(Node node) {
		boolean success = false;
		if (node.isHome()) {
			success = HomeOnTopPool.getInstance().refresh(node);
		} else if (node.isLog()) {
			success = LogOnTopPool.getInstance().refresh(node);
		} else if (node.isBank()) {
			success = BankOnTopPool.getInstance().refresh(node);
		} else if (node.isWatch()) {
			success = WatchOnTopPool.getInstance().refresh(node);
		} else if (node.isTop()) {
			success = MonitorOnTopPool.getInstance().refresh(node);
		}

//		// 过气
//		else if (node.isAid()) {
//			success = OldAidOnTopPool.getInstance().refresh(node);
//		} else if (node.isFront()) {
//			success = OldFrontOnTopPool.getInstance().refresh(node);
//		} else if (node.isArchive()) {
//			success = OldArchiveOnTopPool.getInstance().refresh(node);
//		}

		return success;
	}
	
}