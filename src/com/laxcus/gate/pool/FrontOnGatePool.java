/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.gate.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.set.*;

/**
 * GATE站点上的FRONT站点管理池。<br>
 * 
 * 在GATE站点上，允许一个账号有多个FRONT站点注册。
 * GATE站点负责FRONT站点的事务控制。
 * 
 * @author scott.liang
 * @version 1.1 12/23/2013
 * @since laxcus 1.0
 */
public final class FrontOnGatePool extends HubPool {

	/** 前端站点管理池 **/
	private static FrontOnGatePool selfHandle = new FrontOnGatePool();
	
	/** 账号用户名 -> FRONT站点集合**/
	private Map<Siger, NodeSet> mapUsers = new TreeMap<Siger, NodeSet>();

	/** 站点地址 -> 站点配置 **/
	private Map<Node, FrontSite> mapSites = new TreeMap<Node, FrontSite>();

	/** FRONT散列码 -> FRONT站点 **/
	private Map<ClassCode, FrontSite> mapHashs = new TreeMap<ClassCode, FrontSite>();

	/**
	 * 构造前端站点管理池
	 */
	private FrontOnGatePool() {
		super(SiteTag.FRONT_SITE);
	}

	/**
	 * 返回前端站点管理池静态句柄
	 * @return
	 */
	public static FrontOnGatePool getInstance() {
		return FrontOnGatePool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 启动
		loadFrontChecker();
		// 返回
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapHashs.clear();
		mapSites.clear();
		mapUsers.clear();
	}
	
