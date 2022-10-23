/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.pool;

import com.laxcus.echo.invoke.*;
import com.laxcus.launch.*;
import com.laxcus.site.*;
import com.laxcus.thread.*;

/**
 * 虚拟池。<br><br>
 * 
 * 虚拟池是所有管理池的基类，在站点启动器的框架下运行，继承自互斥锁线程，以线程方式在内存中驻留，为运行在内存中的动态数据提供托管服务。
 * 每个站点都会有数目不等的虚拟池。
 * 
 * @author scott.liang 
 * @version 1.0 01/10/2009
 * @since laxcus 1.0
 */
public abstract class VirtualPool extends MutexThread {

	/**
	 * 进行安全许可检查
	 * @param method 被调用的命令方法名
	 */
	protected static void check(String method) {
		// 安全检查
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String name = String.format("using.%s", method);
			sm.checkPermission(new VirtualPoolPermission(name));
		}
	}

	/** 站点启动器 **/
	private static SiteLauncher launcher;

	/**
	 * 设置站点启动器。每个站点在启动时都要调用这个方法。
	 * @param e 站点启动器句柄
	 */
	public static void setLauncher(SiteLauncher e) {
		// 安全检查
		VirtualPool.check("setLauncher");
		// 设置句柄
		VirtualPool.launcher = e;
	}

	/**
	 * 返回站点启动器句柄。
	 * @return SiteLauncher实例
	 */
	public static SiteLauncher getLauncher() {
		// 安全检查
		VirtualPool.check("getLauncher");
		// 输出句柄
		return VirtualPool.launcher;
	}

	/**
	 * 判断处于在线（登录状态）
	 * @return 返回真或者假
	 */
	protected boolean isLogined() {
		return VirtualPool.launcher.isLogined();
	}
	
	/**
	 * 判断许可证超时
	 * @return 返回真或者假
	 */
	public boolean isLicenceTimeout() {
		return getLauncher().isLicenceTimeout();
	}

	/**
	 * 判断是LINUX操作系统
	 * @return 真或者假
	 */
	public boolean isLinux() {
		return getLauncher().isLinux();
	}

	/**
	 * 判断是WINDOWS操作系统
	 * @return 真或者假
	 */
	public boolean isWindows() {
		return getLauncher().isWindows();
	}

	/**
	 * 返回命令管理池
	 * @return 命令管理池实例
	 */
	protected CommandPool getCommandPool() {
		return getLauncher().getCommandPool();
	}

	/**
	 * 返回调用器管理池
	 * @return 调用器管理池实例
	 */
	protected InvokerPool getInvokerPool() {
		return getLauncher().getInvokerPool();
	}

	/**
	 * 构造一个默认的管理池
	 */
	protected VirtualPool() {
		super();
		// 安全检查
		VirtualPool.check("Init");
	}

	/**
	 * 返回注册的管理站点地址
	 * @param duplicate 复本或者否
	 * @return 返回注册的管理站点地址
	 */
	public Node getHub(boolean duplicate) {
		if (duplicate) {
			return VirtualPool.launcher.getHub().duplicate();
		} else {
			return VirtualPool.launcher.getHub();
		}
	}

	/**
	 * 返回注册站点的管理站点地址
	 * @return 返回注册的管理站点地址
	 */
	public Node getHub() {
		return getHub(false);
	}

	/**
	 * 返回本地节点地址
	 * @param duplicate 复制或者否
	 * @return 返回本地节点地址
	 */
	public Node getLocal(boolean duplicate) {
		if (duplicate) {
			return VirtualPool.launcher.getListener().duplicate();
		} else {
			return VirtualPool.launcher.getListener();
		}
	}

	/**
	 * 返回本地节点地址
	 * @return 返回本地节点地址
	 */
	public Node getLocal() {
		return getLocal(false);
	}
}