/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.dict;

import java.io.*;
import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.account.*;
import com.laxcus.account.pool.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.datetime.*;

/**
 * 账号管理池 <br>
 * 
 * 保存与用户有关的全部资源，包括：用户账号，关联的数据库和数据表配置，快捷组件配置。
 * 
 * 账号管理池参数：
 * 1. 每个目录，对应一个管理器（启动时生成）
 * 2. 每个文件编号，对应目录（编号找目录，目录找管理器）
 * 3. 每个签名，对应锚点（包含用户签名、文件编号、下标、长度）
 * 4. 管理器申请生成新文件时，需要传递自己的根目录
 * 
 * @author scott.liang
 * @version 1.1 8/12/2015
 * @since laxcus 1.0
 */
public class StaffOnAccountPool extends VirtualPool {

	/** 静态句柄 **/
	private static StaffOnAccountPool selfHandle = new StaffOnAccountPool();
	
	/** 迭代索引号 **/
	private int iterateIndex;

	/** 管理器序列号（从0开始） -> 词典管理器 **/
	private Map<Integer, AccountManager> managers = new TreeMap<Integer, AccountManager>();

	/** 文件编号 -> 管理器序列号（序列号从0开始） **/
	private Map<Integer, Integer> files = new TreeMap<Integer, Integer>();

	/** 用户签名 -> 账号坐标 **/
	private TreeMap<Siger, AccountDock> docks = new TreeMap<Siger, AccountDock>();

	/** 最后一个编号 **/
	private int lastNo;

	/**
	 * 构造默认和私有的账号管理池
	 */
	private StaffOnAccountPool() {
		super();
		// 5秒间隔
		setSleepTime(5);
		// 块文件编号下标从1开始，这里自增1
		setLastNo(0);
		// 从0开始
		iterateIndex = 0;
	}

	/**
	 * 返回账号管理池静态句柄
	 * @return
	 */
	public static StaffOnAccountPool getInstance() {
		return StaffOnAccountPool.selfHandle;
	}

	/**
	 * 增加一个账号管理器
	 * @param root 根目录
	 * @return 返回真或者假
	 */
	public boolean addManager(String root) {
		AccountManager manager = new AccountManager();
		boolean success = manager.setRoot(root);
		if (success) {
			manager.setIndex(managers.size()); // 编号从0开始
			managers.put(manager.getIndex(), manager);
		}
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
			return docks.size();
		} finally {
			super.unlockMulti();
		}
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
		Logger.debug(this, "process", "into ...");

		// 加载磁盘上的账号
		boolean success = loadResource();
		// 成功，重新注册；失败，退出
		if (success) {
			getLauncher().checkin(false);
			// 加载定时检测
			loadMemberChecker();
		} else {
			getLauncher().stop();
		}

		// 循环判断线程退出
		while (!isInterrupted()) {
			check();
			// 延时
			sleep();
		}

		Logger.debug(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		docks.clear();
		files.clear();
		managers.clear();
	}
	
	/**
	 * 启动用户资源检测器，定时检测节点上的用户数目
	 */
	private void loadMemberChecker() {
		AccountLauncher launcher = (AccountLauncher) getLauncher();
		MemberCyber cyber = launcher.getMemberCyber();
		Timer timer = getLauncher().getTimer();
		MemberChecker checker = new MemberChecker(this);
		timer.schedule(checker, 0, cyber.getTimeout());
	}

