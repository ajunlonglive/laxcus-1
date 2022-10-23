/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.pool;

import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.watch.*;
import com.laxcus.util.watch.*;

/**
 * TOP站点上的WATCH站点管理池。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/23/2013
 * @since laxcus 1.0
 */
public final class WatchOnTopPool extends TopPool {

	/** WATCH站点静态句柄 **/
	private static WatchOnTopPool selfHandle = new WatchOnTopPool();

	/** 站点地址 -> WATCH站点 **/
	private Map<Node, WatchSite> mapSites = new TreeMap<Node, WatchSite>();
	
	/** 账号管理器 **/
	private WatchManager manager = new WatchManager();

	/**
	 * 构造默认的WATCH站点管理池
	 */
	private WatchOnTopPool() {
		super(SiteTag.WATCH_SITE);
	}

	/**
	 * 返回WATCH站点管理池的静态句柄
	 * @return WATCH站点管理池句柄
	 */
	public static WatchOnTopPool getInstance() {
		return WatchOnTopPool.selfHandle;
	}
	
	/**
	 * 设置文件名
	 * @param e
	 */
	public void setFile(String e) {
		manager.setFile(e);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#iterator()
	 */
	@Override
	protected Map<Node, ? extends Site> iterator() {
		// TODO Auto-generated method stub
		return mapSites;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#infuse(com.laxcus.site.Site)
	 */
	@Override
	protected boolean infuse(Site site) {
		// 保存一个注册站点
		WatchSite watch = (WatchSite) site;
		Node node = watch.getNode();
		
		// 1. 地址不能重复
		boolean success = (mapSites.get(node) == null);
		// 2. 判断包含这个账号
		if (success) {
			WatchUser user = watch.getUser();
			success = manager.contains(user);
		}
		// 3. 保存地址
		if (success) {
			mapSites.put(node, watch);
			watch.refreshTime(); // 刷新时间，用于超时检测时
		}

		Logger.note(this, "infuse", success, "from %s", node);
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#effuse(com.laxcus.site.Node)
	 */
	@Override
	protected Site effuse(Node node) {
		if (node != null) {
			return mapSites.remove(node);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#transmit(com.laxcus.site.Site)
	 */
	@Override
	protected void transmit(Site site) {
		// 通知TOP监视站点和其它WATCH站点
		super.pushSite(site);

//		NodeSet nodes = new NodeSet();
//		// 收集HOME/AID/ARCHIVE/FRONT/TOP MONITOR站点地址，通知这个WATCH站点
//		nodes.pushAll(HomeOnTopPool.getInstance().list());
//		nodes.pushAll(FrontOnTopPool.getInstance().list());
//		nodes.pushAll(AidOnTopPool.getInstance().list());
//		nodes.pushAll(ArchiveOnTopPool.getInstance().list());
//		nodes.pushAll(WatchOnTopPool.getInstance().list());
//		nodes.pushAll(MonitorOnTopPool.getInstance().list());
//		// 删除自己
//		nodes.remove(site.getNode());
//
//		// 投递命令
//		for (Node node : nodes.list()) {
//			PushSite cmd = new PushSite(node);
//			ShiftCastSite shift = new ShiftCastSite(cmd, site.getNode());
//			TopCommandPool.getInstance().admit(shift);
//		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#dismiss(com.laxcus.site.Site)
	 */
	@Override
	protected void dismiss(Site site) {
		// 从TOP监视器上注销
		super.dropSite(site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#destroy(com.laxcus.site.Site)
	 */
	@Override
	protected void destroy(Site site) {
		// 从TOP监视器上注销
		super.destroySite(site);
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
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#check()
	 */
	@Override
	protected void check() {
		// 调用上级检查
		super.check();

		// 更新WATCH账号
		super.lockSingle();
		try {
			if (manager.hasRefresh()) {
				manager.reload();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
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