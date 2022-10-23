/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.launch.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;

/**
 * 分布资源管理器。<br>
 * 
 * 这此基础上，实现代理/检索的操作。
 * 
 * 这个类是给xxxTask去调用。
 * 关键要旨：不要调用xxx.getInstance(), 真接使用句柄，去操作指定的方法，方法不操作xxx.getInstance()。而xxx.getInstance()方法里有安全检查。
 * 
 * @author scott.liang
 * @version 1.0 5/2/2018
 * @since laxcus 1.0
 */
public class TrackManager extends MutexHandler {

	/**
	 * 进行安全许可检查
	 * @param method 被调用的命令方法名
	 */
	protected static void check(String method) {
		// 安全检查
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String name = String.format("using.%s", method);
			sm.checkPermission(new TrackManagerPermission(name));
		}
	}

	/** 命令转义管理池 **/
	protected SwitchPool switchPool;
	
	/** 站点启动器 **/
	protected SiteLauncher siteLauncher;
	
	/** 异步调用器管理池 **/
	protected InvokerPool invokerPool;

	/**
	 * 构造默认的分布资源管理器
	 */
	protected TrackManager() {
		super();
	}

	/**
	 * 设置命令转义管理池。不允许空指针
	 * @param e 命令转义管理池实例
	 */
	public void setSwitchPool(SwitchPool e) {
		Laxkit.nullabled(e);
		switchPool = e;
	}

	/**
	 * 返回命令转义管理池
	 * @return
	 */
	protected SwitchPool getSwitchPool() {
		return switchPool;
	}
	
	/**
	 * 返回本地的调用器管理池
	 * @return 调用器管理池
	 */
	protected InvokerPool getInvokerPool() {
		return invokerPool;
	}
	
	/**
	 * 设置站点启动器。不允许空指针
	 * @param e 站点启动器实例
	 */
	public void setSiteLauncher(SiteLauncher e) {
		Laxkit.nullabled(e);
		siteLauncher = e;
		invokerPool = siteLauncher.getInvokerPool();
	}
	
	/**
	 * 返回管理站点地址
	 * @param duplicate 复制模式
	 * @return 返回节点地址实例
	 */
	public Node getHub(boolean duplicate) {
		if (duplicate) {
			return siteLauncher.getHub().duplicate();
		} else {
			return siteLauncher.getHub();
		}
	}

	/**
	 * 返回当前站点地址
	 * @param duplicate 复制模式
	 * @return 返回节点地址实例
	 */
	public Node getLocal(boolean duplicate) {
		if (duplicate) {
			return siteLauncher.getListener().duplicate();
		} else {
			return siteLauncher.getListener();
		}
	}

	/**
	 * 去管理节点查找数据表
	 * @param space 数据表名
	 * @return 返回表实例，或者空指针
	 */
	protected Table findHubTable(long invokerId, Space space) throws TaskException {
		TakeTable cmd = new TakeTable(space);
		TakeTableHook hook = new TakeTableHook();
		ShiftTakeTable shift = new ShiftTakeTable(cmd, hook);
		// 如果调用器编号有效，设置它
		if (InvokerIdentity.isValid(invokerId)) {
			shift.setRelateId(invokerId); // 设置调用器关联编号
		}

		// 关给调用器处理
		boolean success = switchPool.press(shift);
		if (!success) {
			throw new TaskException("cannot submit to hub");
		}
		// 进入等待状态
		hook.await();

		// 返回表实例
		return hook.getTable();
	}

}