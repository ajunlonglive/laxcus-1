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
import com.laxcus.site.entrance.*;
import com.laxcus.util.*;

/**
 * ENTRANCE站点管理池。<br>
 * 
 * 保存ENTRANCE站点的注册数据
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class EntranceOnBankPool extends BankPool {

	/** 管理池句柄 **/
	private static EntranceOnBankPool selfHandle = new EntranceOnBankPool();

	/** ENTRANCE站点地址 -> ENTRANCE站点配置 **/
	private Map<Node, EntranceSite> mapSites = new TreeMap<Node, EntranceSite>();

	/**
	 * 构造默认的ENTRANCE站点管理池
	 */
	private EntranceOnBankPool() {
		super(SiteTag.ENTRANCE_SITE);
		// ENTRANCE节点的默认最大注册数目
		setMaxMembers(1);
	}

	/**
	 * 返回ENTRANCE站点管理池的静态句柄
	 * @return ENTRANCE站点管理池句柄
	 */
	public static EntranceOnBankPool getInstance() {
		return EntranceOnBankPool.selfHandle;
	}

	/**
	 * 输出全部ENTRANCE站点地址
	 * @return ENTRANCE站点列表
	 */
	public List<Node> getNodes() {
		ArrayList<Node> array = new ArrayList<Node>();
		super.lockMulti();
		try {
			array.addAll(mapSites.keySet());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 输出全部ENTRANCE站点地址
	 * @return ENTRANCE站点列表
	 */
	public List<Site> getSites() {
		ArrayList<Site> array = new ArrayList<Site>();
		super.lockMulti();
		try {
			array.addAll(mapSites.values());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}
	
	/**
	 * 根据注册地址，查找匹配的站点
	 * @param node ENTRANCE站点注册地址
	 * @return Site实例，或者空指针
	 */
	@Override
	public Site find(Node node) {
		Laxkit.nullabled(node);
		super.lockMulti();
		try {
			return mapSites.get(node);
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
		// 判断许可证超时
		if (isLicenceTimeout()) {
			Logger.error(this, "infuse", "licence timeout!");
			return false;
		}
		
		EntranceSite entrance = (EntranceSite) site;
		Node node = entrance.getNode();
		
		// 1. 不允许重复注册
		boolean success = (mapSites.get(node) == null);
		// 如果节点不存在，判断最大数目；若存在，忽略它！
		if (success) {
			if (isMaxMembers(mapSites.size())) {
				Logger.error(this, "infuse", "member out! %d >= %d",mapSites.size(), getMaxMembers());
				return false;
			}
		}

		// 2. 保存管理员账号和刷新时间
		if (success) {
			// 保存注册地址
			mapSites.put(node, entrance);
		}

		// 刷新注册时间
		if (success) {
			entrance.refreshTime();
		}

		Logger.note(this, "infuse", success, "from %s", site);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#effuse(com.laxcus.site.Node)
	 */
	@Override
	protected Site effuse(Node node) {
		// 删除注册地址
		EntranceSite site = mapSites.remove(node);

		// 判断站点存在
		boolean success = (site != null);

		Logger.debug(this, "effuse", success, "from %s", node);

		return site;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#transmit(com.laxcus.site.Site)
	 */
	@Override
	protected void transmit(Site site) {
		// 通知TOP监视站点和WATCH站点，一个ENTRANCE站点加入
		super.pushSite(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#dismiss(com.laxcus.site.Site)
	 */
	@Override
	protected void dismiss(Site site) {
		// 通知TOP监视站点和WATCH站点，一个ENTRANCE站点退出
		super.dropSite(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#destroy(com.laxcus.site.Site)
	 */
	@Override
	protected void destroy(Site site) {
		// 通知TOP监视站点和WATCH站点，一个ENTRANCE站点故障被撤销，请求检查和处理
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