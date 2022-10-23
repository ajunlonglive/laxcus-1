/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch.hub;

import java.util.*;

import com.laxcus.command.site.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.fixp.*;
import com.laxcus.fixp.monitor.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;
import com.laxcus.remote.client.hit.*;
import com.laxcus.remote.client.hub.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;


/**
 * 管理站点监视器。<br><br>
 * 
 * 监视器由监视站点启动。负责监视“管理站点”的工作状态，发现故障做出反馈。<br><br>
 * 
 * 对集群的管理站点（TOP/HOME/BANK）的部署规定：<br>
 * 运行环境中的管理站点必须有三个，一个是管理站点，负责集群的实际业务，另外两个是监视站点，监视和备份管理站点产生的运行数据。三个站点之间用集线器连接，不能跨网段部署，这样做的原因避免因为网段问题造成可能的误判。<br><br>
 * 
 * 监视站点对管理站点的诊断： <br>
 * 监视器注册到管理站点上，定时向管理站点发送HELO激活包，保持通信激活。发生超时，做以下三种判断：<br>
 * 1. 一般超时（超时的2倍），连续多发3个HELO包。这种现象可能是由于通信太忙，或者网络丢包问题造成。<br>
 * 2. 最大超时（是超时时间的3倍），同上处理。<br>
 * 3. 失效性超时（是超时时间的5倍），启动监视站点的协商机制。要求必须是两个监视站点可以相互通信，只有一个监视站点不可以协商。<br>
 * 
 * 
 * @author scott.liang
 * @version 1.3 11/23/2019
 * @since laxcus 1.0
 */
public abstract class HubMonitor extends SitePrecursor {

	/** 管理站点启动器 **/
	private HubLauncher launcher;

	/** 异步命令管理池 **/
	private CommandPool commandPool;

	/**
	 * 构造管理站点监视器。
	 */
	protected HubMonitor() {
		super();
	}

	/**
	 * 设置管理站点启动器
	 * @param e 管理站点启动器实例
	 */
	public void setHubLauncher(HubLauncher e) {
		launcher = e;
	}

	/**
	 * 返回管理站点启动器
	 * @return HubLauncher子类实例
	 */
	public HubLauncher getHubLauncher() {
		return launcher;
	}

	/**
	 * 返回监视站点类型，见SiteTag定义
	 * @return 站点类型
	 */
	public byte getFamily() {
		return launcher.getFamily();
	}

	/**
	 * 返回管理节点地址
	 * @return Node实例或者空指针
	 */
	public Node getManager() {
		return launcher.getManager();
	}

	/**
	 * 返回本地站点监听地址
	 * @return Node实例
	 */
	public Node getLocal() {
		return launcher.getListener();
	}

	/**
	 * 设置命令管理池
	 * @param e CommandPool子类实例
	 */
	public void setCommandPool(CommandPool e) {
		commandPool = e;
	}

	/**
	 * 返回命令管理池
	 * @return CommandPool子类实例
	 */
	public CommandPool getCommandPool() {
		return commandPool;
	}

	/**
	 * 建立客户端
	 * @return
	 */
	private HubClient createHubClient() {
		Node hub = getManager();
		HubClient client = ClientCreator.createHubClient(hub);
		if (client == null) {
			Logger.error(this, "createHubClient", "cannot find %s", hub);
		}
		return client;
	}

	/**
	 * 连接HitVisit服务器 
	 * @param hub 目标地址
	 * @return 返回连接句柄，或者空指针
	 */
	protected HitClient fetchHitClient(Node hub) {
		SocketHost endpoint = hub.getStreamHost();
		HitClient client = ClientCreator.create(HitClient.class, endpoint);
		if (client == null) {
			Logger.error(this, "fetchHitClient", "cannot find %s", hub);
		}
		return client;
	}

