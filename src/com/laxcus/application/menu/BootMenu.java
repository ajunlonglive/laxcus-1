/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.menu;

import javax.swing.*;

import com.laxcus.util.hash.*;

/**
 * 引导菜单
 * 
 * @author scott.liang
 * @version 1.0 7/4/2021
 * @since laxcus 1.0
 */
public class BootMenu extends JMenu {
	
	private static final long serialVersionUID = 5054163874847470600L;
	
	/** 菜单唯一值 **/
	private SHA256Hash hash;
	
	/** 关联的菜单 **/
	private String attachMenu;
	
	public void setHash(SHA256Hash e) {
		hash = e;
	}
	
	public SHA256Hash getHash(){
		return hash;
	}

	/**
	 * 设置关联菜单，允许空值
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
	 * 构造默认的引导菜单
	 */
	public BootMenu() {
		super();
	}

	/**
	 * 构造引导菜单，指定显示文本
	 * @param s
	 */
	public BootMenu(String s) {
		super(s);
	}

	/**
	 * 构造引导菜单
	 * @param action
	 */
	public BootMenu(Action action) {
		super(action);
	}

	/**
	 * 构造引导菜单
	 * @param s 文本
	 * @param b
	 */
	public BootMenu(String s, boolean b) {
		super(s, b);
	}

}