/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import javax.swing.tree.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.naming.*;

/**
 * 浏览窗口数据表节点
 * 
 * @author scott.liang
 * @version 1.0 6/21/2012
 * @since laxcus 1.0
 */
public class TerminalTreeTableNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -234804675569797115L;
	
	/** 表名称 **/
	private Space space;

	/**
	 * 构造默认的浏览窗口数据表节点
	 */
	public TerminalTreeTableNode() {
		super();
	}

	/**
	 * 构造浏览窗口数据表节点，指定数据表
	 * @param e
	 */
	public TerminalTreeTableNode(Space e) {
		this();
		setSpace(e);
	}

	public void setSpace(Space e) {
		space = e;
	}
	
	public Space getSpace() {
		return space;
	}

	/**
	 * 返回数据表名称
	 * @return
	 */
	public Naming getTableName() {
		return space.getTable();
	}
	
//	/** 表名称 **/
//	private Naming name;
//
//	/**
//	 * 构造默认的浏览窗口数据表节点
//	 */
//	public TerminalTreeTableNode() {
//		super();
//	}
//
//	/**
//	 * 构造浏览窗口数据表节点，指定数据表
//	 * @param e
//	 */
//	public TerminalTreeTableNode(Naming e) {
//		this();
//		setName(e);
//	}
//
//	public void setName(Naming e) {
//		name = e;
//	}
//
//	/**
//	 * 返回数据表名称
//	 * @return
//	 */
//	public Naming getName() {
//		return name;
//	}

}
