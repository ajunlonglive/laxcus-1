/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home;

import java.io.*;
import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.home.pool.*;
import com.laxcus.launch.*;
import com.laxcus.launch.hub.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.pool.archive.*;
import com.laxcus.pool.schedule.*;
import com.laxcus.site.*;
import com.laxcus.site.home.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.impl.home.*;
import com.laxcus.xml.*;

/**
 * HOME节点启动器 <br>
 * 
 * 依据对中心站点的设计运行，保存下属工作站点的元数据，对工作站点进行组织协调分配。
 * 
 * @author scott.liang
 * @version 1.1 2/28/2012
 * @since laxcus 1.0
 */
public final class HomeLauncher extends HubLauncher {

	/** HOME节点句柄 **/
	private static HomeLauncher selfHandle = new HomeLauncher();

	/** TOP节点地址 **/
	private Node hub;

	/** 本地节点绑定地址 **/
	private HomeSite local = new HomeSite();

	/**
	 * 构造HOME节点启动器
	 */
	private HomeLauncher() {
		super();
		setExitVM(true);
		setPrintFault(true);
		// HOME节点监听
		setStreamInvoker(new HomeStreamAdapter());
		setPacketInvoker(new HomePacketAdapter());
	}

	/**
	 * 返回HOME节点静态句柄
	 * @return HomeLauncher实例
	 */
	public static HomeLauncher getInstance() {
		return HomeLauncher.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getPublicListener()
	 */
	@Override
	public Node getPublicListener() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public CommandPool getCommandPool() {
		return HomeCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public InvokerPool getInvokerPool() {
		return HomeInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return HomeCustomTrustor.getInstance();
	}

	/**
	 * 判断是注册站点地址
	 * @param e 节点实例
	 * @return 返回真或者假
	 */
	public boolean isHub(Node e) {
		return Laxkit.compareTo(hub, e) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getHub()
	 */
	@Override
	public Node getHub() {
		return hub;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#setHub(com.laxcus.site.Node)
	 */
	@Override
	public void setHub(Node e) {
		Laxkit.nullabled(e);

		hub = e;
	}

	/**
	 * 返回TOP节点主机地址
	 * @return SiteHost实例
	 */
	public SiteHost getHubHost() {
		return hub.getHost();
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
	 * 启动工作节点管理池服务
	 * @return
	 */
	private boolean loadPool() {
		Logger.info(this, "loadPool", "into...");

		VirtualPool[] pools = new VirtualPool[] {
				// 串行工作管理池
				SerialSchedulePool.getInstance(),
				// INVOKE/PRODUCE管理池
				HomeInvokerPool.getInstance(),
				HomeCommandPool.getInstance(),
				// ARCHIVE站点管理池
				AccountOnCommonPool.getInstance(),
				// 站点管理池
				LogOnHomePool.getInstance(), DataOnHomePool.getInstance(),
				CallOnHomePool.getInstance(), BuildOnHomePool.getInstance(),
				WorkOnHomePool.getInstance(), WatchOnHomePool.getInstance(),
				// 监视站点管理池
				MonitorOnHomePool.getInstance(),
				// 测试在线用户与资源的关联
				ScanLinkOnHomePool.getInstance(),
				// COMMAND/INVOKER配置资源
				CustomClassPool.getInstance()};
		// 启动全部管理池
		return startAllPools(pools);
	}

	/**
	 * 停止管理池
	 */
	private void stopPool() {
		Logger.info(this, "stopPool", "into...");
		VirtualPool[] pools = new VirtualPool[] {
				// COMMAND/INVOKER配置资源
				CustomClassPool.getInstance(),
				// 测试在线用户与资源的关联
				ScanLinkOnHomePool.getInstance(),
				// INVOKE/PRODUCE管理池
				HomeCommandPool.getInstance(),
				HomeInvokerPool.getInstance(),
				// 监视站点管理池
				MonitorOnHomePool.getInstance(),

				// 工作站点管理池
				WatchOnHomePool.getInstance(),BuildOnHomePool.getInstance(), WorkOnHomePool.getInstance(),
				CallOnHomePool.getInstance(), DataOnHomePool.getInstance(),
				LogOnHomePool.getInstance(),
				// ARCHIVE站点管理池
				AccountOnCommonPool.getInstance(),
				// 串行工作管理池
				SerialSchedulePool.getInstance() };
		// 关闭全部
		stopAllPools(pools);
	}

	/**
	 * 停止本地日志服务
	 */
	private void stopLog() {
		Logger.stopService();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		//1. 预加载操作（从TOP站点获得系统参数，日志保存在本地）
		boolean success = preload(hub);
		Logger.note(this, "init", success, "preload");
		// 3. 启动FIXP服务器
		if (success) {
			Class<?>[] clazzs = { HitVisitOnHome.class, HubVisitOnHome.class, CommandVisitOnHome.class };
			success = loadSingleListen(clazzs, local.getNode());
		}
		Logger.note(this, "init", success, "load fixp listen");
		// 4. 启动管理池
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", success, "load pool");
		// 5. 检测“local.xml”列表中指定的监视站点，其中任何一个站点成为“管理站点”，当前站点即为“监视站点”。
		if (success) {
			// 发现管理站点
			Node manager = super.consult();
			// 设置管理站点地址
			setManager(manager);
		}
		Logger.note(this, "init", success, "i am '%s'", (isManager() ? "run site" : "backup site"));
		// 6. 注册到服务器
		if (success) {
			success = login();
		}
		Logger.note(this, "init", success, "login");

		// 不成功取消操作
		if (!success) {
			// 停止全部管理池
			stopPool();
			// 停止FIXP监听
			stopListen();
			// 停止日志服务
			stopLog();
		}

		return success;
	}

	/**
	 * 重置HOME站点参数
	 */
	private Site reset() {
		List<Refer> array = StaffOnHomePool.getInstance().getRefers();

		Logger.debug(this, "reset", "refer size: %d", array.size());

		super.lockSingle();
		try {
			// 清除旧的记录
			local.reset();
			// 设置管理站点标识
			local.setManager(isManager());
			// 只有管理站点才设置参数
			if (local.isManager()) {
				for(Refer refer : array) {
					// 建立账号签名
					Siger siger = refer.getUsername();
					local.create(siger);
					// 建立表空间
					for (Space space : refer.getTables()) {
						Logger.debug(this, "reset", "add table:%s", space);
						local.addSpace(siger, space);
					}
				}
			}

			Logger.debug(this, "reset", "member size: %d", local.size());
			Logger.debug(this, "reset", "space size: %d", local.getSpaces().size());

			return local.duplicate();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return null;
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
		
		// 生成参数重置后的副本
		Site site = reset();
		boolean success = (site != null);
		if (success) {
			success = login(site, hub);
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
		logout();
		// 停止全部管理池
		stopPool();
		// 停止FIXP监听(必须在全部管理池停止之后)
		stopListen();
		// 停止日志服务
		stopLog();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		// 当前站点是管理站点，与TOP保持握手；否则，监视运行的HOME站点
		while (!super.isInterrupted()) {
			if (isManager()) {
				lookout();
			} else {
				lookin();
			}
		}

		Logger.info(this, "process", "exit");
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#disableProcess()
	 */
	@Override
	protected void disableProcess() {

	}

	/**
	 * HOME站点进入“管理站点”状态时，与TOP保持定时激活状态
	 */
	private void lookout() {
		// 启动资源管理线程
		StaffOnHomePool.getInstance().start();

		// 与TOP保持激活，直到要求站点退出
		super.defaultProcess();

		// 停止资源管理线程
		StaffOnHomePool.getInstance().stop();
		while(StaffOnHomePool.getInstance().isRunning()) {
			delay(200);
		}
	}

	/**
	 * HOME站点进入“监视站点”状态
	 */
	private void lookin() {
		// 启动备份管理池
		HomeMonitor.getInstance().setHubLauncher(this);
		HomeMonitor.getInstance().setCommandPool(HomeCommandPool.getInstance());
		HomeMonitor.getInstance().start();

		// 与TOP保持激活
		long endtime = refreshEndTime();
		hello();

		// 定时2分钟检查一次
		final long scheduleTimeout = 2 * 60 * 1000L;

		// 在与TOP保持激活的同时，判断HOME监视器退出
		while (!isInterrupted()) {
			if (HomeMonitor.getInstance().isInterrupted()) {
				break;
			}

			// 达到延时注册时间后，或者要求重新注册时，注册到上级站点
			if (registerTimer.isTouch() || isCheckin()) {
				// 恢复重新注册为假
				setCheckin(false);
				// 刷新
				registerTimer.refresh();

				// 监视器在登录状态时，才可以注册
				if (HomeMonitor.getInstance().isLogined()) {
					this.register();
				}

				//				// 必须确认在登录状态，才能重新注册；否则，启动FixpStreamClient注册！
				//				if (isLogined()) {
				//					register();
				//				} else {
				//					kiss(false); // 启动FixpStreamClient注册
				//				}
			}

			// 达到超时时间，清除过期任务
			if (timer.isTimeout(scheduleTimeout)) {
				timer.purge();
				timer.refreshTime();
			}

			// 处理子级业务
			defaultSubProcess();

			// 静默延时...
			endtime = silent(endtime);
		}

		// 关闭备份管理池，并且等待它退出
		HomeMonitor.getInstance().stop();
		while (HomeMonitor.getInstance().isRunning()) {
			delay(200);
		}
	}
	
	/**
	 * 加载HOME节点许可证
	 * 
	 * @param remote 远程操作
	 * @return 返回真或者假
	 */
	@Override
	public boolean loadLicence(boolean remote) {
		// 判断签名一致
		int who = checkLicence();
		if (who == Licence.LICENCE_REFUSE) {
			return false;
		} else if (remote && who == Licence.LICENCE_IGNORE) {
			return false;
		}

		// 生成许可证文件，判断许可证存在
		File file = buildLicenceFile();
		if (file == null) {
			// 如果是远程操作，返回假（失败），否则是真（正确）！
			return (remote ? false : true);
		}

		// 取出“sites-limit”对应的XML成员
		org.w3c.dom.Element limit = fatchLimitElement(file, "home");
		boolean success = (limit != null);
		// 设置注册节点限制
		if (success) {
			success = setMaxMember(limit, "data", DataOnHomePool.getInstance());
		}
		if (success) {
			success = setMaxMember(limit, "work", WorkOnHomePool.getInstance());
		}
		if (success) {
			success = setMaxMember(limit, "build", BuildOnHomePool.getInstance());
		}
		if (success) {
			success = setMaxMember(limit, "call", CallOnHomePool.getInstance());
		}

		return success;
	}

	/**
	 * 加载并且设置本地管理配置
	 * @param filename
	 * @return
	 */
	private boolean loadLocal(String filename) {
		filename = ConfigParser.splitPath(filename);
		if (!Laxkit.hasFile(filename)) {
			Logger.error(this, "localLocal", "not found %s", filename);
			return false;
		}
		
		org.w3c.dom.Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			Logger.error(this, "loadLocal", "cannot resolve %s", filename);
			return false;
		}

		// 解析TOP站点服务器地址
		boolean success = splitHubSite(document);
		if (!success) {
			Logger.error(this, "loadLocal", "cannot be find top site");
			return false;
		}
		// 解析HOME节点的本地绑定地址
		success = splitSingleSite(local, document);
		// 解析和设置回显配置
		if (success) {
			success = splitEcho(document);
		}
		// 解析并且设置FIXP监视器安全配置
		if (success) {
			success = loadSecure(document);
		}
		// 解析自定义资源
		if (success) {
			success = loadCustom(document);
		}
		// 解析备用HOME节点，它备份和监视HOME运行节点。
		if (success) {
			success = loadMonitor(document);
			if (!success) Logger.error(this, "loadLocal", "cannot load monitor");
		}
		
		// 加载许可证，本地执行
		if (success) {
			success = loadLicence(false);
			if (!success) Logger.error(this, "localLocal", "load licence, failed!");
		}
		
		if (!success) return false;

		// HOME节点资源配置目录
		String value = XMLocal.getXMLValue(document.getElementsByTagName(HomeMark.RESOURCE_DIRECTORY));
		if (!createResourcePath(value)) {
			Logger.error(this, "loadLocal", "cannot create directory %s", value);
			return false;
		}
		// 记录到定时检测中
		addDeviceDirectory(value);

		// WATCH站点账号
		value = XMLocal.getXMLValue(document.getElementsByTagName(HomeMark.WATCH_ACCOUNT));
		if (value != null) {
			WatchOnHomePool.getInstance().setFile(value);
		}

		// 用户资源文件分块数目（将用户数据分成多个文件保存，可以减少写入量，提高写入数据）
		value = XMLocal.getXMLValue(document.getElementsByTagName(HomeMark.SKETCH_BLOCKS));
		int num = ConfigParser.splitInteger(value, 1024);
		StaffOnHomePool.getInstance().setBlocks(num);

		// 定时检查各节点之间的关联
		value = XMLocal.getXMLValue(document.getElementsByTagName(HomeMark.SCAN_LINK_TIME));
		long interval = ConfigParser.splitTime(value, 20 * 60 * 1000); // 默认20分钟
		ScanLinkOnHomePool.getInstance().setSleepTimeMillis(interval);

		// 全部资源管理池
		HubPool[] pools = new HubPool[] { MonitorOnHomePool.getInstance(),
				LogOnHomePool.getInstance(), DataOnHomePool.getInstance(),
				WorkOnHomePool.getInstance(), BuildOnHomePool.getInstance(),
				CallOnHomePool.getInstance() , WatchOnHomePool.getInstance()};

		// 管理池激活时间（休息一个时间后重新触发，检查注册站点超时）
		setSleepTimeout(pools, document);
		// 管理池内的子站点超时时间
		setActiveTimeout(pools, document);
		// 管理池内的子站点超时删除时间
		setDeleteTimeout(pools, document);

		// 解析“least-disk”属性
		if (success) {
			loadTimerTasks(document);
		}

		// 加载Logger/Tigger记录
		if (success) {
			success = loadLogResourceWithRemote(filename);
		}
		
		return success;
	}

	/**
	 * HOME启动接口
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		if (System.getSecurityManager() != null) {
			Logger.info("HomeLauncher.main, sandbox loaded!");
		}
		// 将本地链接库目录导入“java.library.path”队列，加载本地目录下面的库文件
		JNILoader.init();

		String filename = args[0];
		boolean success = HomeLauncher.getInstance().loadLocal(filename);
		Logger.note("HomeLauncher.main, load local", success);
		if (success) {
			success = HomeLauncher.getInstance().start();
			Logger.note("HomeLauncher.main, start service", success);
		}

		if (!success) {
			Logger.gushing();
			System.exit(0);
		}
	}

}