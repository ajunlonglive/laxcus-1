/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.pool;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.home.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.util.set.*;

/**
 * HOME站点管理池。<br>
 * TOP站点管理和监督它们的运行。包括增加、删除、更新、超时检测，以及其它HOME站点服务。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/12/2009
 * @since laxcus 1.0
 */
public class HomeOnTopPool extends TopPool {

	/** 管理池静态句柄 **/
	private static HomeOnTopPool selfHandle = new HomeOnTopPool();

	/** HOME站点地址 -> 元数据资源 */
	private Map<Node, HomeSite> mapSites = new TreeMap<Node, HomeSite>();

	/** 注册用户 -> HOME站点集合 **/
	private Map<Siger, NodeSet> mapUsers = new TreeMap<Siger, NodeSet>();

	/** 数据库表 -> HOME站点集合 **/
	private Map<Space, NodeSet> mapSpaces = new TreeMap<Space, NodeSet>();

	/**
	 * 构造私有的HOME管理池
	 */
	private HomeOnTopPool() {
		super(SiteTag.HOME_SITE);
		// HOME节点默认最大注册数目，在许可证之前设置
		setMaxMembers(3);
	}

	/**
	 * 返回HOME管理池静态句柄
	 * @return
	 */
	public static HomeOnTopPool getInstance() {
		return HomeOnTopPool.selfHandle;
	}

	/**
	 * 输出全部用户签名
	 * @return Siger列表
	 */
	public List<Siger> getSigers() {
		super.lockMulti();
		try {
			return new ArrayList<Siger>(mapUsers.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 输出全部注册的HOME站点地址
	 * @return Node列表
	 */
	public List<Node> getNodes() {
		super.lockMulti();
		try {
			return new ArrayList<Node>(mapSites.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 输出全部HOME站点地址
	 * @return SiteHost列表
	 */
	public List<SiteHost> getSiteHosts() {
		ArrayList<SiteHost> array = new ArrayList<SiteHost>();
		super.lockMulti();
		try {
			for (Node node : mapSites.keySet()) {
				array.add(node.getHost());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 输出全部数据表名 
	 * @return 数据表名列表
	 */
	public List<Space> getSpaces() {
		super.lockMulti();
		try {
			return new ArrayList<Space>(mapSpaces.keySet());
		} finally {
			super.unlockMulti();
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
		mapSpaces.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#iterator()
	 */
	@Override
	protected Map<Node, ? extends Site> iterator() {
		return mapSites;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#infuse(com.laxcus.site.Site)
	 */
	@Override
	protected boolean infuse(Site sub) {
		// 判断许可证超时
		if (isLicenceTimeout()) {
			Logger.error(this, "infuse", "licence timeout!");
			return false;
		}
		
		HomeSite site = (HomeSite) sub;
		Node node = site.getNode();

		Logger.debug(this, "infuse", "memeber size:%d", site.size());
		Logger.debug(this, "infuse", "space size: %d", site.getSpaces().size());
		Logger.debug(this, "infuse", "member %d >= %d", mapSites.size(), getMaxMembers());

		// 1. 站点地址不能重复
		boolean success = (mapSites.get(node) == null);

		// 如果节点不存在，判断最大数目；若存在，忽略它！
		if (success) {
			// 判断达到最大成员数
			if (isMaxMembers(mapSites.size())) {
				Logger.error(this, "infuse", "member out! %d >= %d", mapSites.size(), getMaxMembers());
				return false;
			}
		}

		// 2. 保存站点并且刷新时间
		if (success) {
			success = (mapSites.put(node, site) == null);
			site.refreshTime();
		}
		// 2.保存注册用户名
		if (success) {
			for (Siger siger : site.getSigers()) {
				NodeSet set = mapUsers.get(siger);
				if (set == null) {
					set = new NodeSet();
					mapUsers.put(siger, set);
				}
				set.add(node);
			}
		}
		// 3. 保存数据表名
		if (success) {
			for (Space space : site.getSpaces()) {
				NodeSet set = mapSpaces.get(space);
				if (set == null) {
					set = new NodeSet();
					mapSpaces.put(space, set);
				}
				set.add(node);
			}
		}

		Logger.debug(this, "infuse", success, "from %s", node);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#effuse(com.laxcus.site.Node)
	 */
	@Override
	protected Site effuse(Node node) {
		HomeSite site = mapSites.remove(node);
		boolean success = (site != null);
		// 删除表集合
		if (success) {
			for (Space space : site.getSpaces()) {
				NodeSet set = mapSpaces.get(space);
				if (set != null) {
					set.remove(node);
					if (set.isEmpty()) {
						mapSpaces.remove(space);
					}
				}
			}
		}
		// 删除注册用户名
		if (success) {
			for (Siger username : site.getSigers()) {
				NodeSet set = mapUsers.get(username);
				if (set != null) {
					set.remove(node);
					if (set.isEmpty()) {
						mapUsers.remove(username);
					}
				}
			}
		}

		Logger.debug(this, "effuse", success, "from %s", node);
		return site;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#transmit(com.laxcus.site.Site)
	 */
	@Override
	protected void transmit(Site site) {
		// 通知WATCH站点和TOP监视器站点，一个HOME站点加入
		super.pushSite(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#dismiss(com.laxcus.site.Site)
	 */
	@Override
	protected void dismiss(Site site) {
		// 通知WATCH站点和TOP监视站点，取消一个HOME站点记录
		super.dropSite(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#destroy(com.laxcus.site.Site)
	 */
	@Override
	protected void destroy(Site site) {
		// 通知WATCH站点和TOP监视器站点，撤销一个HOME站点记录；
		super.destroySite(site);
	}

	/**
	 * 根据用户名，查找注册的HOME站点集合
	 * @param siger 注册用户名称
	 * @return HOME站点地址集合
	 */
	public NodeSet findSites(Siger siger) {
		super.lockMulti();
		try {
			if (siger != null) {
				return mapUsers.get(siger);
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
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

	/**
	 * 根据数据表名，查找HOME地址
	 * @param space 数据表名 
	 * @return HOME站点集合
	 */
	public NodeSet find(Space space) {
		super.lockMulti();
		try {
			if (space != null) {
				return mapSpaces.get(space);
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

}