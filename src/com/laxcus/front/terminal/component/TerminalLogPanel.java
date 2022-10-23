/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.front.terminal.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;

/**
 * 日志显示面板。
 * 位于右侧下方的选择页中
 * 
 * @author scott.liang
 * @version 1.0 09/06/2009
 * @since laxcus 1.0
 */
public class TerminalLogPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -194587464458674768L;
	
	/** 最多显示日志数字 **/
	private final int MAX_ELEMENTS = 2000;
	
	/** 日志最大尺寸 **/
	private int maxItems;
	
	/** 渲染器 **/
	private TerminalLogCellRenderer renderer;

	/** 日志列表 **/
	private JList list = new JList();

	/** 日志模型 **/
	private DefaultListModel model = new DefaultListModel();
	
	/** 禁止显示日志 **/
	private volatile boolean forbid;

	/** 禁止显示日志按纽 **/
	private JCheckBox cmdForbid= new JCheckBox();

	/**
	 * 构造日志显示面板
	 */
	public TerminalLogPanel() {
		super();
		// 最大行数
		setMaxItems(MAX_ELEMENTS);
		// 默认是假
		forbid = false;
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	class ForbidThread extends SwingEvent {
		ActionEvent event;

		ForbidThread(ActionEvent e) {
			super();
			event = e;
		}

		public void process() {
			if (event.getSource() == cmdForbid) {
				clickForbid();
			}
		}
	}

	/**
	 * 屏蔽或者显示日志
	 */
	private void clickForbid() {
		boolean select = cmdForbid.isSelected();
		if (select) {
			forbid = true;
			// 清除全部日志
			clear();
		} else {
			forbid = false;
		}
		// 拒绝显示日志
		TerminalProperties.writeLogForbid(forbid);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new ForbidThread(e));
	}

	/**
	 * 设置最大显示单元数目。范围在0 - 2000之间
	 * @param n 日志单元数
	 */
	public int setMaxItems(int n) {
		if (n > MAX_ELEMENTS) {
			maxItems = MAX_ELEMENTS;
		} else if (n >= 0 && n <= MAX_ELEMENTS) {
			maxItems = n;
		} else if (n < 0) {
			maxItems = 0;
		}
		// 清除日志
		if (maxItems < 1) {
			clear();
		}
		// 显示日志
		addThread(new NumberThread());
		return maxItems;
	}
	
	/**
	 * 返回最大显示单元数目
	 * @return 日志单元数
	 */
	public int getMaxItems() {
		return maxItems;
	}

	/**
	 * 显示文本
	 */
	private void setLogTooltip() {
		String text = TerminalLauncher.getInstance().findCaption("Window/LogPanel/Number/title");
		String value = String.format(text, maxItems);
		cmdForbid.setToolTipText(value);
	}
	
	/**
	 * 做为工具提示，显示在选择按纽上
	 *
	 * @author scott.liang
	 * @version 1.0 1/6/2021
	 * @since laxcus 1.0
	 */
	class NumberThread extends SwingEvent {
		
		public NumberThread(){
			super();
		}

		/* (non-Javadoc)
		 * @see com.laxcus.util.display.SwingEvent#process()
		 */
		@Override
		public void process() {
			setLogTooltip();
		}
	}
	
	/**
	 * 返回字体
	 * @return
	 */
	public Font getSelectFont() {
		return list.getFont();
	}

	/**
	 * 设置字体
	 * @param e
	 */
	public void setSelectFont(Font e) {
		addThread(new FontThread(e));
	}

	/**
	 * 修正显示字体
	 * @param font
	 */
	private void __exchangeFont(Font font) {
		if (font != null) {
			list.setFont(font);
			cmdForbid.setFont(font);
		}
	}

	class FontThread extends SwingEvent {
		Font font;

		FontThread(Font e) {
			super();
			font = e;
		}

		public void process() {
			__exchangeFont(font);
		}
	}
	
	/**
	 * 顶部日志按纽
	 * @return
	 */
	private JPanel createNorth() {
		// 按纽
		String text = TerminalLauncher.getInstance().findCaption("Window/LogPanel/Forbid/title");
		cmdForbid.setText(text);
		cmdForbid.addActionListener(this);
		
		// 判断是拒绝
		Boolean b = TerminalProperties.readLogForbid();
		if (b != null) {
			forbid = b.booleanValue();
		} else {
			forbid = true; // 如果没有定义，默认是真，不显示日志
		}
		cmdForbid.setSelected(forbid);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 0));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 3, 0, 3));
		panel.add(cmdForbid, BorderLayout.WEST);
		panel.add(new JPanel(), BorderLayout.CENTER);
		return panel;
	}

	/**
	 * 初始化
	 */
	private JScrollPane createCenter() {
		renderer = new TerminalLogCellRenderer();
		list.setCellRenderer(renderer);
		list.setModel(model);
		
		String tooltip = TerminalLauncher.getInstance().findCaption("Window/LogPanel/title");
		FontKit.setToolTipText(list, tooltip);

		// 行高度
		String value = TerminalLauncher.getInstance().findCaption("Window/LogPanel/row-height");
		int rowHeight = ConfigParser.splitInteger(value, 30);
		
		list.setFixedCellHeight(rowHeight);
		list.setBorder(new EmptyBorder(2, 2, 2, 2));
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// 设置字体
		__exchangeFont(TerminalProperties.readTabbedLogFont());
		// 最大日志数目
		Integer max = TerminalProperties.readLogElements();
		if (max != null) {
			maxItems = max.intValue();
			setLogTooltip();
//			setMaxItems(max.intValue());
		}
		
		JScrollPane scroll = new JScrollPane(list);
		FontKit.setToolTipText(scroll, tooltip);
		
		return scroll;
	}
	
	/**
	 * 初始化
	 */
	public void init() {
		// 显示面板
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		panel.setLayout(new BorderLayout(0, 5));
		panel.add(createNorth(), BorderLayout.NORTH);
		panel.add(createCenter(), BorderLayout.CENTER);

		// 窗口布局!
		setLayout(new BorderLayout(0, 0));
		add(panel, BorderLayout.CENTER);
	}
	
