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
import com.laxcus.site.bank.*;

/**
 * TOP站点上的BANK站点管理池。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/25/2018
 * @since laxcus 1.0
 */
public final class BankOnTopPool extends TopPool {

	/** BANK站点静态句柄 **/
	private static BankOnTopPool selfHandle = new BankOnTopPool();

	/** 站点地址 -> BANK站点 **/
	private Map<Node, BankSite> mapSites = new TreeMap<Node, BankSite>();

	/**
	 * 构造默认的BANK站点管理池
	 */
	private BankOnTopPool() {
		super(SiteTag.BANK_SITE);
		// BANK节点的默认最大注册数目
		setMaxMembers(3);
	}

	/**
	 * 返回BANK站点管理池的静态句柄
	 * @return BANK站点管理池句柄
	 */
	public static BankOnTopPool getInstance() {
		return BankOnTopPool.selfHandle;
	}

	/**
	 * 获得BANK管理节点地址
	 * @return 返回节点地址，或者空指针
	 */
	public Node getManagerSite() {
		// 锁定
		super.lockMulti();
		try {
			Iterator<Map.Entry<Node, BankSite>> iterator = mapSites.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Node, BankSite> entry = iterator.next();
				if (entry.getValue().isManager()) {
					return entry.getKey().duplicate();
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
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
		// 判断许可证超时
		if (isLicenceTimeout()) {
			Logger.error(this, "infuse", "licence timeout!");
			return false;
		}
		
		// 保存一个注册站点
		BankSite bank = (BankSite) site;
		Node node = bank.getNode();
		
		Logger.debug(this, "infuse", "member %d >= %d", mapSites.size(), getMaxMembers());

		// 1. 地址不能重复
		boolean success = (mapSites.get(node) == null);

		// 如果节点不存在，判断最大数目；若存在，忽略它！
		if (success) {
			if (isMaxMembers(mapSites.size())) {
				Logger.error(this, "infuse", "member out! %d >= %d", mapSites.size(), getMaxMembers());
				return false;
			}
		}

		// 2. 保存地址
		if (success) {
			mapSites.put(node, bank);
			bank.refreshTime(); // 刷新时间，用于超时检测时
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
		// 通知TOP监视站点和其它BANK站点
		super.pushSite(site);

		//		NodeSet nodes = new NodeSet();
		//		// 收集HOME/AID/ARCHIVE/FRONT/TOP MONITOR站点地址，通知这个BANK站点
		//		nodes.pushAll(HomeOnTopPool.getInstance().list());
		//		nodes.pushAll(FrontOnTopPool.getInstance().list());
		//		nodes.pushAll(AidOnTopPool.getInstance().list());
		//		nodes.pushAll(ArchiveOnTopPool.getInstance().list());
		//		nodes.pushAll(BankOnTopPool.getInstance().list());
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

		//		// 更新BANK账号
		//		super.lockSingle();
		//		try {
		//			if (manager.hasRefresh()) {
		//				manager.reload();
		//			}
		//		} catch (Throwable e) {
		//			Logger.fatal(e);
		//		} finally {
		//			super.unlockSingle();
		//		}
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