/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build;

import java.util.*;

import org.w3c.dom.*;

import com.laxcus.access.*;
import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.build.pool.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.launch.*;
import com.laxcus.launch.job.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;
import com.laxcus.site.build.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.sift.*;
import com.laxcus.task.talk.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.naming.*;
import com.laxcus.visit.impl.build.*;
import com.laxcus.xml.*;

/**
 * BUILD站点启动器。<br>
 * BUILD站点执行数据构建工作，数据构建是按照用户需求，实现用户自定义的ETL服务。<br><br>
 * 
 * BUILD站点的工作是围绕SIFT任务组件进行的，工作范围包括： <br>
 * 1. 通过SiftTaskPool管理注册的构造任务组件（SiftTask的子类）。<br>
 * 2. 启动、监视、停止构造任务组件。<br><br>
 * 
 * 构造任务执行两种操作：<br>
 * 1. modulate：数据优化。不改变原数据结构和数据内容。将属于DATA站点的regulate操作转移过来，减轻DATA站点运行压力。<br>
 * 2. reshuffle：数据重组（或称洗牌）。这是构造任务的主要工作 ，是从原来的数据记录中生成新的数据记录，数据来源是任意多个表的数据。<br><br> 
 * 
 * ETL: extract, transform, load <br><br> 
 * 
 * @author scott.liang
 * @version 1.2 10/2/2013
 * @since laxcus 1.0
 */
public class BuildLauncher extends JobLauncher implements TaskListener {

	/** 启动器句柄 **/
	private static BuildLauncher selfHandle = new BuildLauncher();

	/** 本地站点地址 **/
	private BuildSite local = new BuildSite();

	/**
	 * 构造默认的构造站点启动器
	 */
	private BuildLauncher() {
		super();
		super.setExitVM(true);
		this.setPrintFault(true);
		// 设置FIXP监听接口
		super.setPacketInvoker(new BuildPacketAdapter());
		super.setStreamInvoker(new BuildStreamAdapter());
	}

