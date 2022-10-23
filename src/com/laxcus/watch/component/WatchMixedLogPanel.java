/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.watch.*;

/**
 * 日志显示面板，位于右侧下方的选择页中。
 * 因为SWING组件是线程不安全，所有读写操作被放入SWING事件队列中执行。
 * 
 * @author scott.liang
 * @version 1.0 09/06/2009
 * @since laxcus 1.0
 */
public class WatchMixedLogPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 2178305234736392484L;

	/** 最多显示日志数字 **/
	private final int MAX_ELEMENTS = 2000;

	/** 渲染器 **/
	private WatchMixedLogCellRenderer renderer;

	/** 日志最大尺寸 **/
	private volatile int maxItems;

	/** 日志列表 **/
	private JList list = new JList();

	/** 日志列表 **/
	private DefaultListModel model = new DefaultListModel();

	/** 禁止显示日志 **/
	private volatile boolean forbid;

	/** 禁止显示日志按纽 **/
	private JCheckBox cmdForbid= new JCheckBox();

	/**
	 * 构造日志显示面板
	 */
	public WatchMixedLogPanel() {
		super();
		// 规定的最大日志数目
		setMaxItems(MAX_ELEMENTS);

		// 默认是假
		forbid = false;
	}

	/**
	 * 加入线程
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
			model.clear();
			list.removeAll();
		} else {
			forbid = false;
		}
		// 拒绝显示日志
		WatchProperties.writeLogForbid(forbid);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new ForbidThread(e));
	}

	/**
	 * 设置最大显示单元数目。在规定范围内！
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
//			clear();
			
			// 清除
			addThread(new ClearThread());
		}
		// 显示日志
		addThread(new NumberThread());
		// 返回结果
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
	 * 判断是拒绝显示日志
	 * @return 真或者假
	 */
	public boolean isForbid() {
		return forbid;
	}
	
	/**
	 * 显示文本
	 */
	private void setLogTooltip() {
		String text = WatchLauncher.getInstance().findCaption("Window/LogPanel/Number/title");
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
	 * 顶部日志按纽
	 * @return
	 */
	private JPanel createNorth() {
		// 按纽
		String text = WatchLauncher.getInstance().findCaption("Window/LogPanel/Forbid/title");
		cmdForbid.setText(text);
		cmdForbid.addActionListener(this);

		// 判断是拒绝
		Boolean b = WatchProperties.readLogForbid();
		if (b != null) {
			forbid = b.booleanValue();
		} else {
			forbid = true; // 如果没有定义，默认是真，不显示日志
		}
		cmdForbid.setSelected(forbid);

		// 面板
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 0));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 3, 0, 3));
		panel.add(cmdForbid, BorderLayout.WEST);
		panel.add(new JPanel(), BorderLayout.CENTER);
		return panel;
	}

	/**
	 * 日志记录
	 * @return
	 */
	private JScrollPane createCenter(){
		renderer = new WatchMixedLogCellRenderer();
		list.setCellRenderer(renderer);
		list.setModel(model);

		// 取出XML中的参数
		String tooltip = WatchLauncher.getInstance().findCaption("Window/LogPanel/title");

		// 行高度
		String value = WatchLauncher.getInstance().findCaption("Window/LogPanel/row-height");
		int rowHeight = ConfigParser.splitInteger(value, 30);

		list.setFixedCellHeight(rowHeight);
		FontKit.setToolTipText(list, tooltip);
		list.setBorder(new EmptyBorder(3, 2, 2, 2));
		// 支持多选
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// 修正字体
		__exchangeFont(WatchProperties.readTabbedLogFont());
		// 最大日志数目
		Integer max = WatchProperties.readLogElements();
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
	//		renderer = new WatchMixedLogCellRenderer();
	//		list.setCellRenderer(renderer);
	//		list.setModel(model);
	//
	//		// 取出XML中的参数
	//		String tooltip = WatchLauncher.getInstance().findCaption("Window/LogPanel/title");
	//
	//		// 行高度
	//		String value = WatchLauncher.getInstance().findCaption("Window/LogPanel/row-height");
	//		int rowHeight = ConfigParser.splitInteger(value, 30);
	//
	//		list.setFixedCellHeight(rowHeight);
	//		FontKit.setToolTipText(list, tooltip);
	//		list.setBorder(new EmptyBorder(3, 2, 2, 2));
	//		// 支持多选
	//		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	//
	//		// 修正字体
	//		__exchangeFont(WatchProperties.readTabbedLogFont());
	//		// 最大日志数目
	//		Integer max = WatchProperties.readLogElements();
	//		if (max != null) {
	//			setMaxItems(max.intValue());
	//		}
	//		
	//		JScrollPane scroll = new JScrollPane(list);
	//		FontKit.setToolTipText(scroll, tooltip);
	//		
	//		// 窗口布局!
	//		setLayout(new BorderLayout(0, 0));
	//		add(scroll, BorderLayout.CENTER);
	//	}

	/**
	 * 返回选择的字体
	 * @return
	 */
	public Font getSelectFont() {
		return list.getFont();
	}

	/**
	 * 设置选择的字体
	 * @param font
	 */
	public void setSelectFont(Font font) {
		addThread(new FontThread(font));
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
		addThread(new PushLogThread(logs));
	}

	/**
	 * 修正字体
	 * @param font
	 */
	private void __exchangeFont(Font font) {
		if (font != null) {
			list.setFont(font);
			cmdForbid.setFont(font);
		}
	}

	/**
	 * 字体线程
	 * @author scott.liang
	 * @version 1.0 8/28/2018
	 * @since laxcus 1.0
	 */
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

	/**
	 * 在图形窗口显示日志
	 * @param logs 日志集合
	 */
	private void printLogs(java.util.List<LogItem> logs) {
		final int modelSize = model.size();

		// 如果屏蔽日志，或者日志数目小于1时，清除旧记录，新是日志忽略！
		if (forbid || maxItems < 1) {
			if (modelSize > 0) {
				//				clear(); // 清除记录

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

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		if (renderer != null) {
			renderer.updateUI();
		}
	}


}