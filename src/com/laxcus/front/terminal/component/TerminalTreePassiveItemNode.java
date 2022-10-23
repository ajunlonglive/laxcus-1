/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import javax.swing.tree.*;

import com.laxcus.law.cross.*;

/**
 * 浏览窗口被授权单元节点
 * 
 * @author scott.liang
 * @version 1.0 7/7/2017 80
 * @version laxcus 1.0
 */
public class TerminalTreePassiveItemNode extends DefaultMutableTreeNode {
	
	private static final long serialVersionUID = -4953859679928480630L;
	
	/** 被授权单元 **/
	private PassiveItem item;

	/**
	 * 构造默认的浏览窗口被授权单元节点
	 */
	public TerminalTreePassiveItemNode() {
		super();
	}
	
	/**
	 * 构造浏览窗口被授权单元节点，指定被授权单元
	 * @param item
	 */
	public TerminalTreePassiveItemNode(PassiveItem item) {
		this();
		setPassiveItem(item);
	}

	/**
	 * 设置被授权单元
	 * @param e
	 */
	public void setPassiveItem(PassiveItem e) {
		item = e;
	}

	/**
	 * 返回被授权单元
	 * @return
	 */
	public PassiveItem getPassiveItem() {
		return item;
	}

}