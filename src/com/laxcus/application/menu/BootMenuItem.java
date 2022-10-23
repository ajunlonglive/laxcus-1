/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.menu;

import javax.swing.*;

import com.laxcus.application.manage.*;

/**
 * 引导菜单项
 * 
 * @author scott.liang
 * @version 1.0 7/4/2021
 * @since laxcus 1.0
 */
public class BootMenuItem extends JMenuItem {

	private static final long serialVersionUID = 4739502617699137759L;
	
	/** 引导KEY **/
	private WKey key;

	/** 关联的菜单 **/
	private String attachMenu;

	/**
	 * 设置关联菜单
	 * @param name
	 */
	public void setAttachMenu(String name) {
		attachMenu = name;
	}

	/**
	 * 返回关联菜单
	 * @return 菜单
	 */
	public String getAttachMenu() {
		return attachMenu;
	}

	/**
	 * 构造默认的引导菜单项
	 */
	public BootMenuItem() {
		super();
	}

	/**
	 * 构造引导菜单项，指定图标
	 * @param icon 图标
	 */
	public BootMenuItem(Icon icon) {
		super(icon);
	}

	/**
	 * 构造引导菜单项，指定显示文本
	 * @param text 显示文本
	 */
	public BootMenuItem(String text) {
		super(text);
	}

	/**
	 * 构造引导菜单项
	 * @param action
	 */
	public BootMenuItem(Action action) {
		super(action);
	}

	/**
	 * 构造引导菜单项，指定显示文本和图标
	 * @param text
	 * @param icon
	 */
	public BootMenuItem(String text, Icon icon) {
		super(text, icon);
	}

	/**
	 * 构造引导菜单项，指定文本和快捷键
	 * @param text 文本
	 * @param mnemonic 快捷键
	 */
	public BootMenuItem(String text, int mnemonic) {
		super(text, mnemonic);
	}

//	/**
//	 * 设置引导单元
//	 * @param e
//	 */
//	public void setBootItem(BootItem e) {
//		bootItem = e;
//	}
//	
//	/**
//	 * 返回引导单元
//	 * @return
//	 */
//	public BootItem getBootItem(){
//		return bootItem;
//	}
//	
	
	public void setKey(WKey e){
		key = e;
	}
	
	public WKey getKey() {
		return key;
	}
}