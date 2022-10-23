/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import java.util.*;

import com.laxcus.law.forbid.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;

/**
 * 拒绝操作单元管理池。<br><br>
 * 
 * 保存多个用户的拒绝操作单元，当拒绝操作发生时，所有数据读写操作都将停止，并且两个拒绝操作不能冲突。<br>
 * 任何时间，只允许一个拒绝操作执行。<br>
 * 
 * @author scott.liang
 * @version 1.0 4/1/2017
 * @since laxcus 1.0
 */

//public final class ForbidHouse extends VirtualPool {

public final class ForbidHouse extends MutexHandler {	

	/** 拒绝操作资源管理池句柄 **/
	private static ForbidHouse selfHandle = new ForbidHouse();

	/** 用户签名 -> 拒绝操作表 **/
	private Map<Siger, ForbidSheet> sheets = new TreeMap<Siger, ForbidSheet>();

	/**
	 * 构造拒绝操作资源管理池
	 */
	private ForbidHouse() {
		super();
	}

	/**
	 * 返回拒绝操作资源管理池句柄
	 * 
	 * @return
	 */
	public static ForbidHouse getInstance() {
		return ForbidHouse.selfHandle;
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.thread.VirtualThread#init()
	//	 */
	//	@Override
	//	public boolean init() {
	//		// TODO Auto-generated method stub
	//		return true;
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.thread.VirtualThread#process()
	//	 */
	//	@Override
	//	public void process() {
	//		Logger.info(this, "process", "into ...");
	//		while (!isInterrupted()) {
	//			this.delay(10000);
	//		}
	//		Logger.info(this, "process", "exit");
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.thread.VirtualThread#finish()
	//	 */
	//	@Override
	//	public void finish() {
	//		// TODO Auto-generated method stub
	//
	//	}

	/**
	 * 判断传入的事务，与当前禁止操作存在冲突
	 * @param issuer 用户签名
	 * @param rules 事务规则
	 * @return 冲突返回真，否则假
	 */
	public boolean conflict(Siger issuer, List<RuleItem> rules) {
		super.lockMulti();
		try {
			ForbidSheet sheet = sheets.get(issuer);
			if (sheet != null) {
				for (ForbidItem forbidItem : sheet.list()) {
					for (RuleItem rule : rules) {
						ForbidItem ruleItem = rule.createForbidItem();
						boolean conflict = forbidItem.conflict(ruleItem); // 判断存在冲突
						if (conflict) return true;
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		// 没有冲突
		return false;
	}

	/**
	 * 提交禁止操作单元
	 * @param issuer 用户签名
	 * @param array 禁止操作单元数组
	 * @return 提交成功且保存返回真，否则假
	 */
	public boolean submit(Siger issuer, List<ForbidItem> array) {
		boolean success = false;
		super.lockSingle();
		try {
			ForbidSheet sheet = sheets.get(issuer);
			if (sheet == null) {
				sheet = new ForbidSheet(issuer);
				sheets.put(sheet.getIssuer(), sheet);
			}
			// 检查存在冲突，如果是，返回假
			for (ForbidItem item1 : sheet.list()) {
				for (ForbidItem item2 : array) {
					if (item1.conflict(item2)) {
						return false;
					}
				}
			}
			// 保存全部
			int count = sheet.addAll(array);
			success = (count == array.size());
			if (!success) {
				sheet.removeAll(array);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 撤销禁止操作单元
	 * @param issuer 用户签名
	 * @param array 禁止操作单元数组
	 * @return 全部撤销成功返回真，否则假
	 */
	public boolean revoke(Siger issuer, List<ForbidItem> array) {
		boolean success = false;
		super.lockSingle();
		try {
			ForbidSheet sheet = sheets.get(issuer);
			if (sheet != null) {
				int count = 0;
				for (ForbidItem item : array) {
					if (sheet.contains(item)) {
						count++;
					}
				}
				success = (count == array.size());
				if (success) {
					sheet.removeAll(array);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 根据用户签名，查找已经锁定的禁止操作单元
	 * @param issuer 用户签名
	 * @return 禁止操作单元列表
	 */
	public List<ForbidItem> find(Siger issuer) {
		ArrayList<ForbidItem> array = new ArrayList<ForbidItem>();
		// 多向锁定
		super.lockMulti();
		try {
			if (issuer != null) {
				ForbidSheet sheet = sheets.get(issuer);
				if (sheet != null) {
					array.addAll(sheet.list());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		// 返回结果
		return array;
	}
}