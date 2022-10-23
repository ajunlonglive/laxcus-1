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
import com.laxcus.site.call.*;
import com.laxcus.site.data.*;
import com.laxcus.site.work.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * CALL站点管理池。<br>
 * 
 * CALL站点是网关站点，跨HOME集群运行，会注册到不同的HOME站点下面。
 * 
 * @author scott.liang
 * @version 1.2 11/05/2014
 * @since laxcus 1.0
 */
public final class CallOnHomePool extends HomePool {

	/** CALL站点管理池句柄 **/
	private static CallOnHomePool selfHandle = new CallOnHomePool();

	/** CALL站点地址 -> CALL站点元数据 **/
	private Map<Node, CallSite> mapSites = new TreeMap<Node, CallSite>();

	/** 用户签名 -> CALL站点集合 **/
	private Map<Siger, NodeSet> mapUsers = new TreeMap<Siger, NodeSet>();

	/** 数据表名 -> CALL站点集合 */
	private Map<Space, NodeSet> mapSpaces = new TreeMap<Space, NodeSet>();

	/** 命名阶段 -> CALL站点集合 */
	private Map<Phase, NodeSet> mapPhases = new TreeMap<Phase, NodeSet>();

	/**
	 * 构造CALL站点管理池
	 */
	private CallOnHomePool() {
		super(SiteTag.CALL_SITE);
		// CALL节点的默认最大注册数目
		setMaxMembers(2);
	}

