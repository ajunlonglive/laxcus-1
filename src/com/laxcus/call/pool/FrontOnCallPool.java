/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.call.*;
import com.laxcus.command.halt.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.set.*;

/**
 * CALL站点上的FRONT站点管理池。<br>
 * 
 * 只有注册的FRONT站点，CALL站点才能接受它的数据处理操作。
 * CALL站点接受注册的条件必须是账号匹配
 * 
 * @author scott.liang
 * @version 1.1 9/26/2015
 * @since laxcus 1.0
 */
public final class FrontOnCallPool extends HubPool {

	/** FRONT站点管理池 **/
	private static FrontOnCallPool selfHandle = new FrontOnCallPool();

	/** 注册用户名 -> 站点集合 **/
	private Map<Siger, NodeSet> mapUsers = new TreeMap<Siger, NodeSet>();

	/** 节点地址 -> FRONT节点 **/
	private Map<Node, FrontSite> mapSites = new TreeMap<Node, FrontSite>();

	/** FRONT散列码 -> FRONT节点 **/
	private Map<ClassCode, FrontSite> mapHashs = new TreeMap<ClassCode, FrontSite>();

	/**
	 * 构造FRONT站点管理池
	 */
	private FrontOnCallPool() {
		super(SiteTag.FRONT_SITE);
		// 22秒超时
		super.setActiveTime(22);
	}

	/**
	 * 返回FRONT站点管理池静态句柄
	 * @return
	 */
	public static FrontOnCallPool getInstance() {
		return FrontOnCallPool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 加载
		loadFrontChecker();

		// 返回结果
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 通知FRONT站点退出
		exit();
		// 清除记录
		mapSites.clear();
	}

	/**
	 * 启动用户资源检测器，定时检测节点上的用户数目
	 */
	private void loadFrontChecker() {
		CallLauncher launcher = (CallLauncher) getLauncher();
		FrontCyber cyber = launcher.getFrontCyber();
		Timer timer = getLauncher().getTimer();
		FrontChecker checker = new FrontChecker(this);
		timer.schedule(checker, 0, cyber.getTimeout());
	}

	/**
	 * 检查FRONT在线用户数，发出报告
	 */
	protected void checkFronts() {
		CallLauncher launcher = (CallLauncher) getLauncher();
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
	 * 返回全部登录在CALL节点上的FRONT用户
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
	 * 判断账号存在
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean contains(Siger siger) {
		Laxkit.nullabled(siger);
		super.lockMulti();
		try {
			return (mapUsers.get(siger) != null);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 找到注册站点
	 * @param node
	 * @return
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
	 * 删除注册用户及全部登录节点记录
	 * @param siger 用户签名
	 * @return 返回删除的登录节点数目
	 */
	public int remove(Siger siger) {
		ArrayList<Node> a = new ArrayList<Node>();
		super.lockMulti();
		try {
			NodeSet set = mapUsers.get(siger);
			if (set != null) {
				a.addAll(set.list());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		// 删除地址
		int count = 0;
		for (Node node : a) {
			boolean success = remove(node);
			if (success) count++;
		}

		Logger.debug(this, "remove", "remove %s count %d", siger, count);

		return count;
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
		FrontSite front = (FrontSite) site;
		Node node = front.getNode();
		User user = front.getUser();
		Siger username = user.getUsername();

		// 1. 没有注册
		boolean success = (mapSites.get(node) == null);
		// 2.检查账号存在，匹配用户名和密码
		if (success) {
			User that = StaffOnCallPool.getInstance().findUser(username);
			success = (user != null && user.compareTo(that) == 0);

			// 不成立，判断是来源是被授权人
			if (!success) {
				success = StaffOnCallPool.getInstance().isActiveConferrer(username);
			}
		}

		// 3. 记录注册用户
		if (success) {
			NodeSet set = mapUsers.get(username);
			if (set == null) {
				set = new NodeSet();
				mapUsers.put(username, set);
			}
			set.add(node);
		}
		// 3. 保存注册配置
		if (success) {
			// 保存参数
			mapSites.put(node, front);
			mapHashs.put(front.getHash(), front);
			// 刷新时间
			front.refreshTime();
		}

		Logger.debug(this, "infuse", success, "from %s", node);
		return success;
	}
	
	/**
	 * 删除用户账号
	 * @param siger
	 * @param front
	 */
	private void dropUser(Siger siger, Node front) {
		ShiftDropOnlineMember shift = new ShiftDropOnlineMember(siger, front);
		CallCommandPool.getInstance().admit(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#effuse(com.laxcus.site.Node)
	 */
	@Override
	protected Site effuse(Node node) {
		// 1. 删除注册地址
		FrontSite site = mapSites.remove(node);
		boolean success = (site != null);
		// 2.删除账号
		if (success) {
			Siger username = site.getUsername();
			NodeSet set = mapUsers.get(username);
			if (set != null) {
				set.remove(node);
				if (set.isEmpty()) mapUsers.remove(username);
			}
			// 清除哈希码
			mapHashs.remove(site.getHash());
			
			// 通过HOME节点，通知WATCH节点删除用户账号
			dropUser(username, node);
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
	 * CALL站点通知下属全部FRONT站点退出
	 */
	private void exit() {
		Logger.debug(this, "exit", "notify all front");

		ArrayList<Node> array = new ArrayList<Node>(mapSites.keySet());

		Halt cmd = new Halt();
		ShiftHalt shift = new ShiftHalt(array, cmd);
		CallCommandPool.getInstance().press(shift);
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