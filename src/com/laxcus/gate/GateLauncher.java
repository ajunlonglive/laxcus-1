/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate;

import com.laxcus.gate.pool.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.gate.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.visit.impl.gate.*;
import com.laxcus.xml.*;

/**
 * GATE站点启动器。<br><br>
 * 
 * GATE站点的工作内容：<br>
 * 1. 注册和管理普通用户身份登录的FRONT站点。<br>
 * 2. 支持多个相同账号不同地址的FRONT站点注册。<br>
 * 3. 给FRONT站点分配属于它的表资源。<br>
 * 4. 对FRONT站点进行事务控制。<br><br>
 * 
 * 特别注意：<br>
 * 1. 所有FRONT站点都登录到GATE站点，不管是管理员还是普通的注册用户！！！<br>
 * 2. 由于接受FRONT登录和内网通信，GATE网络流量巨大，UDP缓存应该保证足够大。
 * 如果请求端发生SOCKET接收超时现象，极大可能是GATE FIXP服务器 UDP缓存空间不足，投递的数据包超过缓存容量丢弃所致！
 * <br>
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public final class GateLauncher extends MemberLauncher {

	/** GATE站点实例 **/
	private static GateLauncher selfHandle = new GateLauncher();

	/** GATE站点绑定地址 **/
	private GateSite local = new GateSite();

	/** 编号 **/
	private int no;

	/** FRONT登录GATE节点的循环查询等待间隔，默认是2秒 **/
	private long lingerTimeout;
	
	/** FRONT成员虚拟空间 **/
	private FrontCyber frontCyber = new FrontCyber();
	
	/**
	 * 构造默认和私有的GATE站点启动器
	 */
	private GateLauncher() {
		super();
		// 退出JVM
		setExitVM(true);
		// 在本地写入日志
		setPrintFault(false);
		// GATE站点监听
		setPacketInvoker(new GatePacketAdapter());
		setStreamInvoker(new GateStreamAdapter());
		// 默认编号-1
		setNo(GateSite.INVALID_NO);
		// FRONT登录GATE节点的循环查询等待间隔，默认5秒
		setLingerTimeout(5000);
	}

	/**
	 * 返回GATE站点静态实例
	 * @return GateLauncher实例
	 */
	public static GateLauncher getInstance() {
		return GateLauncher.selfHandle;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#createMoment()
	 */
	@Override
	public Moment createMoment() {
		Moment moment = super.createMoment();
		MemberCyber m = getMemberCyber();
		moment.setMember(new PersonStamp(m.getPersons(), m.getThreshold(),
				StaffOnGatePool.getInstance().size()));
		FrontCyber f = getFrontCyber();
		moment.setOnline(new PersonStamp(f.getPersons(), f.getThreshold(),
				FrontOnGatePool.getInstance().size()));

		return moment;
	}

	/**
	 * 返回FRONT在线用户虚拟空间
	 * @return FrontCyber实例
	 */
	public final FrontCyber getFrontCyber() {
		return frontCyber;
	}

	/**
	 * 设置FRONT登录GATE节点的循环查询等待间隔，最小1秒钟
	 * @param ms 毫秒
	 */
	public void setLingerTimeout(long ms) {
		if (ms >= 1000) {
			lingerTimeout = ms;
		}
	}

	/**
	 * 返回FRONT登录GATE节点的循环查询等待间隔
	 * @return 毫秒
	 */
	public long getLingerTimeout() {
		return lingerTimeout;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getPublicListener()
	 */
	@Override
	public Node getPublicListener() {
		return local.getPublic();
	}

	/**
	 * 设置编号
	 * @param who
	 */
	public void setNo(int who) {
		Logger.info(this, "setNo", "current site no:%d", who);
		no = who;
	}

	/**
	 * 返回编号
	 * @return
	 */
	public int getNo() {
		return no;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public CommandPool getCommandPool() {
		return GateCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public InvokerPool getInvokerPool() {
		return GateInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return GateCustomTrustor.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getSite()
	 */
	@Override
	public Site getSite() {
		return local;
	}

	/**
	 * 返回内网地址
	 * @return Node实例
	 */
	public com.laxcus.site.Node getPrivate() {
		return local.getPrivate();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 1. 预加载，包括站点超时，全局时间，日志发向日志服务器
		boolean success = preload();
		Logger.note(this, "init", success, "preload");
		// 2. 启动FIXP网关服务
		if (success) {
			Class<?>[] clazzs = { CommandVisitOnGate.class, FrontVisitOnGate.class };
			success = loadGatewayListen(clazzs, local.getPrivate(), local.getPublic());
		}
		Logger.note(this, "init", success, "loadListen");
		// 3. 启动工作站点管理池
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", success, "load pool");
		// 注册到管理站点
		if (success) {
			success = login();
		}
		Logger.note(this, "init", success, "login");

		// 出错，停止服务
		if (!success) {
			stopPool();
			stopListen();
			stopLog();
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into ...");
		// 启动资源管理池
		StaffOnGatePool.getInstance().start();
		// 站点循环
		super.defaultProcess();

		// 关闭
		StaffOnGatePool.getInstance().stop();
		while(StaffOnGatePool.getInstance().isRunning()) {
			delay(100);
		}

		Logger.info(this, "process", "exit ...");
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#login()
	 */
	@Override
	public boolean login() {
//		// 如果是登录状态，先注销
//		if (isLogined()) {
//			logout();
//		}
		
		Site site = reset(); // 更新阶段命名
		boolean success = (site != null);
		if (success) {
			success = super.login(site);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#register()
	 */
	@Override
	protected void register() {
		Site site = reset();
		register(site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 注销
		super.logout();
		// 停止资源配置池
		stopPool();
		// 关闭监听服务
		stopListen();
		// 停止日志服务
		stopLog();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#disableProcess()
	 */
	@Override
	protected void disableProcess() {

	}

	/**
	 * 重新注册参数
	 * @return 返回站点副本
	 */
	private GateSite reset() {
		// FRONT节点在线数目
		int members = StaffOnGatePool.getInstance().size();
		
		Moment moment = createMoment();

		super.lockSingle();
		try {
			local.setMoment(moment);
			local.setNo(getNo());		// 节点编号
			local.setMembers(members);	// 在线用户数目
			return local.duplicate();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 启动管理池服务
	 * @return
	 */
	private boolean loadPool() {
		Logger.info(this, "loadPool", "into...");

		VirtualPool[] pools = new VirtualPool[] { 
				BlackOnGatePool.getInstance(), FaultOnGatePool.getInstance(),
				RuleHouse.getInstance(),
				CallOnGatePool.getInstance(),
				FrontOnGatePool.getInstance(), 
				StayFrontOnGatePool.getInstance(), StayConferrerFrontOnGatePool.getInstance(),
				ConferrerFrontOnGatePool.getInstance(), ConferrerStaffOnGatePool.getInstance(),
				GateInvokerPool.getInstance(), GateCommandPool.getInstance(),
				// 自定义COMMAND/INVOKER资源
				CustomClassPool.getInstance()};
		// 启动全部管理池
		return startAllPools(pools);
	}

	/**
	 * 停止管理池服务
	 */
	private void stopPool() {
		VirtualPool[] pools = new VirtualPool[] {
				// 自定义COMMAND/INVOKER资源
				CustomClassPool.getInstance(),
				// GATE命令管理池
				GateCommandPool.getInstance(),
				// GATE调用器管理池
				GateInvokerPool.getInstance(),
				ConferrerStaffOnGatePool.getInstance(), ConferrerFrontOnGatePool.getInstance(),
				StayConferrerFrontOnGatePool.getInstance(), StayFrontOnGatePool.getInstance(), 
				FrontOnGatePool.getInstance(), 
				CallOnGatePool.getInstance(),
				RuleHouse.getInstance(),
				FaultOnGatePool.getInstance(), BlackOnGatePool.getInstance() };
		// 停止全部管理池
		stopAllPools(pools);
	}

	/**
	 * 定义守护线程时间
	 * @param document
	 */
	private void loadDaemon(org.w3c.dom.Document document) {
		org.w3c.dom.Element element = (org.w3c.dom.Element) document.getElementsByTagName("daemon-time").item(0);

		// FRONT管理池线程延时时间
		String input = XMLocal.getValue(element, "sleep-timeout");
		long ms = ConfigParser.splitTime(input, 5000); // 默认5秒
		FrontOnGatePool.getInstance().setSleepTimeMillis(ms);
		ConferrerFrontOnGatePool.getInstance().setSleepTimeMillis(ms);
		// FRONT站点激活时间
		input = XMLocal.getValue(element, ("site-active-timeout"));
		ms = ConfigParser.splitTime(input, 20000); // 默认是20秒
		FrontOnGatePool.getInstance().setActiveTimeMillis(ms);
		ConferrerFrontOnGatePool.getInstance().setActiveTimeMillis(ms);
		// FRONT站点发生超时到被删除时间(是超时时间的3倍)
		input = XMLocal.getValue(element, ("site-delete-timeout"));
		ms = ConfigParser.splitTime(input, 60000); // 默认是60秒
		FrontOnGatePool.getInstance().setDeleteTimeMillis(ms);
		ConferrerFrontOnGatePool.getInstance().setDeleteTimeMillis(ms);

		// 账号超时
		input = XMLocal.getValue(element, "callsite-check-timeout");
		ms = ConfigParser.splitTime(input, CallOnGatePool.getInstance().getInterval()); // 默认1分钟后，允许用户重新检查自己的资源资源
		CallOnGatePool.getInstance().setInterval(ms);

		// 黑名单中的失效时间
		input = XMLocal.getValue(element, OtherMark.BLACKLIST_DISABLE_TIMEOUT);
		ms = ConfigParser.splitTime(input, 20 * 60 * 1000); // 默认20分钟
		BlackOnGatePool.getInstance().setTimeout(ms);
		FaultOnGatePool.getInstance().setTimeout(ms);
		// 黑名单登录时的最大重试次数
		input = XMLocal.getAttribute(element, OtherMark.BLACKLIST_DISABLE_TIMEOUT, "max-retry");
		int retry = ConfigParser.splitInteger(input, 5);
		BlackOnGatePool.getInstance().setMaxRetry(retry);

		Logger.debug(this, "loadDaemon", "sleep timeout %d", FrontOnGatePool
				.getInstance().getSleepTimeMillis());
		Logger.debug(this, "loadDaemon", "site active timeout %d",
				FrontOnGatePool.getInstance().getActiveTimeMillis());
		Logger.debug(this, "loadDaemon", "site delete timeout %d",
				FrontOnGatePool.getInstance().getDeleteTimeMillis());
		Logger.debug(this, "loadDaemon", "callsite-check-timeout:%d",
				CallOnGatePool.getInstance().getInterval());
		Logger.debug(this, "loadDaemon", "blacklist-disable-timeout:%d ms, max retry:%d, faultlist-disable-timeout:%d ms",
				BlackOnGatePool.getInstance().getTimeout(),
				BlackOnGatePool.getInstance().getMaxRetry(),
				FaultOnGatePool.getInstance().getTimeout());
	}

	/**
	 * 解析本地私有参数
	 * @param document XML文档
	 */
	private void splitPrivate(org.w3c.dom.Document document) {
		// FRONT登录等待过程间隔时间
		org.w3c.dom.Element root = (org.w3c.dom.Element) document.getElementsByTagName(SiteMark.MARK_LOCAL_SITE).item(0);
		String input = XMLocal.getValue(root, SiteMark.LINGER_TIMEOUT);
		long ms = ConfigParser.splitTime(input, getLingerTimeout());
		setLingerTimeout(ms);
		
		// 解析成员限值
		splitMemberCyber(document);
		
		// FRONT在线用户限制
		input = XMLocal.getValue(root, SiteMark.MAX_FRONTS);
		frontCyber.setPersons(ConfigParser.splitInteger(input, frontCyber.getPersons()));
		// 最大阀值
		input = XMLocal.getAttribute(root, SiteMark.MAX_FRONTS, SiteMark.MAX_FRONTS_THRESHOLD);
		frontCyber.setThreshold(ConfigParser.splitRate(input, frontCyber.getThreshold()));
		// 超时时间
		input = XMLocal.getAttribute(root, SiteMark.MAX_FRONTS, SiteMark.MAX_FRONTS_CHECKTIMEOUT);
		frontCyber.setTimeout(ConfigParser.splitTime(input, frontCyber.getTimeout()));

		Logger.debug(this, "splitPrivate", "linger timeout: %d ms", getLingerTimeout());
		Logger.info(this, "splitPrivate", "max fronts: %d, threshold: %.2f, check timeout: %d ms",
				frontCyber.getPersons(), frontCyber.getThreshold(), frontCyber.getTimeout());
	}
	
	/**
	 * 加载许可证
	 * 
	 * @param remote 远程加载许可证
	 * @return 成功返回真，否则假
	 */
	@Override
	public boolean loadLicence(boolean remote) {
		int who = checkLicence();
		if (who == Licence.LICENCE_REFUSE) {
			return false;
		} else if (remote && who == Licence.LICENCE_IGNORE) {
			return false;
		}

		return true;
	}
	
	/**
	 * 加载并且解析GATE站点配置文件
	 * 
	 * @param filename 磁盘文件名
	 * @return 成功返回真，否则假
	 */
	private boolean loadLocal(String filename) {
		filename = ConfigParser.splitPath(filename);
		if (!Laxkit.hasFile(filename)) {
			Logger.error(this, "localLocal", "not found %s", filename);
			return false;
		}
		
		org.w3c.dom.Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			return false;
		}
		
		// 解析本地私有参数
		splitPrivate(document);

		// 解析和设置TOP站点地址
		boolean success = splitHubSite(document);
		// 解析和设置本地站点地址
		if (success) {
			success = splitGatewaySite(local, document);
		}
		// 解析回显参数
		if (success) {
			success = splitEcho(document);
		}
		// 解析停止运行任务监听配置
		if (success) {
			success = loadShutdown(document);
		}
		// 解析FIXP安全通信配置资源
		if (success) {
			success = loadSecure(document);
		}
		// 解析自定义资源
		if (success) {
			success = loadCustom(document);
		}
		// 加载许可证
		if (success) {
			success = loadLicence(false);
		}
		
		// 守护线程时间配置
		loadDaemon(document);

		// 解析“least-disk”属性
		if (success) {
			loadTimerTasks(document);
		}

		// 加载日志/追踪记录
		if (success) {
			success = loadLogResourceWithRemote(filename);
		}

		//		// 加载目录配置
		//		if (success) {
		//			success = Logger.loadXML(filename);
		//			if (success) {
		//				loadLogDeviceDirectory();
		//			}
		//		}
		//		// 加载追踪服务配置
		//		if (success) {
		//			success = Tigger.loadXML(filename);
		//			if (success) {
		//				loadTigDeviceDirectory();
		//			}
		//		}
		
		return success;
	}

	/** 
	 * GATE站点启动器入口
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		if (System.getSecurityManager() != null) {
			Logger.info("GateLauncher.main, sandbox loaded!");
		}
		// 将本地链接库目录导入“java.library.path”队列，加载本地目录下面的库文件
		JNILoader.init();

		// 加载配置文件
		String filename = args[0];
		boolean success = GateLauncher.getInstance().loadLocal(filename);
		Logger.note("GateLauncher.main, load local", success);
		// 启动线程
		if (success) {
			success = GateLauncher.getInstance().start();
			Logger.note("GateLauncher.main, start service", success);
		}
		if (!success) {
			Logger.gushing();
			System.exit(0);
		}
	}
}