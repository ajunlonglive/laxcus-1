/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.pool;

import java.util.*;

import com.laxcus.fixp.*;
import com.laxcus.fixp.monitor.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.util.set.*;

/**
 * 控制中心管理池。<br><br>
 * 
 * 控制中心管理池是一个站点管理它的下属注册站点的管理池。<br><br>
 * 
 * 控制中心管理池工作内容：<br>
 * 1. 接受下属站点的注册。<br>
 * 2. 接受下属站点的注销。<br>
 * 3. 监视下属站点的定时刷新（下属站点的定时更新时间是20秒）。<br>
 * 4. 向其它站点分发注册/注销站点的信息。<br>
 * 
 * @author scott.liang
 * @version 1.6 05/13/2015
 * @since laxcus 1.0
 */
public abstract class HubPool extends SitePool {
	
	/** 最大注册成员数，默认是0，除非管理池设置它们！ **/
	private int maxMembers;

	/** 注册站点定时激活时间。单位：毫秒 **/
	private long activeTime;

	/** 注册站点超时删除时间。单位：毫秒 **/
	private long deleteTime;

	/** 注册站点地址集合 **/
	private NodeSet sites = new NodeSet();
	
	/**
	 * 构造控制中心管理池，指定控制中心管理池的站点类型
	 * @param siteFamily 站点类型
	 */
	protected HubPool(byte siteFamily) {
		super(siteFamily);
		// 默认限制是0，有需求子类在启动时设置它！
		maxMembers = 0;
		// 时间参数
		setActiveTime(20);
		setDeleteTime(60);
	}

	/**
	 * 设置最大成员数，小于0表示无限制！
	 * @param what 数目
	 */
	public void setMaxMembers(int what) {
		maxMembers = what;

		Logger.info(this, "setMaxMembers", "%s pool's sites is %d",
				SiteTag.translate(getFamily()), maxMembers);
	}

	/**
	 * 返回最大成员数
	 * @return 任意整数
	 */
	protected int getMaxMembers() {
		return maxMembers ;
	}
	
	/**
	 * 判断已经达到最大成员数。
	 * 这个方法由子类调用
	 * @return 返回真或者假
	 */
	protected boolean isMaxMembers(int size) {
		return size >= maxMembers;
	}

	/**
	 * 设置注册站点定时激活时间。单位：秒。<br><br>
	 * 
	 * 说明：<br>
	 * 1. 注册站点需要这个时间内发送心跳包，否则将被管理站点判断为超时。<br>
	 * 2. 超时后，管理站点将发出UDP召唤包，抵达删除时间后，被管理站点的资源将从内存中释放。<br>
	 * 
	 * @param second
	 */
	public void setActiveTime(int second) {
		setActiveTimeMillis(second * 1000L);
	}

	/**
	 * 设置注册站点定时激活时间。单位：毫秒。
	 * @param ms 毫秒
	 */
	public void setActiveTimeMillis(long ms) {
		if (ms >= 5000) {
			activeTime = ms;
		}
	}

	/**
	 * 注册站点激活超时时间。单位：秒。
	 * @return
	 */
	public int getActiveTime() {
		return (int) (activeTime / 1000);
	}

	/**
	 * 返回站点超时时间。单位：毫秒
	 * @return
	 */
	public long getActiveTimeMillis() {
		return activeTime;
	}

	/**
	 * 设置站点超时被删除时间。单位：秒。<br><br>
	 * 
	 * 说明：<br>
	 * 注册站点达到删除时间后，它的资源将从内存中释放。<br>
	 * 
	 * @param second
	 */
	public void setDeleteTime(int second) {
		setDeleteTimeMillis(second * 1000L);
	}

	/**
	 * 设置站点超时被删除时间。单位：毫秒
	 * @param ms
	 */
	public void setDeleteTimeMillis(long ms) {
		if (ms >= 5000) {
			deleteTime = ms;
		}
	}

	/**
	 * 返回站点超时被删除时间。单位：秒
	 * @return
	 */
	public int getDeleteTime() {
		return (int) (deleteTime / 1000L);
	}

	/**
	 * 返回站点超时删除时间。单位：毫秒
	 * @return
	 */
	public long getDeleteTimeMillis() {
		return deleteTime;
	}

	/**
	 * 管理站点通知下属站点，它的注册时间已经超时，必须马上发送心跳包。<br>
	 * @param endpoint 目标站点地址
	 * @param sends 发送次数
	 * @return 发送成功返回真（大于0），否则假。
	 */
	protected boolean sendTimeout(Node endpoint, int sends) {
		SocketHost address = endpoint.getPacketHost();

		Mark cmd = new Mark(Ask.NOTIFY, Ask.COMEBACK);
		Packet packet = new Packet(address, cmd);
		// 超时定义时间(毫秒)
		packet.addMessage(MessageKey.TIMEOUT, activeTime - 2);

		PacketMessenger messenger = VirtualPool.getLauncher().getPacketMessenger();

		// 通过FIXP UDP服务器发送数据包
		int count = 0;
		for (int index = 0; index < sends; index++) {
			boolean success = messenger.notice(packet);
			if (success) {
				count++;
			}
		}
		return count > 0;
	}