	/**
	 * 备份站点注册到管理站点
	 * @param site 站点配置
	 * @return 注册成功返回“真”，否则“假”。
	 */
	protected boolean login(Site site) {
		HubClient client = createHubClient();
		if (client == null) {
			Logger.error(this, "login", "cannot be fetch %s", getManager());
			return false;
		}

		boolean success = false;
		try {
			// 1. 判断超时时间
			long ms = client.getSiteTimeout(getFamily()); // 超时时间
			success = (ms > 0);
			if (success) {
				setSiteTimeoutMillis(ms);
			}
			// 2. 检查版本
			if (success) {
				Version other = client.getVersion();
				success = (Laxkit.compareTo(launcher.getVersion(), other) == 0);
				Logger.note(this, "login", success, "check version  \"%s\" - \"%s\"", launcher.getVersion(), other);
			}
			// 3. 注册
			if (success) {
				success = false;
				success = client.login(site);
			}
			// 关闭
			client.close();
		} catch (VisitException e) {
			Logger.error(e);
		}
		client.destroy();
		
		// 注册成功，删除本地保存的服务器密钥，全部重新开始！
		if (success) {
			Node hub = getManager();
			launcher.removeCipher(hub);
		}

		// 刷新时间
		if (success) {
			refreshEndTime();
		}
		// 修改注册状态
		setLogined(success);

		Logger.note(this, "login", success, "from %s", getManager());

		return success;
	}

//	/**
//	 * 从管理站点注销。
//	 * @return 注册成功返回真，否则假
//	 */
//	protected boolean logout() {
//		HubClient client = createHubClient();
//		if (client == null) {
//			return false;
//		}
//
//		Node local = launcher.getListener();
//		// 注销
//		boolean success = false;
////		boolean exit = false;
//		try {
//			success = client.logout(local);
//			// 关闭socket
//			client.close();
////			exit = true;
//		} catch (VisitException e) {
//			Logger.error(e);
//		}
////		// 关闭和销毁
////		client.close();
//		
//		// 销毁socket，防止上述没有执行时
//		client.destroy();
//		// 退出HELO连接
//		if (success) {
//			// unhello();
//			launcher.cancel(local);
//		}
//
//		Logger.note(this, "logout", success, "%s to %s", local, getManager());
//
//		return success;
//	}

	/**
	 * 从管理站点注销。
	 * @return 注册成功返回真，否则假
	 */
	protected boolean logout() {
		HubClient client = createHubClient();
		if (client == null) {
			return false;
		}

		Node local = launcher.getListener();
		// 注销
		boolean success = false;
		try {
			success = client.logout(local);
			// 柔性关闭socket
			client.close();
		} catch (VisitException e) {
			Logger.error(e);
		}
		
		// 销毁socket，防止上述没有执行时
		client.destroy();
		

		Logger.note(this, "logout", success, "%s from %s", local, getManager());

		return success;
	}
	
	/**
	 * 通过站点启动器发送数据包
	 * @param packet 数据包
	 */
	protected void sendPacket(Packet packet) {
		PacketMessenger messenger = launcher.getPacketMessenger();
		messenger.notice(packet);
	}


	/**
	 * 向管理站点发送“HELO”激活数据包
	 */
	protected void hello() {
		Node hub = getManager();
		SocketHost endpoint = hub.getPacketHost();
		Node local = launcher.getListener();
		// 发送数据包
		Mark cmd = new Mark(Ask.NOTIFY, Ask.HELO);
		Packet packet = new Packet(endpoint, cmd);
		packet.addMessage(MessageKey.NODE_ADDRESS, local.build());
		sendPacket(packet);

		Logger.debug(this, "hello", "send to '%s'", hub);
	}

	/**
	 * 注册到目标地址
	 * @return 注册成功返回真，否则假
	 */
	protected boolean login() {
		Site site = createSite();
		return login(site);
	}

	/**
	 * 连接管理站点，判断它已经失效
	 * @return 失效返回“真”，否则“假”。
	 */
	private boolean isManagerDisabled() {
		Node hub = getManager();
		// 发送FIXP.TEST命令
		return !launcher.ring(hub);
	}

	/**
	 * 向其它监视器发出询问，判断管理站点是否已经失败。
	 * @param manager 发生故障的节点
	 * @param monitors  监视器站点
	 * @return 返回统计成功数目
	 */
	private int askManagerDisabled(Node manager, List<Node> monitors) {
		int count = 0;
		for (Node monitor : monitors) {
			// 连接到其它监视站点
			HitClient client = fetchHitClient(monitor);
			if (client == null) {
				continue;
			}
			// 判断管理站点失效
			boolean success = false;
			try {
				success = client.isManagerDisabled(manager);
				client.close();
			} catch (VisitException e) {
				Logger.error(e);
			}
			// 销毁
			client.destroy();
			// 成功，统计值增1
			if (success) {
				count++;
			}
		}
		// 判断诊断结果
		Logger.debug(this, "askManagerDisabled", 
				"check manager %s, check monitor site:%d, reply count:%d", 
				manager, monitors.size(), count);
		return count;
	}

