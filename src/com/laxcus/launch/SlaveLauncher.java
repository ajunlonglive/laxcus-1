/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

import com.laxcus.log.client.*;
import com.laxcus.remote.client.hub.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 子级站点启动器。<br><br>
 * 
 * 按照LAXCUS系统架构对站点的定义，除了TOP站点之外的站点都是子级站点。
 * HOME站点是个特例，它同时具有管理站点和子级站点的双重身份，对TOP站点是子级站点，对LOG/CALL/DATA/WORK/BUILD是管理站点。
 * 
 * @author scott.liang
 * @version 1.1 9/26/2015
 * @since laxcus 1.0
 */
public abstract class SlaveLauncher extends SiteLauncher {

	/** 上级站点地址 */
	private Node hub;

	/**
	 * 构造子级站点启动器
	 * @param printer 日志打印器，或者空打针
	 */
	protected SlaveLauncher(LogPrinter printer) {
		super(printer);
		// 默认记录日志
		setPrintFault(true);
	}

	/**
	 * 构造子级站点启动器
	 */
	protected SlaveLauncher() {
		this(null);
	}
	
	/**
	 * 判断定义了HUB地址
	 * @return 返回真或者假
	 */
	public boolean hasHub() {
		return hub != null;
	}

	/**
	 * 判断是注册站点地址
	 * @param e 节点实例
	 * @return 返回真或者假
	 */
	public boolean isHub(Node e) {
		return Laxkit.compareTo(hub, e) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getHub()
	 */
	@Override
	public Node getHub() {
		return hub;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#setHub(com.laxcus.site.Node)
	 */
	@Override
	public void setHub(Node e) {
		hub = e;
	}

	/**
	 * 返回上级站点地址
	 * @return
	 */
	public SiteHost getHubHost() {
		return hub.getHost();
	}

	/**
	 * 停止日志服务
	 */
	protected void stopLog() {
		if (Logger.isRunning()) {
			Logger.stopService();
		}
	}

	/**
	 * 获得连接上级站点的客户端
	 * @return
	 */
	public HubClient fetchHubClient() {
		return fetchHubClient(hub);
	}
	
	/**
	 * 预加载
	 * @return 加载成功返回“真”，否则“假”。
	 */
	protected boolean preload() {
		return super.preload(hub);
	}

	/**
	 * 注册到上级站点
	 */
	protected boolean login(Site site) {
		return super.login(site, hub);
	}

	/**
	 * 定时向上级站点发送激活通知，指定发送包数目
	 * @param size 激活包数目
	 */
	protected void hello(int size) {
		for (int i = 0; i < size; i++) {
			hello();
		}
	}

}