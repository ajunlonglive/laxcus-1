/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.pool;

import java.util.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;

/**
 * 站点运行时容器
 * 
 * @author scott.liang
 * @version 1.0 1/9/2020
 * @since laxcus 1.0
 */
public class SiteRuntimeBasket extends MutexHandler {

	/** WATCH节点运行时容器 **/
	private static SiteRuntimeBasket selfHandle = new SiteRuntimeBasket();

	/** 节点地址 -> 运行时状态 **/
	private Map<Node, SiteRuntime> runtimes = new TreeMap<Node, SiteRuntime>();

	/**
	 * 初始化WATCH节点运行时容器
	 */
	private SiteRuntimeBasket() {
		super();
	}

	/**
	 * 返回WATCH节点运行时容器的静态句柄
	 * @return WATCH节点运行时容器实例
	 */
	public static SiteRuntimeBasket getInstance() {
		return SiteRuntimeBasket.selfHandle;
	}
	
	/**
	 * 成员数
	 * @return 整数
	 */
	public int size() {
		super.lockMulti();
		try {
			return runtimes.size();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 清除全部
	 */
	public void clear() {
		super.lockSingle();
		try {
			runtimes.clear();
		} catch (Throwable e) {
			Logger.error(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 保存节点运行时
	 * @param cmd 命令
	 * @return 成功返回真，否则假
	 */
	public boolean pushRuntime(SiteRuntime cmd) {
		Laxkit.nullabled(cmd);
		boolean success = false;

		// 锁定!
		super.lockSingle();
		try {
			runtimes.put(cmd.getNode(), cmd);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 删除节点运行时
	 * @param node 节点地址
	 * @return 返回真或者假
	 */
	public boolean dropRuntime(Node node) {
		Laxkit.nullabled(node);

		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			SiteRuntime e = runtimes.remove(node);
			success = (e != null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return success;
	}

	/**
	 * 查找节点运行时
	 * @param node 节点地址
	 * @return 返回运行时例
	 */
	public SiteRuntime findRuntime(Node node) {
		Laxkit.nullabled(node);

		// 锁定！
		super.lockMulti();
		try {
			return runtimes.get(node);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 判断运行时有效
	 * @param node 节点地址
	 * @return 返回真或者假
	 */
	public boolean hasRuntime(Node node) {
		SiteRuntime e = findRuntime(node);
		return e != null;
	}
}
