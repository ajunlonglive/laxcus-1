/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.entrance;

import org.w3c.dom.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.entrance.pool.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.Node;
import com.laxcus.site.entrance.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.visit.impl.entrance.*;
import com.laxcus.xml.*;

/**
 * ENTRANCE站点启动器。<br><br>
 * 
 * ENTRANCE站点的工作内容：<br>
 * 1. 保存当前站点的全部GATE用户。<br>
 * 2. 判断黑名单用户。<br>
 * 3. 根据用户签名，重向向到指定的GATE主机地址。<br>
 * 
 * 注意：管理员身份的FRONT站点直接登录到TOP站点，不能登录ENTRANCE站点。
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public final class EntranceLauncher extends SlaveLauncher {

	/** ENTRANCE站点实例 **/
	private static EntranceLauncher selfHandle = new EntranceLauncher();

	/** ENTRANCE站点绑定地址 **/
	private EntranceSite local = new EntranceSite();
	
	/** HASH定位GATE站点 **/
	private volatile boolean hash;
	
	/** FRONT登录ENTRANCE节点的循环查询等待间隔，默认是2秒 **/
	private long lingerTimeout;

	/**
	 * 构造默认和私有的ENTRANCE站点启动器
	 */
	private EntranceLauncher() {
		super();
		// 退出JVM
		setExitVM(true);
		// 在本地写入日志
		setPrintFault(false);
		// ENTRANCE站点监听
		setPacketInvoker(new EntrancePacketAdapter());
		setStreamInvoker(new EntranceStreamAdapter());
		
		// 默认是采用HASH算法定位GATE站点
		setHash(true);
		// FRONT登录ENTRANCE节点的循环查询等待间隔
		setLingerTimeout(2000);
	}

	/**
	 * 返回ENTRANCE站点静态实例
	 * @return 启动器实例
	 */
	public static EntranceLauncher getInstance() {
		return EntranceLauncher.selfHandle;
	}

	/**
	 * HASH算法定位
	 * @param b 真或者假
	 */
	public void setHash(boolean b) {
		hash = b;
	}

	/**
	 * 判断是HASH算法定位GATE站点
	 * @return 返回真或者假
	 */
	public boolean isHash() {
		return hash;
	}

	/**
	 * 设置FRONT登录ENTRANCE节点的循环查询等待间隔，最小1秒钟
	 * @param ms 毫秒
	 */
	public void setLingerTimeout(long ms) {
		if (ms >= 1000) {
			lingerTimeout = ms;
		}
	}

	/**
	 * 返回FRONT登录ENTRANCE节点的循环查询等待间隔
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public CommandPool getCommandPool() {
		return EntranceCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public InvokerPool getInvokerPool() {
		return EntranceInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return EntranceCustomTrustor.getInstance();
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
		// 1. 预加载，包括站点超时，全局时间，日志发给服务器
		boolean success = preload();
		Logger.note(this, "init", success, "preload");
		// 2. 启动FIXP网关服务
		if (success) {
			Class<?>[] clazzs = { CommandVisitOnEntrance.class, FrontVisitOnEntrance.class };
			success = loadGatewayListen(clazzs, local.getPrivate(), local.getPublic());
		}
		Logger.note(this, "init", success, "loadListen");
		// 3. 启动工作站点管理池
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", success, "load pool");
		// 注册到BANK站点
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
		StaffOnEntrancePool.getInstance().start();
		
		// 站点循环
		defaultProcess();

		// 关闭
		StaffOnEntrancePool.getInstance().stop();
		while(StaffOnEntrancePool.getInstance().isRunning()) {
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
	private EntranceSite reset() {
		Moment moment = createMoment();

		// 锁定！
		super.lockSingle();
		try {
			local.setMoment(moment);
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

		// VirtualPool[] pools = new VirtualPool[] { LimitPool.getInstance(),
		// ForbidPool.getInstance(), CallOnDirectPool.getInstance(),
		// RulePool.getInstance(), FrontOnDirectPool.getInstance(),
		// ConferrerFrontOnDirectPool.getInstance(),
		// ConferrerStaffOnDirectPool.getInstance(),
		// DirectInvokerPool.getInstance(), DirectCommandPool.getInstance(),
		// // 自定义COMMAND/INVOKER资源
		// CustomClassPool.getInstance()};

		// 管理池
		VirtualPool[] pools = new VirtualPool[] {
				EntranceInvokerPool.getInstance(),
				EntranceCommandPool.getInstance(),
				// FRONT定位管理池
				FrontOnEntrancePool.getInstance(),
				// 自定义COMMAND/INVOKER资源
				CustomClassPool.getInstance() };

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
				// FRONT定位管理池
				FrontOnEntrancePool.getInstance(),
				// ENTRANCE命令管理池
				EntranceCommandPool.getInstance(),
				// ENTRANCE调用器管理池
				EntranceInvokerPool.getInstance(), };
		// 停止全部管理池
		stopAllPools(pools);
	}
	
	/**
	 * 解析本地私有参数
	 * @param document XML文档
	 */
	private void splitPrivate(org.w3c.dom.Document document) {
		// 命令模式 / 命令超时
		org.w3c.dom.Element element = (org.w3c.dom.Element) document.getElementsByTagName(SiteMark.MARK_LOCAL_SITE).item(0);
		String input = XMLocal.getValue(element, SiteMark.LINGER_TIMEOUT);
		long ms = ConfigParser.splitTime(input, getLingerTimeout());
		setLingerTimeout(ms);
		
		Logger.debug(this, "splitPrivate", "linger timeout: %d ms", getLingerTimeout());
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
	 * 加载并且解析ENTRANCE站点配置文件
	 * 
	 * @param filename
	 * @return
	 */
	private boolean loadLocal(String filename) {
		filename = ConfigParser.splitPath(filename);
		if (!Laxkit.hasFile(filename)) {
			Logger.error(this, "localLocal", "not found %s", filename);
			return false;
		}
		
		Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			return false;
		}
		
		// 解析本地私有参数
		splitPrivate(document);

		// 解析和设置BANK站点地址
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
		// 本地执行加载许可证
		if (success) {
			success = loadLicence(false);
		}
		
		if (!success) {
			return false;
		}

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
	 * ENTRANCE站点启动器入口
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		if (System.getSecurityManager() != null) {
			Logger.info("EntranceLauncher.main, sandbox loaded!");
		}
		// 将本地链接库目录导入“java.library.path”队列，加载本地目录下面的库文件
		JNILoader.init();

		// 加载配置文件
		String filename = args[0];
		boolean success = EntranceLauncher.getInstance().loadLocal(filename);
		Logger.note("EntranceLauncher.main, load local", success);
		// 启动线程
		if (success) {
			success = EntranceLauncher.getInstance().start();
			Logger.note("EntranceLauncher.main, start service", success);
		}
		if (!success) {
			Logger.gushing();
			System.exit(0);
		}
	}
}