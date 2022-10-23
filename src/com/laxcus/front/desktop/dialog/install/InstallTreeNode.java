/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.install;

import javax.swing.*;
import javax.swing.tree.*;

import com.laxcus.application.manage.*;
import com.laxcus.util.*;

/**
 * 安装时的树形节点
 * 
 * @author scott.liang
 * @version 1.0 7/18/2021
 * @since laxcus 1.0
 */
class InstallTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 5730832459787425837L;

//	/** 引导单元 **/
//	private BootItem item;
	
	private WElement item;
	
	/** 图标实例 **/
	private Icon icon;

	/**
	 * 构造安装时的树形节点，指定引导单元
	 * @param item
	 */
	public InstallTreeNode(WElement item, Icon icon) {
		super();
		setItem(item);
		setIcon(icon);
	}

	/**
	 * 设置引导单元
	 * @param who
	 */
	public void setItem(WElement who) {
		item = who;
	}
	
	public void setIcon(Icon e) {
		icon = e;
	}
	
	public Icon getIcon() {
		return icon;
	}
	
	public String getTitle() {
		return item.getTitle();
	}
	
	public boolean isDesktop() {
		if (Laxkit.isClassFrom(item, WProgram.class)) {
			return ((WProgram) item).isDesktop();
		}
		return false;
		// return item.isDesktop();
	}

	public boolean isDock() {
		if (Laxkit.isClassFrom(item, WProgram.class)) {
			return ((WProgram) item).isDock();
		}
		return false;
		// return item.isDesktop();
	}

	/**
	 * 返回引导单元
	 * @return
	 */
	public WElement getItem() {
		return item;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#toString()
	 */
	@Override
	public String toString() {
		return item.getTitle();
	}

}