/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import javax.swing.tree.*;

import com.laxcus.access.column.attribute.*;

/**
 * 浏览窗口表属性节点
 * 
 * @author scott.liang
 * @version 1.0 6/21/2012
 * @since laxcus 1.0
 */
public class TerminalTreeAttributeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 8623751830974233949L;

	private ColumnAttribute attribute;

	/**
	 * 构造默认的浏览窗口表属性节点
	 */
	public TerminalTreeAttributeNode() {
		super();
	}

	/**
	 * 构造浏览窗口表属性节点，指定属性
	 * @param e
	 */
	public TerminalTreeAttributeNode(ColumnAttribute e) {
		this();
		this.setAttribute(e);
	}

	/**
	 * 设置属性
	 * @param e
	 */
	public void setAttribute(ColumnAttribute e) {
		this.attribute = e;
	}

	/**
	 * 返回属性
	 * @return
	 */
	public ColumnAttribute getAttribute() {
		return this.attribute;
	}
}
