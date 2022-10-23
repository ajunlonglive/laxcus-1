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

/**
 * 浏览窗口数据库节点
 * 
 * @author scott.liang
 * @version 1.0 6/21/2012
 * @since laxcus 1.0
 */
public class TerminalTreeSchemaNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 6663759165023115989L;

	/** 数据库定义 **/
	private Schema schema;

	/**
	 * 构造浏览窗口数据库节点
	 */
	public TerminalTreeSchemaNode() {
		super();
	}

	/**
	 * 构造浏览窗口数据库节点，指定数据库名称
	 * @param e
	 */
	public TerminalTreeSchemaNode(Schema e) {
		this();
		setSchema(e);
	}

	/**
	 * 设置数据库
	 * @param e
	 */
	public void setSchema(Schema e) {
		schema = e;
	}
	
	/**
	 * 返回数据库
	 * @return
	 */
	public Schema getSchema() {
		return schema;
	}

	/**
	 * 返回数据库名称
	 * @return
	 */
	public Fame getFame() {
		return schema.getFame();
	}

}
