/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.pool;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.parse.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.site.front.*;
import com.laxcus.front.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.task.contact.near.*;
import com.laxcus.task.establish.end.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.tub.servlet.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;

/**
 * FRONT站点资源管理池。<br>
 * 提供管理权限、数据库、表、CALL站点地址的查询服务。被“终端、控制台、驱动程序”三者共有。
 * 
 * @author scott.liang
 * @version 1.3 8/29/2019
 * @since laxcus 1.0
 */
public abstract class StaffOnFrontPool extends VirtualPool implements ResourceChooser, 
	VisitRobot, PutTrustor, EndTrustor, NearTrustor, TubResourceHelper {

	/** 定时检查CALL节点的触发间隔时间，默认20分钟 **/
	private long checkInterval;

	/** 登录用户账号 **/
	private Account account;

	/** CALL站点地址 -> 关联参数 **/
	private TreeMap<Node, Cube> sites = new TreeMap<Node, Cube>();
	
	/** 节点 -> 云端空间 **/
	private TreeMap<Node, CloudField> cloudFields = new TreeMap<Node, CloudField>();

	/** 被授权表表名 -> 授权表实例 **/
	private TreeMap<Space, Table> passiveTables = new TreeMap<Space, Table>();

	/** 数据表名 -> 全部注册站点 **/
	private TreeMap<Space, NodeSet> tableSites = new TreeMap<Space, NodeSet>();

	/** 阶段命名 -> 全部注册站点 **/
	private TreeMap<Phase, NodeSet> taskSites = new TreeMap<Phase, NodeSet>();

	/** 全部CALL站点 **/
	private NodeSet callSites = new NodeSet();

	/**
	 * 构造资源管理池
	 */
	protected StaffOnFrontPool() {
		super();
		// 默认20分钟
		setCheckInterval(20 * 60 * 1000);
	}

	/**
	 * 设置定时检查CALL节点间隔时间，不能低于60秒
	 * @param ms 毫秒
	 * @return 返回修改后的时间
	 */
	public long setCheckInterval(long ms) {
		if (ms >= 60000) {
			checkInterval = ms;
		}
		return checkInterval;
	}

	/**
	 * 返回定时检查CALL节点间隔时间
	 * @return 检查间隔时间
	 */
	public long getCheckInterval(){
		return checkInterval;
	}

	/**
	 * 返回启动器
	 * @return
	 */
	public static FrontLauncher getFrontLauncher() {
		return (FrontLauncher) VirtualPool.getLauncher();
	}

	/**
	 * 返回异步调用器
	 * @return
	 */
	protected FrontInvokerPool getInvokerPool() {
		return (FrontInvokerPool) super.getInvokerPool();
	}

	/**
	 * 输出全部CALL站点地址
	 * @return NodeSet实例
	 */
	public NodeSet getCallSites() {
		return callSites;
	}

	/**
	 * 返回当前账号
	 * @return 账号实例
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * 判断账号存在且有效
	 * @return 返回真或者假
	 */
	public boolean hasAccount() {
		return account != null;
	}

	/**
	 * 设置当前用户的账号
	 * @param self
	 */
	public void setAccount(Account self) {
		// 旧数据库记录
		ArrayList<Fame> localFames = new ArrayList<Fame>();
		// 旧的被授权单元
		ArrayList<PassiveItem> localCrossTables = new ArrayList<PassiveItem>();

		super.lockSingle();
		try {
			// 保存旧数据库名称
			if (account != null) {
				localFames.addAll(account.getFames());
				localCrossTables.addAll(account.getPassiveItems());
			}

			// 设置新账号
			account = self;
		} finally {
			super.unlockSingle();
		}

		// 删除旧的数据库
		for (Fame fame : localFames) {
			erase(fame);
		}
		// 删除旧的被授权单元
		for(PassiveItem item : localCrossTables) {
			erase(item);
		}

		// 修改站点状态指示
		reveal();

		// 重置数据库记录
		if (self != null) {
			// 显示数据库
			for (Schema schema : account.getSchemas()) {
				exhibit(schema);
			}
			// 显示表
			for (Table table : account.getTables()) {
				exhibit(table);
			}
			// 显示被授权单元
			for(PassiveItem item : account.getPassiveItems()) {
				exhibit(item);
			}

			// 测试代码
			//			exhibit(new PassiveItem(SHAUser.doUsername("pentiums"), new Space("HeloKey", "Technolog")));
		}
	}

	/**
	 * 返回CALL网关站点集合
	 * @return Node列表
	 */
	public List<Node> getGateways() {
		super.lockMulti();
		try {
			return new ArrayList<Node>(sites.keySet());
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 判断CALL节点网关存在
	 * @param hub call节点
	 * @return 返回真或者假
	 */
	public boolean hasGateway(Node hub) {
		boolean success = false;
		// 锁定判断
		super.lockMulti();
		try {
			success = (sites.get(hub) != null);
		} finally {
			super.unlockMulti();
		}
		return success;
	}
	
	/**
	 * 查找云端空间
	 * @param node
	 * @return
	 */
	public CloudField findCloudField(Node node) {
		super.lockMulti();
		try {
			return cloudFields.get(node);
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 判断关联的云存储节点存在
	 * @param hub 云存在节点
	 * @return 返回真或者假
	 */
	public boolean hasCloudSite(Node hub) {
		return findCloudField(hub) != null;
	}

	/**
	 * 返回云端节点
	 * @return Node数组
	 */
	public List<Node> getCloudSites() {
		ArrayList<Node> array = new ArrayList<Node>();
		// 锁定
		super.lockMulti();
		try {
			array.addAll(cloudFields.keySet());
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 增加一个节点
	 * @param field 云端空间
	 */
	public void addCloudField(CloudField field) {
		// 锁定
		super.lockSingle();
		try {
			cloudFields.put(field.getSite(), field);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}
	
	/**
	 * 删除全部云端空间
	 */
	public void removeAllCloudFields() {
		// 锁定
		super.lockSingle();
		try {
			cloudFields.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 根据数据表名找叛逆的CALL站点
	 * @param space 数据表名
	 * @return NodeSet实例
	 */
	public NodeSet findTableSites(Space space) {
		super.lockMulti();
		try {
			return tableSites.get(space);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据阶段命名找CALL站点集合，包括对系统级阶段命名的判断。
	 * @param phase 阶段命名
	 * @return 返回注册站点集合
	 */
	public NodeSet findTaskSites(Phase phase) {
		// 是CONDUCT.PUT/ESTABLISH.END/CONTACT.NEAR系统命名
		if (PhaseTag.isConduct(phase.getFamily())) {
			if (PutTaskPool.getInstance().isSystemLevel(phase.getSock())) {
				return callSites;
			}
		} else if (PhaseTag.isEstablish(phase.getFamily())) {
			if (EndTaskPool.getInstance().isSystemLevel(phase.getSock())) {
				return callSites;
			}
		} else if (PhaseTag.isContact(phase.getFamily())) {
			if (NearTaskPool.getInstance().isSystemLevel(phase.getSock())) {
				return callSites;
			}
		} 

		// 如果是用户的阶段命名，查找注册地址
		super.lockMulti();
		try {
			return taskSites.get(phase);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断表有关联站点
	 * @param space 表名
	 * @return 返回真或者假
	 */
	public boolean hasTableSite(Space space) {
		boolean success = false;
		super.lockMulti();
		try {
			NodeSet set = tableSites.get(space);
			success = (set != null && set.size() > 0);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 增加一个数据表名和对应的CALL站点地址
	 * @param node CALL站点地址
	 * @param space 数据表名
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean addTableSite(Node node, Space space) {
		boolean success = false;
		// 锁定！
		super.lockSingle();
		try {
			// 数据表名保存到站点集合
			Cube cube = sites.get(node);
			if (cube == null) {
				cube = new Cube(node);
				sites.put(node, cube);
			}
			cube.add(space);

			// 数据表名保存到站点集合
			NodeSet set = tableSites.get(space);
			if (set == null) {
				set = new NodeSet();
				tableSites.put(space, set);
			}
			success = set.add(node);
			// 保存站点
			if (success) {
				callSites.add(node);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 判断分布任务组件有关联站点
	 * @param phase 阶段命名
	 * @return 返回真或者假
	 */
	public boolean hasTaskSite(Phase phase) {
		boolean success = false;
		super.lockMulti();
		try {
			NodeSet set = taskSites.get(phase);
			success = (set != null && set.size() > 0);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 增加一个阶段命名和它的CALL站点地址
	 * @param node CALL站点地址
	 * @param phase 阶段命名
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean addTaskSite(Node node, Phase phase) {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			// 阶段命名保存到汇总集合
			Cube cube = sites.get(node);
			if (cube == null) {
				cube = new Cube(node);
				sites.put(node, cube);
			}
			cube.add(phase);

			// 保存到节点集合
			NodeSet set = taskSites.get(phase);
			if (set == null) {
				set = new NodeSet();
				taskSites.put(phase, set);
//				System.out.printf("save phase: %s -> %s\n", phase, node);
			}
			success = set.add(node);
			// 保存
			if (success) {
				callSites.add(node);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 以上成功，显示在图形界面上
		if (success) {
			exhibit(phase);
		}

		return success;
	}


	/**
	 * 删除注册站点，以及关联的表和阶段命名
	 * @param node CALL节点
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Node node) {
		boolean success = false;
		// 单向锁定
		super.lockSingle();
		try {
			Cube cube = sites.remove(node);
			success = (cube != null);
			if (!success) {
				return false;
			}

			// 删除CALL节点
			callSites.remove(node);
			// 删除数据表
			for (Space space : cube.getSpaces()) {
				NodeSet set = tableSites.get(space);
				if (set != null) {
					set.remove(node);
					if (set.isEmpty()) tableSites.remove(space);
				}
			}
			// 删除阶段命名
			for (Phase phase : cube.getPhases()) {
				NodeSet set = taskSites.get(phase);
				if (set != null) {
					set.remove(node);
					// 阶段命名是空值时，清除全部
					if (set.isEmpty()) {
						taskSites.remove(phase);
						// 删除窗口上显示的阶段命名
						erase(phase);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "remove", success, "delete site %s", node);

		return success;
	}

	/**
	 * 清除本地的全部记录
	 */
	public void clear() {
		// 锁定
		super.lockSingle();
		try {
			account = null;
			// 节点
			sites.clear();
			tableSites.clear();
			taskSites.clear();
			// 清除被授权表
			passiveTables.clear();
			// 清除CALL节点
			callSites.clear();
			// 清除云存储节点
			cloudFields.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	//	/**
	//	 * 重新加载全部资源
	//	 * @return 成功返回真，否则假
	//	 */
	//	public boolean reload() {
	//		clear();
	//		
	//		/** 
	//		 * 从网络上（GATE/CALL节点）加载用户的网络数据，包括：
	//		 * 1. 用户账号
	//		 * 2. 被授权表，授权人的GATE站点地址，关联的CALL节点地址 
	//		 ***/
	//		FrontScheduleLoadInvoker invoker = new FrontScheduleLoadInvoker();
	//		return getInvokerPool().launch(invoker);
	//	}

	/**
	 * 根据名称查找数据库配置
	 * @param fame 数据库名称
	 * @return 返回数据库实例，或者空指针
	 */
	public Schema findSchema(Fame fame) {
		super.lockMulti();
		try {
			if (account != null) {
				return account.findSchema(fame);
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.pool.TubPusher#addTubTag(com.laxcus.tub.TubTag)
	 */
	@Override
	public void addTubTag(TubTag tag) {
		exhibit(tag);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.pool.TubPusher#removeTubTag(com.laxcus.tub.TubTag)
	 */
	@Override
	public void removeTubTag(TubTag tag) {
		erase(tag);
	}

	/**
	 * 保存授权人的分享表
	 * @param table 数据表实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addPassiveTable(Table table) {
		boolean success = false;
		super.lockSingle();
		try {
			Space space = table.getSpace();
			success = (account != null && account.hasPassiveTable(space));
			if (success) {
				passiveTables.put(space, table);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 删除授权人的分享表
	 * @param space 数据表名
	 * @return 成功返回真，否则假
	 */
	public boolean removePassiveTable(Space space) {
		boolean success = false;
		super.lockSingle();
		try {
			success = (account != null && account.removePassiveItem(space));
			if (success) {
				passiveTables.remove(space);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 查找被授权表
	 * @param space 数据表名
	 * @return 返回被授权表实例
	 */
	public Table findPassiveTable(Space space) {
		super.lockMulti();
		try {
			if (account != null && account.hasPassiveTable(space)) {
				return passiveTables.get(space);
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 返回被授权表
	 * @return 表名列表
	 */
	public List<Space> getPassiveSpaces() {
		super.lockMulti();
		try {
			return new ArrayList<Space>(passiveTables.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.VisitRobot#isOnline()
	 */
	@Override
	public boolean isOnline() {
		boolean success = false;
		// 锁定，判断条件：1. 已经登录：2. 管理员，3. 用户必须有账号！
		super.lockMulti();
		try {
			boolean b = getFrontLauncher().isLogined();
			if (b) {
				b = getFrontLauncher().isAdministrator();
				if (b) {
					success = true;
				} else {
					success = account != null;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#isAdministrator()
	 */
	@Override
	public boolean isAdministrator() {
		FrontLauncher launcher = StaffOnFrontPool.getFrontLauncher();
		return launcher.isAdministrator();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#isSameAdministrator()
	 */
	@Override
	public boolean isSameAdministrator() {
		FrontLauncher launcher = StaffOnFrontPool.getFrontLauncher();
		if (launcher.isUser()) {
			return canDBA();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#getOwner()
	 */
	@Override
	public Siger getOwner() {
		return getFrontLauncher().getUsername();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#findAccount(com.laxcus.util.Siger)
	 */
	@Override
	public Account findAccount(Siger siger) throws ResourceException {
		// 如果是自己，返回自己的账号
		if (isPrivate(siger)) {
			return account;
		}

		boolean allow = false;
		FrontLauncher launcher = StaffOnFrontPool.getFrontLauncher();
		if (launcher.isAdministrator()) {
			allow = true;
		} else {
			allow = canDBA(); // 拥有DBA身份
		}
		// 以上不成立，弹出异常，拒绝此项操作
		if (!allow) {
			cast(FaultTip.PERMISSION_MISSING);
		}
		
		// 实现查找
		TakeAccount cmd = new TakeAccount(siger);
		TakeAccountHook hook = new TakeAccountHook();
		hook.setTimeout(120000);
		ShiftTakeAccount shift = new ShiftTakeAccount(cmd, hook);
		shift.setSound(false); // 不要声音
		
		// 交给管理池处理
		boolean success = getCommandPool().press(shift);
		if (!success) {
			cast(FaultTip.SYSTEM_DENIED);
		}
		
		// 等待直到返回
		hook.await();

		// 获得结果
		Account account = hook.getAccount();
		success = (account != null);
		// 保存账号和账号下的配置
		if (success) {
			// 保存账号
			return account;
		}
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#findTable(com.laxcus.access.schema.Space)
	 */
	@Override
	public Table findTable(Space space) {
		Table table = null;

		// 查找账号中的表实例
		super.lockMulti();
		try {
			if (account != null) {
				table = account.findTable(space);
			}
		} finally {
			super.unlockMulti();
		}

		// 查找被授权表
		if (table == null) {
			table = findPassiveTable(space);
		}

		// 返回表实例，或者空指针
		return table;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.put.PutTrustor#findPutTable(com.laxcus.access.schema.Space)
	 */
	@Override
	public Table findPutTable(Space space) {
		return findTable(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.end.EndTrustor#findEndTable(com.laxcus.access.schema.Space)
	 */
	@Override
	public Table findEndTable(Space space) {
		return findTable(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.swift.near.NearTrustor#findNearTable(com.laxcus.access.schema.Space)
	 */
	@Override
	public Table findNearTable(Space space) {
		return findTable(space);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TailTrustor#isDesktop()
	 */
	@Override
	public boolean isDesktop() {
		return getFrontLauncher().isDesktop();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TailTrustor#isDriver()
	 */
	@Override
	public boolean isDriver() {
		return getFrontLauncher().isDriver();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TailTrustor#isConsole()
	 */
	@Override
	public boolean isConsole() {
		return getFrontLauncher().isConsole();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TailTrustor#isTerminal()
	 */
	@Override
	public boolean isTerminal() {
		return getFrontLauncher().isTerminal();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TailTrustor#isEdge()
	 */
	@Override
	public boolean isEdge() {
		return getFrontLauncher().isEdge();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TailTrustor#isApplication()
	 */
	@Override
	public boolean isApplication() {
		return false;
	}

	/**
	 * 弹出资源异常
	 * @param no 错误编号
	 * @throws ResourceException
	 */
	private void cast(int no) throws ResourceException {
		FrontLauncher launcher = StaffOnFrontPool.getFrontLauncher();
		String e = launcher.fault(no);
		throw new ResourceException(e);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#hasLocalTask(com.laxcus.util.naming.Phase)
	 */
	@Override
	public boolean hasLocalTask(Phase phase) throws ResourceException {
		// 如果不是规定的在FRONT节点上的阶段类型，弹出异常
		if (!PhaseTag.onFrontSite(phase.getFamily())) {
			cast(FaultTip.SYSTEM_DENIED);
		}

		// 判断存在
		boolean success = false;
		if (PhaseTag.isConduct(phase.getFamily())) {
			success = PutTaskPool.getInstance().contains(phase);
		} else if (PhaseTag.isEstablish(phase.getFamily())) {
			success = EndTaskPool.getInstance().contains(phase);
		} else if (PhaseTag.isContact(phase.getFamily())) {
			success = NearTaskPool.getInstance().contains(phase);
		}

		return success;
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.access.parse.ResourceChooser#hasTable(com.laxcus.access.schema.Space)
//	 */
//	@Override
//	public boolean hasTable(Space space) throws ResourceException {
//		// 管理员不可以建立数据库
//		FrontLauncher launcher = StaffOnFrontPool.getFrontLauncher();
//		if (launcher.isAdministrator()) {
//			cast(FaultTip.SYSTEM_DENIED);
//		}
//
//		// 数据表为当前用户私有，或者是共享表，返回真
//		if (isPrivate(space) || isPassiveTable(space)) {
//			return true;
//		}
//
//		// 拒绝建表
//		if (!canCreateTable(space)) {
//			cast(FaultTip.PERMISSION_MISSING);
//		}
//
//		AssertTable cmd = new AssertTable(space);
//		AssertTableHook hook = new AssertTableHook();
//		hook.setTimeout(120000);
//		ShiftAssertTable shift = new ShiftAssertTable(cmd, hook);
//		// 根据运行环境，交给 DriverCommandPool or MeetCommadnPool 处理
//		boolean success = getLauncher().getCommandPool().press(shift);
//		if (!success) {
//			cast(FaultTip.SYSTEM_DENIED);
//		}
//		hook.await();
//
//		// 取出返回结果
//		AssertTableProduct product = hook.getProduct();
//		success = (product != null);
//		if (success) {
//			success = product.isSuccessful();
//		} else {
//			cast(FaultTip.JOB_TIMEOUT); // 业务超时！
//		}
//
//		Logger.debug(this, "hasTable", success, "check %s is", space);
//
//		return success;
//	}


	
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.access.parse.ResourceChooser#hasSchema(com.laxcus.access.schema.Fame)
//	 */
//	@Override
//	public boolean hasSchema(Fame fame) throws ResourceException {
//		// 管理员不可以建立数据库
//		FrontLauncher launcher = StaffOnFrontPool.getFrontLauncher();
//		if (launcher.isAdministrator()) {
//			cast(FaultTip.SYSTEM_DENIED);
//		}
//
//		// 数据库为当前账号私有，返回真
//		if (isPrivate(fame)) {
//			return true;
//		}
//
//		// 检查建立数据库权限，不允许弹出异常
//		if (!canCreateSchema(fame)) {
//			cast(FaultTip.PERMISSION_MISSING);
//		}
//
//		AssertSchema cmd = new AssertSchema(fame);
//		AssertSchemaHook hook = new AssertSchemaHook();
//		hook.setTimeout(120000);
//		ShiftAssertSchema shift = new ShiftAssertSchema(cmd, hook);
//
//		// 操作命令，返回结果（根据运行环境，分别提交给 MeetCommandPool / DriverCommandPool）
//		boolean success = getLauncher().getCommandPool().press(shift);
//		if (!success) {
//			cast(FaultTip.SYSTEM_DENIED);
//		}
//		// 等待直到返回
//		hook.await();
//
//		// 取出返回结果
//		AssertSchemaProduct product = hook.getProduct();
//		success = (product != null);
//		if (success) {
//			success = product.isSuccessful();
//		} else {
//			cast(FaultTip.JOB_TIMEOUT); // 业务超时！
//		}
//
//		Logger.debug(this, "hasSchema", success, "check %s is", fame);
//
//		return success;
//	}

	/**
	 * 背景：
	 * 管理员/用户都可以查询数据库名称
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#hasSchema(com.laxcus.access.schema.Fame)
	 */
	@Override
	public boolean hasSchema(Fame fame) throws ResourceException {
		// 数据库为当前账号私有，返回真
		if (isPrivate(fame)) {
			return true;
		}

		//		// 管理员不可以建立数据库
		//		FrontLauncher launcher = StaffOnFrontPool.getFrontLauncher();
		//		if (launcher.isAdministrator()) {
		//			cast(FaultTip.SYSTEM_DENIED);
		//		}
		//
		//		// 检查建立数据库权限，不允许弹出异常
		//		if (!canCreateSchema(fame)) {
		//			cast(FaultTip.PERMISSION_MISSING);
		//		}

		AssertSchema cmd = new AssertSchema(fame);
		AssertSchemaHook hook = new AssertSchemaHook();
		hook.setTimeout(120000);
		ShiftAssertSchema shift = new ShiftAssertSchema(cmd, hook);
		shift.setSound(false);

		// 操作命令，返回结果（根据运行环境，分别提交给 MeetCommandPool / DriverCommandPool）
		boolean success = getLauncher().getCommandPool().press(shift);
		if (!success) {
			cast(FaultTip.SYSTEM_DENIED);
		}
		// 等待直到返回
		hook.await();

		// 取出返回结果
		AssertSchemaProduct product = hook.getProduct();
		success = (product != null);
		if (success) {
			success = product.isSuccessful();
		} else {
			cast(FaultTip.JOB_TIMEOUT); // 业务超时！
		}

		Logger.debug(this, "hasSchema", success, "check %s is", fame);

		return success;
	}
	
	/*
	 * 背景：
	 * 1. 管理员可以做全部
	 * 2. 用户只能做自己
	 * @see com.laxcus.access.parse.ResourceChooser#hasTable(com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean hasTable(Space space) throws ResourceException {
		// 2. 数据表为当前用户私有，或者是共享表，返回真
		if (isPrivate(space) || isPassiveTable(space)) {
			return true;
		}

		// 管理员不可以建立数据库
		FrontLauncher launcher = StaffOnFrontPool.getFrontLauncher();

		//		// 拒绝建表
		//		if (!canCreateTable(space)) {
		//			cast(FaultTip.PERMISSION_MISSING);
		//		}

		boolean allow = false;
		
		// 管理员可以做全部
		if (launcher.isAdministrator()) {
			allow = true;
		}
		// 如果数据库属于当前用户，可以查询
		else if (launcher.isUser()) {
			if (isPrivate(space.getSchema())) {
				allow = true;
			}
		}

		// 不允许，弹出异常
		if (!allow) {
			cast(FaultTip.SYSTEM_DENIED);
		}

		// 去服务器上果询
		AssertTable cmd = new AssertTable(space);
		AssertTableHook hook = new AssertTableHook();
		hook.setTimeout(120000);
		ShiftAssertTable shift = new ShiftAssertTable(cmd, hook);
		shift.setSound(false);
		
		// 根据运行环境，交给 DriverCommandPool or MeetCommadnPool 处理
		boolean success = getLauncher().getCommandPool().press(shift);
		if (!success) {
			cast(FaultTip.SYSTEM_DENIED);
		}
		hook.await();

		// 取出返回结果
		AssertTableProduct product = hook.getProduct();
		success = (product != null);
		if (success) {
			success = product.isSuccessful();
		} else {
			cast(FaultTip.JOB_TIMEOUT); // 业务超时！
		}

		Logger.debug(this, "hasTable", success, "check %s is", space);

		return success;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#hasUser(java.lang.String)
	 */
	@Override
	public boolean hasUser(String username) throws ResourceException {
		Siger siger = SHAUser.doUsername(username);
		return hasUser(siger);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#hasUser(com.laxcus.util.Siger)
	 */
	@Override
	public boolean hasUser(Siger siger) throws ResourceException {
		// 是账号持有人自己，返回真
		if (isPrivate(siger)) {
			return true;
		}

		// 如果是数据库管理员，拥有所有权限（不包括建表）
		FrontLauncher launcher = StaffOnFrontPool.getFrontLauncher();
		boolean success = launcher.isAdministrator();
		// 判断当前账号的操作权限
		if (!success) {
			success = canCreateUser();
		}
		// 以上不成立，弹出异常，拒绝此项操作
		if (!success) {
			cast(FaultTip.PERMISSION_MISSING);
		}

		// 去GATE->BANK站点检查用户账号存在
		AssertUser cmd = new AssertUser(siger);
		AssertUserHook hook = new AssertUserHook();
		ShiftAssertUser shift = new ShiftAssertUser(cmd, hook);
		shift.setTimeout(getFrontLauncher().getCommandTimeout());
		shift.setSound(false); // 不要播放声音

		// 交给命令池处理，根据运行环境，是 MeetCommandPool / DriverCommandPool 中的任意一种
		success = getLauncher().getCommandPool().press(shift);
		if (!success) {
			cast(FaultTip.SYSTEM_DENIED);
		}
		hook.await();

		// 判断存在或者否
		AssertUserProduct product = hook.getProduct();
		success = (product != null);
		if (success) {
			success = product.isSuccessful();
		} else {
			cast(FaultTip.JOB_TIMEOUT); // 业务超时！
		}

		Logger.debug(this, "hasUser", success, "check %s is", siger);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#hasTubTag(com.laxcus.util.naming.Naming)
	 */
	@Override
	public boolean hasTubTag(Naming naming) throws ResourceException {
		return TubPool.getInstance().hasTag(naming);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#isPrivate(com.laxcus.util.Siger)
	 */
	@Override
	public boolean isPrivate(Siger siger) {
		super.lockMulti();
		try {
			return account != null
			&& Laxkit.compareTo(siger, account.getUsername()) == 0;
		} finally {
			super.unlockMulti();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#isPrivate(com.laxcus.access.schema.Fame)
	 */
	@Override
	public boolean isPrivate(Fame fame) {
		super.lockMulti();
		try {
			return (account != null && account.hasSchema(fame));
		} finally {
			super.unlockMulti();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#isPrivate(com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean isPrivate(Space space) {
		super.lockMulti();
		try {
			return (account != null && account.hasTable(space)) ;
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 判断当前账号拥有建立账号权限
	 * @return 允许返回“真”，否则“假”。
	 */
	public boolean canCreateUser() {
		super.lockMulti();
		try {
			return account != null && account.canCreateUser();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断有删除账号权限
	 * @return 允许返回“真”，否则“假”。
	 */
	public boolean canDropUser() {
		super.lockMulti();
		try {
			return account != null && account.canDropUser();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断拥有管理员身份
	 * @return 返回真或者假
	 */
	public boolean canDBA() {
		super.lockMulti();
		try {
			return account != null && account.canDBA();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断允许执行开放共享资源操作
	 * @return 返回真或者假
	 */
	public boolean canOpenCross() {
		super.lockMulti();
		try {
			return account != null && account.canOpenResource();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断允许执行关闭共享资源操作
	 * @return 返回真或者假
	 */
	public boolean canCloseCross() {
		super.lockMulti();
		try {
			return account != null && account.canCloseResource();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断有建立数据库权限
	 * @param fame 数据库名称
	 * @return 允许返回“真”，否则“假”。
	 */
	public boolean canCreateSchema(Fame fame) {
		super.lockMulti();
		try {
			return account != null && account.canCreateSchema(fame);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断有删除数据库权限
	 * @param fame 数据库名称
	 * @return 允许返回“真”，否则“假”。
	 */
	public boolean canDropSchema(Fame fame) {
		super.lockMulti();
		try {
			return account != null && account.canDropSchema(fame);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断有建表权限
	 * @param space 数据表名
	 * @return 允许返回“真”，否则“假”。
	 */
	public boolean canCreateTable(Space space) {
		super.lockMulti();
		try {
			return account != null && account.canCreateTable(space);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断有删表权限
	 * @param space
	 * @return 允许返回“真”，否则“假”。
	 */
	public boolean canDropTable(Space space) {
		super.lockMulti();
		try {
			return account != null && account.canDropTable(space);
		} finally {
			super.unlockMulti();
		}
	}

	//	/**
	//	 * 判断当前账号拥有建立快捷组件权限
	//	 * @return 允许返回“真”，否则“假”。
	//	 */
	//	public boolean canPublishSwift() {
	//		super.lockMulti();
	//		try {
	//			return account != null && account.canPublishSwift();
	//		} finally {
	//			super.unlockMulti();
	//		}
	//	}
	//
	//	/**
	//	 * 判断当前账号拥有建立快捷组件权限动态链接库
	//	 * @return 允许返回“真”，否则“假”。
	//	 */
	//	public boolean canPublishSwiftLibrary() {
	//		super.lockMulti();
	//		try {
	//			return account != null && account.canPublishSwiftLibrary();
	//		} finally {
	//			super.unlockMulti();
	//		}
	//	}
	//	
	//	/**
	//	 * 判断有删除快捷组件权限
	//	 * @return 允许返回“真”，否则“假”。
	//	 */
	//	public boolean canDropSwift() {
	//		super.lockMulti();
	//		try {
	//			return account != null && account.canDropSwift();
	//		} finally {
	//			super.unlockMulti();
	//		}
	//	}

	/**
	 * 判断当前账号拥有发布分布任务组件权限
	 * @return 允许返回“真”，否则“假”。
	 */
	public boolean canPublishTask() {
		super.lockMulti();
		try {
			return account != null && account.canPublishTask();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断当前账号拥有发布分布任务组件动态链接库权限
	 * @return 允许返回“真”，否则“假”。
	 */
	public boolean canPublishTaskLibrary() {
		super.lockMulti();
		try {
			return account != null && account.canPublishTaskLibrary();
		} finally {
			super.unlockMulti();
		}
	}

	//	/**
	//	 * 判断当前账号拥有发布码位计算器权限
	//	 * @return 允许返回“真”，否则“假”。
	//	 */
	//	public boolean canPublishScaler() {
	//		super.lockMulti();
	//		try {
	//			return account != null && account.canPublishScaler();
	//		} finally {
	//			super.unlockMulti();
	//		}
	//	}
	//	
	//	/**
	//	 * 判断当前账号拥有发布码位计算器动态链接库权限
	//	 * @return 允许返回“真”，否则“假”。
	//	 */
	//	public boolean canPublishScalerLibrary() {
	//		super.lockMulti();
	//		try {
	//			return account != null && account.canPublishScalerLibrary();
	//		} finally {
	//			super.unlockMulti();
	//		}
	//	}


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
		Logger.info(this, "process", "check interval:%d ms, into...", checkInterval);

		// 下一次触发时间
		long nextTime = System.currentTimeMillis() + checkInterval;

		while (!isInterrupted()) {
			// 没有登录，延时处理
			if (!isLogined()) {
				delay(1000L);
				continue;
			}
			// 如果更新
			if (forwardRefresh) {
				forwardRefresh = false;
				nextTime = System.currentTimeMillis() + forwardTime; // 在xxx秒之后，更新...
			}

			// 达到触发时间，去GATE站点获取最新的CALL站点信息
			if (System.currentTimeMillis() >= nextTime) {
				// 下次触发时间（循环处理）
				nextTime = System.currentTimeMillis() + checkInterval;
				// 检查/增加/删除所属资源
				check();
			}
			// 延时
			delay(1000L);
		}

		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {

	}

	/** 加快更新 **/
	private volatile boolean forwardRefresh = false;
	
	/** 超时... **/
	private volatile long forwardTime = 20000L;
	
	/**
	 * 加快更新
	 * @param timeout
	 */
	public void forwardScheduleRefresh(long timeout) {
		forwardRefresh = true;
		skipRefresh = false;
		if (timeout >= 1000L) {
			forwardTime = timeout;
		}
	}

	/**
	 * 检查/增加/删除资源
	 */
	private void check() {
		if (!skipRefresh) {
			RefreshSchedule cmd = new RefreshSchedule();
			cmd.setSound(false); // 不要播放声音
			getCommandPool().admit(cmd);
		}

		skipRefresh = false;
	}

	/** 跨越 **/
	private volatile boolean skipRefresh = false;

	/**
	 * 跨越刷新 <br>
	 * 当用户删除分布应用软件、数据库、数据表的时候，如果同时启动刷新，会发生显示记录不一致的情况。<br>
	 * 所以，当删除分布应用软件、数据库、数据表时，先这个操作。
	 */
	public void skipScheduleRefresh() {
		skipRefresh = true;
	}

	/**
	 * 保存数据重组时间
	 * @param time 数据重组时间
	 * @return 成功返回真，否则假
	 */
	public boolean createRegulateTime(SwitchTime time) {
		boolean success = false;
		super.lockSingle();
		try {
			success = (account != null);
			if (success) {
				success = account.createSwitchTime(time);
			}
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 删除数据重组时间
	 * @param space 数据表名
	 * @return 成功返回真，否则假
	 */
	public boolean dropRegulateTime(Space space) {
		boolean success = false;
		super.lockSingle();
		try {
			success = (account != null);
			if (success) {
				success = account.dropSwitchTime(space);
			}
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 输出全部数据重组时间
	 * @return 重组时间列表
	 */
	public List<SwitchTime> getSwitchTimes() {
		ArrayList<SwitchTime> array = new ArrayList<SwitchTime>();
		super.lockMulti();
		try {
			if (account != null) {
				array.addAll(account.getSwitchTimes());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 建立数据库
	 * @param schema 数据库配置
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean createSchema(Schema schema) {
		boolean success = (schema != null);
		// 锁定！
		super.lockSingle();
		try {
			if (success) {
				success = (account != null);
			}
			if (success) {
				success = account.createSchema(schema);
			}
		} finally {
			super.unlockSingle();
		}

		// 显示
		if (success) {
			exhibit(schema);
		}

		return success;
	}

	/**
	 * 建立数据表
	 * @param table
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean createTable(Table table) {
		boolean success = (table != null);
		// 锁定！
		super.lockSingle();
		try {
			if (success) {
				success = (account != null);
			}
			if (success) {
				success = account.createTable(table);
			}
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "createTable", success, "create %s", table.getSpace());

		// 在窗口上显示
		if (success) {
			exhibit(table);
		}

		return success;
	}

	/**
	 * 保存和UI界面显示被授权单元
	 * @param item 被授权单元
	 * @return 成功返回真，否则假
	 */
	public boolean createPassiveItem(PassiveItem item) {
		Laxkit.nullabled(item);

		boolean exists = true;
		boolean success = false;
		// 锁定!
		super.lockSingle();
		try {
			// 判断被授权表不存在！
			exists = account.hasPassiveTable(item.getSpace());
			// 保存被授权单元
			success = account.addPassiveItem(item);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 被授权单元不存在且保存成功时，在窗口上显示
		if (!exists && success) {
			exhibit(item);
		}

		//		Logger.debug(this, "createPassiveItem", success, "处理 %s exists %s", item, exists);

		return success;
	}

	/**
	 * 更新被授权单元。前提条件是这个被授权表已经存在
	 * @param item
	 * @return
	 */
	public boolean updatePassiveItem(PassiveItem item) {
		Laxkit.nullabled(item);

		Siger authorizer = item.getAuthorizer();
		Space space = item.getSpace();

		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			// 判断有这个表和授权人
			success = (account.hasPassiveTable(space) && 
					account.hasPassiveAuthorizer(authorizer));
			// 删除旧的
			if (success) {
				success = account.removePassiveItem(authorizer, space);
			}
			// 保存新的
			if (success) {
				success = account.addPassiveItem(item);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 更新
		if (success) {
			exhibit(item);
		}

		//		Logger.debug(this, "updatePassiveItem", success, "exchange %s", item);

		return success;
	}

	/**
	 * 清除内存和UI界面上的被授权单元
	 * @param item 被授权单元
	 * @return 成功返回真，否则假
	 */
	public boolean dropPassiveItem(PassiveItem item) {
		Laxkit.nullabled(item);

		boolean exists = true;
		boolean success = false;
		// 锁定！
		super.lockSingle();
		try {
			// 清除授权单元
			success = account.removePassiveItem(item);
			// 判断被授权表已经不存在
			exists = account.hasPassiveTable(item.getSpace());
			// 已经不存在，从被授权表内存中清除它
			if (!exists) {
				passiveTables.remove(item.getSpace());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 被授权单元删除成功，且内存中不存在时，从窗口上清除它！
		if (success && !exists) {
			erase(item);
		}

		return success;
	}

	/**
	 * 删除表和这个表关联的CALL站点
	 * @param space 数据表名
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean dropTable(Space space) {
		boolean success = false;
		super.lockSingle();
		try {
			// 删除表
			Table table = account.dropTable(space);
			success = (table != null);
			// 删除关联的CALL站点
			NodeSet set = tableSites.remove(space);
			if (set != null) {
				for (Node node : set.list()) {
					Cube cube = sites.get(node);
					cube.remove(space);
					if (cube.isEmpty()) {
						sites.remove(node);
						callSites.remove(node);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 从窗口上删除
		if (success) {
			erase(space);
		}

		Logger.debug(this, "dropTable", success, "delete table %s", space);

		return success;
	}

	/**
	 * 清除一个阶段命名和它关联的节点
	 * @param phase 阶段命名
	 * @return 成功返回真，否则假
	 */
	public boolean dropTask(Phase phase) {
		boolean success = false;
		// 锁定！
		super.lockSingle();
		try {
			NodeSet set = taskSites.remove(phase);
			success = (set != null);
			if (success) {
				for (Node node : set.list()) {
					Cube cube = sites.get(node);
					if (cube.isEmpty()) {
						sites.remove(node);
						callSites.remove(node);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		if (success) {
			// 删除窗口上显示的阶段命名
			erase(phase);
		}

		Logger.debug(this, "dropTask", success, "delete %s", phase);

		return success;
	}



	/**
	 * 删除数据库，以及这个数据库下属的表和关联站点
	 * @param fame 数据库名称
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean dropSchema(Fame fame) {
		Schema schema = null;
		// 锁定删除
		super.lockSingle();
		try {
			schema = account.dropSchema(fame);
		} finally {
			super.unlockSingle();
		}

		boolean success = (schema != null);
		// 删除成功，删除下属表和关联站点
		if (success) {
			for (Space space : schema.getSpaces()) {
				dropTable(space);
			}
		}

		if(success) {
			erase(fame);
		}

		return success;
	}

	/**
	 * 输出全部数据库名称
	 * @return 数据库名称列表
	 */
	public List<Fame> getFames() {
		super.lockMulti();
		try {
			if (account != null) {
				return account.getFames();
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 输出全部数据库
	 * @return 数据库列表
	 */
	public List<Schema> getSchemas() {
		super.lockMulti();
		try {
			if (account != null) {
				return account.getSchemas();
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 输出全部数据表
	 * @return 数据表列表
	 */
	public List<Table> getTables() {
		super.lockMulti();
		try {
			if (account != null) {
				return account.getTables();
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 输出全部数据表名，包括自有表/授权表两种
	 * 
	 * @return 数据表名列表
	 */
	public List<Space> getSpaces() {
		super.lockMulti();
		try {
			return new ArrayList<Space>(tableSites.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 输出全部阶段命名
	 * @return 阶段命名列表
	 */
	public List<Phase> getPhases() {
		super.lockMulti();
		try {
			return new ArrayList<Phase>(taskSites.keySet());
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 查找匹配的阶段命名
	 * @param family 命名类型
	 * @return 返回适配的阶段命名数组
	 */
	public List<Phase> findPhases(int family) {
		ArrayList<Phase> a = new ArrayList<Phase>();
		// 锁定
		super.lockMulti();
		try {
			Iterator<Phase> iterator = taskSites.keySet().iterator(); 
			while (iterator.hasNext()) {
				Phase phase = iterator.next();
				if (phase.getFamily() == family) {
					a.add(phase);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return a;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#isPassiveTable(com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean isPassiveTable(Space space) {
		super.lockMulti();
		try {
			return account != null && account.hasPassiveTable(space);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 返回分享表的授权人
	 * @param space 数据表名
	 * @return 授权人签名
	 */
	public Siger findAuthorizer(Space space) {
		super.lockMulti();
		try {
			if (account != null) {
				return account.findPassiveAuthorizer(space);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 判断是共享数据库
	 * @param fame
	 * @return
	 */
	public boolean isCrossSchema(Fame fame) {
		super.lockMulti();
		try {
			if (account != null) {
				for (PassiveItem e : account.getPassiveItems()) {
					if (Laxkit.compareTo(e.getSchema(), fame) == 0) {
						return true;
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#canUser(short)
	 */
	@Override
	public boolean canUser(short operator) {
		super.lockMulti();
		try {
			return account != null && account.canUser(operator);
		} finally {
			super.unlockMulti();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#canSchema(com.laxcus.access.schema.Fame, short)
	 */
	@Override
	public boolean canSchema(Fame fame, short operator) {
		super.lockMulti();
		try {
			return account != null && account.canSchema(fame, operator);
		} finally {
			super.unlockMulti();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#canTable(com.laxcus.access.schema.Space, short)
	 */
	@Override
	public boolean canTable(Space space, short operator) {
		super.lockMulti();
		try {
			return account != null && account.canTable(space, operator);
		} finally {
			super.unlockMulti();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#canSelect(com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean canSelect(Space space) {
		boolean success = false;
		super.lockMulti();
		try {
			// 判断用户有SELECT权限
			if (account != null) {
				success = (account.hasTable(space) && account.canTable(space, ControlTag.SELECT));
				// 判断被授权表
				if (!success) {
					PassiveItem item = account.findPassiveItem(space);
					success = (item != null && item.allow(new CrossFlag(space, CrossOperator.SELECT)));
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#canSelect(com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean canInsert(Space space) {
		boolean success = false;
		// 锁定 ！
		super.lockMulti();
		try {
			// 判断用户有INSERT权限
			if (account != null) {
				success = (account.hasTable(space) && account.canTable(space, ControlTag.INSERT));
				// 判断被授权表
				if (!success) {
					PassiveItem item = account.findPassiveItem(space);
					success = (item != null && item.allow(new CrossFlag(space, CrossOperator.INSERT)));
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#canSelect(com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean canDelete(Space space) {
		boolean success = false;
		super.lockMulti();
		try {
			// 判断用户有DELETE权限
			if (account != null) {
				success = (account.hasTable(space) && account.canTable(space, ControlTag.DELETE));
				// 判断被授权表
				if (!success) {
					PassiveItem item = account.findPassiveItem(space);
					success = (item != null && item.allow(new CrossFlag(space, CrossOperator.DELETE)));
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#canUpdate(com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean canUpdate(Space space) {
		boolean success = false;
		super.lockMulti();
		try {
			// 判断用户有UPDATE权限
			if (account != null) {
				success = (account.hasTable(space) && account.canTable(space, ControlTag.UPDATE));
				// 判断被授权表
				if (!success) {
					PassiveItem item = account.findPassiveItem(space);
					success = (item != null && item.allow(new CrossFlag(space, CrossOperator.UPDATE)));
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#canConduct()
	 */
	@Override
	public boolean canConduct() {
		boolean success = false;
		// 锁定！
		super.lockMulti();
		try {
			if (account != null) {
				success = account.canConduct();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#canContact()
	 */
	@Override
	public boolean canContact() {
		boolean success = false;
		// 锁定！
		super.lockMulti();
		try {
			if (account != null) {
				success = account.canContact();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#canEstablish()
	 */
	@Override
	public boolean canEstablish() {
		boolean success = false;
		super.lockMulti();
		try {
			if (account != null) {
				success = account.canEstablish();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.parse.ResourceChooser#canEstablish(com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean canEstablish(Space space) {
		boolean success = false;
		// 锁定！
		super.lockMulti();
		try {
			if (account != null) {
				success = account.canEstablish(space);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 根据当前注册用户权级和网络状态，在底栏显示状态图标
	 */
	public abstract void reveal();

	/**
	 * 显示数据库
	 * @param schema
	 */
	protected abstract void exhibit(Schema schema);

	/**
	 * 删除数据库
	 * @param fame
	 */
	protected abstract void erase(Fame fame);

	/**
	 * 显示数据表
	 * @param table
	 */
	protected abstract void exhibit(Table table);

	/**
	 * 删除数据表
	 * @param space
	 */
	protected abstract void erase(Space space);

	/**
	 * 显示分布任务组件的阶段命名
	 * @param phase
	 */
	protected abstract void exhibit(Phase phase);

	/**
	 * 删除分布任务组件的阶段命名
	 * @param phase
	 */
	protected abstract void erase(Phase phase);

	/**
	 * 显示被授权单元
	 * 
	 * @param item
	 */
	protected abstract void exhibit(PassiveItem item);

	/**
	 * 删除被授权单元
	 * 
	 * @param item
	 */
	protected abstract void erase(PassiveItem item);

	/**
	 * 显示边缘容器标记
	 * 
	 * @param tag
	 */
	protected abstract void exhibit(TubTag tag);

	/**
	 * 删除边缘容器标记
	 * 
	 * @param tag
	 */
	protected abstract void erase(TubTag tag);
}