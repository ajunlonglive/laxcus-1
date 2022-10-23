/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.pool;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.field.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.build.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * BUILD站点资源管理池。监管HOME站点下的所有注册BUILD站点。<br>
 * 
 * @author scott.liang
 * @version 1.1 11/12/2009
 * @since laxcus 1.0
 */
public class BuildOnHomePool extends HomePool {

	/** BUILD站点静态句柄 **/
	private static BuildOnHomePool selfHandle = new BuildOnHomePool();

	/** 站点地址 -> BUILD站点地址配置 */
	private Map<Node, BuildSite> mapSites = new TreeMap<Node, BuildSite>();

	/** 注册用户名 -> BUILD站点集合 **/
	private Map<Siger, NodeSet> mapUsers = new TreeMap<Siger, NodeSet>();

	/** 数据表名 -> BUILD站点集合 */
	private Map<Space, NodeSet> mapSpaces = new TreeMap<Space, NodeSet>();

	/** 阶段命名 -> 主机地址集合 */
	private Map<Phase, NodeSet> mapPhases = new TreeMap<Phase, NodeSet>();

	/**
	 * 构造私有的BUILD站点管理池 
	 */
	private BuildOnHomePool() {
		super(SiteTag.BUILD_SITE);
		// BUILD节点的默认最大注册数目
		setMaxMembers(2);
	}

	/**
	 * 返回BUILD站点静态句柄
	 * @return BuildOnHomePool实例
	 */
	public static BuildOnHomePool getInstance() {
		return BuildOnHomePool.selfHandle;
	}

