/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import javax.swing.*;

import com.laxcus.front.terminal.*;

/**
 * TERMINAL节点面板
 * 
 * @author scott.liang
 * @version 1.0 7/29/2020
 * @since laxcus 1.0
 */
public class TerminalPanel extends JPanel {

	private static final long serialVersionUID = -1461501193078089984L;

	/**
	 * 构造TERMINAL节点面板
	 */
	public TerminalPanel() {
		super();
	}

	/**
	 * 解析标签
	 * @param xmlPath
	 * @return
	 */
	protected String findCaption(String xmlPath) {
		return TerminalLauncher.getInstance().findCaption(xmlPath);
	}

	/**
	 * 解析内容
	 * @param xmlPath
	 * @return 抽取的文本
	 */
	protected String findContent(String xmlPath) {
		return TerminalLauncher.getInstance().findContent(xmlPath);
	}

	/**
	 * 返回主窗口
	 * @return
	 */
	protected TerminalWindow getWindow() {
		return TerminalLauncher.getInstance().getWindow();
	}
}