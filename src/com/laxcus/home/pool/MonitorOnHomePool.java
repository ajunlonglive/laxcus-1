/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.pool;

import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.home.*;

/**
 * HOME站点的监视器管理池。<br>
 * 保存HOME备用站点地址。
 * 
 * @author scott.liang
 * @version 1.0 7/15/2013
 * @since laxcus 1.0
 */
public final class MonitorOnHomePool extends HomePool { 

	/** HOME站点管理池静态句柄 **/
	private static MonitorOnHomePool selfHandle = new MonitorOnHomePool();

	/** HOME站点地址  -> 站点参数 **/
	private Map<Node, HomeSite> mapSites = new TreeMap<Node, HomeSite>();

	/**
	 * 构造HOME站点管理池。
	 */
	private MonitorOnHomePool() {
		super(SiteTag.HOME_SITE);
	}

	/**
	 * 返回静态句柄，一个进程中只能有一个
	 * @return MonitorOnHomePool实例
	 */
	public static MonitorOnHomePool getInstance() {
		return MonitorOnHomePool.selfHandle;
	}

	/**
	 * 返回注册的HOME监视站点地址
	 * 
	 * @return - List<Node>
	 */
	public List<Node> getNodes() {
		super.lockMulti();
		try {
			return new ArrayList<Node>(mapSites.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#infuse(com.laxcus.site.Site)
	 */
	@Override
	protected boolean infuse(Site site) {
		// 保存一个注册站点
		HomeSite home = (HomeSite) site;
		Node node = home.getNode();

		// 1. 地址不能重复
		boolean success = (mapSites.get(node) == null);
		// 2. 保存地址
		if (success) {
			mapSites.put(node, home);
			home.refreshTime(); // 刷新时间，用于超时检测时
		}

		Logger.note(this, "infuse", success, "from %s", node);
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#effuse(com.laxcus.site.Node)
	 */
	@Override
	protected Site effuse(Node node) {
		return mapSites.remove(node);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#transmit(com.laxcus.site.Site)
	 */
	@Override
	protected void transmit(Site site) {
		// 通知WATCH站点（集群管理员），一个HOME站点加入
		super.pushSite(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#dismiss(com.laxcus.site.Site)
	 */
	@Override
	protected void dismiss(Site site) {
		// 通知集群管理员，一个HOME站点正常退出
		super.dropSite(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#destroy(com.laxcus.site.Site)
	 */
	@Override
	protected void destroy(Site site) {
		// 通知集群管理员，一个HOME站点异常消失，请求检查和处理
		super.destroySite(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#iterator()
	 */
	@Override
	protected Map<Node, ? extends Site> iterator() {
		return mapSites;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#find(com.laxcus.site.Node)
	 */
	@Override
	public Site find(Node node) {
		super.lockMulti();
		try {
			if (node != null) {
				return mapSites.get(node);
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapSites.clear();
	}

}