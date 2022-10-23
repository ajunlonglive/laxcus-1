/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top;

import java.io.*;

import com.laxcus.command.login.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.launch.*;
import com.laxcus.launch.hub.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.pool.schedule.*;
import com.laxcus.site.*;
import com.laxcus.site.top.*;
import com.laxcus.top.effect.*;
import com.laxcus.top.pool.*;
import com.laxcus.top.resource.*;
import com.laxcus.util.*;
import com.laxcus.visit.impl.top.*;
import com.laxcus.xml.*;

/**
 * 只有孤独的行者才能坚持内心的信念，而创造力的本身正是源自对这种信念孤独的追求。
 * 前面是一条坚难的路，我们从这里开始！！！
 */

/**
 * TOP站点启动器 <br>
 * 
 * 依据对中心站点的设计运行，保存整个集群的元数据。
 * 
 * @author scott.liang
 * @version 1.1 2/28/2012
 * @since laxcus 1.0
 */
public final class TopLauncher extends HubLauncher {

	/** TOP站点启动器句柄 **/
	private static TopLauncher selfHandle = new TopLauncher();

	/** TOP站点 **/
	private TopSite site = new TopSite();

	/**
	 * 构造私有的TOP站点启动器
	 */
	private TopLauncher() {
		super();
		setExitVM(true);
		setPrintFault(true);
		// TOP站点监听
		setStreamInvoker(new TopStreamAdapter());
		setPacketInvoker(new TopPacketAdapter());
		// 默认睡眠5秒
		setSleepTime(5);
	}

