/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.pool;

import java.io.*;
import java.util.*;

import com.laxcus.access.*;
import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.build.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.stub.*;
import com.laxcus.command.task.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.pool.archive.*;
import com.laxcus.task.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.establish.sift.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.naming.*;

/**
 * BUILD站点资源管理池。
 * 
 * @author scott.liang
 * @version 1.3 7/23/2015
 * @since laxcus 1.0
 */
public final class StaffOnBuildPool extends VirtualPool {

	/** BUILD站点资源管理池句柄 **/
	private static StaffOnBuildPool selfHandle = new StaffOnBuildPool();

	/** 数据块编号文件名称 **/
	private final String STUBS = "stubs.conf";

	/** 用户签名 -> 用户资源引用 **/
	private Map<Siger, Refer> mapRefers = new TreeMap<Siger, Refer>();

	/** 数据表名 -> 数据表所有人 **/
	private Map<Space, Siger> mapSpaces = new TreeMap<Space, Siger>();
	
	/** 数据表名  -> 表配置 **/
	private Map<Space, Table> mapTables = new TreeMap<Space, Table>();

	/**
	 * 构造BUILD站点资源管理池
	 */
	private StaffOnBuildPool() {
		super();
	}

	/**
	 * 返回资源管理池句柄
	 * @return
	 */
	public static StaffOnBuildPool getInstance() {
		// 对调用者进行检查
		VirtualPool.check("StaffOnBuildPool.getInstance");
		// 返回句柄
		return StaffOnBuildPool.selfHandle;
	}