	/**
	 * 测试与子节点的连接，任何一个有效就是网络是连通的。
	 * @return 返回真或者假
	 */
	private boolean askSlaveSites() {
		List<Node> slaves = launcher.getSlaveSites();

		// 最少10个
		int size = (slaves.size() <= 10 ? slaves.size() : 10);
		// 统计成功数目
		int count = 0;

		for (int index = 0; index < size; index++) {
			Node node = slaves.get(index);
			// 忽略同类型枢纽节点，包括TOP/HOME/BANK
			if (getFamily() == node.getFamily()) {
				continue;
			}

			// RING一个节点地址，判断网络通信正常，包括UDP/TCP两种模式
			boolean success = launcher.ring(node);
			Logger.note(this, "askSlaveSites", success, "check slave site %s", node);
			if (success) {
				count++;
			}
		}

		boolean success = (count > 0);
		Logger.note(this, "askSlaveSites", success, "check slave site: %d - %d", count, size);
		return success;
	}

	/**
	 * 与其它站点协商，选举自己做为新的管理站点
	 * @param monitors 其它监视站点
	 * @return 成功返回“真”，否则“假”。
	 */
	private boolean discuss(List<Node> monitors) {
		// 本地地址
		Node local = launcher.getListener();
		int count = 0;
		for (Node monitor : monitors) {
			// 连接其它监视站点
			HitClient client = fetchHitClient(monitor);
			if (client == null) {
				continue;
			}
			// 向另一个MONITOR节点协商，选举自己为新的管理节点
			boolean success = false;
			try {
				success = client.discuss(local);
				client.close();
			} catch (VisitException e) {
				Logger.error(e);
			}
			client.destroy();
			if (success) {
				count++;
			}
		}
		// 要求全部监视站点必须接受自己，否则退出
		boolean success = (monitors.size() == count);
		Logger.debug(this, "discuss", success, "site size: %d, count: %d", monitors.size(), count);
		return success;
	}

	/**
	 * 在管理站点超时后，与其它监视站点协商，确定管理站点故障，然后选举一个新的管理站点
	 * @return 当前站点升级为管理站点，返回真，否则假。
	 */
	private boolean voting() {
		Logger.debug(this, "voting", "into...");

		// 系统规定的监视站点
		List<Node> monitors = launcher.getMonitorSites();
		// 管理站点
		Node manager = launcher.getManager();
		// 删除管理站点，剩下的是监视站点
		if (manager != null) {
			monitors.remove(manager);
		}

		Logger.debug(this, "voting", "begin! check manager %s, check monitors size: %d", 
				manager, monitors.size());

		// 必须有其它监视站点，否则无法协商
		boolean success = (monitors.size() > 0);
		if (success) {
			// 连接管理站点，判断它已经失效！
			success = isManagerDisabled();
			if (success) {
				// 询问其它监视器站点，判断管理站点失效，返回有效值
				int count = askManagerDisabled(manager, monitors);
				// 统计值大于0，协商产生一个新的管理节点；否则连接子节点，证明网络连通，自己成为管理节点
				if (count > 0) {
					success = discuss(monitors);
				} else {
					success = askSlaveSites(); 
				}
			}
		} else {
			// 尝试连接其他站点，判断网络连通，自己成为管理节点！
			success = askSlaveSites();
		}

		Logger.debug(this, "voting", success, "end! check manager %s, check monitors size: %d", 
				manager, monitors.size());

		// 以上不成功，退出。注意！这时的退出不是错误！
		if (!success) {
			return false;
		}

		// 转入管理站点，通知下属站点重新注册
		Node local = launcher.getListener();
		// 清除管理站点，自己成为新的管理站点
		launcher.setManager(null);
		
		// 通知同级别的监视站点重新注册到自己
		for (Node partner : monitors) {
			SwitchPartner cmd = new SwitchPartner(local);
			ShiftSwitchPartner shift = new ShiftSwitchPartner(cmd, partner);
			commandPool.admit(shift);
		}

		// 通知下属站点注册到自己
		List<Node> slaves = launcher.getSlaveSites();
		for (Node slave : slaves) {
			// 忽略同类型枢纽节点（管理节点）
			if (getFamily() == slave.getFamily()) {
				continue;
			}

			SwitchHub cmd = new SwitchHub(local);
			// 分发切换站点命令
			ShiftSwitchHub shift = new ShiftSwitchHub(cmd, slave);
			// 加入到命令集合，再去分发
			commandPool.admit(shift);
		}

		return true;
	}