	/**
	 * 对注册的站点进行超时检查。<br><br>
	 * 
	 * 根据最近一次的注册激活时间进行超时判断，超时分两种情况：<br>
	 * 1. 发生超时，但是没有达到删除时限。<br>
	 * 2. 超时达到删除时限。<br>
	 * 第1种情况是向目标站点发出超时警告，第2种情况是删除站点。
	 */
	protected void check() {
		Map<Node, ? extends Site> sites = iterator();
		int size = sites.size();
		if (size == 0) {
			return;
		}

		// Set<Map.Entry<Node, extends ? Site>> a = sites.entrySet();

		ArrayList<Node> deletes = new ArrayList<Node>(size);
		ArrayList<Node> timeouts = new ArrayList<Node>(size);
		// 锁定操作
		super.lockSingle();
		try {
			Collection<? extends Site> values = sites.values();
			for (Site site : values) {
				if (site.isTimeout(deleteTime)) {
					deletes.add(site.getNode());
				} else if (site.isTimeout(activeTime)) {
					timeouts.add(site.getNode());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		//		// 测试！删除过期
		//		try {
		//			if (getLauncher().getFamily() == SiteTag.HOME_SITE) {
		////				deletes.add(new Node("work://192.168.1.103:6300_6300"));
		//				deletes.add(new Node("WORK://127.0.0.1:8333_8333"));
		//			}
		//		} catch (Exception e) {
		//			Logger.error(e);
		//		}

		// 删除达到最大超时限制的站点
		for (Node node : deletes) {
			Logger.error(this, "check", "delete %s", node);
			// 以“故障”方式销毁注册站点
			dispose(node);
			// 删除启动器上与站点关联的密文
			getLauncher().removeCipher(node);
		}
		// 通知超时站点，重新发出激活命令
		for (Node node : timeouts) {
			Logger.warning(this, "check", "callback %s", node);
			sendTimeout(node, 3);
		}
	}

	/**
	 * 统计注册站点数目
	 * @return 返回注册站点数目
	 */
	public int size() {
		super.lockMulti();
		try {
			return iterator().size();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 输出运行站点地址集合。调用端只可以做读取操作，且是锁定方式。如：next, show 方法等。
	 * @return NodeSet实例
	 */
	public NodeSet list() {
		super.lockMulti();
		try {
			return sites;
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 以顺序轮巡方式，选择下一个节点
	 * @return 返回节点地址，没有是空指针
	 */
	public Node next() {
		return sites.next();
	}

	/**
	 * 输出运行站点地址列表
	 * @return 节点列表
	 */
	public List<Node> detail() {
		super.lockMulti();
		try {
			return sites.list();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断站点存在
	 * @param node 站点地址
	 * @return 如果存在返回“真”，否则“假”。
	 */
	public boolean contains(Node node) {
		return find(node) != null;
	}
	
	/**
	 * 保存一个节点地址
	 * @param e
	 */
	protected void addNode(Node e) {
		Laxkit.nullabled(e);
		sites.add(e);
	}

	/**
	 * 增加一个注册站点。
	 * 这个方法对应“HubVisit.login”方法，在保存注册地址前，首先检查和删除旧的同地址站点数据。
	 * @param site LAXCUS站点
	 * @return 保存成功返回“真”，否则“假”。
	 */
	public boolean add(Site site) {
		if (site == null) {
			Logger.error(this, "add", "null pointer!");
			return false;
		} else if (site.getFamily() != getFamily()) {
			Logger.error(this, "add", "not match! %d - %d", site.getFamily(), getFamily());
			return false;
		}

		Node node = site.getNode();
		// 判断站点存在
		boolean inside = contains(node);

		boolean success = false;
		// 锁定!
		super.lockSingle();
		try {
			// 1. 删除旧信息
			if (inside) {
				effuse(node);
			}
			// 2. 保存新的信息
			success = infuse(site);
			// 3. 保存成功，记录这个站点地址
			if (success) {
				sites.add(node);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 以上操作成功，向集群传播这个站点
		if (success) {
			transmit(site);
			
			// 延时触发重新注册
			getLauncher().touch();
		}

		Logger.debug(this, "add", success, "from %s", node);

		return success;
	}

	/**
	 * 删除一个注册站点。<br>
	 * 这个方法对应“HubVisit.logout”方法，是正常的退出。
	 * 
	 * @param node 运行站点地址
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean remove(Node node) {
		if (node == null) {
			Logger.error(this, "remove", "null pointer!");
			return false;
		} else if (node.getFamily() != getFamily()) {
			Logger.error(this, "remove", "not match! %d - %d", node.getFamily(), getFamily());
			return false;
		}

		// 以锁定方式进行删除操作
		Site site = null;
		super.lockSingle();
		try {
			// 返回被删除的站点句柄
			site = effuse(node);
			// 删除注册的站点地址
			if (site != null) {
				sites.remove(node);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 通知关联站点，取消这个站点记录
		boolean success = (site != null);
		if (success) {
			dismiss(site);
			// 延时触发重新注册
			getLauncher().touch();
		}

		Logger.debug(this, "remove", success, "from %s", node);

		return success;
	}

	/**
	 * “dispose”方法是销毁一个注册站点，属于“故障”方式；这有别于“remove”方法的正常删除。
	 * 在销毁注册站点的同时，“dispose”方法还要将被销毁的站点耕耘通知给它的关联站点。
	 * 例如监视站点和集群管理员，并在图形界面上以声音和动画的方式显示这个失效站点。
	 * @param node 失效站点
	 * @return 成功返回真，否则假
	 */
	public boolean dispose(Node node) {
//		if (node == null || node.getFamily() != getFamily()) {
//			return false;
//		}
		
		if (node == null) {
			Logger.error(this, "dispose", "null pointer!");
			return false;
		} else if (node.getFamily() != getFamily()) {
			Logger.error(this, "dispose", "not match! %d - %d", node.getFamily(), getFamily());
			return false;
		}

		// 以锁定方式进行删除操作
		Site site = null;
		super.lockSingle();
		try {
			// 返回被删除的站点句柄
			site = effuse(node);
			// 删除注册的站点地址
			if (site != null) {
				sites.remove(node);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 通知关联站点，这个站点发生故障，要求销毁
		boolean success = (site != null);
		if (success) {
			destroy(site);
			// 延时触发重新注册
			getLauncher().touch();
		}

		Logger.debug(this, "dispose", success, "from %s", node);

		return success;
	}

	/**
	 * 刷新站点被激活的时间。当客户机向服务器发向“hello”激活时，服务器判断和调用这个方法。
	 * @param node 站点地址
	 * @return 刷新成功返回“真”，否则“假”。
	 */
	public boolean refresh(Node node) {
		Site site = find(node);
		boolean success = (site != null);
		if (success) {
			site.refreshTime();
		}

		Logger.debug(this, "refresh", success, "from %s", node);
		return success;
	}

	/**
	 * 返回子集的注册站点集合
	 * @return 注册站点集合
	 */
	protected abstract Map<Node, ? extends Site> iterator();

	/**
	 * 注入一个站点资源。<br>
	 * “infuse”方法被“add”方法调用。“add”方法在调用时进行了锁定处理，所以“infuse”不允许锁定操作，否则会造成互斥锁（死锁）定现象。
	 * @param site 站点资源
	 * @return 添加成功返回“真”，否则“假”。
	 */
	protected abstract boolean infuse(Site site);

	/**
	 * 释出一个站点资源，包括它的元数据。
	 * “effuse”方法被“remove”方法调用。“remove”方法在调用时进行了锁定处理，所以“effuse”不允许锁定操作，否则会造成互斥锁（死锁）现象。
	 * @param node 站点地址
	 * @return 返回被删除的站点实例，或者空指针
	 */
	protected abstract Site effuse(Node node);

	/**
	 * 向关联的站点传播一个站点元数据。<br>
	 * 
	 * “transmit”方法被“add”方法调用，发生在“infuse”方法之后。
	 * 目的是通知集群其它关联的站点，有一个新的站点被集群收录。
	 * @param site 新站点
	 */
	protected abstract void transmit(Site site);

	/**
	 * 以正常状态通知关联站点，取消一个站点记录。<br>
	 * dismiss是正常状态的取消，destroy是故障状态的撤销。它们是相对状态。<br><br>
	 * 
	 * “dismiss”方法被“remove”方法调用，发生在“effuse”方法之后。
	 * 收到消息的站点将从自己的缓冲中撤销这个站点记录。
	 * 
	 * @param site 被释放的站点
	 */
	protected abstract void dismiss(Site site);

	/**
	 * 以故障状态通知关联站点，撤销一个站点记录。<br>
	 * destroy是故障状态的撤销，dismiss是正常状态的取消。它们是相对状态。<br><br>
	 * 
	 * “destroy”方法被“dispose”方法调用，发生在“effuse”方法之后。
	 * 收到消息的站点将从自己的缓存中撤销这个站点记录。相对于“dismiss”的正常撤销，它是一个非正常的状态。
	 * 
	 * @param site 故障站点
	 */
	protected abstract void destroy(Site site);

	/**
	 * 根据运行站点地址，查找一个站点资源
	 * @param node 站点地址
	 * @return 返回站点实例。
	 */
	public abstract Site find(Node node);

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into ...");
		while (!isInterrupted()) {
			// 检查站点
			check();
			// 延时
			sleep();
		}
		Logger.info(this, "process", "exit");
	}

}