	/**
	 * 返回构造站点启动器静态句柄
	 * @return
	 */
	public static BuildLauncher getInstance() {
		// 调用句柄时，进行安全检查
		SiteLauncher.check("BuildLauncher.getInstance");
		// 输出句柄
		return BuildLauncher.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getPublicListener()
	 */
	@Override
	public com.laxcus.site.Node getPublicListener() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public CommandPool getCommandPool() {
		return BuildCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public InvokerPool getInvokerPool() {
		return BuildInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return BuildCustomTrustor.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getSite()
	 */
	@Override
	public Site getSite() {
		return this.local;
	}

	/**
	 * 返回当前是BUILD站点配置
	 * @return
	 */
	public BuildSite getLocal() {
		return this.local;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TaskListener#refreshTask(int)
	 */
	@Override
	public void refreshTask(int family) {
		Logger.debug(this, "refreshTask", "task is %s", PhaseTag.translate(family));
		// 更新参数，重新注册到HOME站点
		if (isLogined()) {
			checkin(false);
		} else {
			kiss();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TaskListener#hasTaskUser(com.laxcus.util.Siger)
	 */
	@Override
	public boolean hasTaskUser(Siger issuer) {
		return StaffOnBuildPool.getInstance().allow(issuer);
	}

	/**
	 * 加载构造任务组件
	 * @return 成功返回真，否则假
	 */
	private boolean loadPool() {
		// SIFT阶段任务事件监听接口
		SiftTaskPool.getInstance().setTaskListener(this);
		// 设置SIFT阶段资源代理
		SiftManager.getInstance().setInvokerPool(BuildInvokerPool.getInstance());
		SiftManager.getInstance().setStaffPool(StaffOnBuildPool.getInstance());
		SiftTaskPool.getInstance().setSiftTrustor(SiftManager.getInstance());
		SiftTaskPool.getInstance().setTalkTrustor(TalkPool.getInstance());

		VirtualPool[] pools = new VirtualPool[] {
				// ACCOUNT站点管理池
				AccountOnCommonPool.getInstance(),
				// 调用器和命令管理池
				BuildInvokerPool.getInstance(), BuildCommandPool.getInstance(),
				// 分布任务组件交互对话池
				TalkPool.getInstance(),
				// SIFT组件管理池
				SiftManager.getInstance(), SiftTaskPool.getInstance(),
//				// 码位计算器管理池
//				ScalerPool.getInstance(),
				// 自定义COMMAND/INVOKER
				CustomClassPool.getInstance() , BuildSwitchPool.getInstance()};
		// 全部启动
		return startAllPools(pools);
	}

	/**
	 * 停止资源管理池服务
	 */
	private void stopPool() {
		Logger.info(this, "stopPool", "stop all...");

		VirtualPool[] pools = new VirtualPool[] {
				// 命令状态切换池
				BuildSwitchPool.getInstance(),
				// COMMAND/INVOKER配置
				CustomClassPool.getInstance(),
				// 命令和调用器管理池
				BuildCommandPool.getInstance(), BuildInvokerPool.getInstance(),
				// 分布任务组件交互对话池
				TalkPool.getInstance(),
				// SIFT组件管理池
				SiftTaskPool.getInstance(), SiftManager.getInstance(),
//				// 码位计算器管理池
//				ScalerPool.getInstance(),
				// ACCOUNT站点管理池
				AccountOnCommonPool.getInstance() };
		// 全部停止 
		stopAllPools(pools);
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
				StaffOnBuildPool.getInstance().size()));

		return moment;
	}

	/**
	 * 更新BUILD站点成员和阶段命名
	 * @return Site子类实例的数据副本
	 */
	private Site reset() {
		// BUILD站点的资源引用和阶段命名
		List<Refer> refers = StaffOnBuildPool.getInstance().getRefers();
		List<Phase> phases = SiftTaskPool.getInstance().getPhases();

		// DEBUG CODE, start
		for (Refer e : refers) {
			Logger.debug(this, "reset", "login %s", e.getUsername());
		}
		for (Phase e : phases) {
			Logger.debug(this, "reset", "publish task %s", e);
		}
		// DEBUG CODE, end
		
		// 瞬时记录
		Moment moment = createMoment();

		// 锁定！
		super.lockSingle();
		try {
			// 释放全部数据
			local.reset();
			// 实例！
			local.setMoment(moment);
			// 建立BUILD站点成员，和保存数据表
			for(Refer refer : refers) {
				Siger siger = refer.getUsername();
				// 建立用户签名
				local.create(siger);
				// 保存数据表名
				for (Space space : refer.getTables()) {
					local.addSpace(siger, space);
				}
			}
			// 保存阶段命名
			for (Phase phase : phases) {
				local.addPhase(phase);
			}
			// 产生数据副本
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
		
		// 刷新任务阶段命名
		Site site = reset();
		// 判断有效
		boolean success = (site != null);
		// 注册到HOME站点
		if (success) {
			success = login(site);
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
		// 1. 前初始化
		boolean success = preload();
		Logger.note(this, "init", success, "preload");
		// 2. 启动FIXP监听器
		if (success) {
			Class<?>[] clazzs = { CommandVisitOnBuild.class };
			success = loadSingleListen(clazzs, local.getNode());
		}
		Logger.note(this, "init", success, "load listen");
		// 3. 加载管理池服务
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", success, "load pool");
		// 4. 注册到HOME站点
		if (success) {
			success = login();
		}
		Logger.note(this, "init", success, "login");
		// 5. 启动资源管理池（包括加载数据块标识号和启动JNI）
		if (success) {
			success = StaffOnBuildPool.getInstance().start();
		}
		Logger.note(this, "init", success, "start staff");

		// 不成功，执行以下操作
		if (!success) {
			if (isLogined()) logout(); // 在登录成功状态下注销
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
		defaultProcess();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 从HOME站点上注销
		logout();
		// 关闭池服务
		stopPool();
		// 停止资源管理池（回收数据块标识号和停止JNI）
		StaffOnBuildPool.getInstance().stop();
		while (StaffOnBuildPool.getInstance().isRunning()) {
			delay(500);
		}
		// 停止FIXP监听
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
	 * 从XML文档中解析数据块存储目录，和设置到磁盘中
	 * @param document
	 * @return
	 */
	private boolean loadCatalogs(Document document) {
		Element element = (Element) document.getElementsByTagName("catalogs").item(0);
		String regulate = XMLocal.getValue(element, "regulate");
		String cache = XMLocal.getValue(element, "cache");
		String[] chunks = XMLocal.getXMLValues(element.getElementsByTagName("chunk"));

		regulate = ConfigParser.splitPath(regulate);
		cache = ConfigParser.splitPath(cache);

		// 设置数据优化目录
		int ret = AccessTrustor.setRegulateDirectory(regulate);
		Logger.note(this, "loadCatalogs", ret == 0, "regulate path is '%s'", regulate);
		if (ret != 0) return false;
		// 设置数据缓存目录
		ret = AccessTrustor.setCacheDirectory(cache);
		Logger.note(this, "loadCatalogs", ret == 0, "cache path is '%s'", cache);
		
		// 保存目录，定时检测
		addDeviceDirectory(regulate);
		addDeviceDirectory(cache);
		
		// 设置数据块目录
		if (ret != 0) return false;
		for (String path : chunks) {
			path = ConfigParser.splitPath(path);
			ret = AccessTrustor.setChunkDirectory(path);
			Logger.note(this, "loadCatalogs", ret == 0, "chunk path is '%s'", path);
			if (ret != 0) return false;
			
			// 保存目录，定时检测
			addDeviceDirectory(path);
		}
		return true;
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
	 * 加载配置文件和解析参数
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
		
		// 成员虚拟空间
		splitMemberCyber(document);

		// 解析和设置HOME站点地址
		boolean success = splitHubSite(document);
		// 解析和设置本地站点地址
		if (success) {
			success = splitSingleSite(local, document);
		}
		// 解析和设置回显配置
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
		
		// 设置分布任务组件发布目录
		if (success) {
			success = setTaskDeployPath(document, SiftTaskPool.getInstance(), "sift");
		}
//		// 设置码位计算器发布目录
//		if (success) {
//			success = setScaleDeployPath(document, ScalerPool.getInstance(), null);
//		}
		// 设置中间数据存取目录
		if (success) {
			success = setTaskMidPath(document, SiftManager.getInstance());
		}

		// BUILD站点资源配置目录，如存在分配的数据块标识号(stub)
		if (success) {
			String path = XMLocal.getXMLValue(document.getElementsByTagName("resource-directory"));
			success = createResourcePath(path);
			// 记录到定时检测目录中
			if (success) {
				addDeviceDirectory(path);
			}
		}
		Logger.note(this, "loadLocal", success, "resource directory %s", getResourcePath());

		// 解析并且设置数据块存储目录
		if (success) {
			success = loadCatalogs(document);
		}

		Logger.note(this, "loadLocal", success, "load catalog");

		// 解析“least-disk”属性
		if (success) {
			loadTimerTasks(document);
		}
		
		// 加载日志/追踪记录
		if (success) {
			success = loadLogResourceWithRemote(filename);
		}

		//		// 加载日志
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
	 * 启动器入口函数
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		if (System.getSecurityManager() != null) {
			Logger.info("BuildLauncher.main, sandbox loaded!");
		}
		// 将本地链接库目录导入“java.library.path”队列，加载本地目录下面的库文件
		JNILoader.init();

		// 1. 初始化本地存取接口
		int ret = AccessTrustor.initialize(null);
		if(ret != 0) {
			Logger.error("initialize failed, program will exit!");
			Logger.gushing();
			return;
		}
		// 2. 设置站点等级。BUILD站点都是主站点，可以读写数据
		ret = AccessTrustor.setRank(RankTag.MASTER);
		if (ret != 0) {
			Logger.error("cannot be set prime site");
			Logger.gushing();
			return;
		}
		// 加载并且解析本地配置
		String filename = args[0];
		boolean success = BuildLauncher.getInstance().loadLocal(filename);
		Logger.note("BuildLauncher.main, load local", success);
		// 启动进行
		if (success) {
			success = BuildLauncher.getInstance().start();
			Logger.note("BuildLauncher.main, start service", success);
		}
		if(!success) {
			Logger.gushing();
			System.exit(0);
		}
	}

}