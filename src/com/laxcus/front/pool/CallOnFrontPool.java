/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.pool;

import java.util.*;

import com.laxcus.fixp.*;
import com.laxcus.fixp.monitor.*;
import com.laxcus.front.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.remote.client.*;
import com.laxcus.remote.client.hub.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.site.rabbet.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;
import com.laxcus.visit.hub.*;

/**
 * CALL站点维持池 <br><br>
 * 
 * 定时从AID获CALL站点，然后连接它们。本地工作包括：<br>
 * 1. 注册 <br>
 * 2. 注销 <br>
 * 3. 定时HELO <br>
 * 
 * @author scott.liang
 * @version 1.2 5/23/2013
 * @since laxcus 1.0
 */
public final class CallOnFrontPool extends VirtualPool {

	/** 资源管理池的静态句柄，全局唯一 **/
	private static CallOnFrontPool selfHandle = new CallOnFrontPool();

	/** CALL站点监听地址（UDP模式） -> CALL站点 **/
	private Map<SocketHost, CallRabbet> mapSites = new TreeMap<SocketHost, CallRabbet>();

	/**
	 * 构造CALL站点管理池
	 */
	private CallOnFrontPool() {
		super();
	}

	/**
	 * 返回CALL站点管理池静态句柄
	 * @return
	 */
	public static CallOnFrontPool getInstance() {
		return CallOnFrontPool.selfHandle;
	}

	/**
	 * 返回FRONT站点启动器静态句柄
	 * @return
	 */
	public static FrontLauncher getLauncher() {
		return (FrontLauncher) VirtualPool.getLauncher();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");
		while (!isInterrupted()) {
			if (mapSites.size() > 0) {
				check();
			}
			delay(1000);
		}
		// 注销全部
		logoutAll();
		// 退出
		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapSites.clear();
	}
	
	/**
	 * 删除FRONT本地保存，与CALL节点通信的密钥
	 * @param hub call节点UDP主机
	 */
	private void removeCipher(SocketHost hub) {
		// FRONT节点删除本地保存、与CALL节点通信的密钥
		Cipher cipher = getLauncher().dropCipher(hub);
		Logger.debug(this, "removeCipher", cipher != null,
				"drop secure cipher %s # %s", hub, cipher);
	}

