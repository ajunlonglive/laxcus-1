/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.pool;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.task.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.pool.archive.*;
import com.laxcus.task.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.contact.distant.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.naming.*;
import com.laxcus.work.*;

/**
 * WORK站点资源管理池 <br>
 * 
 * 提供本地资源的建立、保存、检索服务。
 * 
 * @author scott.liang
 * @version 1.2 5/20/2013
 * @since laxcus 1.0
 */
public final class StaffOnWorkPool extends VirtualPool {

	/** WORK资源管理池句柄 **/
	private static StaffOnWorkPool selfHandle = new StaffOnWorkPool();

	/** 用户名称 -> 用户资源引用 **/
	private Map<Siger, Refer> mapRefers = new TreeMap<Siger, Refer>();

	/** 数据表名 -> 所有人签名 **/
	private Map<Space, Siger> mapSpaces = new TreeMap<Space, Siger>();

	/** 数据表名  -> 表配置 **/
	private Map<Space, Table> mapTables = new TreeMap<Space, Table>();

	/**
	 * 构造WORK资源管理池
	 */
	private StaffOnWorkPool() {
		super();
	}

	/**
	 * 返回WORK资源管理池句柄
	 * @return StaffOnWorkPool实例
	 */
	public static StaffOnWorkPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("StaffOnWorkPool.getInstance");
		// 返回句柄
		return StaffOnWorkPool.selfHandle;
	}

	/**
	 * 判断和加载缺失的系统组件。
	 * 如果存在这个现象，通过HOME->TOP->BANK，随机获取一个ACCOUNT站点，加载系统分布任务组件。
	 */
	private void loadSystemTasks() {
		TakeSystemTaskSite cmd = new TakeSystemTaskSite();
		ShiftLoadSystemTask shift = new ShiftLoadSystemTask(cmd);

		TaskPool[] pools = new TaskPool[] { ToTaskPool.getInstance(), DistantTaskPool.getInstance() };

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
			WorkCommandPool.getInstance().press(shift);
		}
	}

	/**
	 * 预处理工作。
	 * 这项工作在线程中进行，避免影响WorkLauncher线程。
	 * @return 成功返回真，否则假
	 */
	private boolean pretreat() {
		Logger.info(this, "pretreat", "into...");

		// 1. 向HOME申请账号
		boolean success = loadRefers();
		// 2. 加载ACCOUNT站点
		if (success) {
			success = loadAccountSites();
		}
		// 3. 加载本地的任务组件
		if (success) {
			ToTaskPool.getInstance().load();
			DistantTaskPool.getInstance().load();
		}
		// 4. 去ACCOUNT站点，加载关联的TO/DISTANT阶段任务组件
		if (success) {
			loadTasks();
			// 加载账号上的分布任务组件
			loadTaskRefers();
			// 加载缺失的系统分布任务组件
			loadSystemTasks();
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

		// 在线程中启动加载资源的工作
		boolean success = pretreat();
		// 启动成功，通知WORK站点重新注册，否则通知WORK站点停止工作。
		if (success) {
			WorkLauncher.getInstance().checkin(false);
			// 启动资源定时检测
			loadMemberChecker();
			// 推送注册成员
			pushRegisterMember();
		} else {
			WorkLauncher.getInstance().stop();
		}

		// 延时等待退出
		while (!isInterrupted()) {
			// 如果不成功，等待收到通知退出
			if (!success) {
				delay(1000);
				continue;
			}
			// 延时
			sleep();
		}

		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapSpaces.clear();
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
		WorkLauncher launcher = (WorkLauncher) getLauncher();
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
		WorkLauncher launcher = (WorkLauncher) getLauncher();
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
	 * 加载TASK上的资源引用
	 */
	private void loadTaskRefers() {
		ArrayList<Siger> array = new ArrayList<Siger>();
		array.addAll(ToTaskPool.getInstance().scanIssuers());
		array.addAll(DistantTaskPool.getInstance().scanIssuers());

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
				// 重新检查和加载关联的分布任务组件
				if (success) {
					loadTasks(siger);
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
		boolean success = WorkCommandPool.getInstance().press(shift);
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
	 * @return 加载成功返回真，否则假
	 */
	private boolean loadRefers() {
		long freeCapacity = Runtime.getRuntime().freeMemory();
		RequestWorkRefer cmd = new RequestWorkRefer(freeCapacity);
		RequestReferHook hook = new RequestReferHook();
		ShiftRequestRefer shift = new ShiftRequestRefer(cmd, hook);

		boolean success = WorkCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "loadRefers", "cannot submit to hub");
			return false;
		}
		// 进入悬停状态
		hook.await();

		RequestReferProduct product = hook.getRequestReferProduct();
		// 失败
		if (product == null) {
			Logger.error(this, "loadRefers", "cannot be take refer");
			return false;
		}

		// 保存参数
		for (Refer refer : product.list()) {
			create(refer);
		}

		Logger.debug(this, "loadRefers", "user is:%d, space is:%d", mapRefers.size(), mapSpaces.size());

		return true;
	}

	/**
	 * 根据当前用户签名，向HOME站点查询匹配的ACCOUNT站点地址
	 * @return 成功返回真，否则假
	 */
	private boolean loadAccountSites() {
		List<Siger> sigers = getSigers();
		Logger.debug(this, "loadAccountSites", "size is %d", sigers.size());
		// 允许空集合，忽略
		if (sigers.isEmpty()) {
			return true;
		}

		// 获取ACCOUNT站点
		return AccountOnCommonPool.getInstance().load(sigers);
	}

	/**
	 * 加载分布组件
	 */
	private void loadTasks() {
		// 没有ACCOUNT站点，不处理
		if (AccountOnCommonPool.getInstance().isEmpty()) {
			Logger.warning(this, "loadTasks", "account sites is empty!");
			return;
		}

		// 加载系统级分布组件
		loadTasks(null);
		// 加载用户级分布组件
		for(Siger siger : getSigers()) {
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
		return WorkCommandPool.getInstance().admit(cmd);
	}
	
	/**
	 * 加载某个账号下的全部分布组件
	 * @param siger 用户签名
	 * @return 返回被受理的数目
	 */
	public int loadTasks(Siger siger) {
		int[] types = new int[] { PhaseTag.TO, PhaseTag.DISTANT};
		int count = 0;
		for (int index = 0; index < types.length; index++) {
			boolean success = loadTask(siger, types[index]);
			if (success) {
				count++;
			}
		}
		return count;
	}
	/**
	 * 删除CONDUCT.TO / SWIFT.DISTANT阶段管理池下的分布组件
	 * @param issuer 用户签名
	 * @return 返回删除的分布组件数目
	 */
	public boolean dropTask(Siger issuer) {
		int count = 0;
		if(ToTaskPool.getInstance().hasGroup(issuer)) {
			boolean b = ToTaskPool.getInstance().drop(issuer);
			count += (b ? 1 : -1);
		}
		if(DistantTaskPool.getInstance().hasGroup(issuer)) {
			boolean b = DistantTaskPool.getInstance().drop(issuer);
			count += (b ? 1 : -1);
		}
		return count > 0;
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
	 * 删除账号资源
	 * @param siger 账号签名
	 * @return 删除成功返回真，否则假。
	 */
	public boolean drop(Siger siger) {
		Laxkit.nullabled(siger);

		// 锁定删除
		boolean success = false;
		super.lockSingle();
		try {
			success = __drop(siger);
		} catch (Throwable ex) {
			Logger.fatal(ex);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "drop", success, "drop '%s'", siger);

		return success;
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

		Logger.debug(this, "create", success, "create '%s'", refer.getUsername());

		return success;
	}

	/**
	 * 判断数据表已经存在
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean hasTable(Space space) {
		super.lockMulti();
		try {
			return (mapTables.get(space) != null);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 建立数据表。这个方法被“WorkAwardCreateTableInvoker”调用，无条件接受建表。
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
				refer = new Refer(siger);
				mapRefers.put(refer.getUsername(), refer);
			}
			// 保存到资源引用
			refer.addTable(table.getSpace());

			// 表持有人，保存到签名集中
			addSpace(table.getSpace(), siger);

			// 保存表
			mapTables.put(table.getSpace(), table);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "createTable", success, "create '%s'", table);

		return success;
	}

	/**
	 * 删除数据表。这个方法被“WorkAwardDropTableInvoker”调用，删除本地内存中保存的表
	 * @param space 表实例
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
	
//	/**
//	 * 查找与数据库关联的全部数据表
//	 * @param fame 数据库名
//	 * @return 返回关联的表名集合
//	 */
//	public int dropSchema(Fame fame) {
//		Laxkit.nullabled(fame);
//		
//		ArrayList<Space> array = new ArrayList<Space>();
//		// 查找表名
//		super.lockMulti();
//		try {
//			for (Space space : mapSpaces.keySet()) {
//				if (Laxkit.compareTo(space.getSchema(), fame) == 0) {
//					array.add(space);
//				}
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockMulti();
//		}
//		
//		// 删除表
//		int count = 0;
//		for (Space space : array) {
//			boolean success = dropTable(space);
//			if (success) {
//				count++;
//			}
//		}
//		
//		Logger.note(this, "dropSchema", (count > 0), "drop '%s', count %d",
//				fame, count);
//
//		return count;
//	}

	/**
	 * 查找内存中的数据表
	 * @param space 数据表名
	 * @return 返回表实例，或者空指针
	 */
	public Table findLocalTable(Space space) {
		// 判断数据表获取授权许可，包括表持有人或者被授权人
		if (!allow(space)) {
			//			Logger.error(this, "findLocalTable", "refuse %s", space);
			return null;
		}
		// 检查本地内存
		super.lockMulti();
		try {
			return mapTables.get(space);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 去管理站点（HOME站点）查找数据表
	 * @param space 数据表名
	 * @return 返回数据表实例，没有返回空指针
	 */
	private Table findHubTable(Space space) {
		TakeTable cmd = new TakeTable(space);
		TakeTableHook hook = new TakeTableHook();
		ShiftTakeTable shift = new ShiftTakeTable(cmd, hook);

		// 关给调用器处理
		boolean success = WorkCommandPool.getInstance().press(shift);
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
	 * 根据数据表名，查找本地的数据表实例。如果没有，通过网络去管理站点（HOME站点）查找。
	 * @param space 数据表名
	 * @return 返回表实例，没有返回空指针
	 */
	public Table findTable(Space space) {
		// 查找内存中的数据表
		Table table = findLocalTable(space);
		if (table != null) {
			return table;
		}

		// 进入网络查询
		table = findHubTable(space);
		// 判断有效和保存
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
	 * 判断用户签名有效且存在
	 * 允许是表所有人或者被授权人任何一种。
	 * 
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean allow(Siger siger) {
		boolean success = false;
		// 判断签名有效
		super.lockMulti();
		try {
			// 1. 判断是所有人
			success = (mapRefers.get(siger) != null);
			// 2. 不成立，判断是被授权人
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

		// 判断表有效
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
	 * 2. 枚举全部签名，找到资源引用，判断表属于表所有人，或者授权给另一个账号。<br>
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
	 * 2. 枚举全部签名，找到资源引用，判断表属于持有人，或者授权给另一个账号！<br>
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
				success = (Laxkit.compareTo(item.getConferrer(),
						siger) == 0 && item.allow(flag));
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
	 * 输出全部用户签名
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
	 * 查找资源引用
	 * @param siger 用户签名
	 * @return 资源引用实例，或者空指针
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
}