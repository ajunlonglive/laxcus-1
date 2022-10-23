/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work;

import java.util.*;

import org.w3c.dom.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.launch.*;
import com.laxcus.launch.job.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;
import com.laxcus.site.work.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.contact.distant.*;
import com.laxcus.task.flux.*;
import com.laxcus.task.talk.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.naming.*;
import com.laxcus.visit.impl.work.*;
import com.laxcus.work.pool.*;
import com.laxcus.xml.*;

/**
 * WORK站点启动器。<br>
 * 执行分布计算CONDUCT.TO阶段任务的管理和计算。<br>
 * 
 * laxcus.site.default - 站点入口目录
 * laxcus.task.deploy - 分布组件保存目录
 * laxcus.task.mid.dir - 分布组件中间数据存取目录
 * 
 * 上述三个属性在启动文件中指定.用在安全管理权限许可文件中
 * 
 * @author scott.liang
 * @version 1.5 10/14/2015
 * @since laxcus 1.0
 */
public class WorkLauncher extends JobLauncher implements TaskListener {

	/** WORK站点静态句柄 **/
	private static WorkLauncher selfHandle = new WorkLauncher();

	/** 当前WORK站点地址配置 */
	private WorkSite local = new WorkSite();

	/**
	 * 构造WORK站点启动器
	 */
	private WorkLauncher() {
		super();
		setExitVM(true);
		setPrintFault(true);
		// WORK站点监听
		setStreamInvoker(new WorkStreamAdapter());
		setPacketInvoker(new WorkPacketAdapter());
	}

