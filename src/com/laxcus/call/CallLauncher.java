/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call;

import java.util.*;


import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.call.pool.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.launch.*;
import com.laxcus.launch.job.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;
import com.laxcus.site.call.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.*;
import com.laxcus.task.conduct.balance.*;
import com.laxcus.task.conduct.init.*;
import com.laxcus.task.contact.*;
import com.laxcus.task.contact.fork.*;
import com.laxcus.task.contact.merge.*;
import com.laxcus.task.establish.*;
import com.laxcus.task.establish.assign.*;
import com.laxcus.task.establish.issue.*;
import com.laxcus.task.meta.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.impl.call.*;
import com.laxcus.xml.*;

/**
 * CALL站点启动器。<br>
 * 
 * CALL站点是网关站点，对内向HOME注册，对外接受FRONT站点的注册，接受FRONT数据请求命令。CALL处于数据处理的中心地位，协调DATA/WORK/BUILD数据处理任务。<br>
 * 
 * CALL数据处理建立账号上，最少保持一个账号下的所有表的元数据和这个表的阶段命名。增加/删除都以账号为单位。
 * 系统级的分布组件由管理员部署在每个节点上。CALL站点从任务中心获取用户的分布组件任务。
 * 
 * CallSite参数增加/删除只被进程内使用，防止调用时的读写同步异常。
 * 
 * @author scott.liang
 * @version 1.5 5/10/2015
 * @since laxcus 1.0
 */
public final class CallLauncher extends JobLauncher implements TaskListener {

	/** 启动器句柄 **/
	private static CallLauncher selfHandle = new CallLauncher();

	/** 当前CALL站点配置 */
	private CallSite local = new CallSite();

	/** FRONT成员虚拟空间 **/
	private FrontCyber frontCyber = new FrontCyber();
	
	/**
	 * 构造CALL站点启动器
	 */
	private CallLauncher() {
		super();
		setPrintFault(true);
		// CALL站点监听
		setPacketInvoker(new CallPacketAdapter());
		setStreamInvoker(new CallStreamAdapter());
		// 退出JVM
		setExitVM(true);
	}

