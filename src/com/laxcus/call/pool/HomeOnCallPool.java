/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import java.util.*;

import com.laxcus.call.*;
import com.laxcus.command.relate.*;
import com.laxcus.fixp.*;
import com.laxcus.fixp.monitor.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.remote.client.*;
import com.laxcus.remote.client.hub.*;
import com.laxcus.site.*;
import com.laxcus.site.call.*;
import com.laxcus.site.rabbet.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;

/**
 * 关联HOME管理池。CALL定时向这些HOME站点发HELO命令。
 * 
 * @author scott.liang
 * @version 1.0 2/2/2013
 * @since laxcus 1.0
 */
public final class HomeOnCallPool extends VirtualPool {

	/** 资源管理池句柄 **/
	private static HomeOnCallPool selfHandle = new HomeOnCallPool();

	/** HOME站点监听地址（UDP模式） -> 本地维持配置 **/
	private Map<SocketHost, HomeRabbet> rabbets = new TreeMap<SocketHost, HomeRabbet>();

	/**
	 * 构造HOME站点管理池
	 */
	private HomeOnCallPool() {
		super();
	}

	/**
	 * 返回HOME站点管理池静态句柄
	 * @return
	 */
	public static HomeOnCallPool getInstance() {
		return HomeOnCallPool.selfHandle;
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

		long interval = 1200000; //20分钟
		long endtime = System.currentTimeMillis();

		while (!isInterrupted()) {
			if (System.currentTimeMillis() >= endtime) {
				endtime += interval;
				attempt();
			} else if (rabbets.size() > 0) {
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
		rabbets.clear();
	}

	/**
	 * 尝试发送查询关联HOME站点命令
	 */
	private void attempt() {
		List<Siger> list = StaffOnCallPool.getInstance().getSigers();
		for (Siger username : list) {
			FindRelateHome cmd = new FindRelateHome(username);
			CallCommandPool.getInstance().admit(cmd);
		}
	}

	/**
	 * 增加一个注册站点
	 * @param hub
	 * @param site
	 */
	private void add(SocketHost hub, HomeRabbet site) {
		super.lockSingle();
		try {
			rabbets.put(hub, site);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 删除一个站点地址
	 * @param hub
	 * @return
	 */
	private boolean remove(SocketHost hub) {
		if (hub == null) {
			return false;
		}
		boolean success = false;
		super.lockSingle();
		try {
			HomeRabbet rabbet = rabbets.remove(hub);
			success = (rabbet != null);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "remove", success, "remove %s", hub);
		return success;
	}

	/**
	 * 找一个站点
	 * @param hub
	 * @return
	 */
	public HomeRabbet find(SocketHost hub) {
		super.lockMulti();
		try {
			if (hub != null) {
				return rabbets.get(hub);
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 判断存在
	 * @param hub
	 * @return
	 */
	public boolean contains(SocketHost hub) {
		return find(hub) != null;
	}

	/**
	 * 判断存在
	 * @param hub
	 * @return
	 */
	public boolean contains(Node hub) {
		SocketHost endpoint = hub.getPacketHost();
		return contains(endpoint);
	}

	/**
	 * 接收来自HOME站点的激活反馈，刷新时间
	 * @param hub - HOME站点地址
	 * @return 刷新成功返回“真”，否则“假”。
	 */
	public boolean refresh(SocketHost hub) {
		HomeRabbet rabbet = find(hub);
		boolean success = (rabbet != null);
		if (success) {
			rabbet.refreshTime();
		}

		Logger.debug(this, "refresh", success, "from %s", hub);
		return success;
	}

	/**
	 * 检查超时
	 */
	private void check() {
		int size = rabbets.size();
		ArrayList<SocketHost> disables = new ArrayList<SocketHost>(size);
		ArrayList<SocketHost> timeouts = new ArrayList<SocketHost>(size);

		super.lockSingle();
		try {
			Iterator<Map.Entry<SocketHost, HomeRabbet>> iterator = rabbets.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SocketHost, HomeRabbet> entry = iterator.next();
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

		// 删除达到最大超时的连接
		for (SocketHost hub : disables) {
			remove(hub);
		}
		// 向注册地址发送激活操作
		for (SocketHost hub : timeouts) {
			hello(1, hub);
		}
	}

	/**
	 * 以客户机的身份，向服务器发送“退出”命令。<br><br>
	 * 
	 * 操作流程：<br>
	 * 1. 客户机向服务器发送“退出（Ask.NOTITY Ask.EXIT）”命令。<br>
	 * 2. 服务器（在FixpPacketMonitor层面）接收和判断，向客户机发送“再见（Answer.GOODBYE）”应答。<br>
	 * 3. 客户机（在FixpPacketMonitor层面）收到应答，关闭密文（如果有的情况下）。<br>
	 * @param endpoint
	 */
	private void unhello(SocketHost endpoint) {
		Mark cmd = new Mark(Ask.NOTIFY, Ask.EXIT);
		Packet packet = new Packet(endpoint, cmd);
		PacketMessenger messenger = VirtualPool.getLauncher().getPacketMessenger();
		messenger.notice(packet);
	}

	/**
	 * 通过本地SOCKET，向目标地址发送刷新包
	 * @param sends
	 * @param endpoint
	 */
	private void hello(int sends, SocketHost endpoint) {
		// 发送数据包
		Mark cmd = new Mark(Ask.NOTIFY, Ask.HELO);
		Packet packet = new Packet(endpoint, cmd);
		Node node = VirtualPool.getLauncher().getListener();
		packet.addMessage(MessageKey.NODE_ADDRESS, node.build());
		// 发送数据包
		PacketMessenger messenger = VirtualPool.getLauncher().getPacketMessenger();
		for (int i = 0; i < sends; i++) {
			messenger.notice(packet);
		}

		Logger.debug(this, "hello", "send to %s", endpoint);
	}

	/**
	 * 注销全部
	 */
	public void logoutAll() {
		ArrayList<SocketHost> array = new ArrayList<SocketHost>();
		super.lockSingle();
		try {
			array.addAll(rabbets.keySet());
			rabbets.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 注销
		for (SocketHost hub : array) {
			directLogout(hub);
		}
	}

	/**
	 * 从HOME站点注销
	 * @param hub
	 * @return
	 */
	private boolean directLogout(SocketHost hub) {
		// 连接注册站点地址
		HubClient client = ClientCreator.createHubClient(hub);
		if (client == null) {
			Logger.error(this, "directLogout", "cannot find %s", hub);
			return false;
		}

		Node node = VirtualPool.getLauncher().getListener();
		boolean success = false;
		try {
			success = client.logout(node);
			client.close();
		} catch (VisitException e) {
			Logger.error(e);
		}
		client.destroy();

		// 关闭激活
		if (success) {
			unhello(hub);
		}

		Logger.debug(this, "directLogout", success, "from %s", hub);

		return success;
	}

	/**
	 * 从HOME站点上注销
	 * @param hub - 目标站点
	 * @return - 注销成功返回“真”，否则“假”。
	 */
	public boolean logout(SocketHost hub) {
		// 判断站点存在
		if (contains(hub)) {
			return directLogout(hub);
		}
		return false;
	}

	/**
	 * 指定一个HOME站点，从这个站点注销
	 * @param hub - HOME站点地址
	 * @return 注销成功返回“真”，否则“假”。
	 */
	public boolean logout(Node hub) {
		SocketHost endpoint = hub.getPacketHost();
		return logout(endpoint);
	}

	/**
	 * 注册到HOME站点
	 * @param hub - HOME站点
	 * @return 注册成功返回“真”，否则“假”。
	 */
	public boolean login(Node hub) {
		// 取它的UDP通讯地址
		SocketHost endpoint = hub.getPacketHost();
		// 如果地址已经存在，不处理，返回“真”。
		if (contains(endpoint)) {
			return true;
		}

		// 关联站点
		HomeRabbet rabbet = new HomeRabbet(hub);

		HubClient client = ClientCreator.createHubClient(endpoint);
		if (client == null) {
			Logger.error(this, "login", "cannot find %s", hub);
			return false;
		}

		CallSite self = (CallSite) CallLauncher.getInstance().getSite();	
		// 注册到服务器
		boolean success = false;
		try {
			// 1. 取超时时间
			long ms = client.getSiteTimeout(SiteTag.CALL_SITE);
			success = (ms > 0);
			if (success) {
				rabbet.setHubTimeout(ms);
			}
			Logger.debug(this, "login", "%s timeout is %d", hub, ms);
			// 2. 检查版本
			if (success) {
				Version other = client.getVersion();
				success = (Laxkit.compareTo(getLauncher().getVersion(), other) == 0);
				Logger.note(this, "login", success, "check version  \"%s\" - \"%s\"", getLauncher().getVersion(), other);
			}
			// 3. 注册
			if (success) {
				success = false;
				success = client.login(self);
			}
			// 关闭socket
			client.close();
		} catch (VisitException e) {
			success = false;
			Logger.error(e);
		}
		// 销毁socket，如果以上不成功的话...
		client.destroy();

		// 成功，删除本地保存的服务端密钥，同步HubVisitOnHome.login方法也做同样的删除！
		if (success) {
			getLauncher().removeCipher(hub);
		}

		// 保存
		if (success) {
			add(endpoint, rabbet);
		}

		Logger.debug(this, "login", success, "login to %s", hub);

		return success;
	}

	/**
	 * 返回节点集合
	 * @return
	 */
	public List<Node> getNodes() {
		ArrayList<Node> array = new ArrayList<Node>();
		super.lockMulti();
		try {
			for (HomeRabbet rabbet : rabbets.values()) {
				array.add(rabbet.getHub());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

}