	/**
	 * 默认的激活
	 */
	protected void defaultHello() {
		int size = 1; // 发送次数默认是1个
		if (isHurry()) { // 注册站点要求召回
			size = 3;
			unhurry();
		} else if (isMaxTimeout()) { // 最大超时
			size = 3;
		} else if (isMinTimeout()) { // 一般性超时
			size = 2;
		}
		for (int index = 0; index < size; index++) {
			hello();
		}
	}

	/**
	 * 失效延时。
	 * 如果收到中断通知退出！
	 * @param timeout 超时时间
	 */
	private void disableDelay(long timeout) {
		// 确定最后时间，循环判断！
		long lastTime = System.currentTimeMillis() + timeout;
		while (System.currentTimeMillis() < lastTime) {
			// 得到外部其它通知：1. 切换管理节点，2. 进入自循环状态。
			// 发生两种可能，退出循环等待，让SiteLauncher.silent方法自循环
			boolean success = (isSwitchHub() || (isLogout() && isRoundSuspend()));
			if (success) break;
			// 延时500毫秒
			delay(500);
		}
	}

	/**
	 * 默认处理线程
	 */
	protected void defaultProcess() {
		Logger.info(this, "defaultProcess", "into...");

		boolean logined = login();
		if (logined) {
			hello();
		} else {
			launcher.stop();
		}

		long endtime = refreshEndTime();

		while (!isInterrupted()) {
			if (isSwitchHub()) {
				if (isSwitchActive()) { // 如果是第一阶段，转入第二阶段
					endtime = 0L;
					setSwitchState(SitePrecursor.SWITCH_LAUNCH);
				}
				delay(1000);
			} else if (isKiss()) {
				// 删除服务器上的密文
				// boolean success = dropSecure();

				// 删除服务器上的密文
				boolean success = launcher.dropSecure();
				// 注册到指定站点
				if (success) {
					success = login();
				}
				setKiss(!success);
				if (success) {
					hello(); // 重新发送HELO
					endtime = nextTouchTime();
				} else {
					disableDelay(getDisableTimeout());
				}
			} else if (isTouchTimeout(endtime) || isHurry()) {
				endtime = nextTouchTime(); // 下次触发时间
				if (isDisableTimeout()) { // 达到失效超时
					// 启动协商机制
					boolean exit = voting();
					Logger.debug(this, "defaultProcess", "voting is %s", exit);
					if (exit) {
						setInterrupted(true); // 退出
						logined = false;
					}
				} else {
					defaultHello();
				}
			} else {
				resting(endtime);
			}
		}

		// 关闭
		if (logined) {
			logout();
		}

		Logger.info(this, "defaultProcess", "exit");
	}

	/**
	 * 切换注册站点
	 * @param manager 新的管理节点地址
	 * @return 成功返回真，否则假
	 */
	public boolean switchHub(Node manager) {
		// 判断原来的管理地址。如果是空值，不处理
		Node origin = launcher.getManager();
		if (origin == null) {
			return false;
		}

		// 设置激活状态
		setSwitchState(SitePrecursor.SWITCH_ACTIVE);

		// 示停止命令钩子，对应“dropSecure”方法
		launcher.getPacketHelper().stopDropSecureHook(origin.getPacketHost());

		// 等待进入"LAUNCH"状态
		while (!isSwitchLaunch()) {
			delay(1000);
		}

		// 注销
		boolean success = logout();
		
//		// 不成功时，撤销密钥
//		if (!success) {
//			launcher.cancel(origin);
//		}

		// 设置新的管理站点
		launcher.setManager(manager);
		// 注册到新的站点
		success = login();
		// 不成功，恢复原来的站点
		if (success) {
			setKiss(false);
		} else {
			launcher.setManager(origin);
		}
		
		setSwitchState(SitePrecursor.SWITCH_NONE); // 恢复原来的状态
		return success;
	}

	/**
	 * 建立本地站点
	 * @return Site子类实例
	 */
	protected abstract Site createSite();
}