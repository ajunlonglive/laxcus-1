/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import java.io.*;
import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.call.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.field.*;
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
import com.laxcus.task.conduct.balance.*;
import com.laxcus.task.conduct.init.*;
import com.laxcus.task.contact.fork.*;
import com.laxcus.task.contact.merge.*;
import com.laxcus.task.establish.assign.*;
import com.laxcus.task.establish.issue.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.naming.*;

/**
 * CALL站点资源管理池。
 * 
 * @author scott.liang
 * @version 1.3 3/23/2015
 * @since laxcus 1.0
 */
public class StaffOnCallPool extends VirtualPool {

	/** 资源管理池句柄 **/
	private static StaffOnCallPool selfHandle = new StaffOnCallPool();

	/** 用户签名 -> 用户资源引用 **/
	private Map<Siger, Refer> mapRefers = new TreeMap<Siger, Refer>();

	/** 数据表名 -> 数据表所有人 **/
	private Map<Space, Siger> mapSpaces = new TreeMap<Space, Siger>();

	/** 数据表名 -> 表配置 **/
	private Map<Space, Table> mapTables = new TreeMap<Space, Table>();

	/** 重装加载分布组件 **/
	private boolean reloadTask;

	/**
	 * 构造资源管理池
	 */
	private StaffOnCallPool() {
		super();
	}

