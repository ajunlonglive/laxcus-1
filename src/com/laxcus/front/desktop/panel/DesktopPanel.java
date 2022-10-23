/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.panel;

import javax.swing.*;

import com.laxcus.front.desktop.*;
import com.laxcus.util.*;
import com.laxcus.util.event.*;

/**
 * 桌面面板
 * 
 * @author scott.liang
 * @version 1.0 6/17/2021
 * @since laxcus 1.0
 */
public class DesktopPanel extends JPanel {

	private static final long serialVersionUID = -4774474511755887087L;

	/**
	 * 构造默认的桌面面板
	 */
	public DesktopPanel() {
		super();
	}

	/**
	 * 加入线程
	 * @param thread
	 */
	protected void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 判断是METAL界面
	 * @return
	 */
	protected boolean isMetalUI() {
		return GUIKit.isMetalUI();
	}

	/**
	 * 判断是NIMBUS界面
	 * @return
	 */
	protected boolean isNimbusUI() {
		return GUIKit.isNimbusUI();
	}

	/**
	 * 判断是LINUX
	 * @return 返回真或者假
	 */
	protected boolean isLinux() {
		return DesktopLauncher.getInstance().isLinux();
	}

	/**
	 * 判断是WINDOWS
	 * @return 返回真或者假
	 */
	protected boolean isWindows() {
		return DesktopLauncher.getInstance().isWindows();
	}

	
//	/**
//	 * 标题内容
//	 * @param name 名称
//	 * @return 返回文本描述
//	 */
//	private String findCaption(String name) {
//		return DesktopLauncher.getInstance().findCaption(name); // "ToolTip/DBA/title");
//	}
	
//	/**
//	 * 解析标签属性
//	 * @param xmlPath
//	 * @return
//	 */
//	protected String findCaption(String xmlPath) {
//		return DesktopLauncher.getInstance().findCaption(xmlPath);
//	}
//
//	/**
//	 * 解析标签中的内容
//	 * @param xmlPath
//	 * @return
//	 */
//	protected String findContent(String xmlPath) {
//		return DesktopLauncher.getInstance().findCaption(xmlPath);
//	}

}