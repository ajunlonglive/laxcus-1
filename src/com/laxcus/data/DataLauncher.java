/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data;

import java.util.*;

import org.w3c.dom.*;

import com.laxcus.access.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.data.pool.*;
import com.laxcus.data.rollback.*;
import com.laxcus.data.slider.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.launch.*;
import com.laxcus.launch.job.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.pool.archive.*;
import com.laxcus.pool.schedule.*;
import com.laxcus.site.*;
import com.laxcus.site.data.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.from.*;
import com.laxcus.task.establish.rise.*;
import com.laxcus.task.establish.scan.*;
import com.laxcus.task.flux.*;
import com.laxcus.task.talk.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.naming.*;
import com.laxcus.visit.impl.data.*;
import com.laxcus.xml.*;

/**
 * DATA站点启动器。
 * 
 * @author scott.liang
 * @version 1.5 10/23/2015
 * @since laxcus 1.0
 */
public final class DataLauncher extends JobLauncher implements TaskListener {

	/** DATA站点启动器静态句柄 **/
	private static DataLauncher selfHandle = new DataLauncher();

	/** DATA站点资源配置 */
	private DataSite local = new DataSite();
	
	
	/**
	 * 构造默认的DATA站点启动器
	 */
	private DataLauncher() {
		super();
		setExitVM(true);
		setPrintFault(true);
		// DATA节点监听
		setPacketInvoker(new DataPacketAdapter());
		setStreamInvoker(new DataStreamAdapter());
		// 支持人数，默认是10个
		setMaxPersons(10);
	}

