/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import java.util.*;

import com.laxcus.law.limit.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;

/**
 * 限制操作资源管理池。<br><br>
 * 
 * FRONT站点发生操作故障后，把操作故障单元（FaultItem）转换成限制操作单元（LimitItem），保存在这里。<br>
 * 在用户检查和修复数据后撤销。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/25/2017
 * @since laxcus 1.0
 */
//public final class LimitHouse extends VirtualPool {
public final class LimitHouse extends MutexHandler {	

	/** 限制操作资源管理池句柄 **/
	private static LimitHouse selfHandle = new LimitHouse();

	/** 用户签名 -> 限制操作表 **/
	private Map<Siger, LimitSheet> sheets = new TreeMap<Siger, LimitSheet>();

	/**
	 * 构造限制操作资源管理池
	 */
	private LimitHouse() {
		super();
	}

	/**
	 * 返回限制操作资源管理池句柄
	 * 
	 * @return
	 */
	public static LimitHouse getInstance() {
		return LimitHouse.selfHandle;
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.thread.VirtualThread#init()
	//	 */
	//	@Override
	//	public boolean init() {
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
	//			delay(10000);
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
	//	}

	/**
	 * 提交限制操作
	 * @param issuer 用户签名
	 * @param array 限制操作集合
	 * @return 返回已经受理的限制操作集合
	 */
	public List<LimitItem> submit(Siger issuer, List<LimitItem> array) {
		ArrayList<LimitItem> results = new ArrayList<LimitItem>();
		// 单向锁定
		super.lockSingle();
		try {
			LimitSheet sheet = sheets.get(issuer);
			if (sheet == null) {
				sheet = new LimitSheet(issuer);
				sheets.put(sheet.getIssuer(), sheet);
			}
			for (LimitItem item : array) {
				boolean success = sheet.add(item);
				if (success) results.add(item);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return results;
	}

	/**
	 * 撤销限制操作
	 * @param issuer 用户签名
	 * @param array 被撤销的限制操作集合
	 * @return 返回已经撤销的限制操作集合
	 */
	public List<LimitItem> revoke(Siger issuer, List<LimitItem> array) {
		ArrayList<LimitItem> results = new ArrayList<LimitItem>();
		// 单向锁定
		super.lockSingle();
		try {
			LimitSheet sheet = sheets.get(issuer);
			if (sheet != null) {
				for (LimitItem item : array) {
					boolean success = sheet.remove(item);
					if (success) results.add(item);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return results;
	}

	/**
	 * 判断当前的限制操作与事务操作存在冲突
	 * @param issuer 用户签名
	 * @param rules 事务操作集合
	 * @return 返回真或者假
	 */
	public boolean conflict(Siger issuer, List<RuleItem> rules) {
		// 多向锁定
		super.lockMulti();
		try {
			LimitSheet sheet = sheets.get(issuer);
			if (sheet != null) {
				for (LimitItem limitItem : sheet.list()) {
					for (RuleItem ruleItem : rules) {
						// 判断限制操作与事务操作存在冲突
						boolean conflict = limitItem.conflict(ruleItem);
						// 冲突立即退出
						if (conflict) return true;
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

	/**
	 * 以外部限制操作单元为基础，将内存中的限制操作单元调整与它一致的状态。<br><br>
	 * 
	 * 三种情况：<br>
	 * <1> 匹配的保留。<br> 
	 * <2> 外部限制单元包含内部限制单元，保存外部限制单元。<br>
	 * <3> 外部限制单元不包含内部限制单元，删除内部限制单元。<br>
	 * 
	 * @param issuer 用户签名
	 * @param outside 外部传入的限制操作单元数组
	 * @param 返回被保存的对象数目
	 */
	public int revise(Siger issuer, List<LimitItem> outside) {
		super.lockSingle();
		try {
			LimitSheet sheet = sheets.get(issuer);
			// 没有，退出
			if (sheet == null) {
				return 0;
			}
			// 调整到一致状态
			TreeSet<LimitItem> array = new TreeSet<LimitItem>();
			// 以外部传入的对象为基准，与内部对象判断是包含关系，保留外部对象。
			for (LimitItem out : outside) {
				for (LimitItem in : sheet.list()) {
					// 外部传入的对象包含内部对象，保存外部对象
					if (out.embrace(in)) {
						array.add(out.duplicate());
					}
				}
			}
			// 更新。先删除再增加
			sheet.clear();
			sheet.addAll(array);

			// 返回调整后的锁定单元数目
			return sheet.size();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 出错，返回-1
		return -1;
	}

	/**
	 * 根据用户签名，查找已经锁定的限制操作单元
	 * @param issuer 用户签名
	 * @return 限制操作单元列表
	 */
	public List<LimitItem> find(Siger issuer) {
		ArrayList<LimitItem> array = new ArrayList<LimitItem>();
		// 多向锁定
		super.lockMulti();
		try {
			LimitSheet sheet = sheets.get(issuer);
			if (sheet != null) {
				array.addAll(sheet.list());
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