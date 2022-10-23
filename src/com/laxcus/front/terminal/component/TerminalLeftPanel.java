/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.front.terminal.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * TERMINAL终端浏览窗口 <br>
 * 位于左侧，显示云端数据、云端应用软件、本地边缘应用服务
 * 
 * @author scott.liang
 * @version 1.0 3/5/2020
 * @since laxcus 1.0
 */
public class TerminalLeftPanel extends JPanel /* implements ChangeListener */ {

	private static final long serialVersionUID = -2229223379514215015L;

	/** 选项卡 **/
	private JTabbedPane tabbed = new JTabbedPane(JTabbedPane.BOTTOM);

	/** 云端数据浏览窗口 **/
	private TerminalRemoteDataPanel remoteData = new TerminalRemoteDataPanel();
	
	/** 云端应用软件浏览窗口 **/
	private TerminalRemoteSoftwarePanel remoteSoftware = new TerminalRemoteSoftwarePanel();

	/** 本地TUB容器浏览窗口 **/
	private TerminalLocalTubPanel localTub = new TerminalLocalTubPanel();

	/**
	 * 构造终端浏览窗口
	 */
	public TerminalLeftPanel() {
		super();
	}

	/**
	 * 站点浏览面板分割线
	 * @return
	 */
	public int getRemoteDataDividerLocation() {
		return remoteData.getDividerLocation();
	}

	/**
	 * 站点浏览面板分割线
	 * @return
	 */
	public int getRemoteSoftwareDividerLocation() {
		return remoteSoftware.getDividerLocation();
	}

	/**
	 * 成员浏览面板分割线
	 * @return
	 */
	public int getLocalBrowserDividerLocation(){
		return localTub.getDividerLocation();
	}

	/**
	 * 加入线程
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 返回左侧云端浏览面板
	 * @return TerminalRemoteDataPanel实例
	 */
	public TerminalRemoteDataPanel getRemoteDataPanel() {
		return remoteData;
	}

	/**
	 * 返回左侧云端浏览面板
	 * @return TerminalRemoteSoftwarePanel实例
	 */
	public TerminalRemoteSoftwarePanel getRemoteSoftwarePanel() {
		return remoteSoftware;
	}

	/**
	 * 返回左侧本地浏览面板
	 * @return TerminalLocalTubPanel实例
	 */
	public TerminalLocalTubPanel getLocalTubPanel() {
		return localTub;
	}

	/**
	 * 返回TabbedPane选项卡字体
	 * @return Font
	 */
	public Font getTabbedSelectFont() {
		return tabbed.getFont();
	}

	/**
	 * 设置TAB选项卡字体
	 * @param font 字体实例
	 */
	public void setTabbedSelectFont(Font font) {
		addThread(new TabbbedFontThread(font));
	}

	/**
	 * 选项卡字体
	 *
	 * @author scott.liang
	 * @version 1.0 3/5/2020
	 * @since laxcus 1.0
	 */
	class TabbbedFontThread extends SwingEvent {
		Font font;

		TabbbedFontThread(Font e) {
			super();
			font = e;
		}

		public void process() {
			__exchangeTabbedFont(font);
		}
	}

	/**
	 * 清空浏览面板上的内容，包括数据和应用软件
	 */
	public void clearRemoteBrowser() {
		remoteData.clear();
		remoteSoftware.clear();
	}

	/**
	 * 清空浏览面板上的内容
	 */
	public void clearRemoteData() {
		remoteData.clear();
	}
	
	/**
	 * 清空浏览面板上的内容
	 */
	public void clearRemoteSoftware() {
		remoteSoftware.clear();
	}

	/**
	 * 提取XML中的文本内容
	 * @param middle 关键字
	 * @return 返回结果
	 */
	private String getBrowserTitle(String middle) {
		return TerminalLauncher.getInstance().findCaption("Window/Tab/" + middle + "/title");
	}

	/**
	 * 修改选项卡字体
	 * @param font
	 */
	private void __exchangeTabbedFont(Font font) {
		if (font != null) {
			Font sub = UITools.createHeaderFont(font);
			tabbed.setFont(sub);
		}
	}

	/**
	 * 初始化左侧状态栏
	 * @return
	 */
	public void init() {
		// 初化左右的栏
		remoteData.init();
		remoteSoftware.init();
		localTub.init();

		String cloud = getBrowserTitle("Cloud");
		String software = getBrowserTitle("Software");
		String local = getBrowserTitle("Local");
		/** 选项卡，在底部显示 **/
		
//		tabbed.addChangeListener(this);
		
		tabbed.addTab(cloud, remoteData);
		tabbed.addTab(software, remoteSoftware);
		tabbed.addTab(local, localTub);
		tabbed.setBorder(new EmptyBorder(0, 0, 0, 0));

		// 选项卡字体！
		__exchangeTabbedFont(TerminalProperties.readTabbedFont());

		// 定义布局，没有边框！
		setLayout(new BorderLayout(0, 0));
		add(tabbed, BorderLayout.CENTER);
		setBorder(new EmptyBorder(0, 0, 0, 0));
	}

//	/* (non-Javadoc)
//	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
//	 */
//	@Override
//	public void stateChanged(ChangeEvent arg0) {
//		// TODO Auto-generated method stub
//
//	}

}
