/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.bank.pool.*;
import com.laxcus.util.net.*;

/**
 * BANK节点数据包适配器
 * 
 * @author scott.liang
 * @version 1.0 6/25/2018
 * @since laxcus 1.0
 */
public class BankPacketAdapter extends PacketAdapter {

	/**
	 * 构造BANK站点数据包适配器
	 */
	public BankPacketAdapter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#apply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet apply(Packet request) {
		Packet resp = null;
		Mark mark = request.getMark();

		if (Assert.isHelo(mark)) {
			resp = refresh(request); // 激活注册节点
		} else if (Assert.isShutdown(mark)) {
			shutdown(BankLauncher.getInstance(), request);
		} else if (Assert.isComeback(mark)) {
			comeback(request);
		}

		return resp;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#reply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet reply(Packet packet) {
		Packet resp = null;
		Mark mark = packet.getMark();
		short code = mark.getAnswer();

		switch (code) {
		case Answer.ISEE:
			doISee(packet);
			break;
		case Answer.NOTLOGIN:
			doNotLogin(packet);
			break;
		}

		return resp;
	}

	/**
	 * 判断数据包来源，做相关的激活。
	 * @param packet
	 */
	private void doISee(Packet packet) {
		SocketHost from = packet.getRemote();
		Logger.debug(this, "doISee", "from %s", from);

		if (isFromHub(from)) {
			BankLauncher.getInstance().refreshEndTime();
		} else if (isFromRunSite(from)) {
			BankMonitor.getInstance().refreshEndTime();
		}
	}
	
	/**
	 * 要求重新注册
	 * @param packet
	 */
	private void doNotLogin(Packet packet){
		SocketHost from = packet.getRemote();
		Logger.debug(this, "doNotLogin", "from %s", from);

		if(isFromHub(from)) {
			BankLauncher.getInstance().kiss();
		} else if(isFromRunSite(from)) {
			BankMonitor.getInstance().kiss();
		}
	}

	/**
	 * 判断是来自TOP站点的应答
	 * @param from
	 * @return
	 */
	private boolean isFromHub(SocketHost from) {
		Node hub = BankLauncher.getInstance().getHub();
		return hub.getPacketHost().compareTo(from) == 0;
	}

	/**
	 * 判断是来自BANK管理站点的应答
	 * @param from
	 * @return
	 */
	private boolean isFromRunSite(SocketHost from) {
		Node site = BankLauncher.getInstance().getManager();
		boolean success = (site != null);
		if (success) {
			success = (site.getPacketHost().compareTo(from) == 0);
		}
		return success;
	}

	private void comeback(Packet packet) {
		SocketHost endpoint = packet.getRemote();
		Logger.debug(this, "comeback", "from %s", endpoint);

		if (isFromHub(endpoint)) {
			BankLauncher.getInstance().hurry();
		} else if (isFromRunSite(endpoint)) {
			// 通知快速 注册
			BankMonitor.getInstance().hurry();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#active(com.laxcus.site.Node)
	 */
	@Override
	protected boolean active(Node node) {
		boolean success = false;
		if (node.isEntrance()) {
			success = EntranceOnBankPool.getInstance().refresh(node);
		} else if (node.isGate()) {
			success = GateOnBankPool.getInstance().refresh(node);
		} else if (node.isHash()) {
			success = HashOnBankPool.getInstance().refresh(node);
		} else if (node.isAccount()) {
			success = AccountOnBankPool.getInstance().refresh(node);
		} else if (node.isBank()) {
			success = MonitorOnBankPool.getInstance().refresh(node);
		} else if (node.isWatch()) {
			success = WatchOnBankPool.getInstance().refresh(node);
		} else if (node.isLog()) {
			success = LogOnBankPool.getInstance().refresh(node);
		}
		return success;
	}

}