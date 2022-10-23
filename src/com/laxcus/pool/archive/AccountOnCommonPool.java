/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.pool.archive;

import java.util.*;

import com.laxcus.command.account.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * ACCOUNT站点记录。<br>
 * 
 * 这个管理池被HOME、CALL、DATA、WORK、BUILD共同使用。
 * 
 * 这个管理池中的站点地址由ACCOUNT站点主动推送过来。本地调用器是xxxPushArchiveSiteInvoker。
 * 
 * @author scott.liang
 * @version 1.1 8/1/2013
 * @since laxcus 1.0
 */
public class AccountOnCommonPool extends SitePool {

	/** 静态句柄 **/
	private static AccountOnCommonPool selfHandle = new AccountOnCommonPool();

	/** 用户签名 -> ACCOUNT站点集合 **/
	private Map<Siger, NodeSet> mapUsers = new TreeMap<Siger, NodeSet>();

	/** 站点地址 -> ACCOUNT站点 **/
	private Map<Node, MeetTable> mapTables = new TreeMap<Node, MeetTable>();
	
//	/** 系统组件签名 **/
//	private TreeSet<Node> systems = new TreeSet<Node>();

	/**
	 * 构造ACCOUNT站点管理器
	 */
	private AccountOnCommonPool() {
		super(SiteTag.ACCOUNT_SITE);
	}

	/**
	 * 返回ACCOUNT站点管理池句柄
	 * @return
	 */
	public static AccountOnCommonPool getInstance() {
		return AccountOnCommonPool.selfHandle;
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

		// 进行延时
		while (!isInterrupted()) {
			sleep();
		}

		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapUsers.clear();
		mapTables.clear();
	}

	/**
	 * 返回全部ACCOUNT站点地址
	 * @return 节点列表
	 */
	public List<Node> getHubs() {
		super.lockMulti();
		try {
			return new ArrayList<Node>(mapTables.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 返回全部用户签名
	 * @return
	 */
	public List<Siger> getSigers() {
		super.lockMulti();
		try {
			return new ArrayList<Siger>(mapUsers.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断是空集
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		super.lockMulti();
		try {
			return mapUsers.isEmpty() && mapTables.isEmpty();
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 判断账号有关联的ACCOUNT站点
	 * @param issuer 用户签名
	 * @return 返回真或者假
	 */
	public boolean contains(Siger issuer) {
		boolean success = false;
		super.lockMulti();
		try {
			if (issuer != null) {
				NodeSet set = mapUsers.get(issuer);
				success = (set != null && set.size() > 0);
			}
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 判断一个账号和它对应的ACCOUNT站点地址存在
	 * @param issuer 用户签名
	 * @param node ACCOUNT站点地址
	 * @return 存在返回真，否则假
	 */
	public boolean contains(Siger issuer, Node node) {
		boolean success = false;
		super.lockMulti();
		try {
			NodeSet set = mapUsers.get(issuer);
			success = (set != null);
			if (success) {
				success = set.contains(node);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 保存用户签名，和它关联的ACCOUNT站点地址
	 * @param issuer 用户签名
	 * @param node ACCOUNT站点地址
	 * 
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Siger issuer, Node node) {
		// 锁定!
		super.lockSingle();
		try {
			// 用户签名
			NodeSet set = mapUsers.get(issuer);
			if (set == null) {
				set = new NodeSet();
				mapUsers.put(issuer, set);
			}
			set.add(node);

			// ACCOUNT站点
			MeetTable table = mapTables.get(node);
			if (table == null) {
				table = new MeetTable(node);
				mapTables.put(table.getNode(), table);
			}
			table.add(issuer);
			return true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return false;
	}

	/**
	 * 删除一个站点和它下面的用户签名
	 * @param node ACCOUNT站点地址
	 * @return 返回被删除的用户签名列表
	 */
	public List<Siger> remove(Node node) {		
		ArrayList<Siger> array = new ArrayList<Siger>();
		// 锁定删除
		super.lockSingle();
		try {
			MeetTable table = mapTables.remove(node);
			if (table != null) {
				// 删除这个账号下的站点
				for (Siger issuer : table.list()) {
					NodeSet set = mapUsers.get(issuer);
					set.remove(node);
					// 空集，删除这个签名
					if (set.isEmpty()) {
						mapUsers.remove(issuer);
					}
					// 保存被删除的账号
					array.add(issuer);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return array;
	}

	/**
	 * 删除一个用户，和它关联的ACCOUNT站点地址
	 * @param issuer 用户签名
	 * @return 返回被删除的ACCOUNT站点地址
	 */
	public List<Node> remove(Siger issuer) {
		ArrayList<Node> array = new ArrayList<Node>();
		super.lockSingle();
		try {
			if (issuer != null) {
				NodeSet set = mapUsers.remove(issuer);
				if (set != null) {
					for (Node node : set.list()) {
						MeetTable table = mapTables.get(node);
						// 删除这个用户签名
						table.remove(issuer);
						// 空集合，删除表
						if (table.isEmpty()) {
							mapTables.remove(node);
						}
						// 保存被删除的ACCOUNT站点地址
						array.add(node);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return array;
	}

	/**
	 * 根据用户签名，查找关联的ACCOUNT站点
	 * @param issuer 用户签名
	 * @return ACCOUNT站点列表
	 */
	public List<Node> findSites(Siger issuer) {
		super.lockMulti();
		try {
			NodeSet set = mapUsers.get(issuer);
			if (set != null) {
				return set.list();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 根据ACCOUNT站点地址，查找全部用户签名
	 * @param node ACCOUNT站点
	 * @return 用户签名列表
	 */
	public List<Siger> findIssuers(Node node) {
		super.lockMulti();
		try {
			MeetTable table = mapTables.get(node);
			if (table != null) {
				return table.list();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 根据用户签名，获得关联的ACCOUNT站点地址
	 * @param issuer 用户签名
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean load(Siger issuer) {
		ArrayList<Siger> a = new ArrayList<Siger>();
		a.add(issuer);
		return load(a);
	}

	/**
	 * 根据用户签名，获得关联的ACCOUNT站点地址
	 * @param issuers 用户签名列表
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean load(List<Siger> issuers) {
		TakeSigerSite cmd = new TakeSigerSite(issuers);
		// 快速处理
		cmd.setFast(true);
		// 命令钩子
		TakeSigerSiteHook hook = new TakeSigerSiteHook();
		ShiftTakeSigerSite shift = new ShiftTakeSigerSite(cmd, hook);

		// 立即启动查询
		CommandPool pool = getLauncher().getCommandPool();
		boolean success = pool.press(shift);
		if (!success) {
			Logger.error(this, "load", "cannot be submit to hub");
			return false;
		}
		hook.await();

		// 判断成功
		TakeSigerSiteProduct product = hook.getProduct();
		success = (product != null);

		// 保存参数
		if (success) {
			List<Siger> all = product.getUsers();
			for (Siger issuer : all) {
				List<Node> sites = product.findSites(issuer);
				// 保存账号签名和地址
				for (Node node : sites) {
					add(issuer, node);
				}
			}
		}
		
		Logger.debug(this, "load", success, "siger size: %d", (success ? product.size() : -1));

		return success;
	}
}