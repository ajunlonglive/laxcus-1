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
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.data.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * DATA站点管理池。<br>
 * 保存登录到HOME站点下的DATA站点数据。
 * 
 * @author scott.liang
 * @version 1.1 11/5/2009
 * @since laxcus 1.0
 */
public class DataOnHomePool extends HomePool {

	/** DATA站点管理池句柄 **/
	private static DataOnHomePool selfHandle = new DataOnHomePool();

	/** 站点地址 -> DATA站点实例 **/
	private Map<Node, DataSite> mapSites = new TreeMap<Node, DataSite>();
	
	/** 阶段命名的用户签名 -> DATA站点集合 **/
	private Map<Siger, NodeSet> mapUsers = new TreeMap<Siger, NodeSet>();

	/** 命名阶段 -> DATA站点集合 */
	private Map<Phase, NodeSet> mapPhases = new TreeMap<Phase, NodeSet>();

	/** 表名 -> DATA站点集合 */
	private Map<Space, NodeSet> mapSpaces = new TreeMap<Space, NodeSet>();
	
	/** DATA主站点 **/
	private NodeSet masters = new NodeSet();

	/** DATA从站点 **/
	private NodeSet slaves = new NodeSet();
	
	/**
	 * 构造DATA站点管理池
	 */
	private DataOnHomePool() {
		super(SiteTag.DATA_SITE);
		// DATA节点的默认最大注册数目
		setMaxMembers(2);
	}

