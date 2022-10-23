/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.hash;

import org.w3c.dom.*;

import com.laxcus.hash.pool.*;
import com.laxcus.access.diagram.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.hash.*;
import com.laxcus.util.*;
import com.laxcus.visit.impl.hash.*;
import com.laxcus.xml.*;

/**
 * HASH站点启动器。<br><br>
 * 
 * HASH站点在内存中保存每个账号的位置，所有操作都在内存中进行，没有硬盘操作，所以这个节点的物理机需要大内存。<br>
 * HASH站点在启动向BANK站点请求自己的编号和全部ACCOUNT站点地址。<br><br>
 * 
 * <b>注意：在生产环境，必须当全部ACCOUNT站点启动后，才能启动HASH站点，特别注意这个顺序！！！</b><br>
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class HashLauncher extends SlaveLauncher {

	/** 启动器句柄 **/
	private static HashLauncher selfHandle = new HashLauncher();

	/** 当前HASH站点配置 */
	private HashSite local = new HashSite();
	
	/** 散列坐标 **/
	private SiteAxes axes = new SiteAxes();

	/**
	 * 构造HASH站点启动器
	 */
	private HashLauncher() {
		super();		
		setPrintFault(true);
		// HASH站点监听
		setPacketInvoker(new HashPacketAdapter());
		setStreamInvoker(new HashStreamAdapter());
		// 退出JVM
		setExitVM(true);
		// 默认编号-1
		setNo(HashSite.INVALID_NO);
	}

	/**
	 * 返回HASH站点静态句柄
	 * @return HashLauncher实例
	 */
	public static HashLauncher getInstance() {
		return HashLauncher.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getPublicListener()
	 */
	@Override
	public com.laxcus.site.Node getPublicListener() {
		return null;
	}

	/**
	 * 设置编号
	 * @param who
	 */
	public void setNo(int who) {
		Logger.info(this, "setNo", "current site no:%d", who);
		axes.setNo(who);
		local.setNo(who);
	}

	/**
	 * 返回编号
	 * @return
	 */
	public int getNo() {
		return axes.getNo();
	}
	
	/**
	 * 设置全部HASH站点数目
	 * @param i 全部HASH站点数目
	 */
	public void setPartners(int i) {
		axes.setPartners(i);
	}
	
	/**
	 * 返回全部HASH站点数目
	 * @return 全部HASH站点数目
	 */
	public int getPartners() {
		return axes.getPartners();
	}
	
	/**
	 * 返回当前HASH节点坐标
	 * @return 节点坐标
	 */
	public SiteAxes getAxes(){
		return axes;
	}
	
	/**
	 * 判断这个签名在允许的范围内
	 * @param siger 账号签名
	 * @return 返回真或者假
	 */
	public boolean allow(Siger siger) {
		return axes.allow(siger);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public CommandPool getCommandPool() {
		return HashCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public InvokerPool getInvokerPool() {
		return HashInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return HashCustomTrustor.getInstance();
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
	 * 重置参数
	 * @return
	 */
	private Site reset() {
		super.lockSingle();
		try {
			// 本机签名数目
			local.setMembers(StaffOnHashPool.getInstance().size());
			// 输出副本
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
		
		// 生成参数重置后的复本
		Site site = reset();
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
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 1. 启动预处理
		boolean success = preload();
		Logger.note(this, "init", "preload ", success);
		// 2. 启动FIXP服务
		if (success) {
			Class<?>[] clazzs = { CommandVisitOnHash.class };
			success = loadSingleListen(clazzs, local.getNode());
		}
		Logger.note(this, "init", "load listen", success);
		// 3. 启动管理池
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", "load pool", success);
		// 4. 注册
		if (success) {
			success = login();
		}
		
		Logger.note(this, "init", "login", success);

		// 成功，推送注册用户，不成功退出
		if (success) {
			StaffOnHashPool.getInstance().pushRegisterMember();
		} else {
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
		StaffOnHashPool.getInstance().start();

		// 进入循环
		defaultProcess();

		// 关闭
		StaffOnHashPool.getInstance().stop();
		while (StaffOnHashPool.getInstance().isRunning()) {
			delay(100);
		}

		Logger.info(this, "process", "exit ...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 注销
		logout();
		// 停止管理池
		stopPool();
		// 停止监听
		stopListen();
		// 关闭日志
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
	 * 启动管理池
	 * @return
	 */
	private boolean loadPool() {
		Logger.info(this, "loadPool", "into...");
		// 启动管理池
		VirtualPool[] pools = new VirtualPool[] {
				HashInvokerPool.getInstance(), HashCommandPool.getInstance(),
				// 自定义COMMAND/INVOKER资源
				CustomClassPool.getInstance()};
		return startAllPools(pools);
	}

	/**
	 * 停止管理池
	 */
	private void stopPool() {
		Logger.info(this, "stopPool", "...");
		VirtualPool[] pools = new VirtualPool[] {
				CustomClassPool.getInstance(), HashCommandPool.getInstance(),
				HashInvokerPool.getInstance() };
		stopAllPools(pools);
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
	 * 加载本地资源配置
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

		// 解析和设置TOP站点地址
		boolean success = splitHubSite(document);
		// 解析和设置本地站点地址
		if (success) {
			success = splitSingleSite(local, document);
		}
		// 解析回显配置
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
		
		// 管理池延时间隔
		if (success) {
			setStaffPoolSleepInterval(document, StaffOnHashPool.getInstance());
		}

		
		// 解析“least-disk”属性
		if (success) {
			loadTimerTasks(document);
		}

		// 加载日志/追踪记录
		if (success) {
			success = loadLogResourceWithRemote(filename);
		}

		//		// 加载日志目录配置
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
	 * 启动HASH进程
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		if (System.getSecurityManager() != null) {
			Logger.info("HashLauncher.main, sandbox loaded!");
		}
		// 将本地链接库目录导入“java.library.path”队列，加载本地目录下面的库文件
		JNILoader.init();

		// 加载配置文件
		String filename = args[0];
		boolean success = HashLauncher.getInstance().loadLocal(filename);
		Logger.note("HashLauncher.main, load local", success);
		// 启动线程
		if (success) {
			success = HashLauncher.getInstance().start();
			Logger.note("HashLauncher.main, start service", success);
		}
		if (!success) {
			Logger.gushing();
			System.exit(0);
		}
	}

}