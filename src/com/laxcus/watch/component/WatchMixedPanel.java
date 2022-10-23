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

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;
import com.laxcus.watch.*;

/**
 * WATCH选项卡面板。<br>
 * 包括提示信息面板、站点信息面板、日志面板。
 * 
 * @author scott.liang
 * @version 1.1 4/2/2012
 * @since laxcus 1.0
 */
public class WatchMixedPanel extends JPanel {

	private static final long serialVersionUID = 2144636330501301657L;

	/** 选项卡 **/
	private JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

	/** 消息提示面板  **/
	private WatchMixedMessagePanel messagePanel = new WatchMixedMessagePanel();

	/** 二维表面板 **/
	private WatchMixedTablePanel tablePanel = new WatchMixedTablePanel();

	/** 站点运行时面板 **/
	private WatchMixedRuntimePanel runtimePanel = new WatchMixedRuntimePanel();

	/** 日志面板 **/
	private WatchMixedLogPanel logPanel = new WatchMixedLogPanel();

	/**
	 * 构造默认的多操作选项卡面板
	 */
	public WatchMixedPanel() {
		super();
	}
	
	/**
	 * 返回TabbedPane选项卡字体
	 * @return Font
	 */
	public Font getTabbedSelectFont() {
		return tabbedPane.getFont();
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
			Font sub = UITools.createHeaderFont(font);
			tabbedPane.setFont(sub);
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

	/**
	 * 加入线程
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	//	/**
	//	 * 判断是消息框获得焦点
	//	 * 
	//	 * @return 返回真或者假
	//	 */
	//	private boolean isFocusMessage() {
	//		int index = tabbed.getSelectedIndex();
	//		return index == 0;
	//	}

	//	/**
	//	 * 判断是TABLE获得焦点
	//	 * @return 返回真或者假
	//	 */
	//	private boolean isFocusTable() {
	//		int index = tabbed.getSelectedIndex();
	//		return (index == 1);
	//	}

	//	/**
	//	 * 判断是SITE栏获得焦点
	//	 * @return 返回真或者假
	//	 */
	//	private boolean isFocusSite() {
	//		int index = tabbed.getSelectedIndex();
	//		return (index == 2);
	//	}


	//	/**
	//	 * 判断是LOG栏获得焦点
	//	 * @return 返回真或者假
	//	 */
	//	private boolean isFocusLog() {
	//		int index = tabbed.getSelectedIndex();
	//		return (index == 3);
	//	}


	class FocusMessage extends SwingEvent {
		FocusMessage() { super(); }
		public void process() {
			int index = tabbedPane.getSelectedIndex();
			if (index != 0) {
				tabbedPane.setSelectedIndex(0);
			}
		}
	}

	class FocusTable extends SwingEvent {
		FocusTable() { super(); }
		public void process() {
			int index = tabbedPane.getSelectedIndex();
			if (index != 1) {
				tabbedPane.setSelectedIndex(1);
			}
		}
	}

	class FocusSite extends SwingEvent {
		FocusSite() { super(); }
		public void process() {
			int index = tabbedPane.getSelectedIndex();
			if (index != 2) {
				tabbedPane.setSelectedIndex(2);
			}
		}
	}

	class FocusLog extends SwingEvent {
		FocusLog() { super(); }
		public void process() {
			int index = tabbedPane.getSelectedIndex();
			if (index != 3) {
				tabbedPane.setSelectedIndex(3);
			}
		}
	}

	/**
	 * 清除全部记录
	 */
	public void clear() {
		messagePanel.clear();
		tablePanel.clear();
		runtimePanel.clear();
		logPanel.clear();
	}

	/**
	 * 返回消息提示面板
	 * @return
	 */
	public WatchMixedMessagePanel getMessagePanel() {
		return messagePanel;
	}

	/**
	 * 返回表格面板
	 * @return
	 */
	public WatchMixedTablePanel getTablePanel() {
		return tablePanel;
	}

	/**
	 * 返回站点运行时面板
	 * @return
	 */
	public WatchMixedRuntimePanel getRuntimePanel() {
		return runtimePanel;
	}

	/**
	 * 返回日志面板
	 * @return
	 */
	public WatchMixedLogPanel getLogPanel() {
		return logPanel;
	}

	/**
	 * 将焦点移至消息栏
	 */
	public void focusMessage() {
		addThread(new FocusMessage());
	}

	/**
	 * 将焦点移至表格栏
	 */
	public void focusTable() {
		addThread(new FocusTable());
	}

	/**
	 * 将焦点移到站点栏
	 */
	public void focusSite() {
		addThread(new FocusSite());
	}

	/**
	 * 将焦点移到日志栏
	 */
	public void focusLog() {
		addThread(new FocusLog());
	}

//	/* (non-Javadoc)
//	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
//	 */
//	@Override
//	public void stateChanged(ChangeEvent e) {
//
//	}

	/**
	 * 通过XML路径，取出对应的标题
	 * @param xmlPath
	 * @return
	 */
	private String getCaption(String xmlPath) {
		return WatchLauncher.getInstance().findCaption(xmlPath);
	}

	/**
	 * 返回配置中的切换页标题
	 * @param middle
	 * @return
	 */
	private String getTitle(String middle) {
		return getCaption("Window/Tab/" + middle + "/title");
	}

	/**
	 * 建立显示板
	 */
	public void init() {
		// 消息
		messagePanel.init();
		// 二维表
		tablePanel.init();
		// 站点清单
		runtimePanel.init();
		// 日志
		logPanel.init();

		String prompt = getTitle("Prompt");
		String table = getTitle("Table");
		String site = getTitle("Site");
		String log = getTitle("Log");

		// 选项卡字体
		__exchangeTabbedFont(WatchProperties.readTabbedFont());

//		tabbedPane.addChangeListener(this);
		
		tabbedPane.addTab(prompt, messagePanel);
		tabbedPane.addTab(table, tablePanel);
		tabbedPane.addTab(site, runtimePanel);
		tabbedPane.addTab(log, logPanel);
		tabbedPane.setBorder(BorderFactory.createEmptyBorder());

		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
	}

	/**
	 * 普通消息
	 * @param text
	 */
	public void message(String text, boolean sound) {
		messagePanel.message(text, sound);
		focusMessage();
	}

	/**
	 * 警告
	 * @param text
	 */
	public void warning(String text, boolean sound) {
		messagePanel.warning(text, sound);
		focusMessage();
	}

	/**
	 * 故障
	 * @param text
	 */
	public void fault(String text, boolean sound) {
		messagePanel.fault(text, sound);
		focusMessage();
	}

	/**
	 * 普通消息
	 * @param text
	 */
	public void message(String text) {
		message(text, true);
	}

	/**
	 * 警告
	 * @param text
	 */
	public void warning(String text) {
		warning(text, true);
	}

	/**
	 * 故障
	 * @param text
	 */
	public void fault(String text) {
		fault(text, true);
	}

	/**
	 * 返回标题单元数目
	 * @return 单元数目
	 */
	public int getTitleCellCount(){
		return tablePanel.getTitleCellCount();
	}

	/**
	 * 设置表格标题
	 * @param title
	 */
	public void setShowTitle(ShowTitle title) {
		tablePanel.setTitle(title);
		focusTable();
	}

	/**
	 * 增加表格行
	 * @param item
	 */
	public void addShowItem(ShowItem item) {
		tablePanel.addItem(item);
	}

	/**
	 * 显示全部记录
	 * @param title
	 * @param items
	 */
	public void showTable(ShowTitle title, java.util.Collection<ShowItem> items) {
		// 显示记录
		tablePanel.showTable(title, items);
		// 移到表
		focusTable();
	}

	/**
	 * 清除表格内容
	 */
	public void clearShowItems() {
		tablePanel.clear();
	}

	/**
	 * 增加节点记录
	 * @param runtime
	 */
	public void showRuntime(SiteRuntime runtime) {
		runtimePanel.show(runtime);
	}

	/**
	 * 删除节点
	 * @param node
	 */
	public void dropRuntime(Node node) {
		runtimePanel.drop(node);
	}

}