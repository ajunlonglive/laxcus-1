/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import com.laxcus.front.meet.invoker.*;
import com.laxcus.front.terminal.*;
import com.laxcus.front.terminal.dialog.*;
import com.laxcus.front.tub.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.graph.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 终端窗口的选项页面板。
 * 
 * @author scott.liang
 * @version 1.21 9/6/2013
 * @since laxcus 1.0
 */
public class TerminalMixedPanel extends JPanel implements MeetDisplay {

	private static final long serialVersionUID = -4613207430625022392L;

	/** 选项卡 **/
	private JTabbedPane tabbedPane = new JTabbedPane();

	/** 消息提示面板  **/
	private TerminalMessagePanel messagePanel = new TerminalMessagePanel();

	/** 二维表面板 **/
	private TerminalTablePanel tablePanel = new TerminalTablePanel();

	/** 图形面板 **/
	private TerminalGraphPanel graphPanel = new TerminalGraphPanel();

	/** 日志面板 **/
	private TerminalLogPanel logPanel = new TerminalLogPanel();

	/**
	 * 构造选项页面板
	 */
	public TerminalMixedPanel() {
		super();
		// 给异步命令调用器分配终端显示接口
		MeetInvoker.setDefaultDisplay(this);
		// 设置显示器
		TubServlet.setDisplay(new TubDisplayImpl(this));
	}
	
	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
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
	 * 将焦点移至提示栏
	 */
	class FocusMessage extends SwingEvent {
		FocusMessage(){
			super();
		}
		public void process() {
			int index = tabbedPane.getSelectedIndex();
			if (index != 0) {
				tabbedPane.setSelectedIndex(0);
			}
		}
	}

	/**
	 * 将焦点移至表格栏
	 */
	class FocusTable extends SwingEvent {
		FocusTable(){
			super();
		}
		public void process() {
			int index = tabbedPane.getSelectedIndex();
			if (index != 1) {
				tabbedPane.setSelectedIndex(1);
			}
		}
	}

	/**
	 * 将焦点移到图像栏
	 */
	class FocusGraph extends SwingEvent {
		FocusGraph(){
			super();
		}
		public void process() {
			int index = tabbedPane.getSelectedIndex();
			if (index != 2) {
				tabbedPane.setSelectedIndex(2);
			}
		}
	}

	/**
	 * 将焦点移到日志栏
	 */
	class FocusLog extends SwingEvent {
		FocusLog(){
			super();
		}
		public void process() {
			int index = tabbedPane.getSelectedIndex();
			if (index != 3) {
				tabbedPane.setSelectedIndex(3);
			}
		}
	}

	/**
	 * 返回消息提示面板
	 * @return
	 */
	public TerminalMessagePanel getMessagePanel() {
		return messagePanel;
	}

	/**
	 * 返回表格面板
	 * @return
	 */
	public TerminalTablePanel getTablePanel() {
		return tablePanel;
	}

	/**
	 * 返回图形面板
	 * @return
	 */
	public TerminalGraphPanel getGraphPanel() {
		return graphPanel;
	}

	/**
	 * 返回日志面板
	 * @return
	 */
	public TerminalLogPanel getLogPanel() {
		return logPanel;
	}

//	/* (non-Javadoc)
//	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
//	 */
//	@Override
//	public void stateChanged(ChangeEvent event) {
//
//	}

	/**
	 * 通过XML路径，取出对应的标题
	 * @param xmlPath
	 * @return
	 */
	private String getCaption(String xmlPath) {
		return TerminalLauncher.getInstance().findCaption(xmlPath);
	}

	/**
	 * 返回配置中的切换页标题
	 * @param middle
	 * @return
	 */
	private String getTitle(String middle) {
		return getCaption("Window/Tab/" + middle + "/title");
	}

//	/**
//	 * 建立显示板
//	 */
//	public void init() {
//		// 日志
//		logPanel.init();
//		// 消息
//		messagePanel.init();
//		// 二维表
//		tablePanel.init();
//		// 图形
//		graphPanel.init();
//
//		String prompt = getTitle("Prompt");
//		String table = getTitle("Table");
//		String graph = getTitle("Graph");
//		String log = getTitle("Log");
//		
//		// 选项卡字体
//		Font font = tabbed.getFont();
//		String str = prompt + table + graph + log;
//		if (!FontKit.canDisplay(font, str)) {
//			font = FontKit.findFont(font, str);
//			if (font != null) {
//				tabbed.setFont(font);
//			}
//		}
//
//		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/tabbed");
//		Icon iconPrompt = loader.findImage("prompt.png");
//		Icon iconTable = loader.findImage("table.png");
//		Icon iconGraph = loader.findImage("graph.png");
//		Icon iconLog = loader.findImage("log.png");
//
//		tabbed.addChangeListener(this);
//		tabbed.addTab(prompt, iconPrompt, messagePanel);
//		tabbed.addTab(table, iconTable, tablePanel);
//		tabbed.addTab(graph, iconGraph, graphPanel);
//		tabbed.addTab(log, iconLog, logPanel);
//		tabbed.setBorder(new EmptyBorder(1, 1, 1, 1));
//
//		setLayout(new BorderLayout(5, 5));
//		add(tabbed, BorderLayout.CENTER);
//	}

