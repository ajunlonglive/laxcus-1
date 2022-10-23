/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import java.util.*;

import com.laxcus.util.display.*;
import com.laxcus.watch.*;
import com.laxcus.watch.window.*;

/**
 * WATCH节点日志代理
 * 
 * @author scott.liang
 * @version 1.0 9/17/2019
 * @since laxcus 1.0
 */
public final class WatchLogTrustor extends DisplayLogTrustor {

	/** WATCH节点日志代理实例 **/
	private static WatchLogTrustor selfHandle = new WatchLogTrustor();

	/**
	 * 构造默认的WATCH节点日志代理
	 */
	private WatchLogTrustor() {
		super();
	}

	/**
	 * 返回WATCH节点日志代理句柄实例
	 * @return WatchLogTrustor句柄
	 */
	public static WatchLogTrustor getInstance() {
		return WatchLogTrustor.selfHandle;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.DisplayLogTrustor#push()
	 */
	@Override
	protected boolean push() {
		// 判断显示界面
		WatchWindow window = WatchLauncher.getInstance().getWindow();
		if (!window.isVisible()) {
			return false;
		}

		List<LogItem> logs = flush();
		if (logs == null || logs.isEmpty()) {
			return false;
		}

		// 日志进入显示
		WatchMixedLogPanel panel = window.getLogPanel();
		panel.pushLogs(logs);
		return true;
	}

}