	/**
	 * 返回WORK站点的静态句柄
	 * @return WorkLauncher句柄
	 */
	public static WorkLauncher getInstance() {
		// 调用句柄时，进行安全检查
		SiteLauncher.check("WorkLauncher.getInstance");
		// 返回句柄
		return WorkLauncher.selfHandle;
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
		return WorkCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public InvokerPool getInvokerPool() {
		return WorkInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return WorkCustomTrustor.getInstance();
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
	 * 返回本地站点地址配置
	 * @return WorkSite实例
	 */
	public WorkSite getLocal() {
		return local;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TaskListener#refreshTask(int)
	 */
	@Override
	public void refreshTask(int family) {
		Logger.debug(this, "refreshTask", "task is %s", PhaseTag.translate(family));
		// 通知线程，重新注册到HOME站点
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
		return StaffOnWorkPool.getInstance().allow(issuer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 预初始化
		boolean success = preload();
		Logger.note(this, "init", success, "preload");
		// 2. 启动FIXP监听服务器
		if (success) {
			Class<?>[] clazzs = { CommandVisitOnWork.class };
			success = loadSingleListen(clazzs, local.getNode());
		}
		Logger.note(this, "init", "load listen", success);
		// 3. 启动WORK站点的所有资源管理池
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", success, "load pool");
		// 4. 注册本站点到HOME服务器
		if (success) {
			success = login();
		}
		Logger.note(this, "init", success, "login");
		// 5. 启动资源管理池
		if (success) {
			success = StaffOnWorkPool.getInstance().start();
		}
		Logger.note(this, "init", success, "start staff");

		// 不成功，清除
		if (!success) {
			if(isLogined()) logout(); // 在登录成功状态下注销
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
		//		Logger.info(this, "process", "into ...");

		//		// 启动资源管理池
		//		StaffOnWorkPool.getInstance().start();

		defaultProcess();

		//		// 关闭
		//		StaffOnWorkPool.getInstance().stop();
		//		while(StaffOnWorkPool.getInstance().isRunning()) {
		//			delay(100);
		//		}
		//
		//		Logger.info(this, "process", "exit ...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 1. 从HOME服务器注销
		logout();
		// 2. 停止池服务
		stopPool();
		// 3. 关闭资源管理池
		StaffOnWorkPool.getInstance().stop();
		while (StaffOnWorkPool.getInstance().isRunning()) {
			delay(100);
		}
		// 4. 关闭监听服务
		stopListen();
		// 5. 停止日志服务
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
	 * 加载服务池
	 * @return 成功返回真，否则假
	 */
	private boolean loadPool() {
		Logger.info(this, "loadPool", "into...");

//		// 设置码位计算器代理
//		IndexSector.setScaleTrustor(ScalerPool.getInstance());

		// 设置数字签名人代理（FluxTrustorPool内部判断数据执行人是存在且有效）
//		FluxTrustorPool.getInstance().setSigerTrustor(ToManager.getInstance());
		
		IssuerManager.getInstance().setSiteLauncher(this);
		IssuerManager.getInstance().setSwitchPool(WorkSwitchPool.getInstance());
		IssuerManager.getInstance().setStaffPool(StaffOnWorkPool.getInstance());
		FluxTrustorPool.getInstance().setSigerTrustor(IssuerManager.getInstance());

		// CONDUCT.TO阶段资源管理器
		ToManager.getInstance().setSiteLauncher(this);
		ToManager.getInstance().setSwitchPool(WorkSwitchPool.getInstance());
		ToManager.getInstance().setStaffPool(StaffOnWorkPool.getInstance());
		
		// CONDUCT.TO管理池的中间数据委托代理和事件监听接口
		ToTaskPool.getInstance().setFluxTrustor(FluxTrustorPool.getInstance());
		ToTaskPool.getInstance().setTaskListener(this);
		ToTaskPool.getInstance().setToTrustor(ToManager.getInstance());
		ToTaskPool.getInstance().setTalkTrustor(TalkPool.getInstance());

		// CONTACT.DISTANT阶段任务管理器
		DistantManager.getInstance().setSiteLauncher(this);
		DistantManager.getInstance().setSwitchPool(WorkSwitchPool.getInstance());
		DistantManager.getInstance().setStaffPool(StaffOnWorkPool.getInstance());

		// CONTACT.DISTANT管理池的中间数据委托代理和事件监听接口
		DistantTaskPool.getInstance().setFluxTrustor(FluxTrustorPool.getInstance());
		DistantTaskPool.getInstance().setTaskListener(this);
		DistantTaskPool.getInstance().setDistantTrustor(DistantManager.getInstance());
		DistantTaskPool.getInstance().setTalkTrustor(TalkPool.getInstance());

		VirtualPool[] pools = new VirtualPool[] {
				// ARCHIVE站点管理池
				AccountOnCommonPool.getInstance(),
				// 分布任务组件交互对话池 
				TalkPool.getInstance(),
				// 调用器和命令管理池
				WorkInvokerPool.getInstance(), WorkCommandPool.getInstance(),
				// TO阶段组件管理池
				FluxTrustorPool.getInstance(), ToTaskPool.getInstance(),
				
//				// 码位计算器组件管理池
//				ScalerPool.getInstance(),
//				// 快捷组件管理池
//				SwiftPool.getInstance(),
				
				// CONTACT.DISTANT阶段组件管理池
				DistantTaskPool.getInstance(),
				// 自定义调用器管理池
				CustomClassPool.getInstance(), WorkSwitchPool.getInstance() };
		// 全部启动
		return startAllPools(pools);
	}

	/**
	 * 停止全部管理池
	 */
	private void stopPool() {
		Logger.info(this, "stopPool", "...");

		VirtualPool[] pools = new VirtualPool[] {
				WorkSwitchPool.getInstance(), CustomClassPool.getInstance(),
				// 命令和调用器管理池
				WorkCommandPool.getInstance(), WorkInvokerPool.getInstance(),
				// 分布任务组件交互对话池 
				TalkPool.getInstance(),
				// TO阶段组件管理池
				ToTaskPool.getInstance(), FluxTrustorPool.getInstance(),
				
//				// 码位计算器管理池
//				ScalerPool.getInstance(),
				
//				// 快捷组件管理池
//				SwiftPool.getInstance(),
				
				// CONTACT.DISTANT阶段
				DistantTaskPool.getInstance(),
				
				// ARCHIVE站点管理池
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
				StaffOnWorkPool.getInstance().size()));

		return moment;
	}

	/**
	 * 更新阶段命名
	 */
	private Site reset() {
		// WORK站点的资源引用和阶段命名
		List<Refer> refers = StaffOnWorkPool.getInstance().getRefers();
//		List<Phase> phases = ToTaskPool.getInstance().getPhases();
		
		// CONDUCT.TO/CONTACT.DISTANT阶段命名
		ArrayList<Phase> phases = new ArrayList<Phase>();
		phases.addAll(ToTaskPool.getInstance().getPhases());
		phases.addAll(DistantTaskPool.getInstance().getPhases());

		// debug code, start
		for (Refer e : refers) {
			Logger.debug(this, "reset", "login %s", e.getUsername());
		}
		for (Phase e : phases) {
			Logger.debug(this, "reset", "publish task %s", e);
		}
		// debug code, end

		// 瞬时记录
		Moment moment = createMoment();

		// 锁定
		super.lockSingle();
		try {
			// 释放全部旧数据
			local.reset();
			local.setMoment(moment);
			// 建立WORK站点成员，和保存数据表
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
			for (Phase e : phases) {
				local.addPhase(e);
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
		
		// 生成新节点副本
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

//	/**
//	 * 设置快捷组件目录
//	 * @param document XML文档
//	 * @param pool SwiftPool实例
//	 * @param subpath 指定XML路径
//	 * @return 设置成功返回真，否则假
//	 */
//	protected boolean setSwiftDeployPath(Document document, SwiftBufferPool pool, String subpath) {
//		String tag = OtherMark.CONTACT_DIRECTORY;  // 在local.xml文件中定义
//		return setRootPath(document, tag, pool, subpath);
//	}

	/**
	 * 设置快捷组件目录
	 * @param document XML文档
	 * @param pool DistantPool实例
	 * @param subpath 指定XML路径
	 * @return 设置成功返回真，否则假
	 */
	protected boolean setDistantDeployPath(Document document, DistantTaskPool pool, String subpath) {
		String tag = OtherMark.SWIFT_DIRECTORY;  // 在local.xml文件中定义
		return setRootPath(document, tag, pool, subpath);
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
	 * 加载并且解析WORK站点配置文件
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
		
		// 设置CONDUCT.TO分布组件发布目录。目录在local.xml文件设置。
		if(success) {
			success = setTaskDeployPath(document, ToTaskPool.getInstance(), "to");
		}
		// 设置中间数据存储目录。目录在local.xml文件设置。
		if(success) {
			success = setTaskMidPath(document, FluxTrustorPool.getInstance());
		}
		
//		// 用户码位计算器管理池。目录在local.xml文件设置。
//		if(success) {
//			success = setScaleDeployPath(document, ScalerPool.getInstance(), null);
//		}
//		// 快捷组件管理池。目录在local.xml文件设置
//		if(success) {
//			success = setSwiftDeployPath(document, SwiftPool.getInstance(), null);
//		}

		// 设置CONTACT.DISTANT分布组件发布目录。目录在local.xml文件设置
		if (success) {
			success = setTaskDeployPath(document, DistantTaskPool.getInstance(), "distant");
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
		//		// 加载追踪服务
		//		if (success) {
		//			success = Tigger.loadXML(filename);
		//			if (success) {
		//				loadTigDeviceDirectory();
		//			}
		//		}
		
		// loadLogResource

		return success;
	}

	/** 
	 * WORK站点启动器入口
	 * @param args 启动参数
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		if (System.getSecurityManager() != null) {
			Logger.info("WorkLauncher.main, sandbox loaded!");
		}

		// 将本地链接库目录导入“java.library.path”队列，加载本地目录下面的库文件
		JNILoader.init();

		// 加载配置文件
		String filename = args[0];
		boolean success = WorkLauncher.getInstance().loadLocal(filename);
		Logger.note("WorkLauncher.main, load local", success);
		// 启动线程
		if (success) {
			success = WorkLauncher.getInstance().start();
			Logger.note("WorkLauncher.main, start service", success);
		}
		if (!success) {
			Logger.gushing();
			System.exit(0);
		}
	}

}