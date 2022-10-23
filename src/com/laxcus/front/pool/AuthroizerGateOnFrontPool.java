/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.pool;

import java.util.*;

import com.laxcus.command.site.entrance.*;
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
 * 授权人GATE站点管理池
 * 
 * @author scott.liang
 * @version 1.0 8/3/2017
 * @since laxcus 1.0
 */
public class AuthroizerGateOnFrontPool extends VirtualPool {

	/** 授权人静态句柄  **/
	private static AuthroizerGateOnFrontPool selfHandle = new AuthroizerGateOnFrontPool();

	/** GATE站点监听地址（UDP模式） -> GATE连接客户端集合 **/
	private Map<SocketHost, GateRabbetSet> mapSites = new TreeMap<SocketHost, GateRabbetSet>();

	/** 授权人签名 -> GATE连接器 **/
	private Map<Siger, GateRabbet> mapAuthroizer = new TreeMap<Siger, GateRabbet>();

	/**
	 * 构造默认和私有的授权人GATE站点管理池
	 */
	private AuthroizerGateOnFrontPool() {
		super();
	}

	/**
	 * 返回授权人GATE站点管理池静态句柄
	 * @return
	 */
	public static AuthroizerGateOnFrontPool getInstance() {
		return AuthroizerGateOnFrontPool.selfHandle;
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
		mapAuthroizer.clear();
	}

