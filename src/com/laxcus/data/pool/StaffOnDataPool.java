/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.pool;

import java.io.*;
import java.util.*;

import com.laxcus.access.*;
import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.access.stub.chart.*;
import com.laxcus.access.stub.index.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.stub.*;
import com.laxcus.command.task.*;
import com.laxcus.data.*;
import com.laxcus.data.invoker.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.conduct.from.*;
import com.laxcus.task.establish.rise.*;
import com.laxcus.task.establish.scan.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * DATA站点资源管理池。<br>
 * 
 * 提供本地数据块资源的映像/检索服务。
 * 
 * @author scott.liang
 * @version 1.3 3/23/2015
 * @since laxcus 1.0
 */
public final class StaffOnDataPool extends VirtualPool {

	/** DATA站点资源管理池句柄 **/
	private static StaffOnDataPool selfHandle = new StaffOnDataPool();

	/** 数据块编号文件名称 **/
	private final String STUBS = "stubs.conf";

	/** 用户资源引用文件名称 **/
	private final String SKETCHES = "sketches.conf";

	/** 用户名称 -> 用户资源引用 **/
	private Map<Siger, Refer> mapRefers = new TreeMap<Siger, Refer>();

	/** 数据表名 -> 表实例。在运行中从网络获得 **/
	private Map<Space, Table> mapTables = new TreeMap<Space, Table>();

	/** 数据表名 -> 数据块索引图。在运行过程中产生和更新 */
	private Map<Space, StubChartSheet> mapSheets = new TreeMap<Space, StubChartSheet>();

	/** 重新加载索引 **/
	private boolean reloadIndex;

	/** 重新加载缓存映像数据块 **/
	private boolean reloadReflexStub;

	/** 故障表 **/
	private TreeSet<FaultTable> faultTables = new TreeSet<FaultTable>();

	/**
	 * 构造DATA站点资源管理池
	 */
	private StaffOnDataPool() {
		super();
		reloadIndex = false;
		reloadReflexStub = false;
	}

	/**
	 * 返回DATA站点资源管理池句柄
	 * @return
	 */
	public static StaffOnDataPool getInstance() {
		// 对调用者进行检查
		VirtualPool.check("StaffOnDataPool.getInstance");
		// 返回句柄
		return StaffOnDataPool.selfHandle;
	}

	/**
	 * 重新加载索引
	 */
	public void reloadIndex() {
		reloadIndex = true;
		wakeup();
	}

	/**
	 * 重新加载缓存映像块
	 */
	public void reloadCacheReflexStub() {
		reloadReflexStub = true;
		wakeup();
	}

	/**
	 * 判断需要回滚
	 * @return 返回真或者假
	 */
	private boolean isRollback() {
		// 必须是DATA主节点
		boolean success = DataLauncher.getInstance().isMaster();
		// 取出回滚参数。回滚参数定义是DATA站点启动时，由管理员手动输入。
		if(success) {
			String value = System.getProperty("laxcus.rollback"); // 取出回滚参数
			success = (value != null && value.matches("^\\s*(?i)(YES|OK|OKAY)\\s*$"));
		}

		Logger.debug(this, "isRollback", "result is %s", success);

		return success;
	}