	/**
	 * 返回数据节点启动器静态句柄
	 * @return DataLauncher实例
	 */
	public static DataLauncher getInstance() {
		// 调用句柄时，进行安全检查
		SiteLauncher.check("DataLauncher.getInstance");
		// 输出句柄
		return DataLauncher.selfHandle;
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
		return DataCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public InvokerPool getInvokerPool() {
		return DataInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return DataCustomTrustor.getInstance();
	}

	/**
	 * 返回当前站点级别（主站点或者从站点）
	 * @return 站点级别
	 */
	public byte getRank() {
		return local.getRank();
	}

	/**
	 * 判断是主站点
	 * @return  返回真或者假
	 */
	public boolean isMaster() {
		return local.isMaster();
	}

	/**
	 * 判断是从站点
	 * @return  返回真或者假
	 */
	public boolean isSlave() {
		return local.isSlave();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getSite()
	 */
	@Override
	public Site getSite() {
		return local;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TaskListener#refreshTask(int)
	 */
	@Override
	public void refreshTask(int family) {
		Logger.debug(this, "refreshTask", "task is %s", PhaseTag.translate(family));
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
	public boolean hasTaskUser(Siger username) {
		return StaffOnDataPool.getInstance().allow(username);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 1.预初始化
		boolean success = preload();
		Logger.note(this, "init", success, "preload");
		// 2. 启动FIXP监听服务器
		if (success) {
			Class<?>[] clazzs = { CommandVisitOnData.class };
			success = loadSingleListen(clazzs, local.getNode());
		}
		Logger.note(this, "init", success, "load listen");
		// 3. 启动公共管理池
		if (success) {
			success = loadCommonPool();
		}
		Logger.note(this, "init", success, "load common pool");
		// 4. 启动主站点管理池
		if (success && local.isMaster()) {
			success = loadPrimePool();
			Logger.note(this, "init", success, "load prime pool");
		}
		// 5. 注册到HOME节点（初次注册不携带本地资源数据。这是为获取网络资源数据，向HOME节点提供身份验证）
		if (success) {
			success = login();
		}
		Logger.note(this, "init", success, "login");
		// 6. 启动资源管理池
		if(success) {
			success = StaffOnDataPool.getInstance().start();
		}
		Logger.note(this, "init", success, "start staff");

		// 不成功时，执行停止操作。
		if (!success) {
			if(isLogined()) logout(); // 在登录成功状态下注销
			stopPool();
			stopListen();
			stopLog();
		}
		// 返回启动结果
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
		// 1. 从HOME节点注销
		logout();
		// 2. 停止池服务
		stopPool();
		// 3. 停止资源管理池
		StaffOnDataPool.getInstance().stop();
		while (StaffOnDataPool.getInstance().isRunning()) {
			delay(100);
		}
		// 4. 停止FIXP监听服务
		stopListen();
		// 5. 关闭日志服务
		stopLog();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#disableProcess()
	 */
	@Override
	protected void disableProcess() {

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
				StaffOnDataPool.getInstance().size()));

		return moment;
	}

	/**
	 * 更新全部阶段命名和索引
	 * @return 返回当前站点的数据副本
	 */
	private Site reset() {
		ArrayList<Phase> phases = new ArrayList<Phase>();
		phases.addAll(FromTaskPool.getInstance().getPhases());
		// 主站点才保存SCAN阶段命名
		if (isMaster()) {
			phases.addAll(ScanTaskPool.getInstance().getPhases());
		}
		phases.addAll(RiseTaskPool.getInstance().getPhases());
		
		// debug code, start
		for(Phase e : phases) {
			Logger.debug(this, "reset", "publish task %s", e);
		}
		// debug code, end

		// 生成数据块索引库
		StubSchema schema =	StaffOnDataPool.getInstance().createStubSchema();
		// 将数据块映像区分到各自的账号下面
		Map<Siger, DataMember> members = new TreeMap<Siger, DataMember>();
		for (StubTable that : schema.list()) {
			Space space = that.getSpace();
			StubReflex reflex = new StubReflex(space, that.size(), that.getAvailable());
			// 内存数据总长度
			long capacity = StaffOnDataPool.getInstance().findMemoryCapacity(space);
			reflex.setMemoryCapacity(capacity);
			// 保存用户签名和数据块区域
			Siger username = StaffOnDataPool.getInstance().findUser(space);
			// 如果没有找到账号签名，发生这个问题的可能是建立账号和删除账号时出现故障！
			if (username == null) {
				Logger.warning(this, "reset", "cannot be find user by %s", space);
				continue;
			}
			// 将数据块映像保存到不同的账号下面
			DataMember sub = members.get(username);
			if (sub == null) {
				sub = new DataMember(username);
				members.put(username, sub);
			}
			sub.addStubReflex(reflex);
		}

		List<Siger> users = StaffOnDataPool.getInstance().getSigers();
		
		// DEBUG CODE, START
		for (Siger e : users) {
			Logger.debug(this, "reset", "login %s", e);
		}
		// DEBUG CODE, END

		// 获取当前磁盘空间尺寸(自由空间和已经使用的空间)
		long[] capicaty = AccessTrustor.getDiskCapacity();
		
		// 瞬时记录
		Moment moment = createMoment();

		// 锁定！
		super.lockSingle();
		try {
			// 释放全部旧数据
			local.reset();
			// 瞬时记录
			local.setMoment(moment);
			// 自由空间尺寸和已经使用的空间尺寸
			if (capicaty != null && capicaty.length == 2) {
				local.setDiskFreeCapacity(capicaty[0]);
				local.setDiskUsedCapacity(capicaty[1]);
			}
			// 建立DATA站点成员
			for(Siger username : users) {
				local.create(username);
			}
			// 保存FROM/SCAN阶段命名
			for (Phase phase : phases) {
				local.addPhase(phase);
			}

			// 保存数据块区域
			Iterator<Map.Entry<Siger, DataMember>> iterator = members.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, DataMember> entry = iterator.next();
				Siger username = entry.getKey();
				// 根据用户签名，把索引映像区分配到它的名下
				for (StubReflex reflex : entry.getValue().getStubReflexes()) {
					local.addStubReflex(username, reflex);
				}
			}

			// 产生站点数据副本
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

		// 重置数据
		Site site = reset();
		// 判断有效
		boolean success = (site != null);
		// 注册
		if (success) {
			success = login(site);
		}
		// 在资源管理池启动情况下，要求资源管理池向HOME站点发送缓存映像数据块
		if (success && StaffOnDataPool.getInstance().isRunning()) {
			StaffOnDataPool.getInstance().reloadCacheReflexStub();
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
	 * 加载公共管理池
	 * @return 成功返回真，否则假
	 */
	private boolean loadCommonPool() {
//		// 设置码位计算器代理
//		IndexSector.setScaleTrustor(ScalerPool.getInstance());

		// 设置数字签名人代理（FluxTrustorPool内部判断数据执行人是存在且有效）
		FluxTrustorPool.getInstance().setSigerTrustor(FromManager.getInstance());

		// 设置组件事件监听器
		FromTaskPool.getInstance().setTaskListener(this);
		// 设置FROM阶段的中间数据委托代理
		FromTaskPool.getInstance().setFluxTrustor(FluxTrustorPool.getInstance());
		// 设置FROM阶段资源辅助器
		FromManager.getInstance().setSiteLauncher(this);
		FromManager.getInstance().setSwitchPool(DataSwitchPool.getInstance());
		FromManager.getInstance().setStaffPool(StaffOnDataPool.getInstance());
		FromTaskPool.getInstance().setFromTrustor(FromManager.getInstance());
		FromTaskPool.getInstance().setTalkTrustor(TalkPool.getInstance());

		// 设置RISE阶段事件监听器
		RiseTaskPool.getInstance().setTaskListener(this);
		// 设置RISE资源代理
		RiseManager.getInstance().setSwitchPool(DataSwitchPool.getInstance());
		RiseManager.getInstance().setInvokerPool(DataInvokerPool.getInstance());
		RiseManager.getInstance().setStaffPool(StaffOnDataPool.getInstance());
		RiseTaskPool.getInstance().setRiseTrustor(RiseManager.getInstance()); 
		RiseTaskPool.getInstance().setTalkTrustor(TalkPool.getInstance());

		VirtualPool[] pools = new VirtualPool[] {
				// ARCHIVE站点管理池
				AccountOnCommonPool.getInstance(),
				// 串行写操作管理池
				SerialSchedulePool.getInstance(),
				// 分布任务组件交互对话
				TalkPool.getInstance(),
				// 调用器
				DataCommandPool.getInstance(), DataInvokerPool.getInstance(),
				// CONDUCT.FROM
				FluxTrustorPool.getInstance(), FromTaskPool.getInstance(),
				// ESTABLISH.RISE
				RiseManager.getInstance(), RiseTaskPool.getInstance(),
//				// 码位计算器
//				ScalerPool.getInstance(),
				// 自定义COMMAND/INVOKER资源
				CustomClassPool.getInstance(), DataSwitchPool.getInstance()};
		// 全部启动
		return startAllPools(pools);
	}

	/**
	 * 加载只在主站点运行的管理池
	 * @return 成功返回真，否则假
	 */
	private boolean loadPrimePool() {
//		// 设置码位计算器统计池（只有主站点才统计列码位）
//		ScalerCountPool.getInstance().setScaleTrustor(ScalerPool.getInstance());

		// 设置SCAN阶段事件监听器
		ScanTaskPool.getInstance().setTaskListener(this);
		// 设置SCAN资源代理
		ScanManager.getInstance().setInvokerPool(DataInvokerPool.getInstance());
		ScanManager.getInstance().setStaffPool(StaffOnDataPool.getInstance());
		ScanTaskPool.getInstance().setScanTrustor(ScanManager.getInstance()); 
		ScanTaskPool.getInstance().setTalkTrustor(TalkPool.getInstance());

		VirtualPool[] pools = new VirtualPool[] {
				// ESTABLISH.SCAN
				ScanManager.getInstance(), ScanTaskPool.getInstance(),
				DataSliderPool.getInstance() };
		// 全部启动
		return startAllPools(pools);
	}

	/**
	 * 停止管理池服务
	 */
	private void stopPool() {
		VirtualPool[] pools = new VirtualPool[] {
				// 切换池
				DataSwitchPool.getInstance(),
				// 自定义COMMAND/INVOKER资源
				CustomClassPool.getInstance(),
				// 异步命令
				DataCommandPool.getInstance(), DataInvokerPool.getInstance(),
				// 分布任务组件交互对话池
				TalkPool.getInstance(),
				// CONDUCT.FROM管理池
				FromTaskPool.getInstance(),
				// ESTABLISH.SCA管理池（只允许主站点启动）
				ScanTaskPool.getInstance(),
				// ESTABLISH.RISE管理池
				RiseTaskPool.getInstance(),

				// SCAN/RISE存取代理
				ScanManager.getInstance(),
				RiseManager.getInstance(),

//				// 用户码位管理池，主/从站点启动
//				ScalerPool.getInstance(),
				
				// 列码位统计管理池，只在主站点启动
				DataSliderPool.getInstance(),

				// 数据资源
				FluxTrustorPool.getInstance(),
				// 串行写管理池
				SerialSchedulePool.getInstance(),
				// ARCHIVE站点管理池
				AccountOnCommonPool.getInstance()};
		// 全部停止
		stopAllPools(pools);
	}

	/**
	 * 解析和设置数据存储目录
	 * @param document
	 * @return
	 */
	private boolean loadCatalogs(Document document) {
		// 存储数据目录
		Element element = (Element) document.getElementsByTagName("catalogs").item(0);
		String regulate = XMLocal.getValue(element, "regulate");
		String cache = XMLocal.getValue(element, "cache");
		String[] chunks = XMLocal.getXMLValues(element.getElementsByTagName("chunk"));

		regulate = ConfigParser.splitPath(regulate);
		cache = ConfigParser.splitPath(cache);

		// 优化后的数据块目录
		int ret = AccessTrustor.setRegulateDirectory(regulate);
		Logger.note(this, "loadCatalogs", ret == 0, "regulate directory is '%s'", regulate);
		if (ret != 0) return false;
		// 缓存数据块目录
		ret = AccessTrustor.setCacheDirectory(cache);
		Logger.note(this, "loadCatalogs", ret == 0, "cache directory is '%s'", cache);
		if (ret != 0) return false;
		
		// 保存目录，定时检测
		addDeviceDirectory(regulate);
		addDeviceDirectory(cache);
		
		// 存储数据块目录
		for (String path : chunks) {
			path = ConfigParser.splitPath(path);
			ret = AccessTrustor.setChunkDirectory(path);
			Logger.note(this, "loadCatalogs", ret == 0, "chunk directory is '%s'", path);
			if (ret != 0) return false;

			// 保存目录，定时检测
			addDeviceDirectory(path);
		}
		return true;
	}

	/**
	 * 解析私有参数
	 * @param document
	 */
	private void splitPrivate(Document document) {
		// JNI 允许的最大工作线程
		String value = XMLocal.getXMLValue(document.getElementsByTagName("job-threads"));
		int threads = ConfigParser.splitInteger(value, 3);
		AccessTrustor.setWorker(threads);

		Logger.info(this, "splitPrivate", "job threads is %d", threads);
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
	 * 加载并且解析本地配置文件
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

		// 解析和设置HOME节点地址
		boolean success = splitHubSite(document);
		// 解析和设置本地节点地址
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
		
		if (!success) return false;

		Logger.info(this, "loadLocal", "load local resource");

		// DATA节点资源配置目录(如拥有的数据库表等)
		String path = XMLocal.getXMLValue(document.getElementsByTagName("resource-directory"));
		if (!createResourcePath(path)) {
			Logger.error(this, "loadLocal", "cannot create path %s", path);
			return false;
		}
		// 记录到定时检测中
		addDeviceDirectory(path);

		// 设置CONDUCT.FROM阶段任务发布管理目录
		if (success) {
			success = setTaskDeployPath(document, FromTaskPool.getInstance(), "from");
		}
		// 设置ESTABLISH.SCAN阶段任务发布目录
		if (success) {
			success = setTaskDeployPath(document, ScanTaskPool.getInstance(), "scan");
		}
		// 设置ESTABLISH.RISE阶段任务发布目录
		if (success) {
			success = setTaskDeployPath(document, RiseTaskPool.getInstance(), "rise");
		}
		// 设置CONDUCT中间数据存取目录
		if (success) {
			success = setTaskMidPath(document, FluxTrustorPool.getInstance());
		}
		// 设置ESTABLISH.SCAN数据缓存目录
		if(success) {
			success = setTaskMidPath(document, ScanManager.getInstance());
		}
		// 设置ESTABLISH.RISE数据缓存目录
		if(success) {
			success = setTaskMidPath(document, RiseManager.getInstance());
		}

//		// 设置列码位组件管理池目录
//		if(success) {
//			success = setScaleDeployPath(document, ScalerPool.getInstance(), null);
//		}

		if (!success) {
			return false;
		}

		// 解析和设置本地的存储目录(暂时封闭）
		if (!loadCatalogs(document)) {
			Logger.error(this, "loadLocal", "cannot load directory");
			return false;
		}

		// 判断是有效的站点，否则出错
		if(local.isNoneRank()) {
			Logger.error(this, "loadLocal", "illegal site: %s", local);
			return false;
		}
		// 设置JNI.DB的站点级别
		int ret = AccessTrustor.setRank(local.getRank());
		success = (ret == 0);
		if (!success) {
			Logger.error(this, "loadLocal", "cannot be set %d", local.getRank());
			return false;
		}

		// 如果是主节点，有一个回滚目录。它只在DATA主节点定义
		if (local.isMaster()) {
			// 数据回滚目录，只在DATA主节点有效
			path = XMLocal.getXMLValue(document.getElementsByTagName("rollback-directory"));
			if (path == null || path.isEmpty()) {
				Logger.error(this, "loadLocal", "cannot be find 'rollback-directory'");
				return false;
			}
			// 设置目录
			RollbackArchive.setDirectory(path);
			
			// 记录回滚目录，定时检测
			addDeviceDirectory(path);

			Logger.info(this, "localLocal", "rollback directory:\"%s\"", RollbackArchive.getDirectory());
		}

		// 解析私有参数
		splitPrivate(document);
		
		// 解析“least-disk”属性
		loadTimerTasks(document);
		
		// 加载日志/追踪记录
		success = loadLogResourceWithRemote(filename);

		//		// 加载日志配置
		//		success = Logger.loadXML(filename);
		//		if (success) {
		//			loadLogDeviceDirectory();
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
	 * 运行DATA站点
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		// 安全检查
		if (System.getSecurityManager() != null) {
			Logger.info("sandbox loaded!");
		}
		// 将本地链接库目录导入“java.library.path”队列，加载本地目录下面的库文件
		JNILoader.init();

		// 初始化数据存取接口
		int ret = AccessTrustor.initialize(null);
		if (ret != 0) {
			Logger.error("initialize failed, program will exit!");
			Logger.gushing();
			return;
		}
		// 解析并且设置本地资源配置
		String filename = args[0];
		boolean success = DataLauncher.getInstance().loadLocal(filename);
		Logger.note("DataLauncher.main, load local", success);
		// 启动DATA站点
		if (success) {
			success = DataLauncher.getInstance().start();
			Logger.note("DataLauncher.main, start service", success);
		}
		if (!success) {
			Logger.gushing();
			System.exit(0);
		}
	}

}