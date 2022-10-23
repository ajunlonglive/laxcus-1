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
import com.laxcus.command.access.table.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 被授权账号资源管理池（被授权用户账号资源管理池）
 * 
 * 被授权账号登录到授权人的GATE站点，与授权人一起，共同使用授权人的数据资源。
 * 
 * @author scott.liang
 * @version 1.0 7/18/2017
 * @since laxcus 1.0
 */
public class ConferrerStaffOnGatePool extends SeekOnGatePool {

	/** 被授权账号资源管理池句柄 **/
	private static ConferrerStaffOnGatePool selfHandle = new ConferrerStaffOnGatePool();

	/** 被授权人用户签名 -> 用户资源引用 **/
	private Map<Siger, Refer> mapRefers = new TreeMap<Siger, Refer>();

	/** 数据表名 -> 授权人表实例 **/
	private Map<Space, Table> mapTables = new TreeMap<Space, Table>();

	/**
	 * 构造被授权账号资源管理池
	 */
	private ConferrerStaffOnGatePool() {
		super();
	}

	/**
	 * 返回被授权账号资源管理池句柄
	 * 
	 * @return
	 */
	public static ConferrerStaffOnGatePool getInstance() {
		return ConferrerStaffOnGatePool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.debug(this, "process", "inot...");
		while (!isInterrupted()) {
			delay(10000);
		}
		Logger.debug(this, "process", "end...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 释放
		mapRefers.clear();
		mapTables.clear();
	}

	/**
	 * 判断被授权人签名获得许可
	 * @param conferrer 被授权人签名
	 * @return 允许返回真，否则假。
	 */
	public boolean allow(Siger conferrer) {
		super.lockMulti();
		try {
			if (conferrer != null) {
				return mapRefers.get(conferrer) != null;
			}
		} finally {
			super.unlockMulti();
		}
		return false;
	}

	/**
	 * 判断被授权人签名和授权表名获得许可。即被授权人存在，且有匹配的授权数据表。
	 * @param conferrer 被授权人签名
	 * @param space 数据表名
	 * @return 允许返回真，否则假
	 */
	public boolean allow(Siger conferrer, Space space) {
		boolean success = false;
		// 锁定
		super.lockMulti();
		try {
			if (conferrer != null && space != null) {
				Refer refer = mapRefers.get(conferrer);
				if (refer != null) {
					success = refer.hasPassiveTable(space);
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
	 * 删除账号资源
	 * @param conferrer 被授权人签名
	 * @return 删除成功返回真，否则假。
	 */
	private boolean __drop(Siger conferrer) {
		if (conferrer == null) {
			return false;
		}
		
		Refer refer = mapRefers.remove(conferrer);
		boolean success = (refer != null);
		if (success) {
			// 删除表
			for (Space space : refer.getTables()) {
				mapTables.remove(space);
			}
			for (Space space : refer.getPassiveTables()) {
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
		if (refer != null) {
			mapRefers.put(refer.getUsername(), refer);
		}
	}

	/**
	 * 删除账号资源
	 * @param conferrer 被授权人签名
	 * @return 删除成功返回真，否则假。
	 */
	public boolean drop(Siger conferrer) {
		if (conferrer == null) {
			return false;
		}

		boolean success = false;
		// 锁定处理
		super.lockSingle();
		try {
			success = __drop(conferrer);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "drop", success, "drop conferrer \'%s\'", conferrer);

		return success;
	}

	/**
	 * 建立用户资源引用
	 * @param refer 用户资源引用
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean create(Refer refer) {
		if (refer == null) {
			return false;
		}

		boolean success = false;
		// 锁定！先删除旧记录，再保存新记录
		super.lockSingle();
		try {
			__drop(refer.getUsername());
			 __create(refer);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.note(this, "create", success, "create conferrer \'%s\'",
				refer.getUsername());

		return success;
	}

	/**
	 * 查找被授权人用户账号
	 * @param conferrer 被授权人签名
	 * @return 被授权人账号
	 */
	public User find(Siger conferrer) {
		super.lockMulti();
		try {
			if (conferrer != null) {
				Refer refer = mapRefers.get(conferrer);
				if (refer != null) {
					return refer.getUser().duplicate();
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
	 * 查找被授权人的资源引用
	 * @param conferrer 被授权人签名
	 * @return 返回资源引用，没有返回空指针
	 */
	public Refer findRefer(Siger conferrer) {
		super.lockMulti();
		try {
			if (conferrer != null) {
				return mapRefers.get(conferrer);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 根据注册用户名判断账号存在
	 * @param conferrer 被授权人签名
	 * @return 返回真或者假
	 */
	public boolean contains(Siger conferrer) {
		return findRefer(conferrer) != null;
	}

	/**
	 * 向被授权人的账号中增加被授权单元 <br>
	 * 说明：被授权人是“SLAVE FRONT注册站点（从FRONT）”。在ConferrerStaffOnGatePool都是从注册站点<br>
	 * 
	 * @param conferrer 被授权人签名
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
		// 锁定
		super.lockSingle();
		try {
			Refer refer = mapRefers.get(conferrer);
			// 判断账号有效
			success = (refer != null);
			// 保存被授权单元
			if (success) {
				refer.addPassiveItems(items);
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
	 * 把被授权单元从被授权人账号中移除 <br>
	 * 说明：被授权人是“SLAVE FRONT注册站点（从FRONT）”。在ConferrerStaffOnGatePool都是从注册站点<br>
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
		// 锁定
		super.lockSingle();
		try {
			Refer refer = mapRefers.get(conferrer);
			// 判断账号有效
			success = (refer != null);
			// 删除被授权单元
			if (success) {
				refer.removePassiveItems(items);
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
	 * 查找数据表
	 * @param conferrer 被授权用户签名
	 * @param space 数据表名
	 * @return 返回数据表，没有返回空指针
	 */
	public Table findTable(Siger conferrer, Space space) {
		// 如果不允许，返回空指针
		boolean success = allow(conferrer, space);
		if (!success) {
			return null;
		}

		// 从内存里查找表配置
		Table table = null;
		super.lockMulti();
		try {
			table = mapTables.get(space);
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
		table = searchTable(conferrer, space);
		// 表有效
		success = (table != null);
		if (success) {
			super.lockSingle();
			try {
				mapTables.put(table.getSpace(), table);
			} finally {
				super.unlockSingle();
			}
		}

		Logger.debug(this, "findTable", success, "find \"%s\"", space);
		return (success ? table : null);
	}

	/**
	 * 向TOP站点查表
	 * @param conferrer 被授权用户签名
	 * @param space 数据表名
	 * @return 返回表实例或者空指针
	 */
	private Table searchTable(Siger conferrer, Space space) {
		TakeTable cmd = new TakeTable(space);
		cmd.setIssuer(conferrer); // 用户签名

		TakeTableHook hook = new TakeTableHook();
		ShiftTakeTable shift = new ShiftTakeTable(cmd, hook);

		// 提交给命令管理池处理
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
	 * 加载被授权人的资源引用。加载前，先验证授权人账号中存在被授权人。
	 * 
	 * @param authorizer 授权人签名
	 * @param conferrer 被授权人签名
	 * @return 成功返回真，否则假
	 */
	public boolean loadRefer(Siger authorizer, Siger conferrer) {
		// 被授权人注册到授权人站点，先找到授权人账号
		Account account = StaffOnGatePool.getInstance().findAccount(authorizer);
		// 本地没有，去网络上找
		if (account == null) {
			// 查找授权人账号
			account = seekAccount(authorizer);
		}

//		Logger.debug(this, "loadRefer", "授权人账号：%s", (account != null ? "有效！": "无效！"));

		// 判断授权人账号中包含授权人
		boolean success = (account != null && account.hasActiveConferrer(conferrer));

//		Logger.debug(this, "loadRefer", success, "被授权人账号：%s", conferrer);

		// 不成立，返回假
		if (!success) {
			Logger.error(this, "loadRefer", "access denied! %s", conferrer);
			return false;
		}

		// 如果没有授权人账号，在资源管理池建立这个账号
		if (!StaffOnGatePool.getInstance().contains(authorizer)) {
			success = StaffOnGatePool.getInstance().create(account);
			if (!success) {
				Logger.error(this, "loadRefer", "cannot be create account:%s", authorizer);
				return false;
			}
		}

		// 证明有账号后，去ACCOUNT站点下载被授权人的资源引用
		Refer refer = findRefer(conferrer);
		success = (refer != null);
		// 如果本地不存在，通过网络查找资源引用
		if (!success) {
			refer = seekConferrer(conferrer);
			success = (refer != null);
			// 保存这个资源引用
			if (success) {
				success = create(refer);
			}
		}

		Logger.debug(this, "loadRefer", success, "authorizer:%s, conferrer:%s", 
				authorizer, conferrer);

		return success;
	}

	/**
	 * 重新加载被授权账号的资源引用
	 * @param conferrer 被授权签名
	 * @return 成功返回真，否则假
	 */
	public boolean reloadRefer(Siger conferrer) {
		Refer refer = seekConferrer(conferrer);
		boolean success = (refer != null);
		if (success) {
			success = create(refer);
		}

		Logger.debug(this, "loadRefer", success, "conferrer:%s", conferrer);
		return success;
	}


}