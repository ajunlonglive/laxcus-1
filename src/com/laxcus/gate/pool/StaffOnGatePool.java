/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.site.bank.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.gate.*;
import com.laxcus.law.cross.*;
import com.laxcus.law.limit.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;

/**
 * GATE站点资源管理池。
 * 
 * @author scott.liang
 * @version 1.2 5/20/2013
 * @since laxcus 1.0
 */
public final class StaffOnGatePool extends SeekOnGatePool {

	/** 管理员账号(超级用户) */
	private Administrator admin; 

	/**
	 * 设置管理员账号
	 * @param e
	 */
	private void setAdministrator(Administrator e) {
		admin = e.duplicate();
	}

	/**
	 * 返回管理员账号
	 * @return
	 */
	public Administrator getAdministrator() {
		return admin;
	}

	/**
	 * 判断是管理员账号，包括用户名和密码
	 * @param user 用户账号
	 * @return 返回真或者假
	 */
	public boolean isAdminstrator(SHAUser user) {
		return Laxkit.compareTo(admin, user) == 0;
	}

	/** GATE资源管理池句柄 **/
	private static StaffOnGatePool selfHandle = new StaffOnGatePool();

	/** HASH站点编号 -> HASH主机 **/
	private TreeMap<Integer, Node> mapHashs = new TreeMap<Integer, Node>();

	/** 用户签名 -> 用户账号 **/
	private Map<Siger, Account> mapAccounts = new TreeMap<Siger, Account>();

	/** 数据表名 -> 表实例 **/
	private Map<Space, Siger> mapSpaces = new TreeMap<Space, Siger>();

	/**
	 * 构造GATE资源管理池
	 */
	private StaffOnGatePool() {
		super();
		setSleepTime(30);
	}

	/**
	 * 返回GATE资源管理池句柄
	 * 
	 * @return StaffOnGatePool实例
	 */
	public static StaffOnGatePool getInstance() {
		return StaffOnGatePool.selfHandle;
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

		// 1. 加载系统管理员账号
		boolean success = loadAdministrator();
		// 2. 向BANK站点申请自己的编号
		if (success) {
			success = applySerial();
		}
		// 3. 获取全部HASH站点地址
		if (success) {
			success = loadHashSites();
		}

		// 成功，重新注册；否则退出
		if (success) {
			getLauncher().checkin(true); // 立即重新注册
			// 加载用户检测器
			loadMemberChecker();
		} else {
			getLauncher().stop();
		}
		
		// 定时检测失效账号，一分钟一次
		EchoTimer timer = new EchoTimer(60000);

		// 延时等待退出
		while (!isInterrupted()) {
			// 进入延时
			sleep();
			// 不成功，忽略后面的操作
			if (!success) {
				continue;
			}
			// 如果没有HSH站点，重新去BANK站点加载
			if (mapHashs.size() == 0) {
				loadHashSites();
			}
			
			// 达到超时时间，检测在线的失效账号
			if (timer.isTimeout()) {
				timer.refresh();
				checkDisabled();
			}
		}

		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapAccounts.clear();
		mapSpaces.clear();
	}
	
