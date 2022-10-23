/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;
import com.laxcus.watch.*;

/**
 * WATCH节点浏览面板 <br>
 * 位于窗口的左侧，展示登录集群的节点和集群用户！
 * 
 * @author scott.liang
 * @version 1.0 3/5/2020
 * @since laxcus 1.0
 */
public class WatchLeftPanel extends JPanel  {

	private static final long serialVersionUID = 2277722956271673145L;

	/** 选项卡，在底部显示 **/ 
	private JTabbedPane tabbed = new JTabbedPane(JTabbedPane.BOTTOM);

	/** 集群节点浏览面板 **/
	private WatchSiteBrowserPanel siteBrowser = new WatchSiteBrowserPanel();

	/** 集群成员浏览面板 **/
	private WatchMemberBrowserPanel memberBrowser = new WatchMemberBrowserPanel();

	/**
	 * 构造WATCH节点浏览面板
	 */
	public WatchLeftPanel() {
		super();
	}

	/**
	 * 站点浏览面板分割线
	 * @return
	 */
	public int getSiteBrowserDividerLocation() {
		return siteBrowser.getDividerLocation();
	}

	/**
	 * 成员浏览面板分割线
	 * @return
	 */
	public int getMemberBrowserDividerLocation(){
		return memberBrowser.getDividerLocation();
	}

	/**
	 * 加入线程
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 返回集群浏览节点面板
	 * @return
	 */
	public WatchSiteBrowserPanel getSiteBrowserPanel() {
		return siteBrowser;
	}

	/**
	 * 返回集群浏览成员面板
	 * @return
	 */
	public WatchMemberBrowserPanel getMemberBrowserPanel() {
		return memberBrowser;
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
	 * 修改选项卡字体
	 * @param font
	 */
	private void __exchangeTabbedFont(Font font) {
		if (font != null) {
			// 重新定义字体磅数
			Font sub = UITools.createHeaderFont(font);
			tabbed.setFont(sub);
		}
	}

	/**
	 * 选项卡字体
	 *
	 * @author scott.liang
	 * @version 1.0 3/4/2020
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


	//	/* (non-Javadoc)
	//	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	//	 */
	//	@Override
	//	public void stateChanged(ChangeEvent e) {
	//		
	//	}

	/**
	 * 初始化面板
	 */
	public void init() {
		// 初化左右的栏
		siteBrowser.init();
		memberBrowser.init();

		// 显示文本
		String siteText = WatchLauncher.getInstance().findCaption("Window/BrowserTab/Site/title");
		String memberText = WatchLauncher.getInstance().findCaption("Window/BrowserTab/Member/title");

		//		tabbed.addChangeListener(this);
		tabbed.addTab(siteText, siteBrowser);
		tabbed.addTab(memberText, memberBrowser);
		// 不要出现边框！
		tabbed.setBorder(new EmptyBorder(0, 0, 0, 0));

		// 选项卡字体
		__exchangeTabbedFont(WatchProperties.readTabbedFont());

		// 面板布局，不要边框！
		setLayout(new BorderLayout(0, 0));
		add(tabbed, BorderLayout.CENTER);
		setBorder(new EmptyBorder(0, 0, 0, 0));
	}

	/**
	 * 清除全部显示记录
	 */
	public void clear() {
		// 清除界面
		siteBrowser.clear();
		memberBrowser.clear();
	}

	/**
	 * 推送一个新的登录站点
	 * @param node LAXCUS站点地址
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean pushSite(Node node) {
		// 在左侧栏显示登录站点
		return siteBrowser.pushSite(node);
	}

	/**
	 * 正常退出一个登录站点
	 * @param node LAXCUS站点地址
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean dropSite(Node node) {
		// 从左侧栏撤销登录站点
		return siteBrowser.dropSite(node);

		// browser.getRuntimePanel().drop(node);
		// return browser.getSitePanel().drop(node);
	}

	/**
	 * 调整运行时
	 * @param runtime
	 */
	public void modify(SiteRuntime runtime) {
		siteBrowser.modify(runtime);
	}

	/**
	 * 推送新的注册成员
	 * @param siger 用户签名
	 */
	public void pushRegisterMember(Siger siger) {
		memberBrowser.pushRegisterMember(siger);
	}

	/**
	 * 删除注册成员
	 * @param siger 用户签名
	 */
	public void dropRegisterMember(Siger siger) {
		memberBrowser.dropRegisterMember(siger);
	}

	/**
	 * 推送在线成员
	 * @param siger 用户签名
	 */
	public void pushOnlineMember(Siger siger) {
		memberBrowser.pushOnlineMember(siger);
	}

	/**
	 * 删除在线成员
	 * @param siger 用户签名
	 */
	public void dropOnlineMember(Siger siger) {
		memberBrowser.dropOnlineMember(siger);
	}

	/**
	 * 更新在线成员状态
	 * @param siger 用户签名
	 */
	public void updateOnlineMember(Siger siger) {
		memberBrowser.updateOnlineMember(siger);
	}
}