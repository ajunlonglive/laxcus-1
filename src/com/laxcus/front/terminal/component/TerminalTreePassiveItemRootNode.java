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
 * 浏览窗口被授权单元根节点
 * 
 * @author scott.liang
 * @version 1.0 7/7/2017
 * @since laxcus 1.0
 */
public class TerminalTreePassiveItemRootNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 7375723470679992788L;

	/**
	 * 构造默认的浏览窗口被授权单元根节点
	 */
	public TerminalTreePassiveItemRootNode() {
		super();
	}

	/**
	 * 返回显示文本
	 * @return
	 */
	public String getText() {
		return TerminalLauncher.getInstance().findCaption("Window/BrowserDataListPanel/CrossTable/title");
	}
}