	/**
	 * 返回TOP站点启动器静态句柄
	 * @return
	 */
	public static TopLauncher getInstance() {
		return TopLauncher.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getPublicListener()
	 */
	@Override
	public Node getPublicListener() {
		return null;
	}

	/**
	 * TOP站点没有上级站点，返回空指针
	 * @see com.laxcus.launch.SiteLauncher#getHub()
	 */
	@Override
	public Node getHub() {
		return null;
	}

	/**
	 * TOP站点没有上级站点，这里是空方法
	 * @see com.laxcus.launch.SiteLauncher#setHub(com.laxcus.site.Node)
	 */
	@Override
	public void setHub(Node e) {

	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getSite()
	 */
	@Override
	public Site getSite() {
		//		TopSite site = new TopSite();
		////		site.setNode(getListener());
		//		site.setManager(isManager());
		//		return site;

		// 动态设置管理模式
		site.setManager(isManager());

		return site;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#login()
	 */
	@Override
	public boolean login() {
		// TOP是顶级节点，不支持这个操作
		return false; 
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#register()
	 */
	@Override
	protected void register() {
		// TOP是顶级节点，不支持这个操作
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public CommandPool getCommandPool() {
		return TopCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public InvokerPool getInvokerPool() {
		return TopInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return TopCustomTrustor.getInstance();
	}

	/**
	 * 启动管理池
	 * @return
	 */
	private boolean loadPool() {
		Logger.info(this, "loadPool", "into...");

		VirtualPool[] pools = new VirtualPool[] { SerialSchedulePool.getInstance(), 
				MonitorOnTopPool.getInstance(), HomeOnTopPool.getInstance(),

				//				OldFrontOnTopPool.getInstance(), OldAidOnTopPool.getInstance(), OldArchiveOnTopPool.getInstance(), 

				// 站点管理池
				LogOnTopPool.getInstance(), BankOnTopPool.getInstance(),
				WatchOnTopPool.getInstance(),
				// 命令/调用器管理池
				TopInvokerPool.getInstance(), TopCommandPool.getInstance(),
				// COMMAND/INVOKER配置资源
				CustomClassPool.getInstance()};
		// 启动全部管理池
		return startAllPools(pools);
	}

	/**
	 * 停止全部管理池
	 */
	private void stopPool() {
		Logger.info(this, "stopPool", "into...");
		VirtualPool[] pools = new VirtualPool[] {
				// COMMAND/INVOKER自定义配置
				CustomClassPool.getInstance(),
				// TOP命令管理池
				TopCommandPool.getInstance(),
				// TOP注册站点管理池
				TopInvokerPool.getInstance(), WatchOnTopPool.getInstance(),
				HomeOnTopPool.getInstance(),

				//				OldFrontOnTopPool.getInstance(), OldAidOnTopPool.getInstance(), OldArchiveOnTopPool.getInstance(),

				// 站点管理池
				LogOnTopPool.getInstance(), BankOnTopPool.getInstance(), 

				MonitorOnTopPool.getInstance(),
				SerialSchedulePool.getInstance() };
		stopAllPools(pools);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 关闭管理池
		stopPool();
		// 关闭FIXP监听
		stopListen();
		// 关闭日志服务
		stopLog();
		// 释放数据资源
		clearResource();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 1. 加载日志服务/追踪
		boolean success = Logger.loadService(null);
		Logger.note(this, "init", "load log service", success);
		if (success) {
			success = Tigger.loadService(null);
		}
		Logger.note(this, "init", "load tig service", success);
		if (success) {
			success = Biller.loadService(null);
		}
		Logger.note(this, "init", "load bill service", success);

		// 2. 启动FIXP监听服务
		if (success) {
			Class<?>[] clazzes = { HubVisitOnTop.class, HitVisitOnTop.class,
					CommandVisitOnTop.class };
			success = loadSingleListen(clazzes, site.getNode());
		}
		Logger.note(this, "init", "load listen", success);
		// 3. 加载管理池
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", "load pool", success);
		// 4. 选择是管理站点或者备份站点
		if (success) {
			Node manager = consult();
			setManager(manager);
		}
		Logger.info(this, "init", "i am \"%s\"", 
				(isManager() ? "Manager Site" : "Monitor Site"));

		// 以上不成功时，关闭
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
		Logger.info(this, "process", "into...");

		while (!isInterrupted()) {
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
	 * TOP站点进入“管理站点（MASTER）”状态。<br>
	 * 管理站点状态关注外部处理，执行LAXCUS集群管理工作。
	 */
	private void lookout() {
		Logger.info(this, "lookout", "into running...");

		// 首先清除资源
		clearResource();
		// 加载本地资源（如果发生站点切换，将更新全部资源）
		loadResource();

		//		// 启动资源管理池
		//		OldStaffOnTopPool.getInstance().start();

		// 循环
		while (!isInterrupted()) {
			// 睡眠
			sleep();
		}

		//		// 停止资源管理
		//		OldStaffOnTopPool.getInstance().stop();
		//		while (OldStaffOnTopPool.getInstance().isRunning()) {
		//			delay(200);
		//		}
	}

//	/**
//	 * TOP站点进入“监视器（MONITOR）”状态。<br>
//	 * 监视器状态关注内部处理，判断MASTER TOP站点故障，随时替换它。
//	 */
//	private void lookin() {
//		Logger.info(this, "lookin", "into running...");
//
//		// 监视器
//		TopMonitor.getInstance().setHubLauncher(this);
//		TopMonitor.getInstance().setCommandPool(TopCommandPool.getInstance());
//		boolean success = TopMonitor.getInstance().start();
//
//		// 进入监视器值守状态。监视器和启动器任何一个中断，都退出！
//		if (success) {
//			while (!isInterrupted()) {
//				// 站点启动器进程退出
//				if (TopMonitor.getInstance().isInterrupted()) {
//					break;
//				}
//				// 睡眠等待
//				sleep();
//			}
//		}
//
//		// 关闭和等待线程退出
//		TopMonitor.getInstance().stop();
//		while (TopMonitor.getInstance().isRunning()) {
//			delay(200);
//		}
//	}
	
	private boolean registerTo() {
		Node node = getManager();
		if (node == null) {
			return false;
		}

		Site myself = site.duplicate();
		
		LoginSite cmd = new LoginSite(myself);
		cmd.setReply(true); 	// 需要反馈应答
		cmd.setFast(true); 		// 注册是最高优先级，受理服务器马上处理。

		LoginSiteHook hook = new LoginSiteHook();
		ShiftLoginSite shift = new ShiftLoginSite(cmd, hook);
		return getCommandPool().admit(shift);
	}

	/**
	 * TOP站点进入“监视器（MONITOR）”状态。<br>
	 * 监视器状态关注内部处理，判断MASTER TOP站点故障，随时替换它。
	 */
	private void lookin() {
		Logger.info(this, "lookin", "into running...");

		// 监视器
		TopMonitor.getInstance().setHubLauncher(this);
		TopMonitor.getInstance().setCommandPool(TopCommandPool.getInstance());
		boolean success = TopMonitor.getInstance().start();

		// 进入监视器值守状态。监视器和启动器任何一个中断，都退出！
		if (success) {
			// 定时2分钟检查一次
			final long scheduleTimeout = 2 * 60 * 1000L;

			while (!isInterrupted()) {
				// 站点启动器进程退出
				if (TopMonitor.getInstance().isInterrupted()) {
					break;
				}
				
				// 达到延时注册时间后，或者要求重新注册时，注册到上级站点
				if (registerTimer.isTouch() || isCheckin()) {
					// 恢复重新注册为假
					setCheckin(false);
					// 刷新
					registerTimer.refresh();
					
					// 确认在登录状态，启动注册；不在登录状态，TopMonitor会去判断和处理，不在这里考虑
					if (TopMonitor.getInstance().isLogined()) {
						registerTo();
					}
				}

				// 达到超时时间，清除过期任务
				if (timer.isTimeout(scheduleTimeout)) {
					timer.purge();
					timer.refreshTime();
				}

				// 处理子级业务
				defaultSubProcess();
				
				// 睡眠等待
				sleep();
			}
		}

		// 关闭和等待线程退出
		TopMonitor.getInstance().stop();
		while (TopMonitor.getInstance().isRunning()) {
			delay(200);
		}
	}
	
	/**
	 * 关闭日志服务
	 */
	private void stopLog() {
		Logger.stopService();
	}

	/**
	 * 加载许可证
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

		// 生成许可证文件，判断文件存在
		File file = buildLicenceFile();
		if (file == null) {
			// 如果是远程操作，返回假（失败），否则是真（正确）！
			return (remote ? false : true);
		}

		// 取出“subsites-limit-configure”对应的XML成员
		org.w3c.dom.Element limit = fatchLimitElement(file, "top");
		boolean success = (limit != null);
		// 设置注册节点限制
		if (success) {
			success = setMaxMember(limit, "bank", BankOnTopPool.getInstance());
		}
		if (success) {
			success = setMaxMember(limit, "home", HomeOnTopPool.getInstance());
		}

		return success;
	}

	/**
	 * 加载并且解析TOP站点本地配置
	 * @param filename 本地配置文件名
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
			Logger.error(this, "loadLocal", "cannot resolve %s", filename);
			return false;
		}

		// TOP站点的FIXP服务器监听地址
		boolean success = splitSingleSite(site, document);
		if (!success) {
			Logger.error(this, "loadLocal", "cannot resolve local address");
			return false;
		}

		Logger.info(this, "loadLocal", "local site is '%s'", site);

		// 解析回显配置
		success = splitEcho(document);
		if (!success) {
			Logger.error(this, "loadLocal", "cannot resolve echo");
			return false;
		}

		// 后备监视TOP站点地址
		if (!loadMonitor(document)) {
			Logger.error(this, "loadLocal", "cannot resolve backup address!");
			return false;
		}


		// TOP站点配置目录
		String value = XMLocal.getXMLValue(document.getElementsByTagName(TopMark.RESOURCE_DIRECTORY));
		if (!createResourcePath(value)) {
			Logger.error(this, "loadLocal", "cannot create path %s", value);
			return false;
		}
		// 记录到定时检测目录
		addDeviceDirectory(value);

		// 解析并设置FIXP监视器安全配置
		if (!loadSecure(document)) {
			Logger.error(this, "loadLocal", "load secure configure, failed!");
			return false;
		}
		// 解析自定义资源
		if (!loadCustom(document)) {
			Logger.error(this, "loadLocal", "load custom configure, failed!");
			return false;
		}

		// 加载许可证，来自本地操作
		if (!loadLicence(false)) {
			Logger.error(this, "localLocal", "load licence, failed!");
			return false;
		}

		// WATCH站点账号
		value = XMLocal.getXMLValue(document.getElementsByTagName(TopMark.WATCH_ACCOUNT));
		if (value != null&&value.length()>0) {
			WatchOnTopPool.getInstance().setFile(value);
		}

		// 管理池
		HubPool[] pools = new HubPool[] { MonitorOnTopPool.getInstance(),
				HomeOnTopPool.getInstance(), WatchOnTopPool.getInstance(),
				LogOnTopPool.getInstance(), BankOnTopPool.getInstance() };

		// 站点检查触发时间间隔
		setSleepTimeout(pools, document);
		// 注册站点超时时间(所有站点的超时时间是一致的)
		setActiveTimeout(pools, document);
		// 站点发生超时到被删除之间的时间间隔(通常是超时时间的3倍)
		setDeleteTimeout(pools, document);

		// 解析“least-disk”属性
		if (success) {
			loadTimerTasks(document);
		}

//		// 加载许可证
//		if (success) {
//			success = loadLicence();
//			Logger.note(this, "localLocal", success, "load licence");
//		}

		// 加载日志/追踪记录
		if (success) {
			success = loadLogResourceWithRemote(filename);
		}

		return success;
	}

	/**
	 * 把资源数据从内存中释放
	 */
	private void clearResource() {
		//		// 清除记录
		//		DictPool.getInstance().clear();

		// 清除主键记录

		//		KeyIterator.getInstance().flush();

		//		// 在非空状态下才保存，保存实现“Markable”接口的类和类参数。
		//		if (!MarkMapper.isEmpty()) {
		//			MarkMapper.save();
		//		}
	}

	/**
	 * 加载本地资源
	 * @return 成功返回真，否则假
	 */
	private boolean loadResource() {
		//		// 重装加载词典资源
		//		boolean success = DictPool.getInstance().reload();
		//		Logger.note(this, "loadResource", success, "load user account");
		//		// 加载数据块编号
		//		if (success) {
		//			success = StubManager.getInstance().load();
		//		}

		// 加载数据块编号
		boolean success = StubManager.getInstance().load();

		// 加载键值序号
		Logger.note(this, "loadResource", success, "load stub");
		if (success) {
			success = KeyIterator.getInstance().load();
		}
		Logger.note(this, "loadResource", success, "load prime key");

		return success;
	}

	/**
	 * 启动TOP站点（TOP站点分为管理站点和监视站点两种）
	 * @param args 配置参数
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		if (System.getSecurityManager() != null) {
			Logger.info("TopLauncher.main, sandbox loaded!");
		}
		// 将本地链接库目录导入“java.library.path”队列，加载本地目录下面的库文件
		JNILoader.init();

		String filename = args[0];
		boolean success = TopLauncher.getInstance().loadLocal(filename);
		Logger.note("TopLauncher.main, load local", success);
		if (success) {
			success = TopLauncher.getInstance().start();
			Logger.note("TopLauncher.main, start service", success);
		}

		if (!success) {
			Logger.gushing();
			System.exit(0); // 关闭系统
		}
	}

}