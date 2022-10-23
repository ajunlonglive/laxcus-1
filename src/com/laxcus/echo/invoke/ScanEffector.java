/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.util.*;

import com.laxcus.launch.*;

/**
 * 命令/调用器扫描器
 * @author scott.liang
 * @version 1.0 1/15/2019
 * @since laxcus 1.0
 */
public class ScanEffector {

	/** 站点句柄 **/
	private static SiteLauncher launcher;

	/**
	 * 设置站点句柄
	 * @param e SiteLauncher实例
	 */
	public static void setSiteLauncher(SiteLauncher e) {
		ScanEffector.launcher = e;
	}

	/**
	 * 返回站点句柄
	 * @return SiteLauncher实例
	 */
	public static SiteLauncher getSiteLauncher() {
		return ScanEffector.launcher;
	}

	/** 静态句柄 **/
	private static CommandStackTask stackTask;

	/**
	 * 释放旧实例
	 */
	public static void release() {
		// 实例存在，先撤销它
		if (ScanEffector.stackTask != null) {
			ScanEffector.stackTask.cancel();
			Timer timer = ScanEffector.launcher.getTimer();
			timer.purge();
			ScanEffector.stackTask = null;
		}
	}

	/**
	 * 启动参数
	 * @param interval 调用间隔
	 * @return 成功返回真，否则假
	 */
	public static boolean load(long interval) {
		// 最小是10秒间隔
		boolean success = (interval >= 10000);
		if (success) {
			Timer timer = ScanEffector.launcher.getTimer();
			ScanEffector.stackTask = new CommandStackTask(ScanEffector.launcher);
			timer.schedule(ScanEffector.stackTask, 0, interval);
		}
		return success;
	}

	/**
	 * 执行实例
	 * @param start 启动
	 * @param interval 调用间隔
	 * @return 成功返回真，否则假
	 */
	public static boolean reload(boolean start, long interval) {
		// 释放
		ScanEffector.release();
		// 启动线程
		if (start) {
			return ScanEffector.load(interval);
		}
		return true;
	}

}