	/**
	 * 返回CALL站点管理池句柄
	 * @return CallOnHomePool实例
	 */
	public static CallOnHomePool getInstance() {
		return CallOnHomePool.selfHandle;
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
	 * @return Space列表
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
	 * 查找与数据库关联的站点
	 * @param fame 数据库名
	 * @return 返回关联站点列表
	 */
	public List<Node> findSites(Fame fame) {
		TreeSet<Node> nodes = new TreeSet<Node>();
		// 锁定
		super.lockMulti();
		try {
			Iterator<Map.Entry<Space, NodeSet>> iterator = this.mapSpaces.entrySet().iterator();
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
	 * 根据注册用户名，查找CALL站点集合
	 * @param siger 注册用户名称
	 * @return 返回对应的站点集合，或者空指针 
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
	 * 判断CALL站点已经分配了某个用户的存储空间
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean contains(Siger siger) {
		NodeSet set = findSites(siger);
		return set != null && set.size() > 0;
	}

	/**
	 * 根据数据表名，查找CALL站点集合
	 * @param space 数据表名
	 * @return 节点地址集合
	 */
	public NodeSet findSites(Space space) {
		Laxkit.nullabled(space);
		super.lockMulti();
		try {
			return mapSpaces.get(space);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据阶段命名，查找到阶段命名集合
	 * @param phase 阶段命名
	 * @return 节点地址集合
	 */
	public NodeSet findSites(Phase phase) {
		Laxkit.nullabled(phase);
		super.lockMulti();
		try {
			return mapPhases.get(phase);
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 查找某个站点的用户签名
	 * @param node 节点地址
	 * @return 返回签名列表，没有是空指针
	 */
	public List<Siger> findUsers(Node node) {
		Laxkit.nullabled(node);

		super.lockMulti();
		try {
			CallSite site = this.mapSites.get(node);
			if (site != null) {
				return site.getUsers();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 根据阶段命名和数据表名，查找相交的CALL站点集合
	 * @param phase 阶段命名
	 * @param space 数据表名
	 * @return 节点地址集合
	 */
	public NodeSet findAnchor(Phase phase, Space space) {
		TreeSet<Node> nodes = new TreeSet<Node>();
		this.lockMulti();
		try {
			// 检查阶段命名
			NodeSet set = mapPhases.get(phase);
			if (set != null) {
				nodes.addAll(set.list());
			}
			// 检查表名称
			set = mapSpaces.get(space);
			if (set == null) {
				nodes.clear(); // 清除
			} else {
				nodes.retainAll(set.list()); // “与”操作，保留相同的地址
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			this.unlockMulti();
		}

		return new NodeSet(nodes);
	}

	/**
	 * 将取消操作转发给一批CALL站点
	 * @param set CALL站点集合
	 * @param cmd 删除命令
	 */
	private void revoke(Set<Node> set, DropField cmd) {
		if (set.isEmpty()) {
			return;
		}
		ShiftDropField shift = new ShiftDropField(set, cmd);
		HomeCommandPool.getInstance().admit(shift);
	}

	/**
	 * 取消一个BUILD站点
	 * @param site
	 */
	private void revoke(BuildSite site) {
		List<Phase> phases = site.getPhases();
		TreeSet<Node> nodes = new TreeSet<Node>();
		// 根据用户签名，找到关联的CALL站点并且保存它
		for (Phase phase : phases) {
			NodeSet set = findSites(phase.getIssuer());
			if (set != null) {
				nodes.addAll(set.show()); // 保存
			}
		}

		DropBuildField cmd = new DropBuildField(site.getNode());
		this.revoke(nodes, cmd);
	}

	/**
	 * 通知CALL站点撤销某一个站点记录
	 * @param site
	 */
	public void revoke(Site site) {
		if (site.getClass() == DataSite.class) {
			revoke((DataSite) site);
		} else if (site.getClass() == WorkSite.class) {
			revoke((WorkSite) site);
		} else if (site.getClass() == BuildSite.class) {
			revoke((BuildSite) site);
		}
	}

	/**
	 * 通知相关的CALL站点，取消一个WORK站点的记录
	 * @param site
	 */
	private void revoke(WorkSite site) {
		List<Phase> phases = site.getPhases();
		TreeSet<Node> nodes = new TreeSet<Node>();
		for (Phase phase : phases) {
			NodeSet set = findSites(phase.getIssuer());
			if (set != null) {
				nodes.addAll(set.show());
			}
		}
		DropWorkField cmd = new DropWorkField(site.getNode());
		this.revoke(nodes, cmd);
	}

	/**
	 * 通知相关的CALL站点，取消一个DATA站点记录
	 * @param site
	 */
	private void revoke(DataSite site) {
		TreeSet<Node> nodes = new TreeSet<Node>();
		// 根据数据表名查找
		for (Space space : site.getSpaces()) {
			NodeSet set = findSites(space);
			if (set != null) {
				nodes.addAll(set.show());
			}
		}
		// 根据阶段命名查找
		for (Phase phase : site.getPhases()) {
			NodeSet set = findSites(phase.getIssuer());
			if (set != null) {
				nodes.addAll(set.show());
			}
		}
		// 生成删除命令
		DropDataField cmd = new DropDataField(site.getNode());
		revoke(nodes, cmd);
	}

	/**
	 * DATA/WORK/BUILD站点注册后，CALL站点检查匹配账号，要求它们提交“PushXXXField”命令到指定的CALL站点
	 * @param source 命令源地址（DATA/WORK/BUILD站点地址）
	 * @param users 注册账号
	 */
	public void enroll(Node source, List<Siger> users) {
		// 空集合退出
		if (users.isEmpty()) {
			return;
		}
		
		// 取出已经登记在CALL站点上的用户签名
		TreeSet<Siger> sigers = new TreeSet<Siger>();
		sigers.addAll(users);
		for (Siger siger : users) {
			NodeSet set = findSites(siger);
			if (set == null) {
				continue;
			}
			for (Node call : set.show()) {
				List<Siger> a = findUsers(call);
				if (a != null) sigers.addAll(a);
			}
		}
		
		// 根据账号签名，选择匹配的CALL站点. 
		// CALL站点地址 -> 投递命令 
		Map<Node, SelectFieldToCall> sites = new TreeMap<Node, SelectFieldToCall>();
		for (Siger siger : sigers) {
			NodeSet set = findSites(siger);
			if (set == null) {
				continue;
			}
			for (Node call : set.show()) {
				SelectFieldToCall cmd = sites.get(call);
				if (cmd == null) {
					cmd = new SelectFieldToCall(call);
					sites.put(call, cmd);
				}
				cmd.add(siger);
			}
		}
		// 空集合退出
		int size = sites.size();
		if (size == 0) {
			return;
		}
		
		Logger.debug(this, "enroll", "command size:%d", sites.size());

		// 建立转发命令和提交给HOME命令管理池
		ShiftSelectFieldToCall shift = new ShiftSelectFieldToCall(source, sites.values());
		// 交给HOME命令管理池
		HomeCommandPool.getInstance().admit(shift);
	}

	/**
	 * 输出当前保存的全部站点地址
	 * @return
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
	 * 根据站点地址查找配置
	 * @param node CALL站点地址
	 * @return CALL站点配置
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
		
		CallSite call = (CallSite) site;
		Node node = call.getNode();
		
		// 1. 没有注册
		boolean success = (mapSites.get(node) == null);
		// 如果节点不存在，判断最大数目；若存在，忽略它！
		if (success) {
			if (isMaxMembers(mapSites.size())) {
				Logger.error(this, "infuse", "member out! %d >= %d",mapSites.size(), getMaxMembers());
				return false;
			}
		}

		// 2. 保存和刷新时间
		if (success) {
			success = (mapSites.put(node, call) == null);
			call.refreshTime();
		}
		// 3. 保存阶段命名
		if (success) {
			for (Phase phase : call.getPhases()) {
				Logger.debug(this, "infuse", "phase is: %s", phase);
				
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
			for (Space space : call.getSpaces()) {
				Logger.debug(this, "infuse", "table is: %s", space);
				
				NodeSet set = mapSpaces.get(space);
				if (set == null) {
					set = new NodeSet();
					mapSpaces.put(space, set);
				}
				set.add(node);
			}
		}
		// 5. 保存注册账号
		if (success) {
			for (Siger siger : call.getUsers()) {
				NodeSet set = mapUsers.get(siger);
				if (set == null) {
					set = new NodeSet();
					mapUsers.put(siger, set);
				}
				set.add(node);
			}
		}

		Logger.debug(this, "infuse", success, "from %s", call);
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#effuse(com.laxcus.site.Node)
	 */
	@Override
	protected Site effuse(Node node) {
		// 1. 站点存在
		CallSite site = mapSites.remove(node);
		boolean success = (site != null);
		// 2. 删除阶段命名
		if (success) {
			for (Phase phase : site.getPhases()) {
				NodeSet set = mapPhases.get(phase);
				if (set != null) {
					set.remove(node);
					if (set.isEmpty()) mapPhases.remove(phase);
				}
			}
		}
		// 3. 删除数据表名
		if (success) {
			for (Space space : site.getSpaces()) {
				NodeSet set = mapSpaces.get(space);
				if (set != null) {
					set.remove(node);
					if (set.isEmpty()) mapSpaces.remove(space);
				}
			}
		}
		// 4. 删除注册用户
		if (success) {
			for (Siger siger : site.getUsers()) {
				NodeSet set = mapUsers.get(siger);
				if (set != null) {
					set.remove(node);
					if (set.isEmpty()) mapUsers.remove(siger);
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
		// 通知HOME监视站点和WATCH站点，一个CALL站点加入
		super.pushSite(site);
		// 分析CALL站点参数，DATA/WORK/BUILD传递命令给CALL站点
		detect((CallSite)site);
	}

	/**
	 * 分析并且发送命令到关联的DATA/WORK/BUILD站点
	 * @param site
	 */
	private void detect(CallSite site) {
		Node call = site.getNode();
		List<Siger> users = site.getUsers();

		// DATA/WORK/BUILD地址 -> 投递命令
		Map<Node, SelectFieldToCall> sites = new TreeMap<Node, SelectFieldToCall>();

		// 选择DATA站点管理池，与CALL站点账号匹配的站点
		for (Siger siger : users) {
			NodeSet set = DataOnHomePool.getInstance().findSites(siger);
			if(set == null) {
				continue;
			}
			for(Node data : set.show()) {
				SelectFieldToCall cmd = sites.get(data);
				if(cmd == null) {
					cmd = new SelectFieldToCall(call);
					sites.put(data, cmd);
				}
				cmd.add(siger);
			}
		}
		// 选择WORK站点管理池中，与CALL站点账号匹配的站点
		for (Siger siger : users) {
			NodeSet set = WorkOnHomePool.getInstance().findSites(siger);
			if(set == null) {
				continue;
			}
			for(Node work : set.show()) {
				SelectFieldToCall cmd = sites.get(work);
				if(cmd == null) {
					cmd = new SelectFieldToCall(call);
					sites.put(work, cmd);
				}
				cmd.add(siger);
			}
		}
		// 选择BUILD站点管理池中，与CALL站点账号匹配的站点
		for (Siger siger : users) {
			NodeSet set = BuildOnHomePool.getInstance().findSites(siger);
			if(set == null) {
				continue;
			}
			for(Node build : set.show()) {
				SelectFieldToCall cmd = sites.get(build);
				if(cmd == null) {
					cmd = new SelectFieldToCall(call);
					sites.put(build, cmd);
				}
				cmd.add(siger);
			}
		}

		Logger.debug(this, "detect", "command size:%d", sites.size());

		// 生成转发命令，提交给HOME命令管理池
		Iterator<Map.Entry<Node, SelectFieldToCall>> iterator = sites.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, SelectFieldToCall> entry = iterator.next();
			ShiftSelectFieldToCall shift = new ShiftSelectFieldToCall(entry.getKey(), entry.getValue());
			HomeCommandPool.getInstance().admit(shift);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#dismiss(com.laxcus.site.Site)
	 */
	@Override
	protected void dismiss(Site site) {
		// 通知HOME监视器和WATCH站点，一个CALL站点正常退出
		super.dropSite(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#destroy(com.laxcus.site.Site)
	 */
	@Override
	protected void destroy(Site site) {
		// 通知HOME监视器和WATCH站点，以故障状态删除一个CALL站点
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
		mapUsers.clear();
		mapSpaces.clear();
		mapPhases.clear();
	}

}