//	/**
//	 * 初始化
//	 */
//	public void init() {
//		renderer = new TerminalLogCellRenderer();
//		list.setCellRenderer(renderer); // new TerminalLogCellRenderer());
//		list.setModel(model);
//		
//		String tooltip = TerminalLauncher.getInstance().findCaption("Window/LogPanel/title");
//		FontKit.setToolTipText(list, tooltip);
//
//		// 行高度
//		String value = TerminalLauncher.getInstance().findCaption("Window/LogPanel/row-height");
//		int rowHeight = ConfigParser.splitInteger(value, 30);
//		
//		list.setFixedCellHeight(rowHeight);
//		list.setBorder(new EmptyBorder(2, 2, 2, 2));
//		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//
//		// 设置字体
//		__exchangeFont(TerminalProperties.readTabbedLogFont());
//		// 最大日志数目
//		Integer max = TerminalProperties.readLogElements();
//		if (max != null) {
//			setMaxItems(max.intValue());
//		}
//		
//		JScrollPane scroll = new JScrollPane(list);
//		FontKit.setToolTipText(scroll, tooltip);
//		
//		// 本地布局
//		setLayout(new BorderLayout());
//		add(scroll, BorderLayout.CENTER);
//	}

	/**
	 * 在图形窗口显示日志
	 * @param logs 日志集合
	 */
	private void printLogs(java.util.List<LogItem> logs) {
		final int modelSize = model.size();
		
		// 要求单元行小于1时，清除旧记录，同时忽略传入的日志
		if (forbid || maxItems < 1) {
			if (modelSize > 0) { 
				// 清除记录
				model.clear();
				list.removeAll();
			}
			return;
		}

		int deleteTo = 0;
		int addFrom = 0;
		int addTo = 0;
		// 判断:
		// 1. 传入日志超出最大范围
		// 2. 当前模型中的日志和传入日志超出范围
		// 3. 在范围内
		if (logs.size() >= maxItems) {
			deleteTo = modelSize;
			addFrom = logs.size() - maxItems;
			addTo = logs.size();
		} else if (modelSize + logs.size() >= maxItems) {
			addFrom = 0;
			addTo = logs.size();
			int save = maxItems - (addTo - addFrom);
			if (save >= modelSize) {
				deleteTo = modelSize;
			} else {
				deleteTo = modelSize - save;
			}
		} else {
			deleteTo = 0;
			addFrom = 0;
			addTo = logs.size();
		}

		// 清除显示
		if (deleteTo > 0) {
			model.removeRange(0, deleteTo - 1);
		}
		// 增加新的日志
		for (int i = addFrom; i < addTo; i++) {
			model.addElement(logs.get(i));
		}
	}
	
	/**
	 * 推送日志进SWING队列线程
	 *
	 * @author scott.liang
	 * @version 1.0 9/17/2019
	 * @since laxcus 1.0
	 */
	class PushLogThread extends SwingEvent {
		java.util.List<LogItem> logs;

		protected void finalize() {
			if (logs != null) {
				logs = null;
			}
		}
		
		PushLogThread(java.util.List<LogItem> a) {
			super();
			logs = a;
		}

		public void process() {
			printLogs(logs);
		}
	}
	
	/**
	 * 把日志推送进线程
	 * @param logs 日志实例
	 */
	public void pushLogs(java.util.List<LogItem> logs) {
		PushLogThread thread = new PushLogThread(logs);
		addThread(thread);
	}

	class ClearThread extends SwingEvent {
		ClearThread() {
			super();
		}
		public void process() {
			model.clear();
			list.removeAll();
		}
	}
	
	/**
	 * 清除全部日志
	 */
	public void clear() {
		addThread(new ClearThread());
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	public void updateUI() {
		if (renderer != null) {
			renderer.updateUI();
		}
		super.updateUI();
	}
}