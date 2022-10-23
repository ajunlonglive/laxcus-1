/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.dialog;

import java.awt.*;

import com.laxcus.util.display.*;
import com.laxcus.watch.*;

/**
 * WATCH字体对话框
 * 
 * @author scott.liang
 * @version 1.0 7/29/2020
 * @since laxcus 1.0
 */
class WatchCommonFontDialog extends CommonFontDialog {

	private static final long serialVersionUID = -472216578830880464L;

	/**
	 * 构造对话框
	 * 
	 * @param frame 窗口
	 */
	public WatchCommonFontDialog(Frame frame) {
		super(frame);
	}
	
	/**
	 * 构造对话框
	 * 
	 * @param frame 窗口
	 * @param modal 模态
	 */
	public WatchCommonFontDialog(Frame frame, boolean modal) {
		super(frame, modal);
	}
	
	/**
	 * 解析标签
	 * @param xmlPath
	 * @return
	 */
	protected String findCaption(String xmlPath) {
		return WatchLauncher.getInstance().findCaption(xmlPath);
	}

	/**
	 * 解析内容
	 * @param xmlPath
	 * @return 抽取的文本
	 */
	protected String findContent(String xmlPath) {
		return WatchLauncher.getInstance().findContent(xmlPath);
	}

}