	/**
	 * 返回连接的地址
	 * @return Node实例
	 */
	public List<Node> getHubs() {
		ArrayList<Node> array = new ArrayList<Node>();

		// 锁定
		super.lockMulti();
		try {
			Iterator<Map.Entry<Siger, GateRabbet>> iterator = mapAuthroizer.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, GateRabbet> entry = iterator.next();
				GateRabbet rabbet = entry.getValue();
				array.add(rabbet.getHub());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return array;
	}

	/**
	 * 返回授权人单元
	 * @return 授权单元列表
	 */
	public List<AuthorizerItem> getAuthroizeItems() {
		ArrayList<AuthorizerItem> array = new ArrayList<AuthorizerItem>();

		super.lockMulti();
		try {
			Iterator<Map.Entry<Siger, GateRabbet>> iterator = mapAuthroizer.entrySet().iterator();
			while(iterator.hasNext()) {
				Map.Entry<Siger, GateRabbet> entry = iterator.next();
				GateRabbet rabbet = entry.getValue();
				AuthorizerItem item = new  AuthorizerItem(rabbet.getAuthorizer(), rabbet.getHub());
				array.add(item);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return array;
	}

	/**
	 * 返回全部授权人授权
	 * @return Siger列表
	 */
	public List<Siger> getAuthroizes() {
		super.lockMulti();
		try {
			return new ArrayList<Siger>(mapAuthroizer.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 增加一个注册站点
	 * @param hub GATE服务器UDP地址
	 * @param rabbet 本地监视客户端，保持PING激活状态
	 */
	private void add(SocketHost hub, GateRabbet rabbet) {
		super.lockSingle();
		try {
			// 保存参数
			GateRabbetSet set = mapSites.get(hub);
			if (set == null) {
				set = new GateRabbetSet();
				mapSites.put(hub, set);
			}
			set.add(rabbet);

			// 保存签名
			mapAuthroizer.put(rabbet.getAuthorizer(), rabbet);
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
		// 锁定！
		super.lockSingle();
		try {
			// 1. 删除连接器
			GateRabbetSet set = mapSites.remove(hub);
			success = (set != null);
			if (success) {
				// 2. 删除用户签名
				for (GateRabbet rabbet : set.list()) {
					mapAuthroizer.remove(rabbet.getAuthorizer());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "remove", success, "remove %s", hub);
		return success;
	}

	/**
	 * 删除指定GATE地址和关联的授权人
	 * @param hub GATE节点
	 * @param authorizer 授权人
	 * @return 成功返回真，否则假
	 */
	private boolean remove(SocketHost hub, Siger authorizer) {
		if (hub == null || authorizer == null) {
			return false;
		}

		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			GateRabbetSet set = mapSites.get(hub);
			if (set != null) {
				// 删除授权人
				GateRabbet rabbet = mapAuthroizer.remove(authorizer);
				if (rabbet != null) {
					success = set.remove(rabbet);
				}
				// 空集合
				if (set.isEmpty()) {
					mapSites.remove(hub);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "remove", success, "remove %s#%s", authorizer, hub);
		return success;
	}

	/**
	 * 根据授权人签名，查询它的关联站点
	 * @param authorizer 授权人签名
	 * @return 返回GATE站点地址
	 */
	public Node findSite(Siger authorizer) {
		super.lockMulti();
		try {
			GateRabbet rabbet = mapAuthroizer.get(authorizer);
			if (rabbet != null) {
				return rabbet.getHub();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 找一个站点
	 * @param hub
	 * @return
	 */
	public GateRabbetSet find(SocketHost hub) {
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
	 * 判断参数存在
	 * @param hub 主机地址
	 * @param authorizer 授权人签名
	 * @return 返回真或者假
	 */
	public boolean contains(SocketHost hub, Siger authorizer) {
		boolean success = false;
		// 锁定
		super.lockMulti();
		try {
			GateRabbetSet set = mapSites.get(hub);
			if (set != null) {
				success = set.hasAuthorizer(authorizer);
			}
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 判断参数存在
	 * @param hub 授权人GATE节点地址
	 * @param authorizer 授权人签名
	 * @return 返回真或者假
	 */
	public boolean contains(Node hub, Siger authorizer) {
		return contains(hub.getPacketHost(), authorizer);
	}

	/**
	 * 判断某个站点已经注册
	 * @param hub
	 * @return
	 */
	public boolean contains(SocketHost hub) {
		return find(hub) != null;
	}

	/**
	 * 判断某个站点已经注册
	 * @param hub
	 * @return
	 */
	public boolean contains(Node hub) {
		return contains(hub.getPacketHost());
	}

	/**
	 * 接收来自GATE站点的激活反馈，刷新时间
	 * @param hub GATE站点地址
	 * @return 刷新成功返回“真”，否则“假”。
	 */
	public boolean refresh(SocketHost hub) {
		boolean success = false;
		super.lockSingle();
		try {
			GateRabbetSet set = mapSites.get(hub);
			success = (set != null);
			if (success) {
				set.refreshTime(); // 刷新全部时间
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "refresh", success, "from %s", hub);
		return success;
	}

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
	 * 注册到GATE站点
	 * @param site FRONT被授权人站点，区别与FRONT所有人站点
	 * @param hub GATE站点
	 * @return 成功返回真，否则假
	 */
	private boolean login(ConferrerSite site, Node hub) {
		// 取GATE站点的UDP通讯地址
		SocketHost remote = hub.getPacketHost();
		// 授权人签名
		Siger authorizer = site.getAuthorizer(0);

		// 如果地址和授权人已经存在，不处理，返回“真”。
		if (contains(remote, authorizer)) {
			Logger.warning(this, "login", "%s 已经存在！", authorizer);
			return true;
		}

		// 切换UDP端口
		boolean pass = switchTo(remote, site.getNode());
		if (!pass) {
			Logger.error(this, "login", "cannot be find nat! from %s", remote);
			return false;
		}

		// GATE站点
		GateRabbet rabbet = new GateRabbet(hub, authorizer);
		rabbet.setRemote(remote);

		boolean logined = false;
		boolean failed = false;
		FrontClient client = null;

		// 以授权人身份注册到GATE站点
		while (!logined && !failed) {
			client = ClientCreator.createFrontClient(hub, false); // 连接！
			// 判断连接成功
			if (client == null) {
				Logger.error(this, "login", "cannot be find %s", hub);
				break;
			}

			try {
				// 删除旧记录
				boolean release = client.release(site);
				Logger.debug(this, "login", release, "drop history, from %s", hub);
				removeCipher(hub.getPacketHost());
				
				// 1. 取站点激活超时时间！
				long timeout = client.getTimeout();
				boolean success = (timeout > 0);
				if (!success) break;
				rabbet.setHubTimeout(timeout);

				// 2. 取在线延时时间，时间单位：毫秒
				long lingerTimeout = client.getLingerTimeout();
				success = (lingerTimeout > 0);
				if (!success) break;

				// 3. 连网失败后，自动重新登录的间隔时间
				long max = client.getAutoReloginInterval();
				AuthroizerGateOnFrontPool.getLauncher().setAutoRetryInterval(max);

				Logger.info(this, "login", "site timeout %d ms, linger timeout:%d ms, auto relogin interval: %d -> %d ms",
						timeout, lingerTimeout, max, AuthroizerGateOnFrontPool.getLauncher().getAutoRetryInterval());

				// 注册最大延时，2分钟
				long endTime = System.currentTimeMillis() + 120000;

				// 4. 注册，循环等待
				while (!logined && !failed) {
					FrontReport report = client.login(site);
					if (report.isLinger()) {
						if (System.currentTimeMillis() >= endTime) {
							failed = true;
						} else {
							client.delay(lingerTimeout);
						}
					} else if (report.isLogined()) {
						logined = true; // 注册成功
					} else {
						failed = true; // 注册失败
					}
				}
			} catch (VisitException e) {
				Logger.error(e);
				break;
			} catch (Throwable e) {
				Logger.fatal(e);
				break;
			}
			// 销毁连接
			client.destroy();
		}

		// 冗余处理
		if (client != null) {
			client.destroy();
		}

		// 登录成功，判断与GATE节点不是一个地址，删除密钥。注意，必须在“helo”之前发送。
		if (logined) {
			removeCipher(hub.getPacketHost());
		}

		// 保存
		if (logined) {
			// 保存账号
			add(remote, rabbet);
			// 发送HELO指令
			hello(3, remote);
		}

		Logger.debug(this, "login", logined, "[%s] login to %s", authorizer, hub);

		return logined;
	}

	/**
	 * 当前账号，携带授权人签名，注册到授权人的站点。
	 * 
	 * @param hub GATE站点
	 * @param authorizer 授权人签名
	 * @return 注册成功返回真，否则假
	 */
	public boolean login(Node hub, Siger authorizer) {
		FrontSite front = getLauncher().getSite().duplicate();

		// 以被授权人的身份注册到授权人站点
		ConferrerSite site = new ConferrerSite();
		site.setHash(front.getHash());
		site.setNode(front.getNode()); // 当前地址
		site.setConferrer(front.getUser()); // 被授权人账号
		site.addAuthorizer(authorizer); // 授权人签名

		// 以副本状态注册到GATE站点
		return login(site, hub);
	}

	/**
	 * 从GATE站点上注销
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
	 * 指定一个GATE站点，从这个站点注销
	 * @param hub GATE站点地址
	 * @return 注销成功返回“真”，否则“假”。
	 */
	public boolean logout(Node hub) {
		SocketHost endpoint = hub.getPacketHost();
		return logout(endpoint);
	}

	/**
	 * 注销
	 * @param hub GATE注册站点
	 * @param authorizer 授权人签名
	 * @return 返回真或者假
	 */
	public boolean logout(Node hub, Siger authorizer) {
		// 判断存在，删除
		if (contains(hub, authorizer)) {
			SocketHost endpoint = hub.getPacketHost();
			return exit(endpoint, authorizer);
		}
		return false;
	}

	/**
	 * 从GATE站点注销
	 * @param hub GATE UDP节点
	 * @param authorizer 授权人
	 * @return 注销成功返回真，否则假。
	 */
	private boolean exit(SocketHost hub, Siger authorizer) {
		Node node = getLauncher().getListener().duplicate();

		// 切换UDP端口
		boolean pass = switchTo(hub, node);
		if (!pass) {
			Logger.error(this, "exit", "cannot be find nat! from %s", hub);
			return false;
		}

		// 连接注册站点地址
		boolean success = false;
		FrontClient client = ClientCreator.createFrontClient(hub);
		// 连接不成功，退出！
		if (client == null) {
			Logger.error(this, "exit", "cannot be git %s", hub);
			return false;
		}

		// 注销
		try {
			success = client.logout(node, authorizer);
			client.close();
		} catch (VisitException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 销毁
		client.destroy();

		// 从内存中删除
		remove(hub, authorizer);

		//		// 判断地址已经从内存中清除时，通知GATE站点，撤销FIXP UDP服务器上保存的密钥
		//		if (!contains(hub)) {
		//			cancel(hub);
		//		}

		// 本地删除与授权GATE节点“命令信道/数据信道”的PockItem（只在内网中有效）
		getLauncher().getPacketHelper().removePock(hub);
		getLauncher().getReplyHelper().removePock(hub);


		//		// 判断地址已经从内存中清除时，删除本地FIXP UDP服务器上保存的密钥
		//		if (!contains(hub)) {
		//			removeCipher(hub);
		//		}

		// 删除密钥
		removeCipher(hub);

		Logger.debug(this, "exit", success, "%s from %s", authorizer, hub);

		return success;
	}

	/**
	 * 从GATE站点注销
	 * @param hub GATE UDP节点
	 * @return 注销成功返回真，否则假。
	 */
	private boolean exit(SocketHost hub) {
		int count = 0;
		GateRabbetSet set = find(hub);
		if (set != null) {
			for (GateRabbet e : set.list()) {
				boolean success = exit(hub, e.getAuthorizer());
				if (success) count++;
			}
		}
		// 判断成功
		boolean success = (count > 0);
		// 删除地址
		if (success) {
			remove(hub);
		}
		return success;
	}

	//	/**
	//	 * 以客户机的身份，向服务器发送“退出”命令。<br><br>
	//	 * 
	//	 * 操作流程：<br>
	//	 * 1. 客户机向服务器发送“退出（Ask.NOTITY Ask.EXIT）”命令。<br>
	//	 * 2. 客户机不要求服务器端反馈结果（DIRECT_NOTIFY）。<br>
	//	 * 2. 服务器（GATE站点的FixpPacketMonitor）收到后，删除本地保存的FRONT站点密钥 。<br>
	//	 * 
	//	 * @param remote 目标GATE节点
	//	 */
	//	private void cancel(SocketHost remote) {
	//		Node hub = getLauncher().getHub();
	//		// 注销的授权人GATE站点与所有人注册的GATE站点一致时，忽略！
	//		if (Laxkit.compareTo(hub.getPacketHost(), remote) == 0) {
	//			Logger.warning(this, "cancel", "ignored! %s - %s", hub, remote);
	//			return;
	//		}
	//
	//		Mark mark = new Mark(Ask.NOTIFY, Ask.EXIT);
	//		Packet packet = new Packet(remote, mark);
	//		packet.addMessage(MessageKey.DIRECT_NOTIFY, true);
	//
	//		PacketMessenger messenger = getLauncher().getPacketMessenger();
	//		// 通过服务器信道，发送数据包
	//		boolean success = messenger.notice(packet);
	//		// 如果是连接的GATE站点是被授权站点，GATE站点将删除本地保存的FRONT站点密钥，如果不是，多做一次删除也无碍！
	//		if (success) {
	//			getLauncher().removeCipher(remote);
	//		}
	//
	//		Logger.note(this, "cancel", success, "to %s", remote);
	//	}

	/**
	 * FRONT节点以被授权人身份，删除与授权GATE主机通信的本地密钥。
	 * 前提是，FRONT注册GATE主机地址，与授权者GATE主机地址，不是一台机器！
	 * 
	 * 以客户机的身份，向服务器发送“退出”命令。<br><br>
	 * 
	 * 
	 * @param remote 目标GATE节点
	 */
	private void removeCipher(SocketHost remote) {
		Node hub = getLauncher().getHub();
		// 注销的授权人GATE站点与所有人注册的GATE站点一致时，忽略！
		boolean match =  (hub != null && Laxkit.compareTo(hub.getPacketHost(), remote) == 0);
		if (match) {
			Logger.warning(this, "removeCipher", "ignored! %s - %s", hub, remote);
			return;
		}

		Cipher cipher = getLauncher().dropCipher(remote);
		Logger.debug(this, "removeCipher", cipher != null, "drop secure cipher %s # %s", remote, cipher);
	}

	/**
	 * 通过本地SOCKET，向目标地址发送刷新包
	 * @param sends 发达次数
	 * @param remote 目标GATE节点
	 */
	private void hello(int sends, SocketHost remote) {
		Node hub = getLauncher().getHub();
		// 如果发送地址与HUB站点一致时，忽略它！helo操作由FrontLauncher.FixpPacketMonitor来处理。
		if (Laxkit.compareTo(hub.getPacketHost(), remote) == 0) {
			Logger.warning(this, "hello", "ignored! %s - %s", hub, remote);
			return;
		}

		Node node = getLauncher().getListener().duplicate();

		// 切换UDP端口
		boolean pass = switchTo(remote, node);
		if (!pass) {
			Logger.error(this, "hello", "cannot be find nat! from %s", remote);
			return;
		}

		// 发送数据包
		Mark action = new Mark(Ask.NOTIFY, Ask.HELO);
		Packet packet = new Packet(remote, action);
		packet.addMessage(MessageKey.NODE_ADDRESS, node.build());
		// 发送数据包
		PacketMessenger messenger = getLauncher().getPacketMessenger();
		for (int i = 0; i < sends; i++) {
			boolean success = messenger.notice(packet);
			Logger.debug(this, "hello", success, "send to %s", remote);
		}
	}

	/**
	 * 检查超时
	 */
	private void check() {
		int size = mapSites.size();
		if (size < 1) {
			return;
		}

		TreeSet<SocketHost> disables = new TreeSet<SocketHost>();
		TreeSet<SocketHost> timeouts = new TreeSet<SocketHost>();

		super.lockSingle();
		try {
			Iterator<Map.Entry<SocketHost, GateRabbetSet>> iterator = mapSites.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SocketHost, GateRabbetSet> entry = iterator.next();

				for (GateRabbet e : entry.getValue().list()) {
					if (e.isMaxHubTimeout()) {
						disables.add(entry.getKey()); // 达到最大激活时间
					} else if (e.isHubTimeout()) {
						timeouts.add(entry.getKey()); // 达到激活时间
					}
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
			hello(1, hub);
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


}