	/**
	 * 返回DATA站点管理池句柄
	 * @return DataOnHomePool实例
	 */
	public static DataOnHomePool getInstance() {
		return DataOnHomePool.selfHandle;
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
		mapPhases.clear();
		mapSpaces.clear();
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

	/**
	 * 输出全部节点地址
	 * @return 节点地址列表
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
	 * 根据DATA站点地址，查找站点配置
	 * @param node DATA站点地址
	 * @return 成功返回站点句柄，否则空值
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
	protected boolean infuse(Site site) {
		// 判断许可证超时
		if (isLicenceTimeout()) {
			Logger.error(this, "infuse", "licence timeout!");
			return false;
		}
		
		DataSite data = (DataSite) site;
		Node node = data.getNode();
		
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
			success = (mapSites.put(node, data) == null);
			data.refreshTime();
		}
		// 3. 保存表名
		if (success) {
			Logger.debug(this, "infuse", "%s space size %d", site, data.getSpaces().size());
			
			for (Space space : data.getSpaces()) {
				NodeSet set = mapSpaces.get(space);
				if (set == null) {
					set = new NodeSet();
					mapSpaces.put(space, set);
				}
				set.add(node);
			}
		}
		// 4. 保存用户签名
		if (success) {
			for (Siger siger : data.getUsers()) {
				NodeSet set = mapUsers.get(siger);
				if (set == null) {
					set = new NodeSet();
					mapUsers.put(siger, set);
				}
				set.add(node);
			}
		}
		// 5.保存阶段命名
		if (success) {
			for (Phase phase : data.getPhases()) {
				NodeSet set = mapPhases.get(phase);
				if (set == null) {
					set = new NodeSet();
					mapPhases.put(phase, set);
				}
				set.add(node);
			}
		}
		// 分到主从站点保存
		if (success) {
			if (data.isMaster()) {
				masters.add(node);
			} else {
				slaves.add(node);
			}
		}

		Logger.debug(this, "infuse", success, "from %s", site);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#transmit(com.laxcus.site.Site)
	 */
	@Override
	protected void transmit(Site site) {
		super.pushSite(site);
		
		// 从CALL站点管理池选择与这个DATA站点匹配的账号，通知它发送“PushDataField”命令到指定的CALL站点
		DataSite data = (DataSite) site;
		CallOnHomePool.getInstance().enroll(data.getNode(), data.getUsers());

//		CallOnHomePool.getInstance().enroll(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#dismiss(com.laxcus.site.Site)
	 */
	@Override
	protected void dismiss(Site site) {
		// 删除注册站点
		dropSite(site);
		// 通知CALL站点，撤销关联的DATA站点
		CallOnHomePool.getInstance().revoke(site);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#destroy(com.laxcus.site.Site)
	 */
	@Override
	protected void destroy(Site site) {
		// 通知HOME监视器，撤销记录
		destroySite(site);
		// 通知CALL站点，撤销关联DATA站点
		CallOnHomePool.getInstance().revoke(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#effuse(com.laxcus.site.Node)
	 */
	@Override
	protected Site effuse(Node node) {
		DataSite site = mapSites.remove(node);
		boolean success = (site != null);
		// 删除阶段命名
		if (success) {
			for (Phase phase : site.getPhases()) {
				NodeSet set = mapPhases.get(phase);
				if (set != null) {
					set.remove(node);
				}
				if (set == null || set.isEmpty()) {
					mapPhases.remove(phase);
				}
			}
		}
		// 删除用户签名
		if (success) {
			for (Siger siger : site.getUsers()) {
				NodeSet set = mapUsers.get(siger);
				if (set != null) {
					set.remove(node);
				}
				if (set == null || set.isEmpty()) {
					mapUsers.remove(siger);
				}
			}
		}
		// 删除表名
		if (success) {
			for (Space space : site.getSpaces()) {
				NodeSet set = mapSpaces.get(space);
				if (set != null) {
					set.remove(node);
				}
				if (set == null || set.isEmpty()) {
					mapSpaces.remove(space);
				}
			}
		}
		
		// 删除主从站点
		if (success) {
			if (site.isMaster()) {
				masters.remove(node);
			} else {
				slaves.remove(node);
			}
		}

		Logger.note(this, "effuse", success, "from %s", node);

		return site;
	}

	/**
	 * 根据数据表名，查找匹配的站点地址
	 * @param space 数据表名
	 * @return 节点集合列表
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
	 * 根据数据表名，查找全部主站点
	 * @param space 数据表名
	 * @return 节点集合
	 */
	public List<Node> findPrimeSites(Space space) {
		ArrayList<Node> array = new ArrayList<Node>();
		super.lockMulti();
		try {
			NodeSet set = mapSpaces.get(space);
			boolean success = (set != null);
			if (success) {
				for (Node node : set.list()) {
					DataSite site = mapSites.get(node);
					if (site.isMaster()) {
						array.add(node);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "findPrimeSite", "'%s' size is %d", space, array.size());
		return array;
	}
	
	/**
	 * 根据用户签名，查找关联的DATA主站点
	 * @param siger 用户签名
	 * @return 返回全部关联的DATA主站点
	 */
	public List<Node> findPrimeSites(Siger siger) {
		ArrayList<Node> array = new ArrayList<Node>();
		super.lockMulti();
		try {
			NodeSet set = mapUsers.get(siger);
			if (set != null) {
				for (Node node : set.list()) {
					DataSite site = mapSites.get(node);
					if (site.isMaster()) {
						array.add(node);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "findPrimeSites", "'%s' size is %d", siger, array.size());

		return array;
	}

	/**
	 * 根据数据表名找到“从节点”
	 * @param space 数据表名
	 * @return 节点集合
	 */
	public List<Node> findSlaveSites(Space space) {
		ArrayList<Node> array = new ArrayList<Node>();
		super.lockMulti();
		try {
			NodeSet set = mapSpaces.get(space);
			boolean success = (set != null);
			if (success) {
				for (Node node : set.list()) {
					DataSite site = mapSites.get(node);
					if (site.isSlave()) {
						array.add(node);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "findSlaveSite", "'%s' size is %d", space, array.size());
		return array;
	}

	/**
	 * 根据用户签名，查找关联的DATA从站点
	 * @param siger 用户签名
	 * @return 返回全部关联的DATA从站点
	 */
	public List<Node> findSlaveSites(Siger siger) {
		ArrayList<Node> array = new ArrayList<Node>();
		super.lockMulti();
		try {
			NodeSet set = mapUsers.get(siger);
			if (set != null) {
				for (Node node : set.list()) {
					DataSite site = mapSites.get(node);
					if (site.isSlave()) {
						array.add(node);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "findSlaveSites", "'%s' size is %d", siger, array.size());

		return array;
	}
	/**
	 * 根据阶段命名，查找注册地址集合
	 * @param phase 阶段命名
	 * @return 节点集合列表
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
	 * 根据用户签名，查找它的注册地址集合
	 * @param siger 注册用户签名
	 * @return NodeSet
	 */
	public NodeSet findSites(Siger siger) {
		Laxkit.nullabled(siger);
		super.lockMulti();
		try {
			return mapUsers.get(siger);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 查找某个级别的注册站点地址
	 * @param rank 站点级别
	 * @return 站点地址集
	 */
	public List<Node> findRankSites(byte rank) {
		ArrayList<Node> array = new ArrayList<Node>();
		super.lockMulti();
		try {
			Iterator<DataSite> iterator = mapSites.values().iterator();
			while (iterator.hasNext()) {
				DataSite site = iterator.next();
				if (site.getRank() == rank) {
					array.add(site.getNode());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "findRankSites", "size is %d", array.size());
		return array;
	}
	
	/**
	 * 返回主站点地址
	 * @return 站点地址集
	 */
	public NodeSet getMasters() {
		super.lockMulti();
		try {
			return masters;
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 返回从站点地址
	 * @return 站点地址集
	 */
	public NodeSet getSlaves() {
		super.lockMulti();
		try {
			return slaves;
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 查找全部主站点
	 * @return 站点地址集合
	 */
	public List<Node> findPrimeSites() {
		return findRankSites(RankTag.MASTER);
	}

	/**
	 * 查找全部从站点
	 * @return 站点地址集合
	 */
	public List<Node> findSlaveSites() {
		return findRankSites(RankTag.SLAVE);
	}

//	/**
//	 * 根据数据表名，产生一批备份站点地址
//	 * @param space 数据表名
//	 * @return
//	 */
//	public List<Node> createReflexCacheSite(Space space) {
//		ArrayList<Node> array = new ArrayList<Node>();
//
//		Table table = StaffOnHomePool.getInstance().findTable(space);
//		if (table == null) {
//			return array;
//		}
//
//		int left = table.getChunkCopy() - 1;
//		if (left < 1) {
//			return array;
//		}
//		
//		// 找到匹配的从站点。考虑剩余内存空间，数据块的数量...
//		super.lockMulti();
//		try {
//			NodeSet set = mapSpaces.get(space);
//			if (set != null) {
//				for (Node node : set.list()) {
//					DataSite site = mapSites.get(node);
//					if (site.isSlave()) {
//						array.add(node);
//						if(array.size() == left) break; // 达到要求，退出
//					}
//				}
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockMulti();
//		}
//
//		Logger.debug(this, "createReflexCacheSite", "space size %d, site size %d, result size is %d",
//				mapSpaces.size(), mapSites.size(), array.size());
//
//		return array;
//	}
	
}