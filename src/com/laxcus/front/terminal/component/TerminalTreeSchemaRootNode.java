/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import javax.swing.tree.*;

import com.laxcus.front.terminal.*;

/**
 * 浏览窗口数据库根节点
 * 
 * @author scott.liang
 * @version 1.0 4/2/2012
 * @since laxcus 1.0
 */
public class TerminalTreeSchemaRootNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1297064652037616905L;

	/**
	 * 浏览窗口数据库根节点
	 */
	public TerminalTreeSchemaRootNode() {
		super();
	}

	/**
	 * 返回根节点和文本描述
	 * @return
	 */
	public String getText() {
		return TerminalLauncher.getInstance().findCaption("Window/BrowserDataListPanel/SchemaRoot/title");
	}
}