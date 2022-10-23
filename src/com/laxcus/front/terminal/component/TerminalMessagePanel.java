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
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;

/**
 * 消息提示面板。在右侧下方的选择页中
 * 因为SWING组件不是线程安全的，所以读写操作要放入SWING操作队列中处理。
 * 
 * @author scott.liang
 * @version 1.0 8/23/2009
 * @since laxcus 1.0
 */
public class TerminalMessagePanel extends JPanel {

	private static final long serialVersionUID = 4389975218023288488L;
	
	/** 渲染器 **/
	private TerminalMessageCellRenderer renderer;

	/** 列表框 **/
	private JList list = new JList();

	/** 显示模型 **/
	private DefaultListModel model = new DefaultListModel();

	/**
	 * 构造消息提示面板
	 */
	public TerminalMessagePanel() {
		super();
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
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
	 * 修改显示字体
	 * @param font
	 */
	private void __exchangeFont(Font font) {
		if (font != null) {
			list.setFont(font);
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
	 * 初始化
	 */
	public void init() {
		renderer = new TerminalMessageCellRenderer ();
		list.setCellRenderer(renderer);
		list.setModel(model);
		
		String title = TerminalLauncher.getInstance().findCaption("Window/PromptPanel/title");
		// 行高度
		String value = TerminalLauncher.getInstance().findCaption("Window/PromptPanel/row-height");
		int rowHeight = ConfigParser.splitInteger(value, 30);
		
//		// -1是让第一个显示单元自动选择高度
//		list.setFixedCellHeight(-1);
		
		list.setFixedCellHeight(rowHeight);
		// 工具提示
		FontKit.setToolTipText(list,title);
		// 边框: top, left, bottom, right
		list.setBorder(new EmptyBorder(2, 2, 2, 2));
		// 多选
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		list.setEnabled(true);
		
		// 设置字体
		__exchangeFont(TerminalProperties.readTabbedMessageFont());

		JScrollPane scroll = new JScrollPane(list);
		// 工具提示
		FontKit.setToolTipText(scroll, title);

		setLayout(new BorderLayout());
		add(scroll, BorderLayout.CENTER);
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

	class InfluxNoteThread extends SwingEvent {
		NoteItem item;
		InfluxNoteThread(NoteItem e) {
			super();
			item = e;
		}
		public void process() {
			model.addElement(item);
		}
	}

	/**
	 * 清除全部旧的显示记录
	 */
	public void clear() {
		addThread(new ClearThread());
	}
	
	/**
	 * 显示一个成员，追加到SWING事件队列中
	 * @param e
	 */
	private void add(NoteItem e) {
		addThread(new InfluxNoteThread(e));
	}

	/**
	 * 保存一个显示成员
	 * @param status
	 * @param text
	 */
	public void add(int status, String text) {
		NoteItem item = new NoteItem(status, text);
		add(item);
	}

}