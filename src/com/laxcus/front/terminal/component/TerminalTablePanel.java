/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.laxcus.front.terminal.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 二维表面板。<br>
 * 
 * 用于显示表格型数据信息。
 * 
 * @author scott.liang
 * @version 1.0 05/06/2009
 * @since laxcus 1.0
 */
public class TerminalTablePanel extends JPanel {

	private static final long serialVersionUID = 6347934141080774995L;
	
	/** 渲染器 **/
	private TerminalTableCellRenderer renderer;

	/** 终端表格模型 **/
	private TerminalTableModel model; // = new TerminalTableModel();

	/** 命令处理的显示表格 **/
	private JTable table; // = new JTable();
	
//	/** 终端表格模型 **/
//	private TerminalTableModel model = new TerminalTableModel();
//
//	/** 命令处理的显示表格 **/
//	private JTable table = new JTable();

	/**
	 * 构造二维表面板
	 */
	public TerminalTablePanel() {
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
	 * 清除表格
	 */
	private void doClear() {
		// 删除记录
		model.clear();
		// 删除标题
		TableColumnModel columnModel = table.getColumnModel();

		ArrayList<TableColumn> a = new ArrayList<TableColumn>();
		Enumeration<TableColumn> em = columnModel.getColumns();
		while (em.hasMoreElements()) {
			a.add(em.nextElement());
		}
		for (TableColumn e : a) {
			columnModel.removeColumn(e);
		}
	}

	class ClearThread extends SwingEvent {
		ClearThread() { super(); }
		public void process() {
			doClear();
		}
	}
	
	/**
	 * 标题线程
	 */
	class InfluxTableThread extends SwingEvent {
		ShowTitle title;

		Collection<ShowItem> items;

		InfluxTableThread(ShowTitle e, Collection<ShowItem> a) {
			super();
			title = e;
			items = a;
		}

		public void process() {
			// 清除表格
			doClear();
			// 表格
			TableColumn[] columns = title.createTableColumns();
			for (int i = 0; i < columns.length; i++) {
				table.addColumn(columns[i]);
			}
			// 保存标题
			model.setTitle(title);
			// 添加单元
			if (items != null) {
				for (ShowItem item : items) {
					model.addRow(item);
				}
			}
		}
	}

	/**
	 * 标题线程
	 */
	class InfluxTitleThread extends SwingEvent {
		ShowTitle title;

		InfluxTitleThread(ShowTitle e) {
			super();
			title = e;
		}

		public void process() {
			TableColumn[] columns = title.createTableColumns();
			for (int i = 0; i < columns.length; i++) {
				table.addColumn(columns[i]);
			}
			// 保存标题
			model.setTitle(title);
		}
	}

	/**
	 * 行数据线程
	 */
	class InfluxItemThread extends SwingEvent {
		ShowItem item;

		InfluxItemThread(ShowItem a) {
			super();
			item = a;
		}

		public void process() {
			if (item.size() != table.getColumnCount()) {
				throw new IllegalValueException("Not Match! %d != %d",
						item.size(), table.getColumnCount());
			}
			model.addRow(item);
		}
	}

	/**
	 * 复制参数到系统剪贴板
	 */
	class ClipboardCopyThread extends SwingEvent {

		ClipboardCopyThread() {
			super();
		}

		public void process() {
			copyToClipboard();
		}
	}

	/**
	 * 建立档案栏
	 */
	public void init() {
		String tooltip = TerminalLauncher.getInstance().findCaption("Window/TablePanel/title");
		
		// 行高度
		String value = TerminalLauncher.getInstance().findCaption("Window/TablePanel/row-height");
		int rowHeight = ConfigParser.splitInteger(value, 30);
		// 表头高度
		value = TerminalLauncher.getInstance().findCaption("Window/TablePanel/header-height");
		int headerHeight = ConfigParser.splitInteger(value, 28);
		
		// 构造表
		model = new TerminalTableModel();
		table = new JTable(model);
		// 构造渲染器
		renderer = new TerminalTableCellRenderer();
		// 所有支持OBJECT类都使用这个渲染器
		table.setDefaultRenderer(ShowItemCell.class, renderer);
		// 表格单元高度与高度，宽度与宽度之间的间距
		UITools.setTableIntercellSpacing(table);

//		// 设置模型！
//		table.setModel(model);
//		// 构造渲染器
//		renderer = new TerminalTableCellRenderer();
//		// 所有支持OBJECT类都使用这个渲染器
//		table.setDefaultRenderer(Object.class, renderer);
//		// 将 rowMargin 和 columnMargin（单元格之间间距的高度和宽度）设置为 intercellSpacing
//		table.setIntercellSpacing(new Dimension(0, 0)); 

		table.setRowHeight(rowHeight); // 行高度
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowSelectionAllowed(true);
		table.setShowGrid(true);
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		table.setIntercellSpacing(new Dimension(3, 3));
		table.setColumnSelectionAllowed(true);
		table.setSurrendersFocusOnKeystroke(true);
		FontKit.setToolTipText(table, tooltip);
		table.setBorder(new EmptyBorder(2, 2, 2, 2));
		
		
//		// 设置表头的最优范围，主要是定义高度
//		table.getTableHeader().setPreferredSize(new Dimension(20, headerHeight));
		
//		// 判断字体
//		Font font = FontKit.findFont(table.getFont(), tooltip);
//		if(font != null && font.equals(table.getFont())) {
//			table.setFont(font);
//		}
		
		// 调整字体!
		__exchangeFont(TerminalProperties.readTabbedTableFont());

		// 滚动框
		JScrollPane scroll = new JScrollPane(table);
		scroll.setColumnHeader(new HeightViewport(headerHeight)); // 修正JAVA BUG，正确显示表头高度！
		FontKit.setToolTipText(scroll, tooltip);
		// 当前面板布局
		setLayout(new BorderLayout());
		add(scroll, BorderLayout.CENTER);

		// 截获复制操作
		table.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK, true), "CTRL C");
		table.getActionMap().put("CTRL C", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// 放入SWING队列
				ClipboardCopyThread thread = new ClipboardCopyThread();
				addThread(thread);
			}
		});
	}

	/**
	 * 复制内容到剪贴板
	 */
	private void copyToClipboard() {
		int[] rows = table.getSelectedRows();
		int[] columns = table.getSelectedColumns();
		StringBuilder buff = new StringBuilder();
		for (int row : rows) {
			StringBuilder bf = new StringBuilder();
			for (int column : columns) {
				if (bf.length() > 0) bf.append(" ");

				ShowItemCell cell = model.getCellAt(row, column);
				Object str = cell.visible();
				if (Laxkit.isClassFrom(str, String.class)) {
					bf.append((String) str);
				}
			}
			if (buff.length() > 0) buff.append("\r\n");
			buff.append(bf.toString());
		}

		// 复制到系统剪贴板
		Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transfer = new StringSelection(buff.toString());
		board.setContents(transfer, null);
	}

	/**
	 * 清除全部数据
	 */
	public void clear() {
		// 放入SWING组件队列
		addThread(new ClearThread());
	}

	/**
	 * 返回字体
	 * @return
	 */
	public Font getSelectFont() {
		return table.getFont();
	}

	/**
	 * 设置字体
	 * @param font
	 */
	public void setSelectFont(Font font) {
		addThread(new FontThread(font));
	}
	
	/**
	 * 调整字体
	 * 
	 * @param font
	 */
	private void __exchangeFont(Font font) {
		if (font != null) {
			// 标题字体，WINDOWS固定12尺寸,LINUX 14
			Font sub = UITools.createHeaderFont(font);
			table.getTableHeader().setFont(sub);
			// 表格字体
			table.setFont(font);
		}
	}

	/**
	 * 字体线程
	 *
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

	/**
	 * 返回标题单元数目
	 * @return 标题单元数目
	 */
	public int getTitleCellCount() {
		return table.getColumnCount();
	}

	/**
	 * 设置标题
	 * @param title
	 */
	public void setTitle(ShowTitle title) {
		// 清除全部旧记录
		clear();

		// 生成线程
		InfluxTitleThread thread = new InfluxTitleThread(title);
		// 交给异步线程处理，如果有错误，改为异步执行。
		addThread(thread);
	}

	/**
	 * 增加一行显示单元
	 * @param item
	 */
	public void addItem(ShowItem item) {
		Laxkit.nullabled(item);

		// 放入SWING异步队列
		InfluxItemThread thread = new InfluxItemThread(item);
		addThread(thread);
	}

	/**
	 * 显示参数
	 * @param title
	 * @param items
	 */
	public void showTable(ShowTitle title, Collection<ShowItem> items) {
		// 生成线程
		InfluxTableThread thread = new InfluxTableThread(title, items);
		addThread(thread);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		if (renderer != null) {
			renderer.updateUI();
		}
		super.updateUI();
		
		// 表格单元高度与高度，宽度与宽度之间的间距
		UITools.setTableIntercellSpacing(table);
	}

}