	/**
	 * 建立显示板
	 */
	public void init() {
		// 日志
		logPanel.init();
		// 消息
		messagePanel.init();
		// 二维表
		tablePanel.init();
		// 图形
		graphPanel.init();

		String prompt = getTitle("Prompt");
		String table = getTitle("Table");
		String graph = getTitle("Graph");
		String log = getTitle("Log");
		
//		// 选项卡字体
//		Font font = tabbedPane.getFont();
//		String str = prompt + table + graph + log;
//		if (!FontKit.canDisplay(font, str)) {
//			font = FontKit.findFont(font, str);
//			if (font != null) {
//				tabbedPane.setFont(font);
//			}
//		}
		
		// 选项卡字体
		__exchangeTabbedFont(TerminalProperties.readTabbedFont());

//		tabbedPane.addChangeListener(this);
		
		tabbedPane.addTab(prompt, messagePanel);
		tabbedPane.addTab(table, tablePanel);
		tabbedPane.addTab(graph, graphPanel);
		tabbedPane.addTab(log, logPanel);
//		tabbed.setBorder(new EmptyBorder(1, 1, 1, 1));
		tabbedPane.setBorder(BorderFactory.createEmptyBorder());

		setLayout(new BorderLayout(5, 5));
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	/**
	 * 将焦点移至提示栏
	 */
	public void focusPrompt() {
		addThread(new FocusMessage());
	}

	/**
	 * 将焦点移至表格栏
	 */
	public void focusTable() {
		addThread(new FocusTable());
	}

	/**
	 * 将焦点移到图像栏
	 */
	public void focusGraph() {
		addThread(new FocusGraph());
	}

	/**
	 * 将焦点移到日志栏
	 */
	public void focusLog() {
		addThread(new FocusLog());
	}

	/**
	 * 清除日志
	 */
	public void clearLog() {
		logPanel.clear();
	}

//	/**
//	 * 不是驱动程序
//	 * @see com.laxcus.ui.display.MeetDisplay#isDriver()
//	 */
//	@Override
//	public boolean isDriver() {
//		return false;
//	}
//
//	/**
//	 * 不是字符控制台界面
//	 * @see com.laxcus.ui.display.MeetDisplay#isConsole()
//	 */
//	@Override
//	public boolean isConsole() {
//		return false;
//	}
//
//	/**
//	 * 是图形终端办面
//	 * @see com.laxcus.ui.display.MeetDisplay#isWindow()
//	 */
//	@Override
//	public boolean isWindow() {
//		return true;
//	}

	/**
	 * 增加一行提示文本
	 * @param status
	 * @param text
	 * @param focus 要求获得焦点
	 */
	private void addPrompt(int status, String text, boolean focus) {
		messagePanel.add(status, text);
		// 要求获得焦点时...
		if (focus) {
			focusPrompt();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#isUsabled()
	 */
	@Override
	public boolean isUsabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#approveLicence(java.lang.String)
	 */
	@Override
	public boolean approveLicence(String content) {
		TerminalWindow window = TerminalLauncher.getInstance().getWindow();
		TerminalLicenceDialog dialog = new TerminalLicenceDialog(window, true, content);
		dialog.showDialog();
		return dialog.isAccpeted();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#message(java.lang.String, boolean)
	 */
	@Override
	public void message(String text, boolean focus) {
		addPrompt(NoteItem.MESSAGE, text, focus);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#message(java.lang.String)
	 */
	@Override
	public void message(String text) {
		message(text, true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#warning(java.lang.String, boolean)
	 */
	@Override
	public void warning(String text, boolean focus) {
		addPrompt(NoteItem.WARNING, text, focus);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#warning(java.lang.String)
	 */
	@Override
	public void warning(String text) {
		warning(text, true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#fault(java.lang.String, boolean)
	 */
	@Override
	public void fault(String text, boolean focus) {
		addPrompt(NoteItem.FAULT, text, focus);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#fault(java.lang.String)
	 */
	@Override
	public void fault(String text) {
		fault(text, true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.display.Display#clearText()
	 */
	@Override
	public void clearPrompt() {
		messagePanel.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#getTitleCellCount()
	 */
	@Override
	public int getTitleCellCount(){
		return tablePanel.getTitleCellCount();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#setShowTitle(com.laxcus.util.display.show.ShowTitle)
	 */
	@Override
	public void setShowTitle(ShowTitle title) {
		tablePanel.setTitle(title);
		focusTable();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#addShowItem(com.laxcus.util.display.show.ShowItem)
	 */
	@Override
	public void addShowItem(ShowItem item) {
		tablePanel.addItem(item);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#showTable(com.laxcus.util.display.show.ShowTitle, java.util.Collection)
	 */
	@Override
	public void showTable(ShowTitle title, Collection<ShowItem> items) {
		tablePanel.showTable(title, items);
		focusTable();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#setStatusText(java.lang.String)
	 */
	@Override
	public void setStatusText(String text) {
		TerminalLauncher.getInstance().getWindow().setStatusText(text);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#clearShowItems()
	 */
	@Override
	public void clearShowItems() {
		tablePanel.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.display.Display#flash(com.laxcus.task.display.GraphItem)
	 */
	@Override
	public void flash(GraphItem item) {
		graphPanel.flash(item);
		focusGraph();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.display.Display#clearGraph()
	 */
	@Override
	public void clearGraph() {
		graphPanel.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#getProductListener()
	 */
	@Override
	public ProductListener getProductListener() {
		return null;
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.ui.display.MeetDisplay#ratify(java.lang.String)
//	 */
//	@Override
//	public boolean ratify(String content) {
//		return false;
//	}

}