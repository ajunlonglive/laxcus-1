/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.hash.pool;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.site.bank.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.hash.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * HASH站点资源管理池
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public final class StaffOnHashPool extends VirtualPool {

	/** HASH资源管理池句柄 **/
	private static StaffOnHashPool selfHandle = new StaffOnHashPool();
	
	/** ACCOUNT主机地址集合 **/
	private TreeSet<Node> accounts = new TreeSet<Node>();

	/** 用户签名 -> 节点位置 **/
	private TreeMap<Siger, Node> sigers = new TreeMap<Siger, Node>();
	
	/** 新添加的节点 **/
	private ArrayList<Node> pushs = new ArrayList<Node>();
	
	/** 撤销的节点 **/
	private ArrayList<Node> drops = new ArrayList<Node>();

	/**
	 * 构造HASH资源管理池
	 */
	private StaffOnHashPool() {
		super();
		// 30秒触发一次
		setSleepTime(30);
	}

	/**
	 * 返回HASH资源管理池句柄
	 * 
	 * @return HASH资源管理池实例
	 */
	public static StaffOnHashPool getInstance() {
		return StaffOnHashPool.selfHandle;
	}
	
	/**
	 * 输出全部ACCOUNT站点地址
	 * @return 站点集
	 */
	public List<Node> getAccountSites() {
		super.lockMulti();
		try {
			return new ArrayList<Node>(accounts);
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 全部在线用户签名数目
	 * 
	 * @return 签名数目
	 */
	public int size() {
		return sigers.size();
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
		
		// 1. 向BANK站点请求分配编号
		boolean success = applySerial();
		// 2. 获得当前HASH站点数目
		if (success) {
			success = applyCount();
		}
		// 3. 请求全部ACCOUNT站点地址
		if (success) {
			success = loadAccountSites();
		}
		// 4. 向这些ACCOUNT站点请求用户签名
		if (success) {
			success = loadAllSigers();
		}

		// 4. 成功重新注册，不成功退出
		if (success) {
			getLauncher().checkin(true); // 立即重新注册
			// 推送注册成员
			pushRegisterMember();
		} else {
			getLauncher().stop(); // 要求节点停止
		}

		// 延时等待退出
		while (!isInterrupted()) {
			sleep();
			// 如果不成功，以下忽略不处理
			if (!success) {
				continue;
			}
			// 如果成功，做两个事：检查有新加入的节点；如果ACCOUNT节点数目是0，再次去BANK节点找全部的ACCOUNT站点
			// 定时检查
			check();
			// 如果没有ACCOUNT站点记录，定时检索它
			if (accounts.size() == 0) {
				loadAccountSites(); // 再次请求全部的ACCOUNT站点
			}
			// 向这些ACCOUNT站点请求关联的用户签名
			if (accounts.size() > 0 && sigers.size() == 0) {
				loadAllSigers();
			}
		}
		
		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		accounts.clear();
	}
	
	/**
	 * 推送注册成员给WATCH节点，经过HOME转发
	 */
	public void pushRegisterMember() {
		List<Siger> sigers = getSigers();
		if (sigers.size() > 0) {
			ShiftPushRegisterMember shift = new ShiftPushRegisterMember(sigers);
			getCommandPool().admit(shift);
		}
	}
	
	/**
	 * 检查被删除或者新增成员
	 */
	private void check() {
		// 删除
		int size = drops.size();
		for (int i = 0; i < size; i++) {
			Node node = popDropSite();
			if (node != null) {
				unloadSigers(node);
			}
		}

		// 新增
		size = pushs.size();
		for (int i = 0; i < size; i++) {
			Node node = popPushSite();
			if (node != null) {
				loadSigers(node);
			}
		}
	}
	
	/**
	 * 弹出增加的节点
	 * @return
	 */
	private Node popPushSite() {
		super.lockSingle();
		try {
			if (pushs.size() > 0) {
				return pushs.remove(0);
			}
		} finally {
			super.unlockSingle();
		}
		return null;
	}
	
	/**
	 * 弹出待删除的节点
	 * @return
	 */
	private Node popDropSite() {
		super.lockSingle();
		try {
			if (drops.size() > 0) {
				return drops.remove(0);
			}
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 删除一个节点和节点下的全部签名
	 * @param node 节点地址
	 */
	private void unloadSigers(Node node) {
		ArrayList<Siger> array = new ArrayList<Siger>();

		// 锁定
		super.lockSingle();
		try {
			Iterator<Map.Entry<Siger, Node>> iterator = sigers.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, Node> entry = iterator.next();
				if (entry.getValue().compareTo(node) == 0) {
					array.add(entry.getKey());
				}
			}
			// 删除签名
			for (Siger e : array) {
				sigers.remove(e);
			}
			// 删除节点
			accounts.remove(node);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		Logger.info(this, "unloadSigers", "remove %s, account size:%d", node, array.size());
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
			Logger.error(this, "applySerial", "cannot be admit!");
			return false;
		}
		// 钩子等待，直到被唤醒
		hook.await();

		// 返回处理结果
		TakeSiteSerialProduct product = hook.getProduct();
		// 判断成功
		success = (product != null && product.getNo() > -1);
		if (success) {
			HashLauncher.getInstance().setNo(product.getNo());
		}
		return success;
	}
	
	/**
	 * 获取当前HASH站点数目
	 * @return 成功返回真，否则假
	 */
	private boolean applyCount() {
		TakeBankSubSiteCount cmd = new TakeBankSubSiteCount(SiteTag.HASH_SITE);
		TakeBankSubSiteCountHook hook = new TakeBankSubSiteCountHook();
		ShiftTakeBankSubSiteCount shift = new ShiftTakeBankSubSiteCount(cmd, hook);

		// 交给命令管理池
		boolean success = getCommandPool().admit(shift);
		if (!success) {
			Logger.error(this, "applySerial", "cannot be admit!");
			return false;
		}
		// 钩子等待，直到被唤醒
		hook.await();

		TakeBankSubSiteCountProduct product = hook.getProduct();
		success = (product != null && product.getCount() > 0);
		if (success) {
			HashLauncher.getInstance().setPartners( product.getCount());
		}

		Logger.note(this, "applySerial", success, "hash site count:%d",
				HashLauncher.getInstance().getPartners());

		return success;
	}
	
	/**
	 * 保存ACCOUNT节点。只被外部调用，用来保存新增加的节点。<br>
	 * 新增加的节点，将在线程中去提取相关的账号签名，形成“签名-节点地址”的对应关系。
	 * 
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	public boolean add(Node node) {
		Laxkit.nullabled(node);
		
		// 判断节点不存在
		boolean success = false;
		super.lockSingle();
		try {
			if(!accounts.contains(node)) {
				success = accounts.add(node);
			}
		} catch(Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		// 如果节点不存在，通知线程，去加载这个节点下的关联账号签名
		if (success) {
			super.lockSingle();
			try {
				// 没有则增加
				if (!pushs.contains(node)) {
					pushs.add(node);
				}
			} finally {
				super.unlockSingle();
			}
		}
		
		Logger.debug(this, "add", success, "push %s", node);

		return success;
	}
	
	/**
	 * 删除ACCOUNT节点
	 * @param node ACCOUNT节点地址
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Node node) {
		Laxkit.nullabled(node);
		
		// 判断节点存在
		boolean success = false;
		super.lockSingle();
		try {
			if (accounts.contains(node)) {
				success = accounts.remove(node);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 如果节点存在，先保存这个节点，通过线程删除这个节点
		if (success) {
			super.lockSingle();
			try {
				// 没有则增加
				if (!drops.contains(node)) {
					drops.add(node);
				}
			} finally {
				super.unlockSingle();
			}
		}

		Logger.debug(this, "remove", success, "drop %s", node);

		return success;
	}
	
	/**
	 * 返回全部注册用户签名
	 * @return Siger集合
	 */
	public List<Siger> getSigers() {
		super.lockMulti();
		try {
			return new ArrayList<Siger>(sigers.keySet());
		} finally {
			super.unlockMulti();
		}
	}
	/**
	 * HASH站点获取全部ACCOUNT站点地址
	 * @return 成功返回真，否则假
	 */
	private boolean loadAccountSites() {
		TakeBankSubSites cmd = new TakeBankSubSites(SiteTag.ACCOUNT_SITE); // 申请ACCOUNT站点
		TakeBankSubSitesHook hook = new TakeBankSubSitesHook();
		ShiftTakeBankSubSites shift = new ShiftTakeBankSubSites(cmd, hook);

		// 交给命令管理池
		boolean success = getCommandPool().admit(shift);
		if (!success) {
			Logger.error(this, "loadAccountSites", "cannot be admit!");
			return false;
		}
		// 钩子等待，直到被唤醒
		hook.await();

		// 返回处理结果
		TakeBankSubSitesProduct product = hook.getProduct();
		success = (product != null && product.size() > 0);
		if (!success) {
			Logger.error(this, "loadAccountSites", "cannot be catch account sites!");
			return false;
		}
		// 保存全部ACCOUNT站点地址
		for (BankSubSiteItem e : product.list()) {
			// 锁定保存
			super.lockSingle();
			try {
				accounts.add(e.getSite());
			} catch (Throwable ex) {
				Logger.fatal(ex);
			} finally {
				super.unlockSingle();
			}
		}
		
		Logger.info(this, "loadAccountSites", "all account sites:%d", accounts.size());
		
		return true;
	}
	
	/**
	 * 去全部ACCOUNT站点，加载与当前HASH关联的账号签名。
	 * @return 成功返回真，否则假
	 */
	private boolean loadAllSigers() {
		for (Node node : accounts) {
			boolean success = loadSigers(node);
			if (!success) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 去ACCOUNT站点，加载与当前HASH关联的账号签名。
	 * 
	 * @param remote ACCOUNT站点
	 * @return 成功返回真，否则假
	 */
	private boolean loadSigers(Node remote) {
		SiteAxes axes = HashLauncher.getInstance().getAxes();
		TakeAccountSiger cmd = new TakeAccountSiger(axes);
		TakeAccountSigerHook hook = new TakeAccountSigerHook();
		ShiftTakeAccountSiger shift = new ShiftTakeAccountSiger(remote, cmd, hook);

		// 交给命令队列处理
		boolean success = getCommandPool().admit(shift);
		if (!success) {
			Logger.error(this, "loadSigers", "cannot be admit!");
			return false;
		}
		// 钩子等等
		hook.await();

		// 检查反馈结果
		TakeAccountSigerProduct product = hook.getProduct();
		if (product == null) {
			Logger.error(this, "loadSigers", "cannot be take sigers! from %s", remote);
			return false;
		}

		// 地址
		Node node = product.getLocal().duplicate();
		for (Siger siger : product.list()) {
			influx(siger, node); // 保存参数
		}

		Logger.info(this, "loadSigers", "from %s, siger size:%d", node, product.size());

		return true;
	}
	
	/**
	 * 保存签名和ACCOUNT的映像关系
	 * @param siger 签名
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	private boolean influx(Siger siger, Node node) {
		boolean success = false;
		super.lockSingle();
		try {
			sigers.put(siger, node);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 根据用户签名，查找账号的ACCOUNT站点
	 * @param siger 用户签名
	 * @return 返回ACCOUNT站点地址，没有是空指针
	 */
	public Node findAccountSite(Siger siger) {
		super.lockMulti();
		try {
			return sigers.get(siger);
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 判断签名存在
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean hasSiger(Siger siger) {
		boolean success = false;
		super.lockMulti();
		try {
			success = (sigers.get(siger) != null);
		} finally {
			super.unlockMulti();
		}
		return success;
	}
	
	/**
	 * 推送一个签名和它的注册地址
	 * @param siger 用户签名
	 * @param site ACCOUNT站点地址
	 * @return 成功返回真，否则假
	 */
	public boolean push(Siger siger, Node site) {
		// 判断签名在当前HASH站点范围内
		boolean success = HashLauncher.getInstance().allow(siger);
		if (!success) {
			return false;
		}
		// 单向锁定
		super.lockSingle();
		try {
			success = accounts.contains(site);
			if (success) {
				sigers.put(siger, site);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "push", success, "push %s#%s", site, siger);

		return success;
	}
	
	/**
	 * 删除一个账号签名
	 * @param siger 账号签名
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Siger siger) {
		boolean success = false;
		super.lockSingle();
		try {
			success = (sigers.remove(siger) != null);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "remove", success, "remove %s", siger);

		return success;
	}
}