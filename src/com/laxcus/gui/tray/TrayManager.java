/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.tray;

/**
 * 托盘管理器
 * 
 * @author scott.liang
 * @version 1.0 2/25/2022
 * @since laxcus 1.0
 */
public interface TrayManager {

	/**
	 * 注册到托盘到管理器
	 * 
	 * @param e
	 * @return
	 */
	boolean register(Tray e);

	/**
	 * 从管理器删除托盘
	 * 
	 * @param e
	 * @return
	 */
	boolean unregister(Tray e);
}

//public abstract class TrayManager {
//
//	ArrayList<Tray> array = new ArrayList<Tray>();
//	
//	/** 托盘管理器实例 **/
//	public TrayManager() {
//		super();
//	}
//
//	/**
//	 * 加入一个托盘
//	 * @param e
//	 * @return
//	 */
//	public boolean add(Tray e) {
//		Laxkit.nullabled(e);
//		if (!array.contains(e)) {
//			return false;
//		}
//		return array.add(e);
//	}
//
//	/**
//	 * 移除托盘
//	 * @param e
//	 * @return
//	 */
//	public boolean remove(Tray e) {
//		Laxkit.nullabled(e);
//		return array.remove(e);
//	}
//
//	/**
//	 * 返回成员数目
//	 * @return 成员数目
//	 */
//	public int size() {
//		return array.size();
//	}
//
//	/**
//	 * 判断是空集合
//	 * @return 返回真或者假
//	 */
//	public boolean isEmpty() {
//		return array.isEmpty();
//	}
//}