	/**
	 * 推送注册成员给WATCH节点，经过BANK转发
	 */
	public void pushRegisterMember() {
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
		AccountLauncher launcher = (AccountLauncher) getLauncher();
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
	 * 设置最后一个块文件的编号
	 * @param no 编号
	 */
	private void setLastNo(int no) {
		if (lastNo < no) lastNo = no;
	}
	
	/**
	 * 加载磁盘上的数据资源，包括：<br>
	 * 1. 账号文件的编号 <br>
	 * 2. 账号文件里的账号内容 <br>
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean loadResource() {
		boolean success = false;
		
		try {
			Iterator<Map.Entry<Integer, AccountManager>> iterator = managers.entrySet().iterator();
			while (iterator.hasNext()) {
				AccountManager manager = iterator.next().getValue();
				int[] serials = manager.getFileSerials();
				// 保存文件编号
				for (int no : serials) {
					// 最后一个编号（是最大数字，编号全局唯一）
					setLastNo(no);
					// 保存 文件编号 -> 管理器序列号
					files.put(no, manager.getIndex());
				}
				
				// 从指定目录下的全部文件里，提取账号坐标
				List<AccountDock> all = manager.loadAccounts();
				// 返回空指针是错误
				if (all == null) {
					Logger.error(this, "loadAccount", "load %s error!", manager.getRoot());
					return false;
				}

				// 记录每个账号的坐标位置
				for (AccountDock dock : all) {
					// 账号签名 -> 账号坐标
					docks.put(dock.getSiger(), dock);
					
					Logger.debug(this, "loadAccount", "load %s / %s", dock.getSiger(), dock.getDock());
				}
			}
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} 

		Logger.note(this, "loadAccounts", success, "all account size:%d", docks.size());

		return success;
	}

//	/**
//	 * 以锁定方式，加载全部账号
//	 * @return 成功返回真，否则假
//	 */
//	private boolean loadAccounts() {
//		boolean success = false;
//		super.lockSingle();
//		try {
//			Iterator<Map.Entry<Integer, AccountManager>> iterator = managers.entrySet().iterator();
//			while (iterator.hasNext()) {
//				AccountManager manager = iterator.next().getValue();
//				List<AccountDock> all = manager.loadAccounts();
//				// 返回空指针是错误
//				if (all == null) {
//					Logger.error(this, "loadAccount", "load %s error!", manager.getRoot());
//					return false;
//				}
//
//				// 记录每个账号的坐标位置
//				for (AccountDock dock : all) {
//					// 账号签名 -> 账号坐标
//					docks.put(dock.getSiger(), dock);
//					// 保存 文件编号 -> 管理器序列号
//					files.put(dock.getNo(), manager.getIndex());
//				}
//
//				// 最后一个编号（编号全局唯一）
//				AccountFile file = manager.getCurrentFile();
//				if (file != null) {
//					setLastNo(file.getNo());
//				}
//			}
//			success = true;
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//
//		Logger.note(this, "loadAccounts", success, "all account size:%d", docks.size());
//
//		return success;
//	}

//	/**
//	 * 保存文件编号 -> 管理器序列号，形成映射关系
//	 * @param fileNo 文件编号
//	 * @param managerIndex 管理器序列号
//	 */
//	private void addFileNo(int fileNo, int managerIndex) {
//		super.lockSingle();
//		try {
//			Integer oldIndex = files.get(fileNo);
//			// 如果没有，保存它！
//			if (oldIndex == null) {
//				files.put(fileNo, managerIndex);
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//	}

	/**
	 * 以锁定方式，保存一个账号坐标
	 * @param dock 账号坐标
	 */
	private void addDock(AccountDock dock) {
		super.lockSingle();
		try {
			docks.put(dock.getSiger(), dock);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 删除账号坐标
	 * @param siger 用户签名
	 * @return 返回被删除的坐标
	 */
	private AccountDock removeDock(Siger siger) {
		super.lockSingle();
		try {
			return docks.remove(siger);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 管理器根目录（局部）+编号（全局），产生一个新的文件。这个文件必须是不存在的。
	 * 
	 * @param root 管理器根目录
	 * @return 返回一个新账号文件
	 */
	protected AccountFile applyFile(AccountManager manager) {
		super.lockSingle();
		try {
			File root = manager.getRoot();
			while (true) {
				int no = ++lastNo;
				String name = String.format("%d%s", no, AccountManager.SUFFIX);
				File file = new File(root, name);
				// 文件不存在，输出它
				if (!file.exists()) {
					files.put(no, manager.getIndex()); // 文件编号 -> 管理器编号，形成关联绑定
					return new AccountFile(no, file);
				}
			}
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 检查超时账号，从内存中删除它
	 */
	private void check() {
		// 命令集合
		ArrayList<PressRegulate> array = new ArrayList<PressRegulate>();
		
		// 检查参数
		super.lockSingle();
		try {
			Iterator<Map.Entry<Integer, AccountManager>> iterator = 
				managers.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Integer, AccountManager> entry = iterator.next();
				AccountManager manager = entry.getValue();
				// 检查内存超时账号（超时没有使用的账号），从内存中删除
				manager.checkTimeoutAccount();
				
				// 找到达到触发时间的参数
				List<PressRegulate> cmds = manager.checkSwitchTime();
				array.addAll(cmds);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		// 有定时触发命令，转给BANK/GATE站点处理
		if (array.size() > 0) {
			// 转发给BANK站点处理
			BatchPressRegulate cmd = new BatchPressRegulate(array);
			AccountCommandPool.getInstance().admit(cmd);
		}
	}

	/**
	 * 顺序取账号管理器
	 * @return 返回下一个账号管理器
	 */
	private AccountManager next() {
		super.lockSingle();
		try {
			if (iterateIndex >= managers.size()) {
				iterateIndex = 0;
			}
			return managers.get(iterateIndex++);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 根据签名，找到账号管理器坐标。<br><br>
	 * 
	 * 三层定位： <br>
	 * 1. 找到账号坐标 <br>
	 * 2. 通过账号坐标中的编号找到管理器编号 <br>
	 * 3. 通过管理器编号找到管理器 <br>
	 * 
	 * @param siger 账号签名
	 * @return 返回账号管理器坐标，或者空指针
	 */
	private AccountFrame findAccountFrame(Siger siger) {
		// 锁定处理
		super.lockSingle();
		try {
			AccountDock dock = docks.get(siger); //1. 找到账号坐标
			//			Logger.debug(this, "findAccountFrame", (dock!=null), "check AccountDock %s", siger);
			if (dock != null) {
				Integer index = files.get(dock.getNo()); //2. 找到管理器编号
				//				Logger.debug(this, "findAccountFrame", (index!=null), "check Manager Serial: %d", dock.getNo());
				if (index != null) {
					AccountManager manager = managers.get(index); //3. 找到管理器
					//					Logger.debug(this, "findAccountFrame", (manager!=null), "check AccountManager: %d", index.intValue());
					if (manager != null) {
						return new AccountFrame(dock, manager);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	//	/**
	//	 * 查找关联账号的账号管理器
	//	 * @param siger 账号签名
	//	 * @return 返回账号管理器，没有返回空指针
	//	 */
	//	private AccountDock findDock(Siger siger) {
	//		super.lockSingle();
	//		try {
	//			return docks.get(siger);
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockSingle();
	//		}
	//		return null;
	//	}

	//	/**
	//	 * 查找关联账号的账号管理器
	//	 * @param siger 账号签名
	//	 * @return 返回账号管理器，没有返回空指针
	//	 */
	//	private AccountManager findManager(AccountDock dock) {
	//		super.lockSingle();
	//		try {
	//			Integer index = files.get(dock.getNo());
	//			if (index != null) {
	//				return managers.get(index);
	//			}
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockSingle();
	//		}
	//		return null;
	//	}

	//	/**
	//	 * 查找关联账号的账号管理器
	//	 * @param siger 账号签名
	//	 * @return 返回账号管理器，没有返回空指针
	//	 */
	//	private AccountManager findManager(Siger siger) {
	//		AccountDock dock = findDock(siger);
	//		if (dock != null) {
	//			return findManager(dock);
	//		}
	//		return null;
	//	}

	/**
	 * 筛选散列范围内的账号签名
	 * @param axes 节点坐标
	 * @return 返回账号签名列表
	 */
	public List<Siger> choice(SiteAxes axes) {
		ArrayList<Siger> array = new ArrayList<Siger>();

		super.lockMulti();
		try {
			Iterator<Map.Entry<Siger, AccountDock>> iterator = docks.entrySet()
			.iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, AccountDock> entry = iterator.next();
				Siger siger = entry.getKey();
				if (axes.allow(siger)) {
					array.add(siger);
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
	 * 建立一个账号
	 * @param user 用户名称
	 * @return 成功返回真，否则假
	 */
	public boolean createAccount(User user) {
		// 建立时间
		user.setCreateTime(SimpleTimestamp.currentTimeMillis());
		Account account = new Account(user);

		// 顺序取一个账号管理器
		AccountManager manager = next();
		// 保存账号到磁盘
		AccountDock dock = manager.createAccount(account);
		boolean success = (dock != null);
		if (success) {
			addDock(dock);

//			// 保存文件编号 -> 管理器序列号。这是个冗余操作，当管理器序列号没有保存在"files.put"映射中时起作用。
//			addFileNo(dock.getNo(), manager.getIndex());
		}
		return success;
	}

	/**
	 * 删除账号和全部参数
	 * @param siger 账号签名
	 * @return 返回真或者假
	 */
	public boolean dropAccount(Siger siger) {
		AccountFrame frame = findAccountFrame(siger);
		boolean success = (frame != null);
		if (success) {
			success = frame.manager.dropAccount(frame.dock);
			if (success) {
				removeDock(siger);
			}
		}
		return success;
	}

	/**
	 * 从磁盘中读取账号
	 * @param siger 账号签名
	 * @return 返回账号实例，没有是空指针
	 */
	public Account readAccount(Siger siger) {
		// 忽略空指针
		if (siger == null) {
			return null;
		}
		// 找到账号管理器坐标
		AccountFrame frame = findAccountFrame(siger);
		if (frame != null) {
			return frame.manager.readAccount(frame.dock);
		}
		return null;
	}

	/**
	 * 读取账号方位
	 * @param siger
	 * @return
	 */
	public AccountSphere readAccountSphere(Siger siger) {
		// 找到账号管理器坐标
		AccountFrame frame = findAccountFrame(siger);
		if (frame == null) {
			Logger.warning(this, "readAccountSphere", "not found '%s'", siger);
			return null;
		}

		// 读出账号
		Account account = frame.manager.readAccount(frame.dock);
		if (account != null) {
			return new AccountSphere(account, frame);
		}
		
		return null;
	}

	/**
	 * 保存优化触发器到内存
	 * @param sphere 账号图谱
	 * @param time 优化触发器
	 * @return 返回真或者假
	 */
	public boolean addSwitchTime(AccountSphere sphere, SwitchTime time) {
		Siger siger = sphere.account.getUsername().duplicate();
		return sphere.manager.addSwitchTime(siger, time);
	}

	/**
	 * 从内存中释放内存优化器
	 * @param sphere 账号图谱
	 * @param time 优化触发器
	 * @return 返回真或者假
	 */
	public boolean removeSwitchTime(AccountSphere sphere, SwitchTime time) {
		return sphere.manager.removeSwitchTime(time);
	}
	
	/**
	 * 更新账号
	 * @param sphere
	 * @return 成功返回真，否则假
	 */
	public boolean updateAccountSphere(AccountSphere sphere) {
		Account account = sphere.getAccount();
		AccountManager manager = sphere.getManager();
		AccountDock dock = sphere.getDock();
		
		// 更新记录
		AccountDock next = 	manager.updateAccount(dock, account);
		// 不一致时，修改它
		boolean success = (next != null);
		if (success) {
			if (Laxkit.compareTo(next, dock) != 0) {
				addDock(next);
			}
		}

		return success;
	}

//	/**
//	 * 对一个用户账号进行授权
//	 * @param siger 用户签名
//	 * @param permit 权限许可
//	 * @return 成功返回真，否则假
//	 */
//	public boolean grant(Siger siger, Permit permit) {
//		// 找到账号管理器坐标
//		AccountFrame frame = findAccountFrame(siger);
//		if (frame == null) {
//			return false;
//		}
//
//		// 读出账号
//		Account account = frame.manager.readAccount(frame.dock);
//		if (account == null) {
//			return false;
//		}
//		// 向账号授权
//		boolean success = account.grant(permit);
//		if (success) {
//			AccountDock next = frame.manager.updateAccount(frame.dock, account);
//			// 不一致时，修改它
//			success = (next != null);
//			if (success) {
//				if (Laxkit.compareTo(next, frame.dock) != 0) {
//					addDock(next);
//				}
//			}
//		}
//
//		return success;
//	}

//	/**
//	 * 解除一个账号的权限
//	 * @param siger 用户签名
//	 * @param permit 权限许可
//	 * @return 成功返回真，否则假
//	 */
//	public boolean revoke(Siger siger, Permit permit) {
//		// 找到账号管理器坐标
//		AccountFrame frame = findAccountFrame(siger);
//		if (frame == null) {
//			return false;
//		}
//
//		// 读出账号
//		Account account = frame.manager.readAccount(frame.dock);
//		if (account == null) {
//			return false;
//		}
//		// 解除账号中的授权
//		boolean success = account.revoke(permit);
//		if (success) {
//			AccountDock next = frame.manager.updateAccount(frame.dock, account);
//			// 不一致时，修改它
//			success = (next != null);
//			if (success) {
//				if (Laxkit.compareTo(next, frame.dock) != 0) {
//					addDock(next);
//				}
//			}
//		}
//
//		return success;
//	}

	/**
	 * 判断账号存在
	 * @param siger 账号签名
	 * @return 返回真或者假
	 */
	public boolean hasAccount(Siger siger) {
		boolean success = false;
		super.lockMulti();
		try {
			success = (docks.get(siger) != null);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 判断数据库存在
	 * 
	 * @param fame 数据库名
	 * @return 返回真或者假
	 */
	public boolean hasSchema(Fame fame) {
		super.lockMulti();
		try {
			Iterator<Map.Entry<Integer, AccountManager>> iterator = managers.entrySet().iterator();
			while (iterator.hasNext()) {
				AccountManager e = iterator.next().getValue();
				if (e.hasSchema(fame)) {
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
	 * @return Siger列表
	 */
	public List<Siger> getSigers() {
		super.lockMulti();
		try {
			return new ArrayList<Siger>(docks.keySet());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}
	
	/**
	 * 查找关联的签名
	 * 
	 * @param space 数据库名
	 * @return 返回真或者假
	 */
	public Siger findSiger(Space space) {
		super.lockMulti();
		try {
			Iterator<Map.Entry<Integer, AccountManager>> iterator = managers.entrySet().iterator();
			while (iterator.hasNext()) {
				AccountManager e = iterator.next().getValue();
				Siger siger = e.findTable(space);
				if(siger != null) {
					return siger;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 判断数据库存在
	 * 
	 * @param space 数据库名
	 * @return 返回真或者假
	 */
	public boolean hasTable(Space space) {
		return findSiger(space) != null;
	}
	
	/**
	 * 读取一个表配置
	 * @param space 表名
	 * @return 返回表实例
	 */
	public Table readTable(Space space) {
		Siger siger = findSiger(space);
		if (siger == null) {
			return null;
		}
		Account account = readAccount(siger);
		if (account != null) {
			return account.findTable(space);
		}
		return null;
	}

	/**
	 * 在指定的账号下，增加一个数据库
	 * @param siger 用户签名
	 * @param schema 数据库配置
	 * @return 返回真或者假
	 */
	public boolean createSchema(Siger siger, Schema schema) {
		Fame fame = schema.getFame();
		// 如果数据库全局存在是错误
		boolean success = hasSchema(fame);
		if (success) {
			Logger.error(this, "createSchema", "error %s", fame);
			return false;
		}

		// 找到账号管理器坐标
		AccountFrame frame = findAccountFrame(siger);
		if (frame == null) {
			Logger.error(this, "createSchema", "cannot be find dock! siger: %s", siger);
			return false;
		}

		// 从内存或者硬盘取出账号
		Account account = frame.manager.readAccount(frame.dock);
		if (account == null) {
			Logger.error(this, "createSchema", "cannot be read account!  at [%s] %s", siger, frame.dock);
			return false;
		}
		// 数据库存在
		if (account.hasSchema(fame)) {
			Logger.error(this, "createSchema", "fault %s", fame);
			return false;
		}
		
		// 修改为本地时间
		schema.setCreateTime(SimpleTimestamp.currentTimeMillis());
		success = account.createSchema(schema);
		if (!success) {
			Logger.error(this, "createSchema", "cannot be create %s", fame);
			return false;
		}

		// 更新到磁盘
		AccountDock next = frame.manager.updateAccount(frame.dock, account);
		// 判断成功，把数据库名保存到内存里
		success = (next != null);
		if (success) {
			frame.manager.addSchema(siger, fame);
			if (Laxkit.compareTo(next, frame.dock) != 0) {
				addDock(next);
			}
		}
		return success;
	}
	
	/**
	 * 在指定的账号下，删除一个数据库
	 * @param siger 用户签名
	 * @param fame 数据库名
	 * @return 返回真或者假
	 */
	public boolean dropSchema(Siger siger, Fame fame) {
		// 如果数据库不存在是错误
		boolean success = hasSchema(fame);
		if (!success) {
			Logger.error(this, "dropSchema", "cannot be find %s", fame);
			return false;
		}

		// 找到账号管理器坐标
		AccountFrame frame = findAccountFrame(siger);
		if (frame == null) {
			return false;
		}

		// 从内存或者硬盘取出账号
		Account account = frame.manager.readAccount(frame.dock);
		if (account == null) {
			Logger.error(this, "dropSchema", "cannot be read account!  at [%s] %s", siger, frame.dock);
			return false;
		}
		// 从账号中删除数据库及下属表配置
		Schema schema = account.dropSchema(fame);
		success = (schema != null);
		if (!success) {
			Logger.error(this, "dropSchema", "cannot be find '%s'", fame);
			return false;
		}

		// 更新到磁盘，返回新的位置
		AccountDock next = frame.manager.updateAccount(frame.dock, account);
		// 判断成功
		success = (next != null);
		if (success) {
			// 删除内存中的数据表
			for(Space space : schema.getSpaces()) {
				frame.manager.removeTable(space);
			}
			// 删除数据库
			frame.manager.removeSchema(fame);
			// 坐标位置不一致，更新
			if (Laxkit.compareTo(next, frame.dock) != 0) {
				addDock(next);
			}
		}
		return success;
	}
	
	/**
	 * 在指定的账号下，增加一个数据表
	 * @param siger 用户签名
	 * @param table 数据表配置
	 * @return 返回真或者假
	 */
	public boolean createTable(Siger siger, Table table) {
		Space space = table.getSpace();
		// 数据库不存在，或者表存在时...
		boolean success = (!hasSchema(space.getSchema()) || hasTable(space));
		if (success) {
			Logger.error(this, "createTable", "error %s", space);
			return false;
		}

		// 找到账号管理器坐标
		AccountFrame frame = findAccountFrame(siger);
		if (frame == null) {
			Logger.error(this, "createTable", "cannot be find dock! siger: %s", siger);
			return false;
		}

		// 从内存或者硬盘取出账号
		Account account = frame.manager.readAccount(frame.dock);
		if (account == null) {
			Logger.error(this, "createTable", "cannot be read account! at [%s] %s", siger, frame.dock);
			return false;
		}
		
		// 数据库不存在，或者表存在时...
		success = (!account.hasSchema(space.getSchema()) || account.hasTable(space));
		if (success) {
			Logger.error(this, "createTable", "fault %s", space);
			return false;
		}
		
		// 修改为本地时间
		table.setCreateTime(SimpleTimestamp.currentTimeMillis());
		success = account.createTable(table);
		if (!success) {
			Logger.error(this, "createTable", "cannot be create %s", table.getSpace());
			return false;
		}

		// 更新到磁盘
		AccountDock next = frame.manager.updateAccount(frame.dock, account);
		// 判断成功，把数据表名保存到内存里
		success = (next != null);
		if (success) {
			// 保存表名
			frame.manager.addTable(siger, table.getSpace());
			// 地址发生变化，更新内存记录
			if (Laxkit.compareTo(next, frame.dock) != 0) {
				addDock(next);
			}
		}
		return success;
	}
	
	/**
	 * 在指定的账号下，删除一个数据表
	 * @param siger 用户签名
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean dropTable(Siger siger, Space space) {
		// 如果数据表不存在是错误
		boolean success = hasTable(space);
		if (!success) {
			Logger.error(this, "dropTable", "cannot be find %s", space);
			return false;
		}

		// 找到账号管理器坐标
		AccountFrame frame = findAccountFrame(siger);
		if (frame == null) {
			return false;
		}

		// 从内存或者硬盘取出账号
		Account account = frame.manager.readAccount(frame.dock);
		if (account == null) {
			Logger.error(this, "dropTable", "cannot be read account!  at [%s] %s", siger, frame.dock);
			return false;
		}
		// 从账号中删除数据表
		Table table = account.dropTable(space);
		success = (table != null);
		if (!success) {
			Logger.error(this, "dropTable", "cannot be find '%s'", space);
			return false;
		}

		// 更新到磁盘，返回新的位置
		AccountDock next = frame.manager.updateAccount(frame.dock, account);
		// 判断成功
		success = (next != null);
		if (success) {
			// 删除内存里的数据表
			frame.manager.removeTable(space);
			// 坐标位置不一致，更新
			if (Laxkit.compareTo(next, frame.dock) != 0) {
				addDock(next);
			}
		}
		return success;
	}

}