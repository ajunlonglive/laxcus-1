/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home;

import com.laxcus.fixp.*;
import com.laxcus.home.pool.*;
import com.laxcus.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.net.*;

/**
 * HOME站点数据包适配器
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class HomePacketAdapter extends PacketAdapter {

	/**
	 * 构造HOME站点数据包适配器
	 */
	public HomePacketAdapter() {
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

		if (Assert.isHelo(cmd)) {
			resp = refresh(request); // 激活注册节点
		} else if (Assert.isShutdown(cmd)) {
			shutdown(HomeLauncher.getInstance(), request);
		} else if (Assert.isComeback(cmd)) {
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
	 * 判断数据包来源，做相关的激活。
	 * @param packet
	 */
	private void doISee(Packet packet) {
		SocketHost from = packet.getRemote();
		Logger.debug(this, "doISee", "from %s", from);

		if(isFromHub(from)) {
			HomeLauncher.getInstance().refreshEndTime();
		} else if(isFromRunSite(from)) {
			HomeMonitor.getInstance().refreshEndTime();
		}
	}
	
	/**
	 * 上级站点通知没有注册，必须再次注册
	 * @param packet
	 */
	private void doNotLogin(Packet packet){
		SocketHost from = packet.getRemote();
		Logger.debug(this, "doNotLogin", "from %s", from);

		if(isFromHub(from)) {
			HomeLauncher.getInstance().kiss();
		} else if(isFromRunSite(from)) {
			HomeMonitor.getInstance().kiss();
		}
	}

	/**
	 * 判断是来自TOP站点的应答
	 * @param from
	 * @return
	 */
	private boolean isFromHub(SocketHost from) {
		Node hub = HomeLauncher.getInstance().getHub();
		return hub.getPacketHost().compareTo(from) == 0;
	}

	/**
	 * 判断是来自HOME管理站点的应答
	 * @param from
	 * @return
	 */
	private boolean isFromRunSite(SocketHost from) {
		Node site = HomeLauncher.getInstance().getManager();
		boolean success = (site != null);
		if (success) {
			success = (site.getPacketHost().compareTo(from) == 0);
		}
		return success;
	}

	/**
	 * 召回
	 * @param packet
	 */
	private void comeback(Packet packet) {
		SocketHost endpoint = packet.getRemote();
		Logger.debug(this, "comeback", "from %s", endpoint);

		if (isFromHub(endpoint)) {
			HomeLauncher.getInstance().hurry();
		} else if (isFromRunSite(endpoint)) {
			// 通知快速 注册
			HomeMonitor.getInstance().hurry();
		}
	}

	/**
	 * DATA/WORK/CALL/BUILD/LOG/WATCH站点激活注册地址
	 * @see com.laxcus.invoke.PacketAdapter#active(com.laxcus.site.Node)
	 */
	@Override
	protected boolean active(Node node) {
		boolean success = false;
		// 工作站点
		if (node.isCall()) {
			success = CallOnHomePool.getInstance().refresh(node);
		} else if (node.isData()) {
			success = DataOnHomePool.getInstance().refresh(node);
		} else if (node.isWork()) {
			success = WorkOnHomePool.getInstance().refresh(node);
		} else if (node.isBuild()) {
			success = BuildOnHomePool.getInstance().refresh(node);
		}
		// 服务管理站点
		else if (node.isHome()) {
			success = MonitorOnHomePool.getInstance().refresh(node);
		} else if (node.isLog()) {
			success = LogOnHomePool.getInstance().refresh(node);
		} else if (node.isWatch()) {
			success = WatchOnHomePool.getInstance().refresh(node);
		}
		return success;
	}

}