/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.status;

import java.util.*;

import com.laxcus.gui.tray.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;

/**
 * 桌面托盘管理器
 * 
 * @author scott.liang
 * @version 1.0 2/25/2022
 * @since laxcus 1.0
 */
public class DesktopTrayManager extends MutexHandler implements TrayManager {

	/** 托管数组 **/
	private ArrayList<Tray> array = new ArrayList<Tray>();
	
	/** 控制台 **/
	private TrayController controller;

	/**
	 * 构造默认的桌面托盘管理器
	 */
	public DesktopTrayManager() {
		super();
	}
	
	/**
	 * 注销图标
	 * @param e
	 */
	public void setTrayController(TrayController e) {
		controller = e;
	}

	/**
	 * 输出托盘数组
	 * @return Tray数组
	 */
	public Tray[] toArray() {
		// 锁定
		super.lockMulti();
		try {
			Tray[] a = new Tray[array.size()];
			return array.toArray(a);
		} catch (Throwable e) {

		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 加入一个托盘
	 * @param tray
	 * @return
	 */
	@Override
	public boolean register(Tray tray) {
		Laxkit.nullabled(tray);
		// 锁定
		boolean success = false;
		super.lockSingle();
		try {
			// 不存在时，保存它
			if (!array.contains(tray)) {
				success = array.add(tray);
			}
		} catch (Throwable e) {

		} finally {
			super.unlockSingle();
		}
		// 重新加载
		if (success && controller != null) {
			controller.redraw();
		}
		return success;
	}

	/**
	 * 移除托盘
	 * @param tray
	 * @return
	 */
	@Override
	public boolean unregister(Tray tray) {
		Laxkit.nullabled(tray);
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			success = array.remove(tray);
		} catch (Throwable ex) {

		} finally {
			super.unlockSingle();
		}
		
		// 重新加载
		if (success && controller != null) {
			controller.redraw();
		}
		
		return success;
	}

	/**
	 * 返回成员数目
	 * @return 成员数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}
}