	/**
	 * 返回资源管理池句柄
	 * @return
	 */
	public static StaffOnCallPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("StaffOnCallPool.getInstance");
		// 返回句柄
		return StaffOnCallPool.selfHandle;
	}

	/**
	 * 加载磁盘上的用户记录，通过网络获得资源引用（Refer）
	 * 允许是空记录
	 * @return 成功返回真，否则假
	 */
	private boolean loadDiskUser() {
		// 读磁盘上的用户签名
		List<Siger> users = readSigers();
		if (users.isEmpty()) {
			return true;
		}

		// 加载资源
		for (Siger siger : users) {
			// 加载资源引用
			boolean success = loadRefer(siger);
			// 生成一个用户磁盘
			if (success) {
				success = StoreOnCallPool.getInstance().createDisk(siger);
			}
			Logger.note(this, "loadDiskUser", success, "load %s", siger);
		}
		return true;
	}

	/**
	 * 准备进入状态。
	 * 这个工作在线程中进行，避免影响CallLauncher线程工作。
	 * @return 成功返回真，否则假
	 */
	private boolean pretreat() {
		Logger.info(this, "pretreat", "into...");

		// 1. 加载本地的用户账号
		boolean success = loadDiskUser();
		// 2. 申请账号
		if (success) {
			success = loadRefers();
		}
		// 2.加载ACCOUNT站点（为了获取分布组件）
		if (success) {
			success = loadAccountSites();
		}
		// 3.加载本地任务组件
		if (success) {
			InitTaskPool.getInstance().load();
			BalanceTaskPool.getInstance().load();
			IssueTaskPool.getInstance().load();
			AssignTaskPool.getInstance().load();
			ForkTaskPool.getInstance().load();
			MergeTaskPool.getInstance().load();
		}
		// 4. 加载分布任务组件和码位计算器
		if (success) {
			loadTasks();
			// 加载与组件有关的资源引用
			loadTaskRefers();
			// 加载缺失的系统分布任务组件！
			loadSystemTasks();
		}
		
//		// 5. 扫描云存储目录
//		if (success) {
//			StoreOnCallPool.getInstance().scanDisks();
//		}

		// 查询元数据
		if (success) {
			// doFindDataField();
			// doFindWorkField();
			// doFindBuildField();
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
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		// 初始化资源
		boolean success = pretreat();
		// 预处理成功，重新注册；否则退出
		if (success) {
			// 启动HOME辅助池
			HomeOnCallPool.getInstance().start();
		}

		// 成功，采用INVOKE/PRODUCE异步方式，要求CALL站点重新注册；否则退出！
		if (success) {
			getLauncher().checkin(false);
			// 加载检测器
			loadMemberChecker();
			// 推送注册成员
			pushRegisterMember();
		} else {
			getLauncher().stop();
		}

		// 延时等待退出
		while (!isInterrupted()) {
			// 如果不成功，等待退出通知
			if (!success) {
				delay(1000);
				continue;
			}
			// 重新加载任务组件
			if (reloadTask) {
				reloadTask = false;
				loadTasks();
			}
			delay(1000);
		}

		// 以上成功，在退出前关闭辅助HOME管理池
		if (success) {
			// 关闭它
			HomeOnCallPool.getInstance().stop();
			while (HomeOnCallPool.getInstance().isRunning()) {
				delay(200);
			}
		}
		
		// 保存签名
		writeSigers();

		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 关闭它
		HomeOnCallPool.getInstance().stop();
		while (HomeOnCallPool.getInstance().isRunning()) {
			delay(200);
		}
		// 施放配置资源
		mapSpaces.clear();
		mapTables.clear();
		mapRefers.clear();
	}

	/**
	 * 启动用户资源检测器，定时检测节点上的用户数目
	 */
	private void loadMemberChecker() {
		CallLauncher launcher = (CallLauncher) getLauncher();
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
		CallLauncher launcher = (CallLauncher) getLauncher();
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
	 */
	private boolean addSpace(Space space, Siger siger) {
		return (mapSpaces.put(space, siger) == null);
	}

	/**
	 * 删除一个表和用户签名
	 * @param space 数据表名
	 * @param siger 用户签名
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
	 * 输出当前全部数据表名
	 * @return 数据表名列表
	 */
	public List<Space> getSpaces() {
		super.lockMulti();
		try {
			return new ArrayList<Space>(mapSpaces.keySet());
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 判断是活跃状态的被授权人
	 * @param siger
	 * @return
	 */
	public boolean isActiveConferrer(Siger siger) {
		boolean success = false;
		// 判断签名有效
		super.lockMulti();
		try {
			for (Refer refer : mapRefers.values()) {
				if (refer.isEnabled() && refer.hasActiveConferrer(siger)) {
					success = true;
					break;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.note(this, "isActiveConferrer", success, "check %s", siger);

		return success;
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
			// 1. 判断是所有人，且有效
			Refer owner = mapRefers.get(siger);
			if (owner != null) {
				success = owner.isEnabled();
			}
			// 2. 不成立，判断是被授权人（取出全部引用，逐一判断！）
			if (!success) {
				for (Refer other : mapRefers.values()) {
					success = (other.isEnabled() && other.hasActiveConferrer(siger));
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
	 * 查找表的持有人
	 * @param space 数据表名
	 * @return 返回数据表持有人签名（注意！不是被授权人！！！），没有返回空指针
	 */
	public Siger findOwner(Space space) {
		super.lockMulti();
		try {
			return mapSpaces.get(space);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断数据表名获得许可
	 * @param space 数据表名
	 * @return 许可返回“真”，否则“假”。
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
			// 找到资源引用，且必须有效
			Refer owner = mapRefers.get(who);
			if (owner == null || owner.isDisabled()) {
				return false;
			}

			// 判断属于资源引用
			boolean success = (Laxkit.compareTo(owner.getUsername(), siger) == 0);
			if (success) {
				return true;
			}
			// 枚举授权单元，判断被授权人存在，且符合操作要求
			for (ActiveItem item : owner.getActiveItems()) {
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
			// 找到资源引用，且必须有效
			Refer owner = mapRefers.get(who);
			if (owner == null || owner.isDisabled()) {
				return false;
			}

			// 判断属于资源引用
			boolean success = (Laxkit.compareTo(owner.getUsername(), siger) == 0);
			if (success) {
				return true;
			}
			// 枚举授权单元，判断被授权人存在，且符合操作要求
			for (ActiveItem item : owner.getActiveItems()) {
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
	 * 修改注册用户密码
	 * @param user 注册用户
	 * @return 修改成功返回真，否则假。
	 */
	public boolean alter(User user) {
		boolean success = false;
		super.lockSingle();
		try {
			Refer refer = mapRefers.get(user.getUsername());
			success = (refer != null);
			if (success) {
				refer.getUser().setPassword(user.getPassword());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.note(this, "alter", success, "he is '%s'", user);

		return success;
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
	 * 返回全部注册用户签名
	 * @return Siger集合
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
	 * 返回当前可用的数据表名集合
	 * @return List集合
	 */
	public List<Space> getUsedSpaces() {
		super.lockMulti();
		try {
			return new ArrayList<Space>(mapTables.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 
	 */
	public void reloadTask() {
		reloadTask = true;
	}
	
	/**
	 * 判断和加载缺失的系统组件。
	 * 如果存在这个现象，通过HOME->TOP->BANK，随机获取一个ACCOUNT站点，加载系统分布任务组件。
	 */
	private void loadSystemTasks() {
		TakeSystemTaskSite cmd = new TakeSystemTaskSite();
		ShiftLoadSystemTask shift = new ShiftLoadSystemTask(cmd);

		TaskPool[] pools = new TaskPool[] { InitTaskPool.getInstance(),
				BalanceTaskPool.getInstance(), IssueTaskPool.getInstance(),
				AssignTaskPool.getInstance(), ForkTaskPool.getInstance(),
				MergeTaskPool.getInstance() };
		// 判断有组件，如果去加载它！
		for (int i = 0; i < pools.length; i++) {
			TaskPool pool = pools[i];
			boolean success = pool.hasSystemTask();
			if (!success) {
				shift.addFamily(pool.getFamily());
			}
		}

		// 有缺失的组件，启动查询ACCOUNT站点
		if (shift.hasFamily()) {
			CallCommandPool.getInstance().press(shift);
		}
	}

	/**
	 * 加载TASK上的资源引用
	 */
	private void loadTaskRefers() {
		RemoteTaskPool[] pools = new RemoteTaskPool[] {
				InitTaskPool.getInstance(), BalanceTaskPool.getInstance(),
				IssueTaskPool.getInstance(), AssignTaskPool.getInstance(),
				ForkTaskPool.getInstance(), MergeTaskPool.getInstance() };

		TreeSet<Siger> array = new TreeSet<Siger>();
		for (int i = 0; i < pools.length; i++) {
			array.addAll(pools[i].scanIssuers());
		}
		
//		List<Siger> sigers = InitTaskPool.getInstance().scanIssuers();
//		array.addAll(sigers);
//		sigers = BalanceTaskPool.getInstance().scanIssuers();
//		array.addAll(sigers);
//		sigers = IssueTaskPool.getInstance().scanIssuers();
//		array.addAll(sigers);
//		sigers = AssignTaskPool.getInstance().scanIssuers();
//		array.addAll(sigers);
//		sigers = ForkTaskPool.getInstance().scanIssuers();
//		array.addAll(sigers);
//		sigers = MergeTaskPool.getInstance().scanIssuers();
//		array.addAll(sigers);
		
		Logger.debug(this, "loadTaskRefers", "siger size: %d", array.size());
		
		// 加载全部
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
					loadTasks(siger);
//					loadScaler(siger);
				}
			}
		}
	}
	
	/**
	 * 加载资源引用
	 * @param siger 签名
	 * @return 成功返回真，否则假
	 */
	private boolean loadRefer(Siger siger) {
		// 生成命令，交给命令管理池处理
		TakeRefer cmd = new TakeRefer(siger);
		TakeReferHook hook = new TakeReferHook();
		ShiftTakeRefer shift = new ShiftTakeRefer(cmd, hook);
		boolean success = CallCommandPool.getInstance().press(shift);
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
	 * 从HOME站点获得分布资源
	 * @return 成功返回“真”，否则“假”。
	 */
	private boolean loadRefers() {
		long free = Runtime.getRuntime().freeMemory();
		RequestCallRefer cmd = new RequestCallRefer(free);
		// 保存要求忽略的签名
		cmd.addIgnores(StoreOnCallPool.getInstance().getUsers());

		RequestReferHook hook = new RequestReferHook();
		ShiftRequestRefer shift = new ShiftRequestRefer(cmd, hook);

		// 快递投递命令
		boolean success = CallCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "loadRefers", "cannot be submit to hub");
			return false;
		}
		// 进入驻留状态
		hook.await();

		RequestReferProduct product = hook.getRequestReferProduct();
		if (product == null) {
			Logger.error(this, "loadRefers", "cannot be take refer");
			return false;
		}

		// 保存参数
		for (Refer refer : product.list()) {
			create(refer);
		}

		Logger.debug(this, "loadRefers", "users is:%d, spaces is:%d", mapRefers.size(), mapSpaces.size());

		return true;
	}

	/**
	 * 根据签名，获取ACCOUNT站点
	 * @return 成功返回“真”，否则“假”。
	 */
	private boolean loadAccountSites() {
		List<Siger> users = getSigers();
		if (users.isEmpty()) {
			return true;
		}

		return AccountOnCommonPool.getInstance().load(users);
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
	 * @param siger 用户签名
	 * @return 删除成功返回真，否则假。
	 */
	public boolean drop(Siger siger) {
		Laxkit.nullabled(siger);

		boolean success = false;
		// 锁定删除
		super.lockSingle();
		try {
			success = __drop(siger);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		// 签名写入磁盘
		if (success) {
			writeSigers();
		}

		Logger.debug(this, "drop", success, "drop %s", siger);
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
		// 锁定更新
		super.lockSingle();
		try {
			// 删除旧记录
			__drop(refer.getUsername());
			// 保存新记录
			__create(refer);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 签名写入磁盘
		if (success) {
			writeSigers();
		}

		Logger.debug(this, "create", success, "create %s", refer.getUsername());

		return success;
	}

	/**
	 * 从管理中心加载分布组件，部署到管理池中
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
		ArrayList<Siger> array = new ArrayList<Siger>(mapRefers.keySet());
		for (Siger siger : array) {
			loadTasks(siger);
		}
	}

//	/**
//	 * 从ACCOUNT站点加载码位计算器组件。
//	 * @return 成功返回真，否则假
//	 */
//	private void loadScalers() {
//		// 没有ACCOUNT站点，不处理
//		if (AccountOnCommonPool.getInstance().isEmpty()) {
//			Logger.warning(this, "loadTasks", "account sites is empty!");
//			return;
//		}
//
//		ArrayList<Siger> array = new ArrayList<Siger>(mapRefers.keySet());
//		for (Siger siger : array) {
//			loadScaler(siger);
//		}
//	}

	/**
	 * 向ACCOUNT站点获取分布组件
	 * @param issuer 用户签名
	 * @param taskFamily 阶段类型
	 * @return 命令受理并且提交给命令管理池返回真，否则假
	 */
	public boolean loadTask(Siger issuer, int taskFamily) {
		// 判断签名被允许
		if (issuer != null) {
			if (!hasRefer(issuer)) {
				Logger.error(this, "loadTask", "refuse %s", issuer);
				return false;
			}
		}
		// 提交给命令管理池
		TaskPart part = new TaskPart(issuer, taskFamily);
		TakeTaskTag cmd = new TakeTaskTag(part);
		return CallCommandPool.getInstance().admit(cmd);
	}

	/**
	 * 加载某个账号下的全部分布组件
	 * @param siger 用户签名
	 * @return 返回被受理的数目
	 */
	public int loadTasks(Siger siger) {
		int[] types = new int[] { PhaseTag.INIT, PhaseTag.BALANCE, 
				PhaseTag.ISSUE, PhaseTag.ASSIGN , PhaseTag.FORK, PhaseTag.MERGE};
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
	 * 删除CONDUCT/ESTABLISH管理池下的分布组件
	 * @param siger 用户签名
	 * @return 删除成功返回真，否则假
	 */
	public boolean dropTask(Siger siger) {
		int count = 0;
		if (InitTaskPool.getInstance().hasGroup(siger)) {
			boolean b = InitTaskPool.getInstance().drop(siger);
			count += (b ? 1 : -1);
		}
		if (BalanceTaskPool.getInstance().hasGroup(siger)) {
			boolean b = BalanceTaskPool.getInstance().drop(siger);
			count += (b ? 1 : -1);
		}
		if (IssueTaskPool.getInstance().hasGroup(siger)) {
			boolean b = IssueTaskPool.getInstance().drop(siger);
			count += (b ? 1 : -1);
		}
		if (AssignTaskPool.getInstance().hasGroup(siger)) {
			boolean b = AssignTaskPool.getInstance().drop(siger);
			count += (b ? 1 : -1);
		}
		if (ForkTaskPool.getInstance().hasGroup(siger)) {
			boolean b = ForkTaskPool.getInstance().drop(siger);
			count += (b ? 1 : -1);
		}
		if (MergeTaskPool.getInstance().hasGroup(siger)) {
			boolean b = MergeTaskPool.getInstance().drop(siger);
			count += (b ? 1 : -1);
		}
		return count >0;
	}

//	/**
//	 * 根据用户签名，加载码位计算器
//	 * @param siger 用户签名
//	 * @return 成功返回真，否则假。
//	 */
//	public boolean loadScaler(Siger siger) {
//		TakeScalerTag cmd = new TakeScalerTag(siger);
//		return CallCommandPool.getInstance().admit(cmd);
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
	 * 建立一个新表。此方法由“CallAwardCreateTableInvoker”调用。
	 * @param refer 用户资源引用
	 * @param table 表实例
	 * @return 建表成功返回“真”，否则“假”。
	 */
	public boolean createTable(Refer refer, Table table) {
		Siger siger = refer.getUsername();
		if (siger.compareTo(table.getIssuer()) != 0) {
			Logger.debug(this, "createTable", "cannot be match!");
			return false;
		}

		// 保存参数
		boolean success = false;
		Space space = table.getSpace();

		// 锁定
		super.lockSingle();
		try {
			Refer that = mapRefers.get(siger);
			if (that == null) {
				mapRefers.put(siger, refer);
				refer.addTable(space);
				// 保存到记录
				addSpace(space, refer.getUsername());
			} else {
				that.addTable(space);
				// 保存到记录
				addSpace(space, that.getUsername());
			}
			mapTables.put(space, table);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "createTable", success, "create '%s - %s'", siger, space);

		// 保存用户签名到磁盘上
		writeSigers();
		
		return success;
	}

	/**
	 * 删除一个表。此方法由“CallAwardDropTableInvoker”调用。
	 * @param space 数据表名
	 * @return 删除成功返回“真”，否则“假”。
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
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.note(this, "dropTable", success, "drop '%s'", space);

		return success;
	}

	/**
	 * 在本地查找数据表
	 * @param space 数据表名
	 * @return 返回表实例，或者空指针
	 */
	public Table findLocalTable(Space space) {
		Laxkit.nullabled(space);

		// 判断获取授权许可
		if (!allow(space)) {
			Logger.error(this, "findTable", "refuse %s", space);
			return null;
		}

		// 从内存里查找表配置
		super.lockMulti();
		try {
			return mapTables.get(space);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 向HOME站点查表
	 * @param space 数据表名
	 * @return 返回表实例，或者空指针
	 */
	private Table findHubTable(Space space) {
		TakeTable cmd = new TakeTable(space);
		TakeTableHook hook = new TakeTableHook();
		ShiftTakeTable shift = new ShiftTakeTable(cmd, hook);

		boolean success = CallCommandPool.getInstance().press(shift);
		if (!success) {
			hook.done();
			Logger.error(this, "findHubTable", "cannot submit to hub");
			return null;
		}
		// 进入悬停状态
		hook.await();

		return hook.getTable();
	}

	/**
	 * 查找表配置。先在本地找，没有去管理节点找。
	 * @param space 数据表名
	 * @return 返回表实例，或者空指针
	 */
	public Table findTable(Space space) {
		// 在本地查找数据表
		Table table = findLocalTable(space);
		if (table != null) {
			return table;
		}

		// 以上没有找到，去HOME站点查询
		table = findHubTable(space);
		// 判断表有效
		boolean success = (table != null);
		// 保存表
		if (success) {
			super.lockSingle();
			try {
				mapTables.put(table.getSpace(), table);
			} catch (Throwable e) {
				Logger.fatal(e);
			} finally {
				super.unlockSingle();
			}
		}

		Logger.debug(this, "findTable", success, "find '%s", space);

		return (success ? table : null);
	}

	/**
	 * 查找资源引用 
	 * @param siger 用户签名
	 * @return 返回资源引用，或者空指针
	 */
	public Refer findRefer(Siger siger) {
		Laxkit.nullabled(siger);
		super.lockMulti();
		try {
			return mapRefers.get(siger);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断有资源引用
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean hasRefer(Siger siger) {
		return findRefer(siger) != null;
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
	 * 检查注册用户
	 * @param siger 注册用户名（SHA256散列码）
	 * @return 返回注册账号参数或者空指针。
	 */
	public User findUser(Siger siger) {
		super.lockMulti();
		try {
			if (siger != null) {
				Refer refer = mapRefers.get(siger);
				if (refer != null) {
					return refer.getUser();
				}
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}
	
	/**
	 * 向HOME站点查询关联的DATA站点元数据。<br><br>
	 * DATA节点上与“INIT/ISSUE/FORK”三个类型管理池关联。<br>
	 * @return 发布成功返回，没有签名或者发布失败返回假
	 */
	public boolean doFindDataField() {
		FindDataField cmd = new FindDataField();
		cmd.addSpaces(getSpaces());
		RemoteTaskPool[] pools = new RemoteTaskPool[] {
				InitTaskPool.getInstance(), IssueTaskPool.getInstance(),
				ForkTaskPool.getInstance() };
		// 合并组件
		for (int i = 0; i < pools.length; i++) {
			List<Phase> array = pools[i].getPhases();
			for (Phase phase : array) {
				// 只检索用户组件，忽略系统组件
				if (phase.isUserLevel()) {
					cmd.addUser(phase.getIssuer());
				}
			}
		}
		// 空集合，忽略
		if (cmd.getUserCount() == 0) {
			return false;
		}
		// 提交到管理池
		return CallCommandPool.getInstance().admit(cmd);
	}
	
	/**
	 * 向HOME站点查找关联的WORK站点元数据
	 * WORK节点上与“INIT/FORK”二个类型管理池关联。<br>
	 * @return 发布成功返回，没有签名或者发布失败返回假
	 */
	public boolean doFindWorkField() {
		FindWorkField cmd = new FindWorkField();
		RemoteTaskPool[] pools = new RemoteTaskPool[] {
				InitTaskPool.getInstance(), ForkTaskPool.getInstance() };
		// 合并组件
		for (int i = 0; i < pools.length; i++) {
			List<Phase> array = pools[i].getPhases();
			for (Phase phase : array) {
				// 只检索用户组件，忽略系统组件
				if(phase.isUserLevel()) {
					cmd.addUser(phase.getIssuer());
				}
			}
		}
		// 空集合，忽略
		if (cmd.getUserCount() == 0) {
			return false;
		}
		// 提交到管理池
		return CallCommandPool.getInstance().admit(cmd);
	}

	/**
	 * 向HOME站点查询关联的BUILD站点元数据
	 * BUILD节点上与“ISSUE”一个类型管理池关联。<br>
	 * @return 发布成功返回，没有签名或者发布失败返回假
	 */
	public boolean doFindBuildField() {
		FindBuildField cmd = new FindBuildField();
		RemoteTaskPool[] pools = new RemoteTaskPool[] {
				IssueTaskPool.getInstance() };
		// 取出签名
		for (int i = 0; i < pools.length; i++) {
			List<Phase> array = pools[i].getPhases();
			for (Phase phase : array) {
				// 只检索用户组件，忽略系统组件
				if (phase.isUserLevel()) {
					cmd.addUser(phase.getIssuer());
				}
			}
		}
		// 空集合，忽略
		if (cmd.getUserCount() == 0) {
			return false;
		}
		// 提交到管理池
		return CallCommandPool.getInstance().admit(cmd);
	}
	
//	/**
//	 * 向HOME站点查询关联的DATA站点元数据
//	 * @return
//	 */
//	public boolean doFindDataField() {
//		FindDataField cmd = new FindDataField();
//		cmd.addSpaces(getSpaces());
//		// 阶段命名
//		List<Phase> array = InitTaskPool.getInstance().getPhases();
//		for (Phase phase : array) {
//			// 只检索用户组件，忽略系统组件
//			if(phase.isUserLevel()) {
//				cmd.addUser(phase.getIssuer());
//			}
//			//			Siger issuer = phase.getIssuer();
//			//			if (issuer != null) { // 只检索用户组件，忽略系统组件
//			//				cmd.addUser(issuer);
//			//			}
//		}
//		array = IssueTaskPool.getInstance().getPhases();
//		for (Phase phase : array) {
//			// 只检索用户组件，忽略系统组件
//			if(phase.isUserLevel()) {
//				cmd.addUser(phase.getIssuer());
//			}
//			//			Siger issuer = phase.getIssuer();
//			//			if (issuer != null) { // 只检索用户组件，忽略系统组件
//			//				cmd.addUser(issuer);
//			//			}
//		}
//		// 提交到管理池
//		return CallCommandPool.getInstance().admit(cmd);
//	}

//	/**
//	 * 向HOME站点查找关联的WORK站点元数据
//	 * @return
//	 */
//	public boolean doFindWorkField() {
//		FindWorkField cmd = new FindWorkField();
//		List<Phase> array = InitTaskPool.getInstance().getPhases();
//		for (Phase phase : array) {
//			// 只检索用户组件，忽略系统组件
//			if(phase.isUserLevel()) {
//				cmd.addUser(phase.getIssuer());
//			}
//
//			//			Siger issuer = phase.getIssuer();
//			//			if (issuer != null) { // 只检索用户组件，忽略系统组件
//			//				cmd.addUser(issuer);
//			//			}
//
//			//			if (phase.isUserLevel()) { // 只检索用户组件，忽略系统组件
//			//				phase.setFamily(PhaseTag.TO);
//			//				cmd.addPhase(phase);
//			//			}
//		}
//		// 提交到管理池
//		return CallCommandPool.getInstance().admit(cmd);
//	}

//	/**
//	 * 向HOME站点查询关联的BUILD站点元数据
//	 * @return
//	 */
//	public boolean doFindBuildField() {
//		FindBuildField cmd = new FindBuildField();
//		List<Phase> array = IssueTaskPool.getInstance().getPhases();
//		for (Phase phase : array) {
//			// 只检索用户组件，忽略系统组件
//			if (phase.isUserLevel()) {
//				cmd.addUser(phase.getIssuer());
//			}
//
//			//			Siger issuer = phase.getIssuer();
//			//			if (issuer != null) { // 只检索用户组件，忽略系统组件
//			//				cmd.addUser(issuer);
//			//			}
//		}
//		// 提交到管理池
//		return CallCommandPool.getInstance().admit(cmd);
//	}

	/**
	 * 判断当前签名的数据容量空间溢出
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean isTableCapacityFull(Siger siger) {
		Refer refer = findRefer(siger);
		if (refer == null) {
			return false;
		}
		// 定义磁盘空间
		long maxsize = refer.getUser().getMaxSize();
		if (maxsize < 1) {
			return false;
		}
		
		ArrayList<Space> array = new ArrayList<Space>();
		// 锁定
		super.lockSingle();
		try {
			array.addAll(refer.getTables());
		} finally {
			super.unlockSingle();
		}
		// 统计
		long count = 0;
		for (Space space : array) {
			long size = DataOnCallPool.getInstance().countTableCapacity(space);
			count += size;
		}
		boolean success = (count >= maxsize);

		Logger.debug(this, "isTableCapacityFull", success, "%d > %d", count, maxsize);
		
		return success;
	}
	
	/** 用户资源文件后缀 **/
	private final static String SUFFIX = ".sketch";
	
	/**
	 * 读取用户签名
	 * @return
	 */
	private List<Siger> readSigers() {
		ArrayList<Siger> array = new ArrayList<Siger>();

		// 取磁盘目录
		File dir = CallLauncher.getInstance().getResourcePath();
		boolean success = (dir != null && dir.exists() && dir.isDirectory());
		if (!success) {
			return array;
		}

		// 枚举以".sketch"后缀的文件
		File[] files = dir.listFiles();
		// 判断文件
		for (File file : files) {
			success = (file.exists() && file.isFile());
			if (success) {
				String filename = Laxkit.canonical(file);
				success = filename.endsWith(SUFFIX);
			}
			if (!success) {
				continue;
			}
			// 读取磁盘文件
			byte[] data = CallLauncher.getInstance().readFile(file);
			// 读取文本
			ClassReader reader = new ClassReader(data);
			int size = reader.readInt();
			for (int i = 0; i < size; i++) {
				Siger siger = new Siger(reader);
				array.add(siger);
			}
		}
		
		Logger.debug(this, "readSigers", "count users %d", array.size());

		return array;
	}

	/**
	 * 写入签名
	 */
	private boolean writeSigers() {
		ArrayList<Siger> array = new ArrayList<Siger>();
		// 锁定
		super.lockSingle();
		try {
			Iterator<Map.Entry<Siger, Refer>> iterator = mapRefers.entrySet()
					.iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, Refer> entry = iterator.next();
				Siger siger = entry.getKey();
				array.add(siger);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 写入磁盘
		ClassWriter w = new ClassWriter();
		w.writeInt(array.size());
		for(Siger siger : array) {
			w.writeObject(siger);
		}
		// 取出数据
		byte[] data = w.effuse();
		
		// 文件名
		String name = String.format("users.%s", SUFFIX);
		File file = CallLauncher.getInstance().createResourceFile(name);
		// 输出文件
		boolean success = CallLauncher.getInstance().flushFile(file, data);
		Logger.note(this, "writeSigers", success, "write %s", file.toString());
		return success;
	}
}