	/**
	 * 加载分布资源，这项工作在线程里进行。<br>
	 * 在线程里处理的原因是：加载资源是长耗时工作，在线程里加载可避免影响DataLauncher线程运行。
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean pretreat() {
		Logger.info(this, "pretreat", "into...");

		// 1. 从磁盘中加载数据块编号，并且放入JNI
		boolean success = loadStubs();
		// 2. 加载表和启动JNI
		if (success) {
			success = loadJNI();
		}
		// 3. 检测回滚操作。此操作由管理员在启动DATA主节点时执行。保证不受干扰情况下执行
		if (success && isRollback()) {
			success = rollback();
		}
		// 4. 加载索引
		if (success) {
			success = loadIndex();
		}
		// 5. 加载ACCOUNT站点
		if (success) {
			success = loadAccountSites();
		}
		// 6. 加载本地任务组件
		if (success) {
			FromTaskPool.getInstance().load();
			RiseTaskPool.getInstance().load();
			// 如果是DATA主站点，启动数据构造的“SCAN”阶段管理池
			if (DataLauncher.getInstance().isMaster()) {
				ScanTaskPool.getInstance().load();
			}
		}
		// 7. 从网络获取和加载更新的分布任务组件和码位计算器（异步获取）
		if (success) {
			loadTasks();

			//			// 加载组件签名相关的资源引用
			//			loadTaskRefers();

			// 向HOME节点报告故障表！
			submitFaultTables();

			// 加载缺失的系统分布任务组件
			loadSystemTasks();
		}

		// 以上不成功，停止JNI服务
		if (!success) {
			stopJNI();
		}

		Logger.note(this, "pretreat", success, "load staff");

		// 返回结果
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		//		Logger.info(this, "init", "into...");
		//
		//		// 1. 从磁盘中加载数据块编号，并且放入JNI
		//		boolean success = loadStubs();
		//		// 2. 加载表和启动JNI
		//		if (success) {
		//			success = loadJNI();
		//		}
		//		// 3. 检测回滚操作。此操作由管理员在启动DATA主节点时执行。保证不受干扰情况下执行
		//		if (success && isRollback()) {
		//			success = rollback();
		//		}
		//		// 4. 加载索引
		//		if (success) {
		//			success = loadIndex();
		//		}
		//		// 5. 加载ACCOUNT站点
		//		if (success) {
		//			success = loadArchiveSites();
		//		}
		//		// 6. 加载本地任务组件
		//		if (success) {
		//			FromTaskPool.getInstance().reload();
		//			RiseTaskPool.getInstance().reload();
		//			// 如果是DATA主站点，启动数据构造的“SCAN”阶段管理池
		//			if (DataLauncher.getInstance().isMaster()) {
		//				ScanTaskPool.getInstance().reload();
		//			}
		//		}
		//		// 7. 从网络获取和加载更新的分布任务组件和码位计算器（异步获取）
		//		if (success) {
		//			loadTasks();
		//			loadScalers();
		//		}
		//
		//		// 以上不成功，停止JNI服务
		//		if(!success) {
		//			stopJNI();
		//		}
		//
		//		Logger.note(this, "init", success, "load staff");
		//
		//		// 返回结果
		//		return success;

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		// 加载分布资源
		final boolean success = pretreat();
		// 成功通知DATA站点重新注册，否则通知DATA站点退出
		if (success) {
			DataLauncher.getInstance().checkin(false);
			// 加载用户资源检测器
			loadMemberChecker();
			// 推送注册成员
			pushRegisterMember();
		} else {
			DataLauncher.getInstance().stop();
		}

		// 加载数据块编号的检查间隔时间是1分钟
		final long interval = 60000L;
		// 触发时间
		long touchTime = System.currentTimeMillis() + interval; 
		// 检查更新索引和加载数据块编号
		while (!isInterrupted()) {
			// 不成功，循环等待退出
			if (!success) {
				delay(1000);
				continue;
			}

			// 正常情况...
			if (reloadIndex) {
				reloadStubIndex(); // 更新索引
				reloadIndex = false;
			} else if (reloadReflexStub) {
				doSetCacheReflexStub(); // 重新设置缓存映像数据块
				reloadReflexStub = false;
			} else if (System.currentTimeMillis() >= touchTime) { // 达到触发时间时...
				doTakeStubs(); // 检查剩余数据块编号，获取新编号
				touchTime = System.currentTimeMillis() + interval;
			} else {
				delay(1000L); // 延时1秒
			}

			// 检查最小内存，低于阀值就报警！
		}

		// 如果以上成功，在退出前关闭服务
		if (success) {
			// 写入数据块编号
			writeStubs();
			// 释放JNI
			stopJNI();
		}

		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapSheets.clear();
		mapTables.clear();
		mapRefers.clear();
	}
	
	/**
	 * 统计成员数
	 * 
	 * @return 整数
	 */
	public int size() {
		super.lockMulti();
		try {
			return mapRefers.size();
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 启动用户资源检测器，定时检测节点上的用户数目
	 */
	private void loadMemberChecker() {
		DataLauncher launcher = (DataLauncher) getLauncher();
		MemberCyber cyber = launcher.getMemberCyber();
		Timer timer = getLauncher().getTimer();
		MemberChecker checker = new MemberChecker(this);
		timer.schedule(checker, 0, cyber.getTimeout());
	}

	/**
	 * 推送注册成员给WATCH节点，经过HOME转发
	 */
	private void pushRegisterMember() {
		List<Siger> sigers = getSigers();
		if (sigers.size() > 0) {
			ShiftPushRegisterMember shift = new ShiftPushRegisterMember(sigers);
			getCommandPool().admit(shift);
		}
	}

	/**
	 * 检查在线用户数，发出报告
	 */
	protected void checkMembers() {
		DataLauncher launcher = (DataLauncher) getLauncher();
		MemberCyber cyber = launcher.getMemberCyber();

		// 判断用户数满员/虚拟空间不足
		int members = size();
		if (cyber.isFull(members)) {
			MemberFull cmd = new MemberFull(cyber.getPersons(), members);
			getCommandPool().admit(cmd);
		} else if (cyber.isMissing(members)) {
			MemberMissing cmd = new MemberMissing(cyber.getPersons(), members);
			getCommandPool().admit(cmd);
		}
	}

	/**
	 * 判断和加载缺失的系统组件。
	 * 如果存在这个现象，通过HOME->TOP->BANK，随机获取一个ACCOUNT站点，加载系统分布任务组件。
	 */
	private void loadSystemTasks() {
		TakeSystemTaskSite cmd = new TakeSystemTaskSite();
		ShiftLoadSystemTask shift = new ShiftLoadSystemTask(cmd);

		// 分布任务组件池
		ArrayList<TaskPool> pools = new ArrayList<TaskPool>();
		pools.add(FromTaskPool.getInstance());
		pools.add(RiseTaskPool.getInstance());
		// 主站点需要检查SCAN阶段组件，从站点不需要检查SCAN阶段组件
		if (DataLauncher.getInstance().isMaster()) {
			pools.add(ScanTaskPool.getInstance());
		}

		// 判断有组件
		for (int index = 0; index < pools.size(); index++) {
			TaskPool pool = pools.get(index);
			boolean success = pool.hasSystemTask();
			if (!success) {
				shift.addFamily(pool.getFamily());
			}
		}

		// 有缺失的组件，启动查询ACCOUNT站点
		if (shift.hasFamily()) {
			DataCommandPool.getInstance().press(shift);
		}
	}

	/**
	 * 返回资源引用
	 * @param siger 用户签名
	 * @return 资源引用
	 */
	public Refer findRefer(Siger siger) {
		super.lockMulti();
		try {
			return mapRefers.get(siger);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断已经有资源引用
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean hasRefer(Siger siger) {
		return findRefer(siger) != null;
	}

	/**
	 * 输出全部用户资源引用
	 * @return Refer列表
	 */
	public List<Refer> getRefers() {
		super.lockMulti();
		try {
			return new ArrayList<Refer>(mapRefers.values());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据用户签名分配用户资源引用
	 * @param siger 用户签名
	 * @return 返回Refer实例
	 */
	private Refer allocate(Siger siger) {
		Refer refer = mapRefers.get(siger);
		if(refer == null) {
			refer = new Refer(new User(siger));
			mapRefers.put(refer.getUsername(), refer);
		}
		return refer;
	}

	/**
	 * 根据一个用户签名建立用户资源引用。<br>
	 * 这个方法是当HOME站点发送建立账号或者建表命令后才调用。
	 * 
	 * @param siger 注册用户名称
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean create(Siger siger) {
		Laxkit.nullabled(siger);

		// 如果存在，不能够再建立
		if (hasRefer(siger)) {
			Logger.error(this, "create", "'%s' has created!", siger);
			return false;
		}

		// 保存新记录
		boolean success = false;
		super.lockSingle();
		try {
			Refer refer = allocate(siger);
			success = (refer != null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 建立账号成功，立即更新账号记录
		if (success) {
			writeSketches();
		}

		Logger.note(this, "create", success, "create '%s'", siger);

		return success;
	}

	/**
	 * 根据用户签名，删除它的全部配置参数，以及磁盘上的存储数据。
	 * 
	 * @param siger 用户签名
	 * @return 删除成功返回真，否则假。
	 */
	public boolean drop(Siger siger) {
		Laxkit.nullabled(siger);

		// 锁定删除
		boolean success = false;
		super.lockSingle();
		try {
			Refer refer = mapRefers.get(siger);
			success = (refer != null);
			// 存在，删除记录
			if (success) {
				for (Space space : refer.getTables()) {
					// 删除磁盘数据
					int ret = AccessTrustor.deleteSpace(space);
					boolean b = (ret >= 0);
					Logger.note(this, "drop", b, "drop table %s code %d", space, ret);

					// 删除表配置
					mapTables.remove(space);
				}
				// 在最后删除内存配置
				mapRefers.remove(siger);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 删除成功，立即更新磁盘记录
		if (success) {
			writeSketches();
		}

		Logger.note(this, "drop", success, "drop %s", siger);
		return success;
	}

	/**
	 * 接受来自“SetRefer”的资源引用 赋值，以相同的资源引用和本地表为条件，保存赋值引用的授权单元。<br>
	 * 通过资源引用和授权单元，在“allow”方法里，判断发起请求的用户具备操作许可。<br><br>
	 * 
	 * 授权单元是数据表持有人，通过“open share table/open share database”命令分配给其他用户，操作自己数据表的权利许可。
	 * 
	 * @param that 传入的引用
	 * @return 成功返回真，否则假
	 */
	public boolean buckle(Refer that) {
		boolean success = false;
		Siger siger = that.getUsername();

		super.lockSingle();
		try {
			// 1. 判断资源引用存在
			Refer refer = mapRefers.get(siger);
			success = (refer != null);
			// 2. 更新授权单元（授权单元是用户通过命令分配给其他用户的操作许可）
			if (success) {
				// 处理授权单元，即授权人给被授权人的单元
				refer.clearActiveItems();
				// 保存授权单元有两个条件：1. 本地有这个表，2. 资源引用有这个表。然后保存相关基于表的授权单元。
				for(Space space : mapTables.keySet()) {
					// 如果不是授权人的表，忽略它！
					if(!refer.hasTable(space)) {
						continue;
					}
					// 保存基于同表名的授权单元
					refer.addActiveItems(that.findActiveItems(space));
				}
			}

			//				// 清除旧的
			//				refer.clearActiveItems();
			//				refer.clearPassiveItems();
			//
			//				// 以表为基础，复制这个账号中相关的
			//				for (Space space : mapTables.keySet()) {
			//					refer.addActiveItems(that.findActiveItems(space));
			//					refer.addPassiveItems(that.findPassiveItems(space));
			//				}

			//				// 复制全部
			//				refer.addActiveItems(that.getActiveItems());
			//				refer.addPassiveItems(that.getPassiveItems());

			//				// 判断条件，被授权答是当前用户，授权表存在于当前节点
			//				for (PassiveItem item : that.getPassiveItems()) {
			//					Space space = item.getSpace();
			//					boolean exists = (mapTables.get(space) != null);
			//					if (exists) {
			//						refer.addPassiveItem(item);
			//					}
			//				}
			//				
			//				// 保存新的
			//				for (Space space : refer.getTables()) {
			//					List<ActiveItem> ai = that.findActiveItems(space);
			//					List<PassiveItem> pi = that.findPassiveItems(space);
			//					Logger.debug(this, "buckle", "find %s, active items:%d, passive items:%d",
			//							space, ai.size(), pi.size());
			//					refer.addActiveItems(ai);
			//					refer.addPassiveItems(pi);
			//
			////					refer.addActiveItems(that.findActiveItems(space));
			////					refer.addPassiveItems(that.findPassiveItems(space));
			//				}

			//				// debug code, start
			//				Logger.debug(this, "buckle", "active item size:%d, passive item size:%d",
			//						refer.getActiveItems().size(), refer.getPassiveItems().size());
			//				for (ActiveItem e : refer.getActiveItems()) {
			//					Logger.debug(this, "buckle", "active item： %s", e);
			//				}
			//				for (PassiveItem e : refer.getPassiveItems()) {
			//					Logger.debug(this, "buckle", "passive item： %s", e);
			//				}
			//				Logger.debug(this, "block", "active item size:%d, passive item size:%d",
			//						that.getActiveItems().size(), that.getPassiveItems().size());
			//				for (ActiveItem e : that.getActiveItems()) {
			//					Logger.debug(this, "block", "active item： %s", e);
			//				}
			//				for (PassiveItem e : that.getPassiveItems()) {
			//					Logger.debug(this, "block", "passive item： %s", e);
			//				}
			//				// debug code, end


		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.note(this, "buckle", success, "update %s", siger);

		return success;
	}

	/**
	 * 重装加载数据块索引
	 */
	private void reloadStubIndex() {
		boolean success = loadIndex();
		if (success) {
			// 通知站点重新注册
			getLauncher().checkin(false);
		}
	}

	/**
	 * 向HOME站点发送映缓存映像数据块编号集合
	 */
	private void doSetCacheReflexStub() {
		// 未启动不加载
		if (!AccessTrustor.isLaunched()) {
			Logger.warning(this, "doSetCacheReflexStub", "cannot be load access");
			return;
		}
		// 限制从站点使用这项功能
		if (!DataLauncher.getInstance().isSlave()) {
			return;
		}

		/** 本地地址 **/
		Node local = getLauncher().getListener();

		// 从JNI接口抓取关联的参数
		List<CacheReflexStub> list = AccessTrustor.getCacheReflexStubs(local);

		Logger.debug(this, "doSetCacheReflexStub", "cache reflex stub size:%d",
				(list == null ? -1 : list.size()));

		if (list == null) {
			return;
		}

		// 设置命令
		SetCacheReflexStub cmd = new SetCacheReflexStub();
		// 单向和快速投递（不需要返回应答，走快速数据处理通道）
		cmd.setDirect(true);
		cmd.setQuick(true);
		cmd.addAll(list);

		// 快速提交给HOME节点，再经HOME转发给CALL（包括发送空数据）
		DataCommandPool.getInstance().press(cmd);
	}

	/**
	 * 从HOME站点获得新的数据块编号
	 */
	private void doTakeStubs() {
		// 只有主站点才需要获取数据块
		if (!DataLauncher.getInstance().isMaster()) {
			return;
		}
		int count = AccessTrustor.getCountFreeStubs();
		if (count >= 5) {
			return;
		}

		TakeStub cmd = new TakeStub(5); // 申请5个数据块编号
		TakeStubHook hook = new TakeStubHook();
		ShiftTakeStub shift = new ShiftTakeStub(cmd, hook);

		// 进入快车道
		boolean success = DataCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "doTakeStub", "cannot be send to home");
			return;
		}
		// 进入逗留状态，直到收到处理结果
		hook.await();

		StubProduct product = hook.getStubProduct();
		success = (product != null);
		// 取出数据块编号，保存到磁盘上
		if (success) {
			for (long stub : product.list()) {
				AccessTrustor.addStub(stub);
			}
		}
	}

	/**
	 * 统计一个数据表空间的索引数据尺寸（元数据）
	 * @param space 数据表名
	 * @return 实时统计内存索引尺寸
	 */
	public long findMemoryCapacity(Space space) {
		long capacity = 0L;
		super.lockMulti();
		try {
			StubChartSheet sheet = mapSheets.get(space);
			if (sheet != null) {
				capacity = sheet.getMemoryCapacity();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "findMemoryCapacity", "%s memory capacity is %d", space, capacity);

		return capacity;
	}

	/**
	 * 使用SQL查询语句，查找关联的数据块编号
	 * @param query SQL查询语句
	 * @return 数据块编号集合。
	 */
	public StubSet query(Query query) {
		Space space = query.getSpace();
		super.lockMulti();
		try {
			StubChartSheet sheet = mapSheets.get(space);
			if (sheet != null) {
				return sheet.find(query.getWhere());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 返回表集合
	 * @return
	 */
	public Set<Space> getSpaces() {
		super.lockMulti();
		try {
			return new TreeSet<Space>(mapTables.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据数据表名，判断数据表存在
	 * @param space 数据表名
	 * @return 存在返回真，否则假
	 */
	public boolean hasTable(Space space) {
		return findTable(space) != null;
	}

	/**
	 * 建立表空间，配置数据写入内存
	 * @param table 表配置
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean createTable(Table table) {
		Laxkit.nullabled(table);

		// 检查表
		Space space = table.getSpace();
		// 如果表存在，拒绝操作
		if (hasTable(space)) {
			Logger.error(this, "createTable", "refuse %s", space);
			return false;
		}

		// 建立磁盘数据空间
		int ret = AccessTrustor.createSpace(table);
		boolean success = (ret >= 0);

		Logger.note(this, "createTable", success, "create '%s' return code %d", space, ret);

		// 保存参数
		if (success) {
			super.lockSingle();
			try {
				// 建立用户资源
				Refer refer = allocate(table.getIssuer());
				refer.addTable(table.getSpace());
				// 保存表配置
				mapTables.put(space, table);
			} catch (Throwable e) {
				Logger.fatal(e);
			} finally {
				super.unlockSingle();
			}
		}

		if (success) {
			// 用户资源写入本地磁盘
			writeSketches();
			// 通知线程重新加载索引
			reloadIndex();
		}

		return success;
	}

	/**
	 * 根据数据表名，删除它磁盘上全部表数据
	 * @param space 数据表名
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean dropTable(Space space) {
		Laxkit.nullabled(space);

		// 如果数据表不存在，拒绝操作
		if (!hasTable(space)) {
			Logger.error(this, "dropTable", "cannot be find %s", space);
			return false;
		}

		// 调用JNI接口删除表空间及下属的全部数据
		int ret = AccessTrustor.deleteSpace(space);
		// 负数是错误码，否则是成功
		boolean success = (ret >= 0);

		Logger.note(this, "dropTable", success, "drop '%s' return code %d", space, ret);

		// 建立配置
		if (success) {
			super.lockSingle();
			try {
				// 删除索引
				mapSheets.remove(space);
				// 删除表
				Table table = mapTables.remove(space);
				// 删除用户资源引用中的表空间
				Refer refer = mapRefers.get(table.getIssuer());
				if (refer != null) {
					refer.removeTable(space);
					// 无单元删除基于某个表的授权单元/被授权单元
					refer.dropActiveItems(space);
					refer.dropPassiveItems(space);
				}
			} catch (Throwable e) {
				Logger.fatal(e);
			} finally {
				super.unlockSingle();
			}
		}

		if(success) {
			// 用户资源引用写入磁盘
			writeSketches();
			// 通知线程重新加载索引
			reloadIndex();
		}

		return success;
	}

	/**
	 * 根据数据表名，查找数据表
	 * 这个操作只在本地执行，不发生网络通讯。
	 * 
	 * @param space 数据表名
	 * @return 返回数据表实例
	 */
	public Table findTable(Space space) {
		Laxkit.nullabled(space);

		super.lockMulti();
		try {
			return mapTables.get(space);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据数据表名，查找它的用户签名
	 * @param space 数据表名
	 * @return 返回签名实例
	 */
	public Siger findUser(Space space) {
		Laxkit.nullabled(space);

		Siger siger = null;
		super.lockMulti();
		try {
			Table table = mapTables.get(space);
			if (table != null) {
				siger = table.getIssuer();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "findUser", "%s is %s", space, siger);

		return siger;
	}

	/**
	 * 查找一个账号下的全部数据表名
	 * @param siger 用户签名
	 * @return 数据表名列表
	 */
	public List<Space> findSpaces(Siger siger) {
		ArrayList<Space> array = new ArrayList<Space>();
		super.lockMulti();
		try {
			Refer refer = mapRefers.get(siger);
			if (refer != null) {
				array.addAll(refer.getTables());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 查找与数据库关联的全部数据表
	 * @param fame 数据库名
	 * @return 返回关联的表名集合
	 */
	public List<Space> findSpaces(Fame fame) {
		ArrayList<Space> array = new ArrayList<Space>();
		// 查找表名
		super.lockMulti();
		try {
			for(Space space : mapTables.keySet()) {
				if (Laxkit.compareTo(space.getSchema(), fame) == 0) {
					array.add(space);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return array;
	}

	/**
	 * 根据数据表名，去管理站点（HOME站点）搜索数据表
	 * @param space 数据表名
	 * @return Table实例
	 */
	private Table searchTable(Space space) {
		TakeTable cmd = new TakeTable(space);
		TakeTableHook hook = new TakeTableHook();
		ShiftTakeTable shift = new ShiftTakeTable(cmd, hook);

		boolean success = DataCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "searchTable", "cannot submit to hub");
			return null;
		}
		// 进入悬停状态
		hook.await();

		return hook.getTable();
	}

	/**
	 * 输出当前注册的全部用户签名
	 * @return 用户签名列表
	 */
	public List<Siger> getSigers() {
		super.lockMulti();
		try {
			return new ArrayList<Siger>(mapRefers.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 向ACCOUNT站点获取分布任务组件 <br>
	 * 获取过程是一个异步处理模式，发送命令后不再等待。
	 */
	private void loadTasks() {
		// 没有ACCOUNT站点，不处理
		if (AccountOnCommonPool.getInstance().isEmpty()) {
			return;
		}
		// 加载系统级分布组件
		loadTasks(null);
		// 加载用户级分布组件
		List<Siger> sigers = getSigers();
		for (Siger siger : sigers) {
			loadTasks(siger);
		}
	}

	/**
	 * 向ACCOUNT站点获取分布组件
	 * @param siger 用户签名
	 * @param family 阶段类型
	 * @return 命令受理并且提交给命令管理池返回真，否则假
	 */
	public boolean loadTask(Siger siger, int family) {
		// 判断签名被允许
		if (siger != null) {
			if (!hasRefer(siger)) {
				Logger.error(this, "loadTask", "refuse %s", siger);
				return false;
			}
		}
		// 提交给命令管理池
		TaskPart part = new TaskPart(siger, family);
		TakeTaskTag cmd = new TakeTaskTag(part);
		return DataCommandPool.getInstance().admit(cmd);
	}

	/**
	 * 加载某个账号下的全部分布组件
	 * @param siger 用户签名
	 * @return 返回被受理的数目
	 */
	public int loadTasks(Siger siger) {
		int count = 0;
		// 加载CONDUCT.FORM阶段组件
		boolean success = loadTask(siger, PhaseTag.FROM);
		if (success) {
			count++;
		}
		// 主站点加载ESTABLISH.SCAN阶段组件
		if (DataLauncher.getInstance().isMaster()) {
			success = loadTask(siger, PhaseTag.SCAN);
			if (success) {
				count++;
			}
		}
		// 主从站点加载ESTABLISH.RISE阶段组件
		success = loadTask(siger, PhaseTag.RISE);
		if (success) {
			count++;
		}

		Logger.debug(this, "loadTasks", "load count %d", count);
		return count;
	}

	/**
	 * 删除CONDUCT.FROM/ESTABLISH.SCAN/ESTABLISH.RISE阶段管理池下的分布组件
	 * @param issuer 用户签名
	 * @return 被删除的组件总数
	 */
	public boolean dropTask(Siger issuer) {
		int count = 0;
		if (FromTaskPool.getInstance().hasGroup(issuer)) {
			boolean b = FromTaskPool.getInstance().drop(issuer);
			count += (b ? 1 : -1);
		}
		if (ScanTaskPool.getInstance().hasGroup(issuer)) {
			boolean b = ScanTaskPool.getInstance().drop(issuer);
			count += (b ? 1 : -1);
		}
		if (RiseTaskPool.getInstance().hasGroup(issuer)) {
			boolean b = RiseTaskPool.getInstance().drop(issuer);
			count += (b ? 1 : -1);
		}
		return count > 0;
	}

//	/**
//	 * 向ACCOUNT站点获取码位计算器组件
//	 */
//	private void loadScalers() {
//		// 没有ACCOUNT站点，不处理
//		if (AccountOnCommonPool.getInstance().isEmpty()) {
//			return;
//		}
//		// 加载用户的码位计算器（码位计算器没有系统级组件，全部是用户）
//		List<Siger> users = getSigers();
//		for (Siger siger : users) {
//			loadScaler(siger);
//		}
//	}

//	/**
//	 * 加载码位计算器
//	 * @param siger 用户签名
//	 * @return 成功返回真，否则假
//	 */
//	public boolean loadScaler(Siger siger) {
//		TakeScalerTag cmd = new TakeScalerTag(siger);
//		return DataCommandPool.getInstance().admit(cmd);
//	}

//	/**
//	 * 删除码位计算器
//	 * @param siger 用户签名
//	 * @return 成功返回真，否则假。
//	 */
//	public boolean dropScaler(Siger siger) {
//		int count = ScalerPool.getInstance().drop(siger);
//		return count > 0;
//	}

	/**
	 * 保存故障表
	 * @param e 实例 
	 * @return 成功返回真，否则假
	 */
	private boolean addFaultTable(FaultTable e) {
		super.lockSingle();
		try {
			return faultTables.add(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 向HOME节点提交发生故障的表，转交管理员请求检查
	 */
	private void submitFaultTables() {
		List<FaultTable> tables = getFaultTables();
		if (tables.size() > 0) {
			SubmitFaultTable cmd = new SubmitFaultTable();
			cmd.addAll(tables);
			getCommandPool().admit(cmd);
		}
	}

	/**
	 * 提取全部故障表
	 * @return 输出故障表
	 */
	public List<FaultTable> getFaultTables() {
		super.lockMulti();
		try {
			return new ArrayList<FaultTable>(faultTables);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断故障表存在
	 * @param e 故障表
	 * @return 返回真或者假
	 */
	public boolean hasFaultTable(FaultTable e) {
		super.lockMulti();
		try {
			return faultTables.contains(e);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 从磁盘读取并且输入数据块编号到JNI
	 * @return 成功返回真，否则假
	 */
	private boolean loadStubs() {
		List<java.lang.Long> array = readStubs();
		// 如果是空集合，去网络上获取新的数据块
		if (array.isEmpty()) {
			Logger.warning(this, "loadStubs", "disk stubs is empty set");
			doTakeStubs();
			return true;
		}

		for (long stub : array) {
			int ret = AccessTrustor.addStub(stub);
			boolean success = (ret == 0);
			Logger.note(this, "loadStubs", success, "%d", stub);
			if (!success) {
				return false;
			}
		}

		Logger.info(this, "loadStubs", "load size %d", array.size());
		return true;
	}

	/**
	 * 启动本地数据存取服务 <br>
	 * 1. 加载磁盘配置 <br>
	 * 2. 建立用户资源引用 <br>
	 * 3. 通过网络获得表配置 <br>
	 * 4. 初始化JNI接口中的表空间 <br>
	 * 5. 启动Access数据存储服务 <br><br>
	 * 
	 * @return 成功返回“真”，否则“假”。
	 */
	private boolean loadJNI() {
		// 从磁盘读取配置
		Set<Sketch> array = readSketches();
		// 启动加载
		for(Sketch sketch : array) {
			Siger siger = sketch.getSiger();
			// 分配账号
			Refer refer = allocate(siger);

			// 从配置中找到数据表名，从HOME站点加载数据表
			for(Space space : sketch.list()) {
				Table table = searchTable(space);
				if (table == null) {
					Logger.error(this, "loadJNI", "cannot find '%s'", space);
					// 保存
					addFaultTable(new FaultTable(siger, space));
					continue;
				}

				Logger.debug(this, "loadJNI", "init '%s' by %s", table, table.getIssuer());

				// 初始化数据空间（只分配不启动，留待launch方法处理）
				int ret = AccessTrustor.initSpace(table);
				boolean success = (ret >= 0);
				Logger.note(this, "loadJNI", success, "init space '%s', return code %d", space, ret);
				// 出错，不处理
				if (!success) {
					return false;
				}
				// 保存数据表名
				refer.addTable(table.getSpace());
				// 保存表实例
				mapTables.put(table.getSpace(), table);
			}

			// 加载账号所有人的资源引用，把授权单元/被授权单元导入进来
			loadRefer(siger);
		}

		// 启动数据存取服务
		int ret = AccessTrustor.launch(null);
		boolean success = (ret == 0);
		Logger.note(this, "loadJNI", success, "launch JNI.DB is %d", ret);

		return success;
	}

	/**
	 * 停止数据存取服务
	 */
	private void stopJNI() {
		AccessTrustor.stop();
	}

	/**
	 * 回滚数据
	 * @return 成功返回真，否则假
	 */
	private boolean rollback() {
		Node local = DataLauncher.getInstance().getListener();
		RollbackHistory cmd = new RollbackHistory(local);
		DataRollbackHistoryInvoker invoker = new DataRollbackHistoryInvoker(cmd);
		// 启动操作
		return invoker.launch();
	}

	/**
	 * 更新索引数据
	 * @param array
	 */
	private void update(List<StubArea> array) {
		// 清除
		mapSheets.clear();
		// 重新加载
		for (StubArea area : array) {
			Space space = area.getSpace();
			// 保存索引映像
			StubChartSheet sheet = mapSheets.get(space);
			if (sheet == null) {
				sheet = new StubChartSheet(space, area.getLeft());
				mapSheets.put(space, sheet);
			}

			// 更新表下数据块总长度，保存一个数据块成员的索引
			for (StubItem item : area.list()) {
				// 更新数据块总长度
				sheet.addAvailable(item.getLength());
				// 保存索引
				long stub = item.getStub();
				for (StubIndex index : item.list()) {
					sheet.add(stub, index);
				}
			}
			// 设置数据块数目
			sheet.setStubs(area.size());
		}
	}

	/**
	 * 解析从JNI接口中提取的数据块索引信息，转换为JAVA类保存
	 * @return 成功返回真，否则假
	 */
	private boolean loadIndex() {
		List<StubArea> array = null;
		try {
			array = AccessTrustor.getAllIndexLogs();
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 判断成功
		boolean success = (array != null);

		Logger.debug(this, "loadIndex", success, "StubArea size:%d", (array == null ? -1 : array.size()));

		// 锁定更新
		super.lockSingle();
		try {
			if (success) {
				update(array);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "loadIndex", success, "stub chart sheet size:%d", mapSheets.size());

		return success;
	}

	/**
	 * 查找数据表的有效容量
	 * @param space 数据表名
	 * @return 返回指定长度，没有是0。
	 */
	public StubChartSheet findStubChartSheet(Space space) {
		super.lockMulti();
		try {
			return mapSheets.get(space);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据当前的索引记录，建立一个新的索引块记录
	 * @return StubSchema实例
	 */
	public StubSchema createStubSchema() {
		StubSchema schema = new StubSchema();
		super.lockMulti();
		try {
			for (StubChartSheet sheet : mapSheets.values()) {
				Space space = sheet.getSpace();
				long available = sheet.getAvailable();
				long left = sheet.getLeft();
				StubSet set = sheet.getStubSet();

				StubTable table = new StubTable(space, available, left);
				table.addAll(set.list());
				schema.add(table);

				Logger.debug(this, "createStubSchema", "create \"%s\", used size:%d, left size:%d", 
						space, available, left);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "createStubSchema", "table element size::%d", schema.size());

		// 返回结果
		return schema;
	}

	/**
	 * 根据指定的数据表名，产生一个数据块索引表
	 * @param space 数据表名
	 * @return StubTable实例，或者空值
	 */
	public StubTable createStubTable(Space space) {
		if (space == null) {
			return null;
		}
		StubTable table = null;
		super.lockMulti();
		try {
			StubChartSheet sheet = mapSheets.get(space);
			if (sheet != null) {
				long available = sheet.getAvailable();
				long left = sheet.getLeft();
				StubSet set = sheet.getStubSet();

				// debug code, start 数据块编号
				for (long stub : set.list()) {
					Logger.debug(this, "createStubTable", "stub:%X, used:%d, free:%d", 
							stub, available, left);
				}
				// debug code, end

				// 建立表
				table = new StubTable(space, available, left);
				table.addAll(set.list());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return table;
	}

	/**
	 * 从磁盘读取数据块编号
	 * @return java.util.List<Long>集合
	 */
	private List<java.lang.Long> readStubs() {
		ArrayList<java.lang.Long> array = new ArrayList<java.lang.Long>();
		File file = DataLauncher.getInstance().createResourceFile(STUBS);
		if (!file.exists()) {
			return array;
		}
		byte[] b = DataLauncher.getInstance().readFile(file);
		ClassReader reader = new ClassReader(b);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			long stub = reader.readLong();
			array.add(stub);
		}
		return array;
	}

	/**
	 * 输出JNI接口中未使用的数据块编号
	 * @return
	 */
	private boolean writeStubs() {
		long[] array = AccessTrustor.getFreeStubs();

		int size = (array == null ? 0 : array.length);
		ClassWriter writer = new ClassWriter();
		writer.writeInt(size);
		for (int i = 0; i < size; i++) {
			writer.writeLong(array[i]);
		}
		byte[] b = writer.effuse();
		File file = DataLauncher.getInstance().createResourceFile(STUBS);
		return DataLauncher.getInstance().flushFile(file, b);
	}

	/**
	 * 从磁盘读取账号配属
	 * @return Set<Sketch> 集合
	 */
	private Set<Sketch> readSketches() {
		TreeSet<Sketch> array = new TreeSet<Sketch>();

		File file = DataLauncher.getInstance().createResourceFile(SKETCHES);
		// 允许文件不存在的情况
		if (!file.exists()) {
			Logger.warning(this, "readSketches", "empty member!");
			return array;
		}

		byte[] b = DataLauncher.getInstance().readFile(file);
		ClassReader reader = new ClassReader(b);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Sketch sketch = new Sketch(reader);
			array.add(sketch);
		}

		Logger.debug(this, "readSketches", "sketch size: %d", array.size());

		return array;
	}

	/**
	 * 用户资源引用数据写入磁盘
	 * @return 成功写入返回真，否则假
	 */
	private boolean writeSketches() {
		ClassWriter writer = new ClassWriter(10240);
		// 数据写入磁盘
		File file = DataLauncher.getInstance().createResourceFile(SKETCHES);

		boolean success = false;
		super.lockSingle();
		try {
			writer.writeInt(mapRefers.size());
			for (Refer refer : mapRefers.values()) {
				// DATA站点下，只有自己账号的专属表，不包含其他用户赋与的分享表
				Sketch sketch = new Sketch(refer.getUsername());
				sketch.addAll(refer.getTables());
				// 写入集合
				writer.writeObject(sketch);
			}
			// 写入磁盘
			byte[] b = writer.effuse();
			success = DataLauncher.getInstance().flushFile(file, b);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "writeSketches", success, "write sketch");

		return success;
	}

	/**
	 * 根据用户签名，向HOME站点查询匹配的ACCOUNT站点地址
	 */
	private boolean loadAccountSites() {
		List<Siger> sigers = getSigers();
		Logger.debug(this, "loadAccountSites", "size is %d", sigers.size());
		// 忽略
		if (sigers.isEmpty()) {
			return true;
		}

		// 获取ACCOUNT站点
		return AccountOnCommonPool.getInstance().load(sigers);
	}

	/**
	 * 根据指定的表，判断剩余空间在可操作范围内（保留最少1G的磁盘备用空间）
	 * @param space 表名
	 * @param capacity 指定的内存空间
	 * @return 符合条件返回真，否则假
	 */
	public boolean conform(Space space, long capacity) {
		boolean success = false;
		super.lockMulti();
		try {
			StubChartSheet sheet = mapSheets.get(space);
			success = (sheet != null);
			// 保证剩余1G的空间
			if (success) {
				success = (sheet.getLeft() - Laxkit.GB >= capacity);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	//	/**
	//	 * 加载TASK上的资源引用
	//	 */
	//	private void loadTaskRefers() {
	////		TreeSet<Siger> array = new TreeSet<Siger>();
	////		List<Siger> sigers = FromTaskPool.getInstance().getIssuers();
	////		array.addAll(sigers);
	////		sigers = RiseTaskPool.getInstance().getIssuers();
	////		array.addAll(sigers);
	////		// 如果是DATA主站点，启动数据构造的“SCAN”阶段管理池
	////		if (DataLauncher.getInstance().isMaster()) {
	////			sigers = ScanTaskPool.getInstance().getIssuers();
	////			array.addAll(sigers);
	////		}
	//		
	//		
	//		
	//		// 加载全部
	//		List<Siger> array = getSigers();
	//		for (Siger siger : array) {
	//			// 不存在，加载这个资源引用
	//			boolean success = (!hasRefer(siger));
	//			if (success) {
	//				success = loadRefer(siger);
	//			}
	//			// 加载资源引用
	//			if (success) {
	//				success = AccountOnCommonPool.getInstance().contains(siger);
	//				if (!success) {
	//					success = AccountOnCommonPool.getInstance().load(siger);
	//				}
	//				if (success) {
	//					loadTasks(siger);
	//				}
	//			}
	//		}
	//	}

	/**
	 * 加载资源引用
	 * @param siger 用户签名
	 * @return 成功返回真，否则假
	 */
	private boolean loadRefer(Siger siger) {
		// 生成命令，交给命令管理池处理
		TakeRefer cmd = new TakeRefer(siger);
		TakeReferHook hook = new TakeReferHook();
		ShiftTakeRefer shift = new ShiftTakeRefer(cmd, hook);
		boolean success = DataCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "loadRefer", "cannot be press!");
			return false;
		}
		hook.await();
		// 返回资源引用
		Refer refer = hook.getRefer();
		success = (refer != null);
		if (success) {
			success = buckle(refer);
		}

		Logger.note(this, "loadRefer", success, "load %s", siger);

		return success;
	}

	//	/**
	//	 * 根据授权人账号，查询关联的授权单元，转为被授权单元保存
	//	 * @param authorizer 授权人
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean loadPassiveItems(Siger authorizer) {
	//		SeekActiveItem cmd = new SeekActiveItem(authorizer);
	//		SeekActiveItemHook hook = new SeekActiveItemHook();
	//		ShiftSeekActiveItem shift = new ShiftSeekActiveItem(cmd, hook);
	//
	//		// 交给
	//		boolean success = DataCommandPool.getInstance().press(shift);
	//		if (!success) {
	//			Logger.error(this, "loadPassiveItems", "cannot submit to hub");
	//			return false;
	//		}
	//		// 等待，直到返回结果
	//		hook.await();
	//
	//		SeekActiveItemProduct product = hook.getProduct();
	//		success = (product != null);
	//		// 成功，保存数据
	//		if (success) {
	//			ArrayList<ActiveItem> array = new ArrayList<ActiveItem>();
	//			for (ActiveItem e : product.list()) {
	//				// 如果有这个表名，保存它
	//				if (mapTables.containsKey(e.getSpace())) {
	//					array.add(e);
	//				}
	//			}
	//			// 保存
	//			int count = addPassiveItems(authorizer, array);
	//			success = (count > 0);
	//		}
	//
	//		Logger.info(this, "loadPassiveItems", "save passive items %d to \'%s\' ", 
	//				(success ? product.size(): -1), authorizer);
	//
	//		return success;
	//	}

	//	/**
	//	 * 向被授权账号增加被授权单元
	//	 * @param authorizer 授权人签名
	//	 * @param items 授权单元列表
	//	 * @return 返回增加的成员数目
	//	 */
	//	private int addPassiveItems(Siger authorizer, List<ActiveItem> items) {
	//		int count = 0;
	//		for (ActiveItem item : items) {
	//			Siger conferrer = item.getConferrer();
	//			Refer refer = mapRefers.get(conferrer);
	//			if (refer == null) {
	//				User user = new User(conferrer);
	//				refer = new Refer(user);
	//				mapRefers.put(refer.getUsername(), refer);
	//			}
	//			// 生成被授权单元
	//			PassiveItem e =	item.createPassiveItem(authorizer);
	//			boolean success = refer.addPassiveItem(e);
	//			if (success) count++;
	//		}
	//
	//		Logger.info(this, "addPassiveItems", "\'%s\' passive items %d",
	//				authorizer, count);
	//
	//		return count ;
	//	}

	//	/**
	//	 * 把被授权单元从被授权账号中移除
	//	 * @param authorizer 授权人签名
	//	 * @param items 授权单元列表
	//	 * @return 返回删除的成员数目
	//	 */
	//	private int removePassiveItems(Siger authorizer, List<ActiveItem> items) {
	//		int count = 0;
	//		// 删除被授权单元
	//		for (ActiveItem item : items) {
	//			Siger conferrer = item.getConferrer();
	//			Refer refer = mapRefers.get(conferrer);
	//			// 不存在，下一个
	//			if (refer == null) {
	//				continue;
	//			}
	//			// 根据授权单元，生成被授权单元
	//			PassiveItem e =	item.createPassiveItem(authorizer);
	//			boolean success = refer.removePassiveItem(e);
	//			if (success) count++;
	//			// 判断是空集合，删除这个账号
	//			if (refer.isEmptyPassiveItems()) {
	//				mapRefers.remove(conferrer);
	//			}
	//		}
	//
	//		Logger.info(this, "removePassiveItems", "\'%s\' passive items %d", authorizer, count);
	//
	//		return count;
	//	}

	//	/**
	//	 * 向授权人的账号中，增加一批授权单元
	//	 * @param authorizer 授权人（数据表持有人）
	//	 * @param items 授权单元列表
	//	 * @return 成功返回真，否则假
	//	 */
	//	public boolean addActiveItems(Siger authorizer, List<ActiveItem> items) {
	//		// 判断获得授权许可
	//		if (!allow(authorizer)) {
	//			Logger.error(this, "addActiveItems", "refuse '%s'", authorizer);
	//			return false;
	//		}
	//
	//		boolean success = false;
	//		super.lockSingle();
	//		try {
	//			Refer refer = mapRefers.get(authorizer);
	//			// 增加被授权人
	//			success = (refer != null);
	//			// 保存授权单元
	//			if (success) {
	//				refer.addActiveItems(items);
	//			}
	//			// 将授权单元转为被授权单元保存
	//			if (success) {
	//				addPassiveItems(authorizer, items);
	//			}
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockSingle();
	//		}
	//
	//		Logger.info(this, "addActiveItems", "\'%s\' save active items %d", authorizer, items.size());
	//
	//		return success;
	//	}

	//	/**
	//	 * 把授权单元从授权人账号中移除
	//	 * @param authorizer 授权人（数据表持有人）
	//	 * @param items 授权单元列表
	//	 * @return 成功返回真，否则假
	//	 */
	//	public boolean removeActiveItems(Siger authorizer, List<ActiveItem> items) {
	//		// 判断获得授权许可
	//		if (!allow(authorizer)) {
	//			Logger.error(this, "removeActiveItems", "refuse '%s'", authorizer);
	//			return false;
	//		}
	//
	//		boolean success = false;
	//		super.lockSingle();
	//		try {
	//			Refer refer = mapRefers.get(authorizer);
	//			// 判断有效
	//			success = (refer != null);
	//			// 删除授权单元
	//			if (success) {
	//				refer.removeActiveItems(items);
	//			}
	//			// 将授权单元转为被授权单元删除
	//			if (success) {
	//				removePassiveItems(authorizer, items);
	//			}
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockSingle();
	//		}
	//
	//		Logger.note(this, "removeActiveItems", success, "drop from \'%s\'", authorizer);
	//
	//		return success;
	//	}

	//	/**
	//	 * 判断用户签名有效且存在。
	//	 * 这个判断只针对表的所有人，被授权人不能使用这个方法。
	//	 * 
	//	 * @param siger 用户签名
	//	 * @return 返回真或者假
	//	 */
	//	public boolean allow(Siger siger) {
	//		Laxkit.nullabled(siger);
	//		super.lockMulti();
	//		try {
	//			return (mapRefers.get(siger) != null);
	//		} finally {
	//			super.unlockMulti();
	//		}
	//	}

	/**
	 * 判断用户签名有效且存在。
	 * 允许是表的所有人或者被授权人的任何一种。
	 * 
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean allow(Siger siger) {
		boolean success = false;

		// 锁定处理
		super.lockMulti();
		try {
			// 判断是表所有人（授权人）
			success = (mapRefers.get(siger) != null);
			// 如果不成立，从全部引用中，判断是表的被授权人
			if (!success) {
				for (Refer refer : mapRefers.values()) {
					success = refer.hasActiveConferrer(siger);
					if (success) {
						break;
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return success;
	}

	/**
	 * 判断表存在
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean allow(Space space) {
		Laxkit.nullabled(space);
		super.lockMulti();
		try {
			return (mapTables.get(space) != null);
		} finally {
			super.unlockMulti();
		}
	}

	//	/**
	//	 * 判断用户签名和数据表名有效且存在
	//	 * @param siger 用户签名
	//	 * @param space 数据表名
	//	 * @return 返回真或者假
	//	 */
	//	public boolean allow(Siger siger, Space space) {
	//		boolean success = false;
	//		super.lockMulti();
	//		try {
	//			Refer refer = mapRefers.get(siger);
	//			// 判断有效
	//			if (refer != null) {
	//				// 1. 判断是标准用户，且表有效
	//				success = refer.hasTable(space);
	//				// 2. 前述条件不成立，假定是被授权用户，判断被授权表存在（被授权表存在，一定是被授权用户)
	//				if (!success) {
	//					success = refer.hasPassiveTable(space);
	//				}
	//			}
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockMulti();
	//		}
	//
	////		Logger.debug(this, "allow", success, "%s/%s", siger, space);
	//
	//		return success;
	//	}

	/**
	 * 判断用户签名、数据表名有效。<br>
	 * 流程： <br>
	 * 1. 判断表存在，这是基础。<br>
	 * 2. 判断表属于表持有人。<br>
	 * 3. 第2项不成立，通过表持有人找到资源引用，判断资源引用中有被授权人和关联授权表。<br>
	 * 
	 * @param siger 用户签名
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean allow(Siger siger, Space space) {
		// 锁定
		super.lockMulti();
		try {
			// 1. 表必须存在
			Table table = mapTables.get(space);
			if(table == null) {
				return false;
			}
			// 2. 判断是表持有人
			Siger issuer = table.getIssuer();
			boolean success = (Laxkit.compareTo(issuer, siger) == 0);
			if (success) {
				return true;
			}
			// 3. 通过找到表持有人的资源引用，判断有被授权人
			Refer refer = mapRefers.get(issuer);
			// 枚举每一个单元
			if (refer != null) {
				for (ActiveItem item : refer.getActiveItems()) {
					success = (Laxkit.compareTo(item.getConferrer(), siger) == 0 && Laxkit
							.compareTo(item.getSpace(), space) == 0);
					if (success) {
						return true;
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		//		Logger.debug(siger, this, "allow", success, "%s/%s", siger, space);

		return false;
	}

	/**
	 * 判断用户签名、表名、共享操作符有效且可以通过。<br>
	 * 流程：<br>
	 * 1. 判断数据表存在，这是基础前提。<br>
	 * 2. 判断签名和数据表持有人一致，退出！<br>
	 * 3. 第2项不成立，找到表持有人，判断有匹配的授权单元！<br><br>
	 * 
	 * @param siger 用户签名（可能是授权人或者被授权人）
	 * @param flag 资源共享标识
	 * @return 返回真或者假
	 */
	public boolean allow(Siger siger, CrossFlag flag) {
		// 锁定
		super.lockMulti();
		try {
			// 1. 找到表，这是基础前提
			Table table = mapTables.get(flag.getSpace());
			if (table == null) {
				return false;
			}
			// 2. 判断表名和传入签名一致，这是表持有人，可以做任何操作
			Siger issuer = table.getIssuer();
			boolean	success = (Laxkit.compareTo(issuer, siger) == 0);
			if (success) {
				return true;
			}
			// 3. 找到表持有人，判断有授权单元且允许操作
			Refer refer = mapRefers.get(issuer);
			if (refer != null) {
				// 枚举授权单元，判断被授权人存在，且符合操作要求
				for (ActiveItem item : refer.getActiveItems()) {
					success = ((Laxkit.compareTo(item.getConferrer(),siger) == 0) 
							&& item.allow(flag));
					if (success) {
						return true;
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		//		Logger.debug(siger, this, "allow", success, "%s/%s", siger, flag);

		return false;
	}


	/**
	 * 判断用户签名、表名、共享操作符有效且可以通过。<br>
	 * 流程： <br>
	 * 1. 判断数据表存在，这是基础前提。<br>
	 * 2. 判断签名和数据表持有人一致，退出！<br>
	 * 3. 找到表持有人，判断有授权单元，且匹配！<br><br>
	 * 
	 * @param siger 用户签名（可能是授权人或者被授权人）
	 * @param space 数据表名
	 * @param operator 共享操作符
	 * @return 返回真或者假
	 */
	public boolean allow(Siger siger, Space space, int operator) {
		CrossFlag flag = new CrossFlag(space, operator);
		return allow(siger, flag);
	}


	//	/**
	//	 * 判断用户签名和共享操作有效且通过
	//	 * @param siger 用户签名
	//	 * @param flag 共享操作符
	//	 * @return 返回真或者假
	//	 */
	//	public boolean allow(Siger siger, CrossFlag flag) {
	//		boolean success = false;
	//		super.lockMulti();
	//		try {
	//			Refer refer = mapRefers.get(siger);
	//			// 判断有效
	//			if (refer != null) {
	//				// 1. 判断是标准用户，且表有效
	//				success = refer.hasTable(flag.getSpace());
	//				// 2. 前述条件不成立，假定是被授权用户，且匹配被授权单元（匹配被授权单元，一定是被授权用户)
	//				if (!success) {
	//					success = refer.hasPassiveFlag(flag);
	//				}
	//			}
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockMulti();
	//		}
	//
	//		Logger.debug(this, "allow", success, "%s/%s", siger, flag);
	//
	//		return success;
	//	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.task.SiteTrustor#getLocal(long)
	//	 */
	//	@Override
	//	public Node getLocal(long invokerId) throws TaskException {
	//		boolean success = DataInvokerPool.getInstance().hasInvoker(invokerId);
	//		if (!success) {
	//			throw new TaskSecurityException("security denied '%d'", invokerId);
	//		}
	//		return getLauncher().getListener();
	//	}

	//	/**
	//	 * 根据调用器编号和数据表名，检查操作权限
	//	 * @param invokerId 调用器编号
	//	 * @param space 数据表名
	//	 * @throws TaskException
	//	 */
	//	private void check(long invokerId, Space space) throws TaskException {
	//		EchoInvoker invoker = DataInvokerPool.getInstance().findInvoker(invokerId);
	//		if (invoker == null) {
	//			throw new TaskException("cannot be find %d", invokerId);
	//		}
	//		// 签名
	//		Siger issuer = invoker.getIssuer();
	//		// 如果用户是被授权用户，判断它允许指定表操作
	//		if (!allow(issuer, space)) {
	//			throw new TaskSecurityException("security denied '%s'", issuer);
	//		}
	//	}
	//
	//	/**
	//	 * 根据调用器编号，检查操作权限
	//	 * @param invokerId 调用器编号
	//	 * @param space 数据表名
	//	 * @param operator 共享操作符
	//	 * @throws TaskException
	//	 */
	//	private void check(long invokerId, Space space, int operator) throws TaskException {
	//		EchoInvoker invoker = DataInvokerPool.getInstance().findInvoker(invokerId);
	//		if (invoker == null) {
	//			throw new TaskException("cannot be find %d", invokerId);
	//		}
	//		// 签名
	//		Siger issuer = invoker.getIssuer();
	//		CrossFlag flag = new CrossFlag(space, operator);
	//		// 如果用户是被授权用户，判断它允许操作，包括用户自己表或者被授权表
	//		if (!allow(issuer, flag)) {
	//			throw new TaskSecurityException("security denied '%s#%s'", issuer, flag);
	//		}
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.from.FromAssistor#findFromTable(com.laxcus.util.Siger, com.laxcus.access.schema.Space)
	//	 */
	//	@Override
	//	public Table findFromTable(long invokerId, Space space) throws TaskException {
	//		// 判断操作允许
	//		check(invokerId, space);
	//		// 从内存里查找表配置
	//		return findTable(space);
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.from.FromTrustor#select(long, com.laxcus.command.access.Select, long)
	//	 */
	//	@Override
	//	public byte[] select(long invokerId, Select cmd, long stub) throws TaskException {
	//		// 检查操作权限
	//		check(invokerId, cmd.getSpace(), CrossOperator.SELECT);
	//		// 通过SELECT代理，执行检索操作（检索代码太多，转移到另类处理）
	//		SelectTasker e = new SelectTasker();
	//		return e.process(cmd, stub);
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.from.FromTrustor#insert(long, com.laxcus.command.access.Insert)
	//	 */
	//	@Override
	//	public int insert(long invokerId, Insert cmd) throws TaskException {
	//		// 检查操作权限
	//		check(invokerId, cmd.getSpace(), CrossOperator.INSERT);
	//
	//		// 通过本地代理去处理INSERT操作，调用器同时要完成数据备份到从站点的工作
	//		TrustInsert trust = new TrustInsert(cmd);
	//		TrustInsertHook hook = new TrustInsertHook();
	//		ShiftTrustInsert shift = new ShiftTrustInsert(trust, hook);
	//
	//		// 提交给命令管理池
	//		boolean success = DataCommandPool.getInstance().press(shift);
	//		// 不成功返回空值
	//		if (!success) {
	//			throw new TaskException("cannot be press!");
	//		}
	//		hook.await();
	//
	//		// 如果有故障，弹出异常
	//		if (hook.isFault()) {
	//			throw new TaskException(hook.getFault());
	//		}
	//
	//		// 返回写入数据
	//		return hook.getRows();
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.from.FromTrustor#delete(long, com.laxcus.command.access.Delete, long)
	//	 */
	//	@Override
	//	public int delete(long invokerId, Delete cmd, long stub)throws TaskException {
	//		// 检查操作权限
	//		check(invokerId, cmd.getSpace(), CrossOperator.DELETE);
	//
	//		// 生成命令
	//		TrustDelete delete = new TrustDelete(cmd, stub);
	//		TrustDeleteHook hook = new TrustDeleteHook();
	//		ShiftTrustDelete shift = new ShiftTrustDelete(delete, hook);
	//
	//		// 提交给命令管理池
	//		boolean success = DataCommandPool.getInstance().press(shift);
	//		// 不成功返回空值
	//		if (!success) {
	//			throw new TaskException("cannot be press!");
	//		}
	//		hook.await();
	//
	//		// 如果有故障，弹出异常
	//		if (hook.isFault()) {
	//			throw new TaskException(hook.getFault());
	//		}
	//
	//		// 返回删除行数
	//		return hook.getRows();
	//	}

}