	/**
	 * 检查在线且失效的账号，立即删除它！
	 */
	private void checkDisabled() {
		int size = mapAccounts.size();
		if (size == 0) {
			return;
		}
		ArrayList<Siger> array = new ArrayList<Siger>(size);

		// 锁定，检查失效的账号
		super.lockSingle();
		try {
			Iterator<Map.Entry<Siger, Account>> iterator = mapAccounts.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, Account> entry = iterator.next();
				Account account = entry.getValue();
				boolean disabled = account.getUser().isDisabled();
				// 失效，保存这个账号
				if (disabled) {
					array.add(entry.getKey());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 删除失效的在线账号
		for (Siger siger : array) {
			drop(siger);
			FrontOnGatePool.getInstance().drop(siger);
		}
	}

	/**
	 * 启动用户资源检测器，定时检测节点上的用户数目
	 */
	private void loadMemberChecker() {
		GateLauncher launcher = (GateLauncher) getLauncher();
		MemberCyber cyber = launcher.getMemberCyber();
		Timer timer = getLauncher().getTimer();
		MemberChecker checker = new MemberChecker(this);
		timer.schedule(checker, 0, cyber.getTimeout());
	}

	/**
	 * 检查在线用户数，发出报告
	 */
	protected void checkMembers() {
		GateLauncher launcher = (GateLauncher) getLauncher();
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
	 * 加载系统管理员账号
	 * @return 成功返回真，否则假
	 */
	private boolean loadAdministrator() {
		TakeAdministrator cmd = new TakeAdministrator();
		TakeAdministratorHook hook = new TakeAdministratorHook();
		ShiftTakeAdministrator shift = new ShiftTakeAdministrator(cmd, hook);
		// 提交给命令池
		boolean success = getCommandPool().press(shift);
		if (!success) {
			Logger.debug(this, "loadAdministrator", "cannot press");
			return false;
		}
		// 等待反馈结果
		hook.await();

		Administrator adm = hook.getAdministrator();
		success = (adm != null);
		if (success) {
			setAdministrator(adm);
		}

		Logger.debug(this, "loadAdministrator", success, "load administrator account");

		return success;
	}

	/**
	 * 向BANK站点申请当前站点的机器编号
	 * @return 成功返回真，否则假
	 */
	private boolean applySerial() {
		TakeSiteSerial cmd = new TakeSiteSerial(getLauncher().getFamily());
		TakeSiteSerialHook hook = new TakeSiteSerialHook();
		ShiftTakeSiteSerial shift = new ShiftTakeSiteSerial(cmd, hook);

		// 交给命令管理池
		boolean success = getCommandPool().admit(shift);
		if (!success) {
			Logger.error(this, "applySerial", "cannot admit!");
			return false;
		}
		// 钩子等待，直到被唤醒
		hook.await();

		// 返回处理结果
		TakeSiteSerialProduct product = hook.getProduct();
		// 判断成功
		success = (product != null && product.getNo() > -1);
		if (success) {
			GateLauncher.getInstance().setNo(product.getNo());
		}

		return success;
	}

	/**
	 * 保存地址
	 * @param no 节点编号
	 * @param node HASH站点地址
	 * @return 成功返回真，否则假
	 */
	public boolean add(int no, Node node) {
		boolean success = false;
		super.lockSingle();
		try {
			mapHashs.put(no, node);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 删除地址
	 * @param no 节点编号
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(int no) {
		boolean success = false;
		super.lockSingle();
		try {
			Node node = mapHashs.remove(no);
			success = (node != null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * GATE站点获取全部HASH站点
	 * @return 成功返回真，否则假
	 */
	private boolean loadHashSites() {
		TakeBankSubSites cmd = new TakeBankSubSites(SiteTag.HASH_SITE); // 申请HASH站点
		TakeBankSubSitesHook hook = new TakeBankSubSitesHook();
		ShiftTakeBankSubSites shift = new ShiftTakeBankSubSites(cmd, hook);

		// 交给命令管理池
		boolean success = getCommandPool().admit(shift);
		if (!success) {
			Logger.error(this, "loadHashSites", "cannot be admit!");
			return false;
		}
		// 钩子等待，直到被唤醒
		hook.await();

		// 返回处理结果
		TakeBankSubSitesProduct product = hook.getProduct();
		success = (product != null && product.size() > 0);
		if (!success) {
			Logger.error(this, "loadHashSites", "cannot be catch hash sites!");
			return false;
		}

		// HASH站点的编号 -> 主机地址的关联
		for (BankSubSiteItem e : product.list()) {
			BankSerialSiteItem item = (BankSerialSiteItem) e;
			//			mapHashs.put(item.getNo(), item.getSite());
			add(item.getNo(), item.getSite());
		}

		Logger.info(this, "loadHashSites", "all hash sites:%d", mapHashs.size());

		return true;
	}

	/**
	 * 统计在线用户数目
	 * @return 在线用户数目
	 */
	public int size() {
		super.lockMulti();
		try {
			return mapAccounts.size();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断用户签名获得许可
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean allow(Siger siger) {
		boolean success = false;
		super.lockMulti();
		try {
			if (siger != null) {
				Account account = mapAccounts.get(siger);
				success = (account != null && account.isEnabled());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 判断用户签名和它下属数据表名获得许可
	 * @param siger 用户签名
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean allow(Siger siger, Space space) {
		// 判断账号和表关联
		boolean success = false;
		// 锁定
		super.lockMulti();
		try {
			if (siger != null && space != null) {
				Account account = mapAccounts.get(siger);
				boolean b = (account != null && account.isEnabled());
				if (b) {
					success = (account.hasTable(space) || account.hasPassiveTable(space));
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		// 返回结果
		return success;
	}

	/**
	 * 根据用户签名，定位HASH主机地址
	 * @param siger 用户签名
	 * @return 返回HASH主机地址，发生故障返回空指针
	 */
	public Node locate(Siger siger) {
		super.lockMulti();
		try {
			if (siger != null) {
				int size = mapHashs.size();
				// 根据模值，返回对应的HASH站点地址
				if (size > 0) {
					int no = siger.mod(size);
					return mapHashs.get(no);
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
	 * 删除账号，输出被授权人签名
	 * @param siger 账号签名
	 * @return 成功返回真，否则假
	 */
	private boolean __drop(Siger siger) {
		if (siger == null) {
			return false;
		}
		// 删除
		Account account = mapAccounts.remove(siger);
		boolean success = (account != null);
		// 成功，删除表
		if (success) {
			for (Space space : account.getSpaces()) {
				mapSpaces.remove(space);
			}
		}
		return success;
	}

	/**
	 * 增加一个账号
	 * @param account 账号
	 * @param conferrers 账号关联的被授权人
	 */
	private void __create(Account account) {
		// 保存账号
		mapAccounts.put(account.getUsername(), account);
		// 保存数据表
		for (Space space : account.getSpaces()) {
			mapSpaces.put(space, account.getUsername());
			//	Logger.debug(this, "create", "Table: %s", space);
		}
	}

	/**
	 * 删除账号资源
	 * @param siger 账号签名
	 * @return 删除成功返回真，否则假。
	 */
	public boolean drop(Siger siger) {
		if (siger == null) {
			return false;
		}

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

		Logger.debug(this, "drop", success, "remove %s", siger);

		return success;
	}

	/**
	 * 建立一个用户账号。
	 * 如果账号已经存在，那么它删除旧的，再建立新的。
	 * 
	 * @param account 用户账号
	 * @return 成功返回真，否则假
	 */
	public boolean create(Account account) {
		if (account == null) {
			return false;
		}

		boolean success = false;
		// 锁定处理，先删除后增加
		super.lockSingle();
		try {
			// 删除旧记录
			__drop(account.getUsername());
			// 增加新记录
			__create(account);
			success = true;
		} catch (Throwable ex) {
			Logger.fatal(ex);
		} finally {
			super.unlockSingle();
		}

		Logger.note(this, "create", success, "create %s", account.getUsername());

		return success;
	}

	/**
	 * 修改注册用户密码
	 * @param user 注册用户
	 * @return 修改成功返回真，否则假。
	 */
	public boolean alter(User user) {
		if (user == null) {
			return false;
		}

		// 锁定
		boolean success = false;
		super.lockSingle();
		try {
			Account account = mapAccounts.get(user.getUsername());
			success = (account != null);
			if (success) {
				account.getUser().setPassword(user.getPassword());
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
	 * 查找用户账号
	 * @param user 用户签名
	 * @param duplicate 生成数据副本
	 * @return 返回账号实例，或者空指针
	 */
	public Account findAccount(User user, boolean duplicate) {
		if (user == null) {
			return null;
		}

		// 锁定
		super.lockSingle();
		try {
			Account account = mapAccounts.get(user.getUsername());
			// 找到且用户账号匹配
			boolean success = (account != null && Laxkit.compareTo(account.getUser(), user) == 0);
			if (success) {
				if (duplicate) {
					return account.duplicate();
				}
				return account;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 查找用户账号
	 * @param siger 用户签名
	 * @param duplicate 生成数据副本
	 * @return 返回账号实例，或者空指针
	 */
	public Account findAccount(Siger siger, boolean duplicate) {
		if (siger == null) {
			return null;
		}

		Account account = null;
		super.lockSingle();
		try {
			account = mapAccounts.get(siger);
			if (account != null && duplicate) {
				account = account.duplicate();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return account;
	}

	/**
	 * 在本地查找用户账号
	 * @param siger 用户签名
	 * @return 返回账号实例，或者空指针
	 */
	public Account findAccount(Siger siger) {
		return findAccount(siger, false);
	}

	/**
	 * 根据注册用户名判断账号存在
	 * @param siger 用户签名
	 * @return 存在返回“真”，否则“假”。
	 */
	public boolean contains(Siger siger) {
		return find(siger) != null;
	}

	/**
	 * 根据用户（包括用户和密码），判断账号存在
	 * @param user 用户
	 * @return 存在返回真，否则假
	 */
	public boolean contains(User user) {
		return findAccount(user, false) != null;
	}

	/**
	 * 重新加载一个账号（去ACCOUNT获取）
	 * @param siger 用户签名
	 * @return 账号存在且加载成功，返回真，否则假。
	 */
	public boolean reloadAccount(Siger siger) {
		//		// 如果不存在
		//		boolean success = contains(siger);
		//		if (!success) {
		//			return false;
		//		}

		// 更新加载
		Account account = seekAccount(siger);
		boolean success = (account != null);
		if (success) {
			success = create(account);
		}
		return success;
	}

	/**
	 * 加载一个用户账号
	 * @param user 用户
	 * @return 加载成功返回真，否则假
	 */
	public boolean loadAccount(User user) {
		if (user == null) {
			return false;
		}

		Siger siger = user.getUsername();
		// 1. 账号本地存在，判断名称/密码一致
		Account account = findAccount(siger);
		if (account != null) {
			return (Laxkit.compareTo(user, account.getUser()) == 0);
		}

		// 2.通过网络找到账号
		account = seekAccount(siger);
		boolean success = (account != null);
		// 判断签名一致
		if (success) {
			success = (Laxkit.compareTo(user, account.getUser()) == 0);
			Logger.debug(this, "loadAccount", success, "COMPARE:%s / %s", user, account.getUser());
		}
		// 判断账号不是禁用状态，并且没有到期
		if (success) {
			success = account.getUser().isEnabled();

			Logger.debug(this, "loadAccount", success, "enabled is ");
		}
		
		// 成功，保存账号
		if (success) {
			success = create(account);
		}

		Logger.debug(this, "loadAccount", success, "load %s", siger);

		return success;
	}

	/**
	 * 根据用户签名和传入的锁定单元，从用户资源引用中找到关联的限制操作单元
	 * @param siger 用户签名
	 * @param input 锁定单元数组
	 * @return 返回关联的限制操作单元数组
	 */
	public List<LimitItem> dress(Siger siger, List<FaultItem> input) {
		TreeSet<LimitItem> array = new TreeSet<LimitItem>();

		super.lockSingle();
		try {
			Account account = mapAccounts.get(siger);
			if (account != null) {
				for (LimitItem limitItem : account.getLimitItems()) {
					for (FaultItem faultItem : input) {
						if (limitItem.match(faultItem)) {
							array.add(limitItem.duplicate());
						}
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 输出结果
		return new ArrayList<LimitItem>(array);
	}


	/**
	 * 向授权人账号增加一批授权单元 <br>
	 * 说明：授权人正常的FRONT注册站点（在StaffOnGatePool都是正常的注册站点）
	 * 
	 * @param authorizer 授权人
	 * @param items 授权单元列表
	 * @return 成功返回真，否则假
	 */
	public boolean addActiveItems(Siger authorizer, List<ActiveItem> items) {
		// 判断获得授权许可
		if (!allow(authorizer)) {
			Logger.error(this, "addActiveItems", "refuse '%s'", authorizer);
			return false;
		}

		boolean success = false;
		super.lockSingle();
		try {
			Account account = mapAccounts.get(authorizer);
			success = (account != null);
			if (success) {
				account.addActiveItems(items);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.note(this, "addActiveItems", success, "save to '%s'", authorizer);

		return success;
	}


	/**
	 * 把授权单元从授权人账号中移除 <br>
	 * 说明：授权人正常的FRONT注册站点（在StaffOnGatePool都是正常的注册站点）
	 * 
	 * @param authorizer 授权人
	 * @param items 授权单元列表
	 * @return 成功返回真，否则假
	 */
	public boolean removeActiveItems(Siger authorizer, List<ActiveItem> items) {
		// 判断获得授权许可
		if (!allow(authorizer)) {
			Logger.error(this, "removeActiveItems", "refuse '%s'", authorizer);
			return false;
		}

		// 锁定
		boolean success = false;
		super.lockSingle();
		try {
			Account account = mapAccounts.get(authorizer);
			success = (account != null);
			if (success) {
				account.removeActiveItems(items);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.note(this, "removeActiveItems", success, "drop from '%s'", authorizer);

		return success;
	}

	/**
	 * 判断被授权人存在
	 * @param conferrer 用户签名
	 * @return 存在返回真，否则假。
	 */
	public boolean hasConferrer(Siger conferrer) {
		if (conferrer == null) {
			return false;
		}
		// 锁定
		super.lockMulti();
		try {
			Iterator<Map.Entry<Siger, Account>> iterator = mapAccounts.entrySet().iterator();
			while (iterator.hasNext()) {
				Account account = iterator.next().getValue();
				// 判断有被授权人
				if (account.hasActiveConferrer(conferrer)) {
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
	 * 向被授权人账号中增加被授权单元 <br>
	 * 说明：被授权人是“正常的FRONT”注册站点（在StaffOnGatePool都是正常的注册站点）。<br>
	 * 
	 * @param conferrer 被授权人
	 * @param items 被授权单元集合
	 * @return 成功返回真，否则假
	 */
	public boolean addPassiveItems(Siger conferrer, List<PassiveItem> items) {
		// 判断获得授权许可
		if (!allow(conferrer)) {
			Logger.error(this, "addPassiveItems", "refuse '%s'", conferrer);
			return false;
		}

		boolean success = false;
		super.lockSingle();
		try {
			Account account = mapAccounts.get(conferrer);
			// 向被授权人账号增加被授权单元
			success = (account != null);
			if (success) {
				account.addPassiveItems(items);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.note(this, "addPassiveItems", success, "save to '%s'", conferrer);

		return success;
	}

	/**
	 * 把被授权单元从被授权人账号中移除。<br>
	 * 说明：被授权人是“正常的FRONT”注册站点（在StaffOnGatePool都是正常的注册站点）。<br>
	 * 
	 * @param conferrer 被授权人
	 * @param items 被授权单元集合
	 * @return 成功返回真，否则假
	 */
	public boolean removePassiveItems(Siger conferrer, List<PassiveItem> items) {
		// 判断获得授权许可
		if (!allow(conferrer)) {
			Logger.error(this, "removePassiveItems", "refuse '%s'", conferrer);
			return false;
		}

		boolean success = false;
		super.lockSingle();
		try {
			Account account = mapAccounts.get(conferrer);
			success = (account != null);
			// 从被授权人账号中撤销被授权单元
			if (success) {
				account.removePassiveItems(items);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.note(this, "removePassiveItems", success, "drop from '%s'", conferrer);

		return success;
	}

	/**
	 * 删除一个表。此方法由“GateAwardDropTableInvoker”调用。
	 * @param siger 用户签名
	 * @param space 数据表名
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean dropTable(Siger siger, Space space) {
		// 不允许空指针
		if (siger == null || space == null) {
			return false;
		}

		boolean success = false;
		super.lockSingle();
		try {
			Account account = mapAccounts.get(siger);
			success = (account != null && account.hasTable(space));
			// 删除用户资源引用中和表配置数据
			if (success) {
				account.dropTable(space);
				mapSpaces.remove(space);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "dropTable", success, "drop '%s'", space);

		return success;
	}

	/**
	 * 查询表配置
	 * @param siger 用户签名
	 * @param space 数据表名
	 * @return 表实例
	 */
	public Table findTable(Siger siger, Space space) {
		// 判断获得授权许可
		boolean success = allow(siger, space);
		if (!success) {
			Logger.error(this, "findTable", "refuse '%s#%s'", siger, space);
			return null;
		}

		// 从内存里查找表配置
		Table table = null;
		super.lockMulti();
		try {
			Account account = mapAccounts.get(siger);
			if (account != null) {
				table = account.findTable(space);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		// 返回表实例
		if (table != null) {
			return table;
		}

		// 去TOP站点查询表配置
		table = searchTable(siger, space);
		// 表有效
		success = (table != null);
		// 锁定保存
		if (success) {
			super.lockSingle();
			try {
				mapSpaces.put(table.getSpace(), table.getIssuer());
			} catch (Throwable e) {
				Logger.fatal(e);
			} finally {
				super.unlockSingle();
			}
		}

		Logger.debug(this, "findTable", success, "find \"%s\"", space);
		return (success ? table : null);
	}

	/**
	 * 向ACCOUNT站点查表
	 * @param siger 用户签名
	 * @param space 数据表名
	 * @return 返回表实例或者空指针
	 */
	public Table searchTable(Siger siger, Space space) {
		// 不允许空指针
		if (siger == null || space == null) {
			return null;
		}

		TakeTable cmd = new TakeTable(space);
		cmd.setIssuer(siger); // 用户签名

		TakeTableHook hook = new TakeTableHook();
		ShiftTakeTable shift = new ShiftTakeTable(cmd, hook);

		boolean success = GateCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "searchTable", "cannot submit to hub");
			return null;
		}
		// 等待反馈结果
		hook.await();
		// 返回表
		return hook.getTable();
	}

	/**
	 * 根据用户签名，查找关联的限制操作单元
	 * @param siger 用户签名
	 * @return 限制操作单元数组
	 */
	public List<LimitItem> findLimit(Siger siger) {
		ArrayList<LimitItem> array = new ArrayList<LimitItem>();

		super.lockMulti();
		try {
			Account account = mapAccounts.get(siger);
			if (account != null) {
				array.addAll(account.getLimitItems());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 输出当前全部用户数字签名
	 * @return 用户签名列表
	 */
	public List<Siger> getSigers() {
		ArrayList<Siger> a = new ArrayList<Siger>();
		// 锁定，取全部账号签名
		super.lockMulti();
		try {
			a.addAll(mapAccounts.keySet());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return a;
	}

	/**
	 * 查找一个账号的最大任务数目
	 * @param siger 用户签名
	 * @return 整形值
	 */
	public int findJobs(Siger siger) {
		User user = find(siger);
		return (user == null ? -1 : user.getJobs());
	}

	/**
	 * 找账号配置
	 * @param siger 用户签名
	 * @return 返回用户
	 */
	public User find(Siger siger) {
		// 锁定
		super.lockMulti();
		try {
			if (siger != null) {
				Account account = mapAccounts.get(siger);
				if (account != null) {
					return account.getUser();
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

}