	/**
	 * 加载分布资源。
	 * 这项工作在线程中进行，避免加载工作长时间阻塞，影响BUILD站点线程运行。
	 * @return 成功返回真，否则假。
	 */
	private boolean pretreat() {
		Logger.info(this, "pretreat", "into...");

		// 1. 加载数据块编号
		boolean success = loadStubs();
		// 2. 启动数据存取服务
		if (success) {
			success = loadJNI();
		}
		// 3. 向HOME站点申请账号资源
		if (success) {
			success = loadRefers();
		}
		// 4. 获取关联的ACCOUNT站点
		if (success) {
			success = loadArchiveSites();
		}
		// 5. 加载本地分布任务组件
		if (success) {
			SiftTaskPool.getInstance().load();
		}
		// 6. 从网络加载、更新分布任务组件和码位计算器（异步处理）
		if (success) {
			loadTasks();
			// 加载与组件有关的资源引用
			loadTaskRefers();
			// 加载缺失的系统分布任务组件
			loadSystemTasks();
		}

		// 以上不成功，关闭JNI服务
		if (!success) {
			stopJNI();
		}

		Logger.note(this, "pretreat", success, "load staff");

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		// 启动资源
		boolean success = pretreat();
		if (success) {
			BuildLauncher.getInstance().checkin(false);
			// 启动用户数检测
			loadMemberChecker();
			// 推送注册成员
			pushRegisterMember();
		} else {
			BuildLauncher.getInstance().stop();
		}

		// 加载数据块编号的检查间隔时间是1分钟
		final long interval = 60000L;
		long touchTime = System.currentTimeMillis() + interval;

		// 延时等待退出
		while (!isInterrupted()) {
			// 如果不成功，等待退出
			if (!success) {
				delay(1000);
				continue;
			}
			// 定时检查剩余未分配的数据块
			if (System.currentTimeMillis() >= touchTime) {
				doTakeStubs();
				touchTime = System.currentTimeMillis() + interval;
			} else {
				delay(1000L);
			}
		}

		// 如果成功，在退出前，释放本地资源
		if (success) {
			// 保存未使用的数据块编号
			writeStubs();
			// 停止数据存取
			stopJNI();
		}

		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapRefers.clear();
		mapTables.clear();
		mapSpaces.clear();
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
		BuildLauncher launcher = (BuildLauncher) getLauncher();
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
		BuildLauncher launcher = (BuildLauncher) getLauncher();
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
	 * 保存一个表和用户签名
	 * @param space 数据表名
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	private boolean addSpace(Space space, Siger siger) {
		return mapSpaces.put(space, siger) == null;
	}

	/**
	 * 删除一个表和用户签名
	 * @param space 数据表名
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	private boolean removeSpace(Space space, Siger siger) {
		Siger who = mapSpaces.get(space);
		boolean success = (Laxkit.compareTo(siger, who) == 0);
		if (success) {
			mapSpaces.remove(space);
		}
		return success;
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
			for(Space space : mapSpaces.keySet()) {
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
	 * 返回全部用户签名
	 * @return 签名列表
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
	 * 返回资源引用 
	 * @param siger 用户签名
	 * @return 返回资源引用，没有是空指针
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
	 * 判断有这个资源引用
	 * @param siger 签名
	 * @return 返回真或者假
	 */
	public boolean hasRefer(Siger siger) {
		return findRefer(siger) != null;
	}
	
	/**
	 * 判断用户能够获得许可
	 * 允许是表所有人或者被授权人任何一种。
	 * 
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean allow(Siger siger) {
		boolean success = false;

		// 判断用户引用存在
		super.lockMulti();
		try {
			// 1. 判断是表所有人
			success = (mapRefers.get(siger) != null);
			// 2. 判断是表被授权人
			if (!success) {
				for (Refer refer : mapRefers.values()) {
					success = refer.hasActiveConferrer(siger);
					if (success) break;
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
	 * 判断是获得授权的数据表（包括专属表和分享表两种）
	 * @param space 数据表名
	 * @return 允许返回真，否则假
	 */
	public boolean allow(Space space) {
		Laxkit.nullabled(space);

		// 判断表存在
		super.lockMulti();
		try {
			return (mapSpaces.get(space) != null);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断用户签名、数据表名有效。<br>
	 * 流程： <br>
	 * 1. 判断表存在，这是基础。<br>
	 * 2. 枚举全部签名，找到资源引用，判断表属于表所有人还是授权单元。<br>
	 * 
	 * @param siger 用户签名
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean allow(Siger siger, Space space) {
		// 锁定
		super.lockMulti();
		try {
			// 找到关联的用户签名
			Siger who = mapSpaces.get(space);
			if (who == null) {
				return false;
			}
			// 找到资源引用
			Refer refer = mapRefers.get(who);
			if (refer == null) {
				return false;
			}

			// 判断属于资源引用
			boolean success = (Laxkit.compareTo(refer.getUsername(), siger) == 0);
			if (success) {
				return true;
			}
			// 枚举授权单元，判断被授权人存在，且符合操作要求
			for (ActiveItem item : refer.getActiveItems()) {
				success = (Laxkit.compareTo(item.getConferrer(),
						siger) == 0 && Laxkit.compareTo(item.getSpace(), space) == 0);
				if (success) {
					return true;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return false;
	}
	
	/**
	 * 判断用户签名、表名、共享操作符有效且可以通过。<br>
	 * 流程：<br>
	 * 1. 判断数据表存在，这是基础前提。<br>
	 * 2. 枚举全部签名，找到资源引用，判断表属于持有人，或者授权单元一致！<br>
	 * 
	 * @param siger 用户签名（可能是授权人或者被授权人）
	 * @param flag 资源共享标识
	 * @return 返回真或者假
	 */
	public boolean allow(Siger siger, CrossFlag flag) {
		// 锁定
		super.lockMulti();
		try {
			// 找到关联的用户签名
			Siger who = mapSpaces.get(flag.getSpace());
			if (who == null) {
				return false;
			}
			// 找到资源引用
			Refer refer = mapRefers.get(who);
			if (refer == null) {
				return false;
			}

			// 判断属于资源引用
			boolean success = (Laxkit.compareTo(refer.getUsername(), siger) == 0);
			if (success) {
				return true;
			}
			// 枚举授权单元，判断被授权人存在，且符合操作要求
			for (ActiveItem item : refer.getActiveItems()) {
				success = (Laxkit.compareTo(item.getConferrer(), siger) == 0 && item
						.allow(flag));
				if (success) {
					return true;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return false;
	}
	

	/**
	 * 删除账号资源
	 * @param siger 账号签名
	 * @return 删除成功返回真，否则假。
	 */
	private boolean __drop(Siger siger) {
		Refer refer = mapRefers.remove(siger);
		boolean success = (refer != null);
		if (success) {
			List<Space> spaces = refer.getTables();
			// 删除专属表
			for (Space space : spaces) {
				removeSpace(space, siger); // 删除表持有人
				mapTables.remove(space);
			}
		}
		return success;
	}

	/**
	 * 建立用户资源引用
	 * @param refer 用户资源引用
	 */
	private void __create(Refer refer) {
		mapRefers.put(refer.getUsername(), refer);
		// 保存专属表（加入的表属于注册的用户）
		List<Space> spaces = refer.getTables();
		for (Space space : spaces) {
			addSpace(space, refer.getUsername()); // 表的持有人
		}
	}
	
	/**
	 * 建立用户资源引用
	 * @param refer 用户资源引用
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean create(Refer refer) {
		Laxkit.nullabled(refer);

		boolean success = false;
		// 保存新记录
		super.lockSingle();
		try {
			// 删除旧记录
			__drop(refer.getUsername());
			__create(refer);
			success = true;
		} catch (Throwable ex) {
			Logger.fatal(ex);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 删除账号资源
	 * @param siger 账号签名
	 * @return 删除成功返回真，否则假。
	 */
	public boolean drop(Siger siger) {
		Laxkit.nullabled(siger);

		boolean success = false;
		super.lockSingle();
		try {
			success = __drop(siger);
		} catch (Throwable ex) {
			Logger.fatal(ex);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 建立数据表。这个方法被“BuildAwardCreateTableInvoker”调用，无条件接受建表。
	 * @param table 数据表实例
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean createTable(Table table) {
		Laxkit.nullabled(table);

		boolean success = false;
		super.lockSingle();
		try {
			Siger siger = table.getIssuer();
			Refer refer = mapRefers.get(siger);
			if (refer == null) {
				refer = new Refer(new User(siger));
				mapRefers.put(refer.getUsername(), refer);
			}
			// 保存表
			refer.addTable(table.getSpace());
			addSpace(table.getSpace(), siger); // 表持有人
			mapTables.put(table.getSpace(), table);
			success = true;
		} catch (Throwable ex) {
			Logger.fatal(ex);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "createTable", success, "create '%s'", table);

		return success;
	}

	/**
	 * 根据数据表名，删除数据表。这个方法被“BuildAwardDropTableInvoker”调用，删除本地内存中保存的表
	 * @param space 数据表名
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean dropTable(Space space) {
		boolean success = false;

		super.lockSingle();
		try {
			Siger who = mapSpaces.remove(space);
			if (who != null) {
				Refer refer = mapRefers.get(who);
				success = (refer != null);
				if (success) {
					// 删除表实例
					mapTables.remove(space);
					// 删除表实例
					refer.removeTable(space);
					// 删除授权单元
					refer.removeActiveItem(space);
				}
			}
		} catch (Throwable ex) {
			Logger.fatal(ex);
		} finally {
			super.unlockSingle();
		}

		Logger.note(this, "dropTable", success, "drop '%s'", space);
		return success;
	}

	/**
	 * 根据数据表名，去管理站点（HOME站点）获取数据表实例
	 * @param e 数据表名
	 * @return 返回数据表实例，或者空指针
	 */
	private Table searchTable(Space e) {
		TakeTable cmd = new TakeTable(e);
		TakeTableHook hook = new TakeTableHook();
		ShiftTakeTable shift = new ShiftTakeTable(cmd, hook);

		boolean success = BuildCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "searchTable", "cannot submit to hub");
			return null;
		}
		// 进入等待状态
		hook.await();
		// 返回表实例
		return hook.getTable();
	}

	/**
	 * 根据数据表名，查找它的数据表实例。如果本地没有，去管理站点（HOME站点）查找
	 * @param space 数据表名
	 * @return 返回数据表实例，没有返回空指针
	 */
	public Table findTable(Space space) {
		Laxkit.nullabled(space);

		// 判断获取授权许可
		if (!allow(space)) {
			Logger.error(this, "findTable", "refuse %s", space);
			return null;
		}

		// 检查本地内存
		Table table = null;
		super.lockMulti();
		try {
			table = mapTables.get(space);
		} finally {
			super.unlockMulti();
		}
		if (table != null) {
			return table;
		}

		// 进入网络查询
		table = searchTable(space);
		boolean success = (table != null);
		// 保存数据表
		if (success) {
			super.lockSingle();
			try {
				mapTables.put(table.getSpace(), table);
			} finally {
				super.unlockSingle();
			}
		}
		// 返回句柄
		return (success ? table : null);
	}

	/**
	 * 启动JNI服务
	 */
	private boolean loadJNI() {
		int ret = AccessTrustor.launch(null);
		boolean success = (ret == 0);
		Logger.note(this, "loadJNI", success, "Launch JNI.DB is %d", ret);

		return success;
	}

	/**
	 * 停止数据存取服务
	 */
	private void stopJNI() {
		AccessTrustor.stop();
	}

	/**
	 * 判断和加载缺失的系统组件。
	 * 如果存在这个现象，通过HOME->TOP->BANK，随机获取一个ACCOUNT站点，加载系统分布任务组件。
	 */
	private void loadSystemTasks() {
		TakeSystemTaskSite cmd = new TakeSystemTaskSite();
		ShiftLoadSystemTask shift = new ShiftLoadSystemTask(cmd);

		TaskPool[] pools = new TaskPool[] { SiftTaskPool.getInstance() };

		// 判断有组件
		for (int i = 0; i < pools.length; i++) {
			TaskPool pool = pools[i];
			boolean success = pool.hasSystemTask();
			if (!success) {
				shift.addFamily(pool.getFamily());
			}
		}

		// 有缺失的组件，启动查询ACCOUNT站点
		if (shift.hasFamily()) {
			BuildCommandPool.getInstance().press(shift);
		}
	}
	
	/**
	 * 加载TASK上的资源引用
	 */
	private void loadTaskRefers() {
		List<Siger> array = SiftTaskPool.getInstance().scanIssuers();
		Logger.debug(this, "loadTaskRefers", "siger size: %d", array.size());
		
		for (Siger siger : array) {
			// 不存在，加载这个资源引用
			boolean success = (!hasRefer(siger));
			if (success) {
				success = loadRefer(siger);
			}
			// 加载资源引用
			if (success) {
				success = AccountOnCommonPool.getInstance().contains(siger);
				if (!success) {
					success = AccountOnCommonPool.getInstance().load(siger);
				}
				if (success) {
					loadTask(siger);
//					loadScaler(siger);
				}
			}
		}
	}
	
	/**
	 * 加载资源引用
	 * @param siger
	 * @return
	 */
	private boolean loadRefer(Siger siger) {
		// 生成命令，交给命令管理池处理
		TakeRefer cmd = new TakeRefer(siger);
		TakeReferHook hook = new TakeReferHook();
		ShiftTakeRefer shift = new ShiftTakeRefer(cmd, hook);
		boolean success = BuildCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "loadRefer", "cannot be press!");
			return false;
		}
		hook.await();
		// 返回资源引用
		Refer refer = hook.getRefer();
		success = (refer != null);
		if (success) {
			success = create(refer);
		}

		Logger.note(this, "loadRefer", success, "load %s", siger);

		return success;
	}

	/**
	 * 从HOME站点获取用户资源
	 * @return
	 */
	private boolean loadRefers() {
		RequestBuildRefer cmd = new RequestBuildRefer(Runtime.getRuntime().freeMemory());
		RequestReferHook hook = new RequestReferHook();
		ShiftRequestRefer shift = new ShiftRequestRefer(cmd, hook);

		boolean success = BuildCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "loadRefers", "cannot submit to hub");
			return false;
		}
		// 进入悬停状态
		hook.await();

		RequestReferProduct product = hook.getRequestReferProduct();
		if (product == null) {
			Logger.error(this, "loadRefers", "cannot be take refer");
			return false;
		}

		// 保存用户资源引用
		for (Refer refer : product.list()) {
			create(refer);
		}

		Logger.debug(this, "loadRefers", "user is:%d, space is:%d",
				mapRefers.size(), mapSpaces.size());

		return true;
	}

	/**
	 * 根据当前用户签名，向HOME站点查询匹配的ACCOUNT站点地址
	 * @return 成功返回真，否则假
	 */
	private boolean loadArchiveSites() {
		List<Siger> users = getSigers();
		Logger.debug(this, "loacArchiveSite", "size is %d", users.size());
		// 忽略
		if (users.isEmpty()) {
			return true;
		}

		// 获取ACCOUNT站点
		return AccountOnCommonPool.getInstance().load(users);
	}

	/**
	 * 加载SIFT阶段分布组件，包括系统级和用户级两种
	 */
	private void loadTasks() {
		// 没有ACCOUNT站点，不处理
		if (AccountOnCommonPool.getInstance().isEmpty()) {
			Logger.warning(this, "loadTasks", "account sites is empty!");
			return;
		}
		// 加载系统级分布组件
		loadTask(null);
		// 加载用户级分布组件
		for(Siger siger : getSigers()) {
			loadTask(siger);
		}
	}

	/**
	 * 向ACCOUNT站点获取分布组件
	 * @param siger 账号签名
	 * @return 命令受理并且提交给命令管理池返回真，否则假
	 */
	public boolean loadTask(Siger siger) {
		// 判断签名被允许
		if (siger != null) {
			if (!hasRefer(siger)) {
				Logger.error(this, "loadTask", "refuse %s", siger);
				return false;
			}
		}
		// 提交给Build命令管理池
		TaskPart part = new TaskPart(siger, PhaseTag.SIFT);
		TakeTaskTag cmd = new TakeTaskTag(part);
		return BuildCommandPool.getInstance().admit(cmd);
	}

	/**
	 * 删除ESTABLISH.SIFT阶段管理池下的分布组件
	 * @param siger 用户签名
	 * @return 返回删除的分布组件数目
	 */
	public boolean dropTask(Siger siger) {
		int count = 0;
		if(SiftTaskPool.getInstance().hasGroup(siger)) {
			boolean  b = SiftTaskPool.getInstance().drop(siger);
			count += (b ? 1 : -1);
		}
		return count >0;
	}

//	/**
//	 * 向ACCOUNT站点获取关联的码位计算器
//	 */
//	private void loadScalers() {
//		// 没有ACCOUNT站点，不处理
//		if (AccountOnCommonPool.getInstance().isEmpty()) {
//			Logger.warning(this, "loadScalers", "account sites is empty!");
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
//		return BuildCommandPool.getInstance().admit(cmd);
//	}

//	/**
//	 * 删除一个账号下的码位计算器组件
//	 * @param siger 用户签名
//	 * @return 成功返回真，否则假
//	 */
//	public boolean dropScaler(Siger siger) {
//		int count = ScalerPool.getInstance().drop(siger);
//		return count > 0;
//	}

	/**
	 * 从磁盘读取未使用的数据块编号，保存到JNI接口
	 * @return
	 */
	private boolean loadStubs() {
		Set<Long> array = readStubs();
		for(long stub : array) {
			int ret = AccessTrustor.addStub(stub);
			boolean success = (ret == 0);
			Logger.note(this, "loadStubs", success, "%d", stub);
			if(!success) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 从磁盘配置文件读取数据块编号
	 * @return 长整型列表
	 */
	private Set<Long> readStubs() {
		TreeSet<Long> array = new TreeSet<Long>();
		File file = BuildLauncher.getInstance().createResourceFile(STUBS);
		if (!file.exists()) {
			return array;
		}
		byte[] b = BuildLauncher.getInstance().readFile(file);
		ClassReader reader = new ClassReader(b);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			long stub = reader.readLong();
			array.add(stub);
		}
		return array;
	}

	/**
	 * 读出未使用的数据块编号，保存到磁盘配置文件
	 * @return 成功返回真，否则假
	 */
	private boolean writeStubs() {
		// 从JNI接口中读取未使用的数据块编号
		long[] array = AccessTrustor.getFreeStubs();
		// 写入可类化存储器
		int size = (array == null ? 0 : array.length);
		ClassWriter writer = new ClassWriter();
		writer.writeInt(size);
		for (int i = 0; i < size; i++) {
			writer.writeLong(array[i]);
		}
		// 数据块编号写入磁盘
		byte[] b = writer.effuse();
		File file = BuildLauncher.getInstance().createResourceFile(STUBS);
		return BuildLauncher.getInstance().flushFile(file, b);
	}

	/**
	 * 从HOME站点获取数据块编号
	 */
	private void doTakeStubs() {
		int count = AccessTrustor.getCountFreeStubs();
		if (count >= 5) {
			return;
		}

		TakeStub cmd = new TakeStub(5); // 申请5个数据块编号
		TakeStubHook hook = new TakeStubHook();
		ShiftTakeStub shift = new ShiftTakeStub(cmd, hook);

		// 进入快车道
		boolean success = BuildCommandPool.getInstance().press(shift);
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
				int ret = AccessTrustor.addStub(stub);
				boolean b = (ret == 0);
				Logger.debug(this, "doTakeStubs", b, "add stub %x is %d", stub, ret);
			}
		}
	}

}