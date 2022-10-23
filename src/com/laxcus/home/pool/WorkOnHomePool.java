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
import com.laxcus.site.work.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * WORK站点管理池。<br>
 * 提供HOME集群的WORK站点管理服务。
 * 
 * @author scott.liang
 * @version 1.2 10/28/2012
 * @since laxcus 1.0
 */
public final class WorkOnHomePool extends HomePool { 

	/** WORK站点管理池静态句柄 **/
	private static WorkOnHomePool selfHandle = new WorkOnHomePool();

	/** WORK站点地址  -> 站点参数 **/
	private Map<Node, WorkSite> mapSites = new TreeMap<Node, WorkSite>();

	/** 用户名称  -> WORK站点集合 **/
	private Map<Siger, NodeSet> mapUsers = new TreeMap<Siger, NodeSet>();

	/** 数据表名 -> WORK站点集合 */
	private Map<Space, NodeSet> mapSpaces = new TreeMap<Space, NodeSet>();

	/** 阶段命名  -> WORK站点集合 **/
	private Map<Phase, NodeSet> mapPhases = new TreeMap<Phase, NodeSet>();

	/**
	 * 构造WORK站点管理池。
	 */
	private WorkOnHomePool() {
		super(SiteTag.WORK_SITE);
		// WORK节点的默认最大注册数目
		setMaxMembers(2);
	}

	/**
	 * 返回静态句柄，一个进程中只能有一个
	 * @return WorkOnHomePool实例
	 */
	public static WorkOnHomePool getInstance() {
		return WorkOnHomePool.selfHandle;
	}
	
	/**
	 * 判断WORK站点已经分配了某个用户的存储空间
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
	 * 查找有效的CONDUCT.TO/CONDUCT.SUBTO阶段命名
	 * @param siger 用户签名
	 * @return Phase列表
	 */
	public List<PushWorkField> validate(Siger siger) {
		TreeMap<Node, PushWorkField> fields = new TreeMap<Node, PushWorkField>();

		super.lockMulti();
		try {
			NodeSet set = mapUsers.get(siger);
			if (set != null) {
				for (Node node : set.list()) {
					WorkSite site = mapSites.get(node);
					if (site == null) {
						continue;
					}
					List<Phase> phases = site.findPhase(siger);
					if (phases.isEmpty()) {
						continue;
					}
					// 判断存在！
					PushWorkField field = fields.get(node);
					if (field == null) {
						field = new PushWorkField(node);
						fields.put(field.getNode(), field);
					}
					field.addPhases(phases);
					// 保存这个签名
					field.addSiger(siger);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		
		return new ArrayList<PushWorkField>(fields.values());
	}

	/**
	 * 根据阶段命名，枚举匹配的WORK地址
	 * @param phase 阶段命名
	 * @return 节点列表
	 */
	public List<Node> enumlate(Phase phase) {
		ArrayList<Node> array = new ArrayList<Node>();

		super.lockMulti();
		try {
			if (phase.getSub() == null) {
				for (Phase key : mapPhases.keySet()) {
					if (key.getSock().compareTo(phase.getSock()) == 0) {
						NodeSet set = mapPhases.get(key);
						if (set != null) array.addAll(set.list());
					}
				}
			} else {
				NodeSet set = mapPhases.get(phase);
				if (set != null) array.addAll(set.list());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return array;
	}

	/**
	 * 根据节点地址， 查找对应的配置
	 * @param node 节点地址
	 * @return 站点实例
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
		
		WorkSite work = (WorkSite) site;
		Node node = work.getNode();
		
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
			success = (mapSites.put(node, work) == null);
		}
		// 3. 保存阶段命名
		if (success) {
			for (Phase phase : work.getPhases()) {
				NodeSet set = mapPhases.get(phase);
				if (set == null) {
					set = new NodeSet();
					mapPhases.put(phase, set);
				}
				set.add(node);
			}
		}
		// 4. 保存表名
		if (success) {
			for (Space space : work.getSpaces()) {
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
			for (Siger siger : work.getUsers()) {
				NodeSet set = mapUsers.get(siger);
				if (set == null) {
					set = new NodeSet();
					mapUsers.put(siger, set);
				}
				set.add(node);
			}
			work.refreshTime();
		}

		Logger.note(this, "infuse", success, "from %s", work);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#effuse(com.laxcus.site.Node)
	 */
	@Override
	protected Site effuse(Node node) {
		// 1. 站点存在
		WorkSite site = mapSites.remove(node);
		boolean success = (site != null);
		// 2. 删除阶段命名
		if (success) {
			for (Phase key : site.getPhases()) {
				NodeSet set = mapPhases.get(key);
				if (set != null) {
					set.remove(node);
					if (set.isEmpty()) mapPhases.remove(key);
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
					if (set.isEmpty())
						mapUsers.remove(siger);
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
		// 通知HOME监视站点和WATCH站点，一个WORK站点加入
		super.pushSite(site);
		
		// 从CALL站点管理池选择与这个WORK站点匹配的账号，通知它发送“PushWorkField”命令到指定的CALL站点
		WorkSite work = (WorkSite) site;
		CallOnHomePool.getInstance().enroll(work.getNode(), work.getUsers());

//		// 通知对应的CALL站点，增加对WORK站点的记录
//		CallOnHomePool.getInstance().enroll(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#dismiss(com.laxcus.site.Site)
	 */
	@Override
	protected void dismiss(Site site) {
		// 通知HOME监视站点，撤销一个WORK站点记录
		super.dropSite(site);
		// 通知CALL站点，解除对WORK站点的记录
		CallOnHomePool.getInstance().revoke(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#destroy(com.laxcus.site.Site)
	 */
	@Override
	protected void destroy(Site site) {
		// 通知HOME监视器和WATCH站点，一个WORK站点发生故障，撤销它的记录，并检查和处理
		super.destroySite(site);
		// 通知CALL站点，撤销一个WORK站点记录
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
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapSpaces.clear();
		mapPhases.clear();
		mapUsers.clear();
		mapSites.clear();
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
//			// 检查超时
//			if (mapSites.size() > 0) {
//				check(mapSites);
//			}
//			// 延时
//			sleep();
//		}
//		Logger.info(this, "process", "exit");
//	}
}