	/**
	 * 判断BUILD站点已经分配了某个用户的存储空间
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean contains(Siger siger) {
		NodeSet set = findSites(siger);
		return set != null && set.size() > 0;
	}
	
	/**
	 * 输出全部用户签名
	 * @return Siger列表
	 */
	public List<Siger> getUsers() {
		super.lockMulti();
		try {
			return new ArrayList<Siger>(mapUsers.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据注册用户名查找WORK站点集合
	 * @param siger 注册用户名称
	 * @return 返回对应的节点集合，或者空指针 
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

	/**
	 * 查询关联的阶段命名站点
	 * @param phase 阶段命名
	 * @return 返回对应的节点集合，或者空指针
	 */
	public NodeSet findSites(Phase phase) {
		super.lockMulti();
		try {
			if (phase != null) {
				return mapPhases.get(phase);
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 查找关联数据表的站点
	 * @param space 数据表
	 * @return 返回对应的节点集合，或者空指针
	 */
	public NodeSet findSites(Space space) {
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

	/**
	 * 查找与数据库关联的站点
	 * @param fame 数据库名
	 * @return 返回关联站点列表
	 */
	public List<Node> findSites(Fame fame) {
		TreeSet<Node> nodes = new TreeSet<Node>();
		// 锁定
		super.lockMulti();
		try {
			Iterator<Map.Entry<Space, NodeSet>> iterator = mapSpaces.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Space, NodeSet> entry = iterator.next();
				Space space = entry.getKey();
				if (Laxkit.compareTo(space.getSchema(), fame) == 0) {
					NodeSet set = entry.getValue();
					nodes.addAll(set.list());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		// 输出结果
		return new ArrayList<Node>(nodes);
	}

	/**
	 * 查找有效的ESTABLISH.SIFT阶段命名
	 * @param siger 用户签名
	 * @return Phase列表
	 */
	public List<PushBuildField> validate(Siger siger) {
		TreeMap<Node, PushBuildField> fields = new TreeMap<Node, PushBuildField>();

		super.lockMulti();
		try {
			NodeSet set = mapUsers.get(siger);
			if (set != null) {
				for (Node node : set.list()) {
					BuildSite site = mapSites.get(node);
					if (site == null) {
						continue;
					}
					List<Phase> phases = site.findPhase(siger);
					if (phases.isEmpty()) {
						continue;
					}
					PushBuildField field = fields.get(node);
					if (field == null) {
						field = new PushBuildField(node);
						fields.put(field.getNode(), field);
					}
					field.addPhases(phases);
					field.addSiger(siger);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return new ArrayList<PushBuildField>(fields.values());
	}

	/**
	 * 根据节点地址，查找BUILD站点配置
	 * @param node BUILD节点地址
	 * @return 成功返回站点句柄，否则是空值
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
		// 判断许可证超时
		if (isLicenceTimeout()) {
			Logger.error(this, "infuse", "licence timeout!");
			return false;
		}
		
		BuildSite build = (BuildSite) site;
		Node node = build.getNode();
		
		// 1. 地址不存在
		boolean success = (mapSites.get(node) == null);
		// 如果节点不存在，判断最大数目；若存在，忽略它！
		if (success) {
			if (isMaxMembers(mapSites.size())) {
				Logger.error(this, "infuse", "member out! %d >= %d",mapSites.size(), getMaxMembers());
				return false;
			}
		}

		// 2. 保存站点并且刷新时间
		if (success) {
			success = (mapSites.put(node, build) == null);
		}
		// 3. 保存阶段命名
		if (success) {
			for (Phase phase : build.getPhases()) {
				NodeSet set = mapPhases.get(phase);
				if (set == null) {
					set = new NodeSet();
					mapPhases.put(phase, set);
				}
				set.add(node);
			}
		}
		// 4. 保存表
		if (success) {
			for (Space space : build.getSpaces()) {
				NodeSet set = mapSpaces.get(space);
				if (set == null) {
					set = new NodeSet();
					mapSpaces.put(space, set);
				}
				set.add(node);
			}
		}
		// 5. 保存用户名称
		if (success) {
			for (Siger siger : build.getUsers()) {
				NodeSet set = mapUsers.get(siger);
				if (set == null) {
					set = new NodeSet();
					mapUsers.put(siger, set);
				}
				set.add(node);
			}
			build.refreshTime();
		}

		Logger.note(this, "infuse", success, "from %s", build);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#effuse(com.laxcus.site.Node)
	 */
	@Override
	protected Site effuse(Node node) {
		// 1. 站点存在
		BuildSite site = mapSites.remove(node);
		boolean success = (site != null);
		// 2. 删除阶段命名
		if (success) {
			for (Phase key : site.getPhases()) {
				NodeSet set = mapPhases.get(key);
				if (set != null) {
					set.remove(node);
					if (set.isEmpty())
						mapPhases.remove(key);
				}
			}
		}
		// 3. 删除表名
		if (success) {
			for (Space key : site.getSpaces()) {
				NodeSet set = mapSpaces.get(key);
				if (set != null) {
					set.remove(node);
					if (set.isEmpty()) mapSpaces.remove(key);
				}
			}
		}
		// 4. 删除用户名
		if (success) {
			for (Siger siger : site.getUsers()) {
				NodeSet set = mapUsers.get(siger);
				if (set != null) {
					set.remove(node);
					if (set.isEmpty()) mapUsers.remove(siger);
				}
			}
		}

		Logger.note(this, "effuse", success, "from %s", node);

		return site;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#transmit(com.laxcus.site.Site)
	 */
	@Override
	protected void transmit(Site site) {
		// 通知HOME监视器和WATCH站点，增加BUILD站点
		super.pushSite(site);

		// 从CALL站点管理池选择与这个BUILD站点匹配的账号，通知它发送“PushBuildField”命令到指定的CALL站点
		BuildSite build = (BuildSite) site;
		CallOnHomePool.getInstance().enroll(build.getNode(), build.getUsers());
		
		// 通知CALL站点增加记录
		// CallOnHomePool.getInstance().enroll(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#dismiss(com.laxcus.site.Site)
	 */
	@Override
	protected void dismiss(Site site) {
		// 通知HOME监视站点和WATCH站点，删除记录
		super.dropSite(site);
		// 通知CALL站点删除记录
		CallOnHomePool.getInstance().revoke(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#destroy(com.laxcus.site.Site)
	 */
	@Override
	protected void destroy(Site site) {
		// 通知HOME监视站点和WATCH站点，删除记录（故障状态）
		super.destroySite(site);
		// 通知CALL站点删除记录
		CallOnHomePool.getInstance().revoke(site);
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

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.thread.VirtualThread#process()
	//	 */
	//	@Override
	//	public void process() {
	//		Logger.info(this, "process", "into ...");
	//		while (!isInterrupted()) {
	//			if (mapSites.size() > 0) { // 超时检查
	//				check(mapSites);
	//			}
	//			sleep();
	//		}
	//		Logger.info(this, "process", "exit");
	//	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapSites.clear();
		mapUsers.clear();
		mapPhases.clear();
		mapSites.clear();
	}


}