	/**
	 * 返回注册成员数目
	 * @return 数字
	 */
	public int getMembers() {
		super.lockMulti();
		try {
			return mapSites.size();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 启动用户资源检测器，定时检测节点上的用户数目
	 */
	private void loadFrontChecker() {
		GateLauncher launcher = (GateLauncher) getLauncher();
		FrontCyber cyber = launcher.getFrontCyber();
		Timer timer = getLauncher().getTimer();
		FrontChecker checker = new FrontChecker(this);
		timer.schedule(checker, 0, cyber.getTimeout());
	}
	
//	private void print() {
//		Logger.debug(this, "print", "front user count %d", mapUsers.size());
//		for (Siger e : mapUsers.keySet()) {
//			Logger.debug(this, "print", "siger is %s", e);
//		}
//		Logger.debug(this, "print", "front site count %d", mapSites.size());
//		for (Node e : mapSites.keySet()) {
//			Logger.debug(this, "print", "node is %s", e);
//		}
//	}

	/**
	 * 检查FRONT在线用户数，发出报告
	 */
	protected void checkFronts() {
		//		print();

		GateLauncher launcher = (GateLauncher) getLauncher();
		FrontCyber cyber = launcher.getFrontCyber();

		// 判断用户数满员/虚拟空间不足
		int members = mapSites.size();
		if (cyber.isFull(members)) {
			FrontFull cmd = new FrontFull(cyber.getPersons(), members);
			getCommandPool().admit(cmd);
		} else if (cyber.isMissing(members)) {
			FrontMissing cmd = new FrontMissing(cyber.getPersons(), members);
			getCommandPool().admit(cmd);
		}
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
		// FRONT站点参数
		FrontSite front = (FrontSite) site;
		Node node = front.getNode();
		User user = front.getUser();
		Siger siger = user.getUsername();

		// 允许在线成员数目，管理员在线登录数目由配置中定义，普通注册用户的在线数由管理员通过“SetMaxMembers”命令设置。
		int members = 1;

		// 1. 判断地址不存在
		boolean success = (mapSites.get(node) == null);
		if (!success) {
			Logger.error(this, "infuse", "duplicate site: %s", node);
			return false;
		}
		// 2. 判断是管理员或者普通注册用户，且有效
		if (StaffOnGatePool.getInstance().isAdminstrator(user)) {
			// 系统管理员的在线登录数目
			members = StaffOnGatePool.getInstance().getAdministrator().getMembers();
		} else {
			User real = StaffOnGatePool.getInstance().find(siger);
			success = (real != null && real.equals(user));
			if (!success) {
				Logger.error(this, "infuse", "illegal '%s'", user);
				return false;
			}
			members = real.getMembers();
		}
		// 3. 达到最大用户数，不允许注册
		NodeSet set = mapUsers.get(siger);
		success = !(set != null && set.size() >= members);
		if (!success) {
			Logger.warning(this, "infuse", "too large '%s', max members:%d", user, members);
			return false;
		}
		if (set == null) {
			set = new NodeSet();
			mapUsers.put(siger, set);
		}
		// 保存地址到站点集合
		set.add(node);
		// 注册登录用户
		mapSites.put(node, front);
		mapHashs.put(front.getHash(), front);
		// 刷新时间
		front.refreshTime();

		Logger.debug(this, "infuse", success, "login %s", node);

		return success;
	}
	
	/**
	 * 删除用户账号
	 * @param siger
	 * @param front
	 */
	private void dropUser(Siger siger, Node front) {
		ShiftDropOnlineMember shift = new ShiftDropOnlineMember(siger, front);
		GateCommandPool.getInstance().admit(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#effuse(com.laxcus.site.Node)
	 */
	@Override
	protected Site effuse(Node node) {
		// 删除注册地址
		FrontSite site = mapSites.remove(node);
		boolean success = (site != null);
		// 删除账号
		if (success) {
			Siger siger = site.getUsername();
			NodeSet set = mapUsers.get(siger);
			if (set != null) {
				set.remove(node);
				if (set.isEmpty()) mapUsers.remove(siger);
			}
			// 清除散列码
			mapHashs.remove(site.getHash());
			
			// 通知WATCH节点，这个用户账号需要删除
			dropUser(siger, node);
		}

		Logger.note(this, "effuse", success, "logout %s", node);

		return site;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#transmit(com.laxcus.site.Site)
	 */
	@Override
	protected void transmit(Site site) {

	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#dismiss(com.laxcus.site.Site)
	 */
	@Override
	protected void dismiss(Site site) {

	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#destroy(com.laxcus.site.Site)
	 */
	@Override
	protected void destroy(Site site) {

	}

	/**
	 * 删除某个账号下的全部节点
	 * @param siger 注册账号用户名称
	 * @return 删除成功返回“真”，否则“假”。
	 * @throws NullPointerException - 如果用户名称是空指针
	 */
	public boolean drop(Siger siger) {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			if (siger != null) {
				NodeSet set = mapUsers.remove(siger);
				success = (set != null);
				if (success) {
					for (Node node : set.list()) {
						FrontSite site = mapSites.remove(node);
						// 清除散列码
						if (site != null) {
							mapHashs.remove(site.getHash());
						}
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "drop", success, "result is");
		return success;
	}

	/**
	 * 判断用户存在。登录返回真，否则假。
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean contains(Siger siger) {
		boolean success = false;
		super.lockMulti();
		try {
			if (siger != null) {
				success = (mapUsers.get(siger) != null);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 判断用户和地址存在
	 * @param siger 用户签名
	 * @param node 来源地址
	 * @return 返回真或者假
	 */
	public boolean contains(Siger siger, Node node) {
		boolean success = false;
		super.lockMulti();
		try {
			if (siger != null && node != null) {
				NodeSet set = mapUsers.get(siger);
				if (set != null) {
					success = set.contains(node);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 查找关联的FRONT站点
	 * @param siger 用户签名
	 * @return FRONT站点地址
	 */
	public List<Node> findFronts(Siger siger) {
		super.lockMulti();
		try {
			if (siger != null) {
				NodeSet set = mapUsers.get(siger);
				if (set != null && set.size() > 0) {
					return set.list();
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 输出全部签名
	 * @return Siger列表
	 */
	public List<Siger> getSigers() {
		ArrayList<Siger> a = new ArrayList<Siger>();
		super.lockMulti();
		try {
			a.addAll(mapUsers.keySet());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return a;
	}

	/**
	 * 根据签名，判断在线注册用户达到最大数目。
	 * @param siger 用户签名
	 * @return 达到最大数目返回真，否则是假
	 */
	public boolean isMaxout(Siger siger) {
		// 找到注册账号
		User user = StaffOnGatePool.getInstance().find(siger);
		// 如果账号不存在，返回真。默认是达到最大数目
		if (user == null) {
			Logger.warning(this, "isMaxout", "cannot be find '%s'", siger);
			return false;
		}
		// 最大用户数目
		int members = user.getMembers();

		// 3. 找到注册用户，判断达到最大数目
		boolean success = false;
		super.lockMulti();
		try {
			NodeSet set = mapUsers.get(siger);
			success = (set != null && set.size() >= members);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		if (success) {
			Logger.warning(this, "isMaxout", "too large '%s', max members:%d", siger, members);
		}
		return success;
	}

	/**
	 * 根据签名，查找同时在线的登录节点数目
	 * @param siger 用户签名
	 * @return 返回登录节点数目
	 */
	public int findMembers(Siger siger) {
		int members = 0;
		super.lockMulti();
		try {
			NodeSet set = mapUsers.get(siger);
			if (set != null) {
				members = set.size();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return members;
	}


	/**
	 * 根据FRONT哈希码，删除一个节点。<br>
	 * 
	 * FRONT哈希码，基于MAC地址和所属类名生成，是FRONT节点的唯一值。
	 * 这要求每台计算机，只能有一个FRONT节点登录。
	 * 
	 * @param hash
	 * @return 返回被删除的节点地址
	 */
	public Node remove(ClassCode hash) {
		// 如果是空值，忽略！
		if (hash == null) {
			Logger.error(this, "remove", "FrontHash is null pointer!");
			return null;
		}
		
		Node node = null;
	
		// 以锁定方式进行删除操作
		super.lockSingle();
		try {
			FrontSite site = mapHashs.remove(hash);
			if (site != null) {
				node = site.getNode();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		// 判断有效，删除其它值
		if (node != null) {
			remove(node);
		}
		
		Logger.debug(this, "remove", node != null, "drop %s # %s", hash, node);

		// 返回FRONT运行节点
		return node;
	}
	
}