	/**
	 * 返回CALL站点静态句柄
	 * @return CallLauncher实例
	 */
	public static CallLauncher getInstance() {
		// 调用句柄时，进行安全检查
		SiteLauncher.check("CallLauncher.getInstance");
		// 输出句柄
		return CallLauncher.selfHandle;
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
				StaffOnCallPool.getInstance().size()));
		FrontCyber f = getFrontCyber();
		moment.setOnline(new PersonStamp(f.getPersons(), f.getThreshold(),
				FrontOnCallPool.getInstance().size()));

		return moment;
	}
	
	/**
	 * 返回FRONT在线用户虚拟空间
	 * @return FrontCyber实例
	 */
	public final FrontCyber getFrontCyber() {
		return frontCyber;
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
		return CallCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public InvokerPool getInvokerPool() {
		return CallInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return CallCustomTrustor.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TaskListener#refreshTask(int)
	 */
	@Override
	public void refreshTask(int phaseFamily) {
		Logger.debug(this, "refreshTask", "this is %s", PhaseTag.translate(phaseFamily));
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
	public boolean hasTaskUser(Siger siger) {
		return StaffOnCallPool.getInstance().allow(siger);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getSite()
	 */
	@Override
	public CallSite getSite() {
		return local;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 1. 预初始化
		boolean success = preload();
		Logger.note(this, "init", "preload", success);
		// 2. 启动FIXP监听
		if (success) {
			Class<?>[] clazzs = { CommandVisitOnCall.class, HubVisitOnCall.class, FrontVisitOnCall.class };
			success = super.loadGatewayListen(clazzs, local.getPrivate(), local.getPublic());
		}
		Logger.note(this, "init", success, "load listen");
		// 3. 启动管理池
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", success, "load pool");
		// 4. 注册
		if (success) {
			success = login();
		}
		Logger.note(this, "init", success, "login");
		// 5.启动CALL站点的资源管理池
		if (success) {
			success = StaffOnCallPool.getInstance().start();
		}
		Logger.note(this, "init", success, "start staff");

		// 找到HOME集群下，分配数量最少的表（CALL提出申请的数量，HOME分配表和表的数量）
		// 通过这个表，找到ACCOUNT站点上的与这个表关联的分布任务组件

		// 不成功退出运行
		if (!success) {
			if(isLogined()) logout();
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
		//
		//		// 启动资源管理池
		//		StaffOnCallPool.getInstance().start();

		// 默认的站点处理循环
		defaultProcess();

		//		// 站点循环等待退出
		//		refreshEndTime();
		//		long endtime = 0L;
		//		while (!isInterrupted()) {
		//			if (hasLogin()) {
		//				endtime = nextTouchTime();
		//				login();
		//			} else if (hasRelogin() || isMaxTimeout()) {
		//				endtime = nextTouchTime();
		//				relogin();
		//			} else if (isBing() || isTouchTimeout(endtime)) {
		//				endtime = nextTouchTime();
		//				hello();
		//			} else {
		//				long left = endtime - System.currentTimeMillis();
		//				if (left > 1000) left = 1000;
		//				delay(left);
		//			}
		//		}

		//		// 关闭它
		//		StaffOnCallPool.getInstance().stop();
		//		while (StaffOnCallPool.getInstance().isRunning()) {
		//			delay(100);
		//		}

		Logger.info(this, "process", "exit ...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 1. 从HOME站点注销
		logout();
		// 2. 停止管理池
		stopPool();
		// 3.关闭它
		StaffOnCallPool.getInstance().stop();
		while (StaffOnCallPool.getInstance().isRunning()) {
			delay(100);
		}
		// 4.. 停止FIXP监听服务
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
	 * 启动管理池
	 * @return
	 */
	private boolean loadPool() {		
//		// 设置码位计算器代理
//		IndexSector.setScaleTrustor(ScalerPool.getInstance());
		
		// 给资源辅助器分配关联的管理池句柄。运行过程中，他们直接使用这些管理池句柄，沙箱检查
		SeekManager[] managers = new SeekManager[] {
			FromSeekManager.getInstance(), ToSeekManager.getInstance(),
			ScanSeekManager.getInstance(), SiftSeekManager.getInstance(),
			DistantSeekManager.getInstance()
		};
		for (int i = 0; i < managers.length; i++) {
			managers[i].setSiteLauncher(this);
			managers[i].setSwitchPool(CallSwitchPool.getInstance());
			managers[i].setStaffPool(StaffOnCallPool.getInstance());
		}
		
		FromSeekManager.getInstance().setMetaPool(DataOnCallPool.getInstance());
		FromSeekManager.getInstance().setInitPool(InitTaskPool.getInstance());
		ToSeekManager.getInstance().setMetaPool(WorkOnCallPool.getInstance());
		ToSeekManager.getInstance().setInitPool(InitTaskPool.getInstance());
		
		ScanSeekManager.getInstance().setMetaPool(DataOnCallPool.getInstance());
		ScanSeekManager.getInstance().setIssuePool(IssueTaskPool.getInstance());
		SiftSeekManager.getInstance().setMetaPool(BuildOnCallPool.getInstance());
		SiftSeekManager.getInstance().setIssuePool(IssueTaskPool.getInstance());

		// CONDUCT INIT/BALANCE任务管理池
		DesignTaskPool[] designs = new DesignTaskPool[] {
				InitTaskPool.getInstance(), BalanceTaskPool.getInstance() };
		for (int i = 0; i < designs.length; i++) {
			designs[i].setTaskListener(this);
			designs[i].setFromSeeker(FromSeekManager.getInstance());
			designs[i].setToSeeker(ToSeekManager.getInstance());
		}

		// ESTABLISH ISSUE/ASSIGN任务管理池
		SerialTaskPool[] serials = new SerialTaskPool[] {
				IssueTaskPool.getInstance(), AssignTaskPool.getInstance() };
		for (int i = 0; i < serials.length; i++) {
			serials[i].setTaskListener(this);
			serials[i].setScanSeeker(ScanSeekManager.getInstance());
			serials[i].setSiftSeeker(SiftSeekManager.getInstance());
		}
		
		// CONTACT FORK/MERGE任务管理池
		CastTaskPool[] casts = new CastTaskPool[] {
				ForkTaskPool.getInstance(), MergeTaskPool.getInstance() };
		for (int i = 0; i < casts.length; i++) {
			casts[i].setTaskListener(this);
			casts[i].setDistantSeeker(DistantSeekManager.getInstance());
		}

		// 元数据缓存管理池
		MetaPool.getInstance().setInvokerPool(CallInvokerPool.getInstance());
		InitTaskPool.getInstance().setMetaTrustor(MetaPool.getInstance());
		BalanceTaskPool.getInstance().setMetaTrustor(MetaPool.getInstance());
		IssueTaskPool.getInstance().setMetaTrustor(MetaPool.getInstance());
		AssignTaskPool.getInstance().setMetaTrustor(MetaPool.getInstance());
		ForkTaskPool.getInstance().setMetaTrustor(MetaPool.getInstance());
		MergeTaskPool.getInstance().setMetaTrustor(MetaPool.getInstance());
		
		// TO资源辅助器（它不是管理池）
		ToSeekManager.getInstance().setMetaPool(WorkOnCallPool.getInstance());
		ToSeekManager.getInstance().setInitPool(InitTaskPool.getInstance());

		// SIFT资源辅助器（它不是管理池）
		SiftSeekManager.getInstance().setMetaPool(BuildOnCallPool.getInstance());
		SiftSeekManager.getInstance().setIssuePool(IssueTaskPool.getInstance());
		
		// DISTANT资源辅助器（它不是管理池）
		DistantSeekManager.getInstance().setMetaPool(WorkOnCallPool.getInstance());
		DistantSeekManager.getInstance().setForkPool(ForkTaskPool.getInstance());

		VirtualPool[] pools = new VirtualPool[] {
				// 用户日志管理池
				UserLogPool.getInstance(),
				// ACCOUNT站点管理池
				AccountOnCommonPool.getInstance(),
				// 缓存数据块管理池
				CacheReflexStubOnCallPool.getInstance(),
				// 调用器和命令管理池
				CallInvokerPool.getInstance(), CallCommandPool.getInstance(),
				// FRONT管理池
				FrontOnCallPool.getInstance(),
				// DATA/WORK/BUILD资源管理池
				DataOnCallPool.getInstance(), WorkOnCallPool.getInstance(),
				BuildOnCallPool.getInstance(),
				// 元数据缓存管理池
				MetaPool.getInstance(),
				// 分布计算（FROM/TO资源，INIT/BALANCE阶段）
				InitTaskPool.getInstance(), BalanceTaskPool.getInstance(),
				// 分布数据构建（SCAN/AT资源，ISSUE/ASSIGN/ESCH阶段任务）
				IssueTaskPool.getInstance(), AssignTaskPool.getInstance(),
				// 快速计算(FORK/MERGE阶段)
				ForkTaskPool.getInstance(), MergeTaskPool.getInstance(),
				// 存储管理池
				StoreOnCallPool.getInstance(),
//				// 码位计算器管理池
//				ScalerPool.getInstance(),
				// 自定义COMMAND/INVOKER资源
				CustomClassPool.getInstance(), CallSwitchPool.getInstance()};

		// 启动管理池
		return startAllPools(pools);
	}

	/**
	 * 停止全部管理池线程
	 */
	private void stopPool() {
		VirtualPool[] pools = new VirtualPool[] {
				// 命令切换池
				CallSwitchPool.getInstance(),
				// 自定义COMMAND/INVOKER资源
				CustomClassPool.getInstance(),
				// 云存储目录
				StoreOnCallPool.getInstance(),

				// WORK站点资源
				WorkOnCallPool.getInstance(),
				// DATA站点资源
				DataOnCallPool.getInstance(),
				// FRONT管理池
				FrontOnCallPool.getInstance(),
				// 异步命令处理池
				CallCommandPool.getInstance(), CallInvokerPool.getInstance(),
				// 分布数据构建
				IssueTaskPool.getInstance(), AssignTaskPool.getInstance(),
				// BUILD站点资源
				BuildOnCallPool.getInstance(),
				// 分布数据计算
				InitTaskPool.getInstance(), BalanceTaskPool.getInstance(),
				// 快速计算
				ForkTaskPool.getInstance(), MergeTaskPool.getInstance(),
//				// 码位计算器
//				ScalerPool.getInstance(),
				// 元数据缓存池
				MetaPool.getInstance(),
				// 缓存数据块管理池
				CacheReflexStubOnCallPool.getInstance(),
				// ACCOUNT站点管理池
				AccountOnCommonPool.getInstance(),
				// 用户日志管理池
				UserLogPool.getInstance() };

		// 停止管理池
		stopAllPools(pools);
	}

//	/**
//	 * 更新全部参数，包括阶段命名和数据表名
//	 */
//	private Site reset() {
//		// 全部分布任务组件
//		ArrayList<Phase> phases = new ArrayList<Phase>();
//		phases.addAll(IssueTaskPool.getInstance().getPhases());
//		phases.addAll(AssignTaskPool.getInstance().getPhases());
//		phases.addAll(InitTaskPool.getInstance().getPhases());
//		phases.addAll(BalanceTaskPool.getInstance().getPhases());
//
//		// 用户资源引用列表
//		List<Refer> refers = StaffOnCallPool.getInstance().getRefers();
//		
////		// 账号签名 -> 数据表映像
////		TreeMap<Siger, SpaceSet> sets = new TreeMap<Siger, SpaceSet>();
////		for(Refer refer : refers) {
////			Siger siger = refer.getUsername();
////			// 生成映像关系
////			SpaceSet set = sets.get(siger);
////			if(set == null) {
////				set = new SpaceSet();
////				sets.put(siger, set);
////			}
////			// 判断DATA数据表在CALL站点上注册
////			for (Space space : refer.getTables()) {
////				if (DataOnCallPool.getInstance().contains(space)) {
////					set.add(space);
////				}
////			}
////		}
//
//		// DEBUG CODE, start
//		for(Phase e : phases) {
//			Logger.debug(this, "reset", "publish task %s", e);
//		}
//		// DEBUG CODE, end
//
//		// 重置数据（删除表空间和阶段命名，但是不能删除账号签名。删除账号在“DROP USER”时发生）
//		super.lockSingle();
//		try {
//			// 清除旧数据
//			local.reset();
//			// 保存数据表名
//			for (Refer refer : refers) {
//				Siger siger = refer.getUsername();
//				// 建立CALL站点成员
//				local.create(siger);
//				// 保存数据表名
//				for (Space space : refer.getTables()) {
//					local.addSpace(siger, space);
//				}
//			}
//			// 保存阶段命名
//			for (Phase phase : phases) {
//				local.addPhase(phase);
//			}
//			// 产生数据副本
//			return local.duplicate();
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//		return null;
//	}

	/**
	 * 更新全部参数，包括阶段命名和数据表名
	 */
	private Site reset() {
		// 全部分布任务组件
		ArrayList<Phase> phases = new ArrayList<Phase>();
		phases.addAll(IssueTaskPool.getInstance().getPhases());
		phases.addAll(AssignTaskPool.getInstance().getPhases());
		phases.addAll(InitTaskPool.getInstance().getPhases());
		phases.addAll(BalanceTaskPool.getInstance().getPhases());
		phases.addAll(ForkTaskPool.getInstance().getPhases());
		phases.addAll(MergeTaskPool.getInstance().getPhases());

		// 用户资源引用列表
		List<Refer> refers = StaffOnCallPool.getInstance().getRefers();
		
		// DEBUG CODE, START
		for (Refer e : refers) {
			Logger.debug(this, "reset", "login %s", e.getUsername());
		}
		// DEBUG CODE, END
		
		// 账号签名 -> 数据表映像
		TreeMap<Siger, SpaceSet> sets = new TreeMap<Siger, SpaceSet>();
		for(Refer refer : refers) {
			Siger siger = refer.getUsername();
			// 生成映像关系
			SpaceSet set = sets.get(siger);
			if(set == null) {
				set = new SpaceSet();
				sets.put(siger, set);
			}
			// 判断DATA数据表在CALL站点上注册，保证他们形成关联可使用！
			for (Space space : refer.getTables()) {
				boolean success = DataOnCallPool.getInstance().contains(space);
				if (!success) {
					Logger.warning(this, "reset", "DataSite missing! table is %s", space);
					continue;
				} 
				// 保存表名
				set.add(space);
				Logger.debug(this, "reset", "login table %s", space);
			}
		}
		
		// DEBUG CODE, start
		Logger.debug(this, "reset", "users size:%d, phases size:%d", sets.size(),phases.size());
		if (sets.size() > 0) {
			// 保存用户签名 -> 数据表名
			Iterator<Map.Entry<Siger, SpaceSet>> iterator = sets.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, SpaceSet> entry = iterator.next();
				Logger.debug(this, "reset", "%s table size:%d", 
						entry.getKey(), entry.getValue().size());
			}
		}
		for (Phase e : phases) {
			Logger.debug(this, "reset", "publish task %s", e);
		}
		// DEBUG CODE, end
		
		// 瞬时记录
		Moment moment = createMoment();
		
		List<CloudField> fields = StoreOnCallPool.getInstance().createCloudFields();
		
		for (CloudField field : fields) {
			Logger.debug(this, "reset", "%s # %d %d # %d %d", field.getSiger(),
					field.getMaxCapacity(), field.getUsedCapacity(),
					field.getDirectires(), field.getFiles());
		}

		// 重置数据（删除表空间和阶段命名，但是不能删除账号签名。删除账号在“DROP USER”时发生）
		super.lockSingle();
		try {
			// 清除旧数据
			local.reset();
			// 瞬时记录
			local.setMoment(moment);
			
			// 保存用户签名 -> 数据表名
			Iterator<Map.Entry<Siger, SpaceSet>> iterator = sets.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, SpaceSet> entry = iterator.next();
				Siger siger = entry.getKey();
				// 建立签名 
				local.create(siger);
				// 保存全部表
				local.addSpaces(siger, entry.getValue().list());
			}
			
			// 保存阶段命名
			for (Phase phase : phases) {
				local.addPhase(phase);
			}
			
			// 云端磁盘空间，参数：用户签名，规定尺寸，根目录，文件数量
			for (CloudField field : fields) {
				local.setCloudField(field);
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
		
		// 再注册
		Site site = reset();
		boolean success = (site != null);
		if (success) {
			success = super.login(site);
		}
		// 注册到主站点之后，再注册到关联站点
		if (success) {

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

	/**
	 * 解析FRONT用户虚拟空间
	 * @param document 成员
	 */
	protected void splitPrivate(org.w3c.dom.Document document) {
		splitMemberCyber(document);
		
		org.w3c.dom.Element root = (org.w3c.dom.Element) document.getElementsByTagName(SiteMark.MARK_LOCAL_SITE).item(0);

		// 管理节点规定子节点的一般延时注册时间，只针对TOP/HOME/BANK三类管理节点
		
		// FRONT用户限制
		String input = XMLocal.getValue(root, SiteMark.MAX_FRONTS);
		frontCyber.setPersons(ConfigParser.splitInteger(input, frontCyber.getPersons()));
		// 最大阀值
		input = XMLocal.getAttribute(root, SiteMark.MAX_FRONTS, SiteMark.MAX_FRONTS_THRESHOLD);
		frontCyber.setThreshold(ConfigParser.splitRate(input, frontCyber.getThreshold()));
		// 超时时间
		input = XMLocal.getAttribute(root, SiteMark.MAX_FRONTS, SiteMark.MAX_FRONTS_CHECKTIMEOUT);
		frontCyber.setTimeout(ConfigParser.splitTime(input, frontCyber.getTimeout()));

		Logger.info(this, "splitPrivate", "max fronts: %d, threshold: %.2f, check timeout: %d ms",
				frontCyber.getPersons(), frontCyber.getThreshold(), frontCyber.getTimeout());
	}
	
	/**
	 * 加载资源目录
	 * @param document
	 * @return 成功返回真，否则假
	 */
	private boolean loadResourcePath(org.w3c.dom.Document document) {
		// CALL节点资源配置目录
		String value = XMLocal.getXMLValue(document.getElementsByTagName("resource-directory"));
		// 建立目录
		boolean success = createResourcePath(value);

		Logger.note(this, "loadResourcePath", success, "create resource directory %s", value);
		return success;
	}

	/**
	 * 加载存储目录
	 * @param document
	 * @return
	 */
	private boolean loadStorePaths(org.w3c.dom.Document document) {
		// 存储数据目录
		org.w3c.dom.Element element = (org.w3c.dom.Element) document.getElementsByTagName("cloud-directories").item(0);
		String[] paths = XMLocal.getXMLValues(element.getElementsByTagName("directory"));
		
		int count = 0;
		for (String path : paths) {
			path = ConfigParser.splitPath(path);
			// 保存到
			StoreOnCallPool.getInstance().addRoot(path);
			Logger.info(this, "loadStorePaths", "store directory is '%s'", path);
			// 保存目录，定时检测
			addDeviceDirectory(path);
			count++;
		}
		return (count > 0);
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
	 * 加载/解析CALL节点资源配置
	 * @param filename
	 */
	public boolean loadLocal(String filename) {
		if (System.getSecurityManager() != null) {
			Logger.info(this, "loadLocal", "sandbox loaded!");
		}

		filename = ConfigParser.splitPath(filename);
		if (!Laxkit.hasFile(filename)) {
			Logger.error(this, "localLocal", "not found %s", filename);
			return false;
		}
		
		org.w3c.dom.Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			return false;
		}
		
		// 成员虚拟空间/FRONT用户虚拟空间
		splitPrivate(document);
		
		// 加载资源目录
		boolean success = loadResourcePath(document);
		// 加载云存储目录
		if (success) {
			success = loadStorePaths(document);
		}

		// 解析和设置HOME节点地址
		if (success) {
			success = splitHubSite(document);
		}
		// 解析和设置本地节点地址
		if (success) {
			success = splitGatewaySite(local, document);
		}
		// 解析和设置回显配置
		if (success) {
			success = splitEcho(document);
		}
		// 设置用户日志目录（CALL站点）
		if (success) {
			success = setUserLogPath(document);
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
		
		// 元数据缓存配置
		if (success) {
			success = setTaskMidPath(document, MetaPool.getInstance());
		}
		// 设置CONDUCT.INIT阶段管理池的发布目录
		if (success) {
			success = setTaskDeployPath(document, InitTaskPool.getInstance(), "init");
		}
		// 设置CONDUCT.BALANCE阶段管理池的发布目录
		if (success) {
			success = setTaskDeployPath(document, BalanceTaskPool.getInstance(), "balance");
		}
		// 设置ESTABLISH.ISSUE阶段管理池的发布目录
		if (success) {
			success = setTaskDeployPath(document, IssueTaskPool.getInstance(), "issue");
		}
		// 设置ESTABLISH.ASSIGN阶段管理池的发布目录
		if (success) {
			success = setTaskDeployPath(document, AssignTaskPool.getInstance(), "assign");
		}
		// 设置CONTACT.FORK阶段管理池的发布目录
		if (success) {
			success = setTaskDeployPath(document, ForkTaskPool.getInstance(), "fork");
		}
		// 设置CONTACT.MERGE阶段管理池的发布目录
		if (success) {
			success = setTaskDeployPath(document, MergeTaskPool.getInstance(), "merge");
		}

		//		// 设置码位计算器管理池的发布目录
		//		if(success) {
		//			success = setScaleDeployPath(document, ScalerPool.getInstance(), null);
		//		}

		// 解析“least-disk”属性
		if (success) {
			loadTimerTasks(document);
		}

		// 加载日志/追踪记录
		if (success) {
			success = loadLogResourceWithRemote(filename);
		}
		

		//		// 加载日志配置
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
	 * 启动
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}
		if (System.getSecurityManager() != null) {
			Logger.info("CallLauncher.main, sandbox loaded!");
		}
		
		// 将本地链接库目录导入“java.library.path”队列，加载本地目录下面的库文件
		JNILoader.init();

		String filename = args[0];
		boolean success = CallLauncher.getInstance().loadLocal(filename);
		Logger.note("CallLauncher.main, load local", success);
		if (success) {
			success = CallLauncher.getInstance().start();
			Logger.note("CallLauncher.main, start service", success);
		}
		if (!success) {
			Logger.gushing();
			System.exit(0);
		}
	}



}