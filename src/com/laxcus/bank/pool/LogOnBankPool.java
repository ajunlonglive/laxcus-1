/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.pool;

import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.log.*;
import com.laxcus.util.net.*;

/**
 * 日志站点管理池。<br>
 * 监管BANK集群下的所有注册站点，包括ACCOUNT、HASH、GATE、ENTRANCE。这些站点负责资源管理工作。
 * 
 * @author scott.liang 
 * @version 1.0 6/25/2018
 * @since laxcus 1.0
 */
public final class LogOnBankPool extends BankPool {

	/** 静态句柄 **/
	private static LogOnBankPool selfHandle = new LogOnBankPool();

	/** 站点地址 -> 日志站点 **/
	private Map<Node, LogSite> mapSites = new TreeMap<Node, LogSite>();

	/**
	 * 构造日志站点管理池
	 */
	private LogOnBankPool() {
		super(SiteTag.LOG_SITE);
	}

	/**
	 * 返回日志站点管理池的静态句柄
	 * @return 日志站点管理池实例
	 */
	public static LogOnBankPool getInstance() {
		return LogOnBankPool.selfHandle;
	}

	/**
	 * 根据节点地址， 查找对应的配置
	 * @param node 节点地址
	 * @return 对应的站点
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
	 * @see com.laxcus.pool.HubPool#infuse(com.laxcus.site.Site)
	 */
	@Override
	protected boolean infuse(Site site) {
		// 保存一个注册站点
		LogSite log = (LogSite) site;
		Node node = log.getNode();

		// 1. 地址不能重复
		boolean success = (mapSites.get(node) == null);
		// 2. 保存地址
		if (success) {
			mapSites.put(node, log);
			log.refreshTime(); // 刷新时间，用于超时检测时
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
		super.pushSite(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#dismiss(com.laxcus.site.Site)
	 */
	@Override
	protected void dismiss(Site site) {
		// 通知BANK监视器和WATCH站点，一个站点正常退出
		super.dropSite(site);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#destroy(com.laxcus.site.Site)
	 */
	@Override
	protected void destroy(Site site) {
		// 通知BANK监视器和WATCH站点，以故障状态删除站点
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

	/**
	 * 根据请求站点类型，分配一个日志站点的地址 
	 * @param family 站点类型
	 * @return SiteHost实例，或者空
	 */
	public SiteHost selectLog(byte family) {
		Logger.info(this, "selectLog", "this is '%s'", SiteTag.translate(family));

		Node backup = null;
		int min = Integer.MAX_VALUE;
		super.lockSingle();
		try {
			for (Node node : mapSites.keySet()) {
				LogSite site = mapSites.get(node);
				if (site.hasLog(family)) {
					if (site.getCount() < min) {
						min = site.getCount();
						backup = node;
					}
				}
			}
			if(backup == null) {
				Logger.error(this, "selectLog", "cannot find log site '%s'", SiteTag.translate(family));
				return null;
			}
			LogSite logSite = mapSites.get(backup);
			logSite.addCount(1);

			LogNode node = logSite.findLog(family);
			return new SiteHost(logSite.getInetAddress(), node.getPort(), node.getPort());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 根据请求站点类型，分配一个日志站点的地址 
	 * @param family 站点类型
	 * @return SiteHost实例，或者空
	 */
	public SiteHost selectTig(byte family) {
		Logger.info(this, "selectTig", "this is '%s'", SiteTag.translate(family));

		Node backup = null;
		int min = Integer.MAX_VALUE;
		super.lockSingle();
		try {
			for (Node node : mapSites.keySet()) {
				LogSite site = mapSites.get(node);
				if (site.hasTig(family)) {
					if (site.getCount() < min) {
						min = site.getCount();
						backup = node;
					}
				}
			}
			if(backup == null) {
				Logger.error(this, "selectTig", "cannot find tig site '%s'", SiteTag.translate(family));
				return null;
			}
			LogSite tigSite = mapSites.get(backup);
			tigSite.addCount(1);

			TigNode node = tigSite.findTig(family);
			return new SiteHost(tigSite.getInetAddress(), node.getPort(), node.getPort());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}
	
	/**
	 * 根据请求站点类型，分配一个日志站点的地址 
	 * @param family 站点类型
	 * @return SiteHost实例，或者空
	 */
	public SiteHost selectBill(byte family) {
		Logger.info(this, "selectBill", "this is '%s'", SiteTag.translate(family));

		Node backup = null;
		int min = Integer.MAX_VALUE;
		super.lockSingle();
		try {
			for (Node node : mapSites.keySet()) {
				LogSite site = mapSites.get(node);
				if (site.hasBill(family)) {
					if (site.getCount() < min) {
						min = site.getCount();
						backup = node;
					}
				}
			}
			if (backup == null) {
				Logger.error(this, "selectBill", "cannot find tig site '%s'", SiteTag.translate(family));
				return null;
			}
			LogSite tigSite = mapSites.get(backup);
			tigSite.addCount(1);

			BillNode node = tigSite.findBill(family);
			return new SiteHost(tigSite.getInetAddress(), node.getPort(), node.getPort());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		this.mapSites.clear();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

}