	/**
	 * 增加一个注册站点
	 * @param hub
	 * @param site
	 */
	private void add(SocketHost hub, CallRabbet site) {
		super.lockSingle();
		try {
			mapSites.put(hub, site);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 删除注册地址
	 * @param hub
	 * @return
	 */
	private boolean remove(SocketHost hub) {
		if (hub == null) {
			return false;
		}
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			CallRabbet site = mapSites.remove(hub);
			success = (site != null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "remove", success, "remove %s", hub);
		return success;
	}

	/**
	 * 找一个站点
	 * @param hub 来源SOCKET地址
	 * @return 返回实例，或者空指针
	 */
	public CallRabbet find(SocketHost hub) {
		super.lockMulti();
		try {
			if (hub != null) {
				return mapSites.get(hub);
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 判断某个站点已经注册
	 * @param hub CALL节点套接字地址
	 * @return 存在返回真，否则假
	 */
	public boolean contains(SocketHost hub) {
		return find(hub) != null;
	}

	/**
	 * 判断某个站点已经注册
	 * @param hub CALL节点
	 * @return 存在返回真，否则假
	 */
	public boolean contains(Node hub) {
		CallRabbet rt = find(hub.getPacketHost());
		if (rt != null) {
			return Laxkit.compareTo(rt.getHub(), hub) == 0;
		}
		return false;
	}
	
	/**
	 * 判断某个站点已经注册
	 * @param hub CALL节点
	 * @return 返回真或者假
	 */
	public boolean hasSite(Node hub) {
		return contains(hub);
	}

	/**
	 * 接收来自CALL站点的激活反馈，刷新时间
	 * @param hub CALL站点地址
	 * @return 刷新成功返回“真”，否则“假”。
	 */
	public boolean refresh(SocketHost hub) {
		CallRabbet site = find(hub);
		boolean success = (site != null);
		if (success) {
			site.refreshTime();
		}

		Logger.debug(this, "refresh", success, "from %s", hub);
		return success;
	}

	/**
	 * 检查超时
	 */
	private void check() {
		int size = mapSites.size();
		ArrayList<SocketHost> disables = new ArrayList<SocketHost>(size);
		ArrayList<SocketHost> timeouts = new ArrayList<SocketHost>(size);

		super.lockSingle();
		try {
			Iterator<Map.Entry<SocketHost, CallRabbet>> iterator = mapSites.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SocketHost, CallRabbet> entry = iterator.next();
				if (entry.getValue().isMaxHubTimeout()) {
					disables.add(entry.getKey()); // 达到最大激活时间
				} else if (entry.getValue().isHubTimeout()) {
					timeouts.add(entry.getKey()); // 达到激活时间
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		//		Logger.debug(this, "check", "site size:%d, disable size:%d, timeout size:%d", size, disables.size(), timeouts.size());

		// 删除达到最大超时的连接
		for (SocketHost hub : disables) {
			remove(hub);
		}
		// 向注册地址发送激活操作
		for (SocketHost hub : timeouts) {
			hello(hub, 1);
		}
	}

	//	/**
	//	 * 以客户机的身份，向服务器发送“退出”命令。<br><br>
	//	 * 
	//	 * 操作流程：<br>
	//	 * 1. 客户机向服务器发送“退出（Ask.NOTITY Ask.EXIT）”命令。<br>
	//	 * 2. 客户机不要求服务器端反馈结果（DIRECT_NOTIFY）。<br>
	//	 * 3. 服务器（FixpPacketMonitor）收到后，删除本地保存的密钥。<br>
	//	 * 
	//	 * @param endpoint CALL站点目标地址
	//	 */
	//	private void cancel(SocketHost endpoint) {
	//		Mark mark = new Mark(Ask.NOTIFY, Ask.EXIT);
	//		Packet packet = new Packet(endpoint, mark);
	//		packet.addMessage(MessageKey.DIRECT_NOTIFY, true);
	//		// 走notice信道，发出退出命令
	//		PacketMessenger messenger = getLauncher().getPacketMessenger();
	//		boolean success = messenger.notice(packet);
	//
	//		// 删除密钥
	//		if (success) {
	//			getLauncher().removeCipher(endpoint);
	//		}
	//
	//		Logger.note(this, "cancel", success, "to %s", endpoint);
	//	}

//	/**
//	 * 服务器（FixpPacketMonitor）删除本地保存的CALL节点通信密钥。<br>
//	 * 
//	 * 注销与CALL节点通信后，执行这项操作！
//	 * 
//	 * @param endpoint CALL站点目标地址
//	 */
//	private void removeCipher(SocketHost hub) {
//		Cipher cipher = getLauncher().dropCipher(hub);
//
//		Logger.debug(this, "removeCipher", cipher != null,
//				"drop secure cipher %s # %s", hub, cipher);
//	}

	/**
	 * 切换UDP端口
	 * @param remote 目标地址
	 * @param node 本地节点
	 * @return 成功返回真，否则假
	 */
	private boolean switchTo(SocketHost remote, Node node) {
		// 判断是内网
		boolean success = getLauncher().isPock();
		// 不成立，忽略它!
		if (!success) {
			return true;
		}

		// 找到NAT地址
		SocketHost nat = getLauncher().getPacketHelper().findPockLocal(remote);
		if (nat == null) {
			Logger.error(this, "switchTo", "cannot be find nat! from %s", remote);
			return false;
		}
		// 修改NAT出口地址
		node.getHost().setUDPort(nat.getPort());
		Logger.info(this, "switchTo", "exchange nat %s! from %s", node, remote);
		return true;
	}

	/**
	 * 通过本地SOCKET，向目标CALL地址发送刷新包
	 * @param endpoint CALL节点套接字地址
	 * @param count 数据包发送统计
	 */
	private void hello(SocketHost endpoint, int count) {
		// FRONT节点地址
		Node node = getLauncher().getListener().duplicate();

		// 如果FRONT节点在内网，节点的UDP端口修改为NAT的UDP端口
		boolean pass = switchTo(endpoint, node);
		if (!pass) {
			Logger.error(this, "hello", "cannot be find nat! from %s", endpoint);
			return;
		}

		// 发送数据包
		Mark mark = new Mark(Ask.NOTIFY, Ask.HELO);
		Packet packet = new Packet(endpoint, mark);
		packet.addMessage(MessageKey.NODE_ADDRESS, node.build());
		// 走FIXP UDP信道，向CALL节点发送数据包
		PacketMessenger messenger = getLauncher().getPacketMessenger();
		for (int i = 0; i < count; i++) {
			boolean success = messenger.notice(packet);
			Logger.debug(this, "hello", success, "send to %s", endpoint);
		}
	}

	/**
	 * 注销全部
	 */
	public void logoutAll() {
		ArrayList<SocketHost> array = new ArrayList<SocketHost>();
		super.lockSingle();
		try {
			array.addAll(mapSites.keySet());
			mapSites.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 注销
		for (SocketHost hub : array) {
			exit(hub);
		}
	}

	//	/**
	//	 * 从CALL站点注销
	//	 * @param hub 服务器地址
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean exit(SocketHost hub) {
	//		// 连接注册站点地址
	//		HubClient client = ClientCreator.createHubClient(hub);
	//		if (client == null) {
	//			Logger.error(this, "exit", "cannot find %s", hub);
	//			return false;
	//		}
	//
	//		Node node = getLauncher().getListener();
	//		boolean success = false;
	//		try {
	//			success = client.logout(node);
	//			client.close();
	//		} catch (VisitException e) {
	//			Logger.error(e);
	//		}
	//		// 彻底销毁
	//		client.destroy();
	//		
	//		// 撤销与CALL节点的UDP通信
	//		cancel(hub);
	//		
	////		// 撤销UDP通信
	////		if (success) {
	////			cancel(hub);
	////		}
	//
	//		Logger.debug(this, "exit", success, "from %s", hub);
	//
	//		return success;
	//	}

	/**
	 * 从CALL站点注销
	 * @param hub 服务器地址
	 * @return 成功返回真，否则假
	 */
	private boolean exit(SocketHost hub) {
		// 连接注册的CALL站点地址
		FrontClient client = ClientCreator.createFrontClient(hub); // UDP连接
		if (client == null) {
			Logger.error(this, "exit", "cannot be git %s", hub);
			return false;
		}

		// 生成副本
		Node node = getLauncher().getListener().duplicate();

		// 如果FRONT节点在内网，节点的UDP端口修改为NAT的UDP端口
		boolean pass = switchTo(hub, node);
		if (!pass) {
			Logger.error(this, "exit", "cannot be find nat! from %s", hub);
			return false;
		}

		boolean success = false;
		try {
			success = client.logout(node);
			client.close();
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 彻底销毁
		client.destroy();

		// 删除内存记录
		remove(hub);

		//		// 撤销与CALL节点的UDP通信
		//		cancel(hub);

		// 本地删除与CALL节点“命令信道/数据信道”的PockItem（只在内网中有效）
		getLauncher().getPacketHelper().removePock(hub);
		getLauncher().getReplyHelper().removePock(hub);

		// FRONT节点删除本地保存、与CALL节点通信的密钥
		removeCipher(hub);

		Logger.debug(this, "exit", success, "from %s", hub);

		return success;
	}

	/**
	 * 从CALL站点上注销
	 * @param hub 目标站点
	 * @return 注销成功返回“真”，否则“假”。
	 */
	public boolean logout(SocketHost hub) {
		// 判断站点存在
		if (contains(hub)) {
			return exit(hub);
		}
		return false;
	}

	/**
	 * 指定一个CALL站点，从这个站点注销
	 * @param hub CALL站点地址
	 * @return 注销成功返回“真”，否则“假”。
	 */
	public boolean logout(Node hub) {
		SocketHost endpoint = hub.getPacketHost();
		return logout(endpoint);
	}

	/**
	 * 重新注册到CALL节点。 重新注册的前提是必须保存有CALL节点地址
	 * 两步操作：
	 * 1. 删除本地保存的旧地址
	 * 2. 删除成功后，注册地址
	 * 
	 * @param hub CALL站点地址
	 * @return 成功返回真，否则假
	 */
	public boolean relogin(Node hub) {
		SocketHost remote = hub.getPacketHost();
		// 删除旧的地址
		boolean success = remove(remote);
		if (success) {
			success = login(hub);
		}

		Logger.debug(this, "relogin", success, "relogin to %s", hub);

		return success;
	}

	//	/**
	//	 * 注册到CALL站点
	//	 * 
	//	 * @param hub CALL站点
	//	 * @return 注册成功返回“真”，否则“假”。
	//	 */
	//	public boolean login(Node hub) {
	//		// 取它的UDP通讯地址
	//		SocketHost remote = hub.getPacketHost();
	//		// 如果地址已经存在，不处理，返回“真”。
	//		if (contains(remote)) {
	//			return true;
	//		}
	//
	//		// 关联站点
	//		CallRabbet rabbet = new CallRabbet(hub);
	//
	//		HubClient client = ClientCreator.createHubClient(remote);
	//		if (client == null) {
	//			Logger.error(this, "login", "cannot find %s", hub);
	//			return false;
	//		}
	//
	//		FrontSite self = getLauncher().getSite().duplicate();
	//		// 注册到CALL站点
	//		boolean success = false;
	//		try {
	//			long ms = client.getSiteTimeout(SiteTag.FRONT_SITE);
	//			success = (ms > 0);
	//			if (success) {
	//				rabbet.setHubTimeout(ms);
	//			}
	//			
	//			// 与HubVisitOnCall.getSiteTimeout方法的私有规定，FRONT_SITE * 3 , 
	//			// 表示获取FRONT断网后自动重新注册到CALL节点的时间（注意！是线程内部的自动注册，非人工手动注册！）
	//			if (success) {
	//				long max = client.getSiteTimeout((byte) (SiteTag.FRONT_SITE * 3));
	//				CallOnFrontPool.getLauncher().setAutoRetryInterval(max);
	//				Logger.info(this, "login", "%s auto relogin interval:%d ms", hub, max);
	//			}
	//			
	//			Logger.debug(this, "login", "%s timeout is %d", hub, ms);
	//			if (success) {
	//				success = false;
	//				success = client.login(self);
	//			}
	//			client.close(); // 柔性关闭
	//		} catch (VisitException e) {
	//			success = false;
	//			Logger.error(e);
	//		} catch (Throwable e) {
	//			success = false;
	//			Logger.fatal(e);
	//		}
	//		// 强制关闭
	//		client.destroy();
	//
	//		// 保存
	//		if (success) {
	//			// 保存地址
	//			add(remote, rabbet);
	//			// 向目标地址发送HELO指令
	//			hello(remote, 3);
	//		}
	//
	//		Logger.debug(this, "login", success, "login to %s", hub);
	//
	//		return success;
	//	}

	/**
	 * 注册到CALL站点
	 * 
	 * @param hub CALL站点
	 * @return 注册成功返回“真”，否则“假”。
	 */
	public boolean login(Node hub) {
		// 取它的UDP通讯地址
		SocketHost remote = hub.getPacketHost();
		// 如果地址已经存在，不处理，返回“真”。
		if (contains(remote)) {
			return true;
		}

		// 关联站点
		CallRabbet rabbet = new CallRabbet(hub);

		// 以UDP方式连接CALL节点
		FrontClient client = ClientCreator.createFrontClient(remote);
		if (client == null) {
			Logger.error(this, "login", "cannot be connect %s", hub);
			return false;
		}

		// 生成副本
		FrontSite self = getLauncher().getSite().duplicate();

		// 如果FRONT节点在内网，节点的UDP端口修改为NAT的UDP端口
		boolean pass = switchTo(remote, self.getNode());
		if (!pass) {
			Logger.error(this, "login", "cannot be find nat! from %s", hub);
			return false;
		}

		// 注册到CALL站点
		boolean success = false;
		try {
			// 释放服务器上旧记录，删除本地保存的密钥（可能存在的）
			boolean release = client.release(self);
			removeCipher(remote);
			Logger.debug(this, "login", release, "drop history, from %s", remote);
			
			// CALL节点UDP PING的超时时间
			long ms = client.getTimeout(); 
			success = (ms > 0);
			if (success) {
				rabbet.setHubTimeout(ms);
			}

			// 取FRONT断网后，自动重新注册到CALL节点的间隔时间。（注意！是线程内部的自动注册，而非人工手动注册！）
			if (success) {
				long max = client.getAutoReloginInterval(); 
				CallOnFrontPool.getLauncher().setAutoRetryInterval(max);
				Logger.info(this, "login", "%s , auto relogin interval: %d -> %d ms", 
						hub, max, CallOnFrontPool.getLauncher().getAutoRetryInterval());
			}

			Logger.debug(this, "login", "%s timeout is %d", hub, ms);
			if (success) {
				success = false;
				FrontReport report = client.login(self);
				// 判断登录成功！
				success = (report != null && report.isLogined());
			}
			client.close(); // 柔性关闭
		} catch (VisitException e) {
			success = false;
			Logger.error(e);
		} catch (Throwable e) {
			success = false;
			Logger.fatal(e);
		}
		// 强制关闭
		client.destroy();
		
		// 成功，删除本地保存的密钥
		if (success) {
			removeCipher(hub.getPacketHost());
		}

		// 以上成功，保存地址，向CALL节点发送HELO指令
		if (success) {
			// 保存地址
			add(remote, rabbet);

			// 重新向目标地址发送HELO指令
			hello(remote, 3);
		}

		Logger.debug(this, "login", success, "login to %s", hub);

		return success;
	}

	/**
	 * 返回当前FRONT节点注册的CALL节点集合
	 * @return 节点集合
	 */
	public List<Node> getHubs() {
		ArrayList<Node> array = new ArrayList<Node>();
		// 锁定 
		super.lockMulti();
		try {
			for (CallRabbet sibling : mapSites.values()) {
				array.add(sibling.getHub());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}
}