/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.dialog;

import java.awt.*;

import com.laxcus.front.terminal.*;
import com.laxcus.util.display.*;

/**
 * TERMINAL字体对话框
 * 
 * @author scott.liang
 * @version 1.0 7/21/2020
 * @since laxcus 1.0
 */
class TerminalCommonFontDialog extends CommonFontDialog {

	private static final long serialVersionUID = 8667144772282930520L;

	/**
	 * 构造对话窗口
	 * @param owner
	 */
	public TerminalCommonFontDialog(Frame owner) {
		super(owner);
	}
	
	/**
	 * 构造对话框
	 * 
	 * @param owner 窗口
	 * @param modal 模态
	 */
	public TerminalCommonFontDialog(Frame owner, boolean modal) {
		super(owner, modal);
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



}