/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;
import com.laxcus.watch.*;

/**
 * WATCH站点二维表面板。<br><br>
 * 用于显示表格型数据信息。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 05/06/2009
 * @since laxcus 1.0
 */
public class WatchMixedTablePanel extends JPanel {

	private static final long serialVersionUID = 2764572288189544087L;
	
	/** 渲染器 **/
	private WatchMixedTableCellRenderer renderer;
	
	/** 处理结果模型，提前初始化！ **/
	private WatchMixedTableModel model;// = new WatchMixedTableModel();

	/** 命令处理的显示表格 **/
	private JTable table; // = new JTable();
	
	/** 重要说明！JTable table = new JTable(), WatchMixedTableModel model = new WatchMixedTableModel() 必须在类声明中构造！否则在显示时会出错！原因暂时不明！没有查SWING源码，可能是JAVA SWING的BUG!  **/

//	/** 处理结果模型，提前初始化！ **/
//	private WatchMixedTableModel model = new WatchMixedTableModel();
//
//	/** 命令处理的显示表格 **/
//	private JTable table = new JTable();

	/**
	 * 构造WATCH站点二维表面板
	 */
	public WatchMixedTablePanel() {
		super();
	}

	/**
	 * 加入线程
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

	//	/**
	//	 * 清除全部数据
	//	 */
	//	public void clear() {
	//		Runnable thread = new Runnable() {
	//			public void run() {
	//				// 删除记录
	//				model.clear();
	//				// 删除标题
	//				TableColumnModel columnModel = table.getColumnModel();
	//
	//				ArrayList<TableColumn> a = new ArrayList<TableColumn>();
	//				Enumeration<TableColumn> em = columnModel.getColumns();
	//				while (em.hasMoreElements()) {
	//					a.add(em.nextElement());
	//				}
	//				for (TableColumn e : a) {
	//					columnModel.removeColumn(e);
	//				}
	//			}
	//		};
	//		// 放入SWING组件队列
	//		addThread(thread);
	//	}

	/**
	 * 显示全部记录
	 */
	class InfluxTableThread extends SwingEvent {
		ShowTitle title;
		Collection<ShowItem> items;

		InfluxTableThread(ShowTitle e, Collection<ShowItem> a) {
			super();
			title = e;
			// columns = title.createTableColumns();
			items = a;
		}

		public void process() {
			// 清除表格
			doClear();

			// 显示标题
			TableColumn[] columns = title.createTableColumns();
			for (int i = 0; i < columns.length; i++) {
				table.addColumn(columns[i]);
			}
			// 保存标题
			model.setTitle(title);
			// 显示单元
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

		TableColumn[] columns;

		InfluxTitleThread(ShowTitle e) {
			super();
			title = e;
			columns = title.createTableColumns();
		}

		public void process() {
			// TableColumn[] columns = title.createTableColumns();

			// 显示标题
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
		String tooltip = WatchLauncher.getInstance().findCaption("Window/TablePanel/title");

		// 行高度
		String value = WatchLauncher.getInstance().findCaption("Window/TablePanel/row-height");
		int rowHeight = ConfigParser.splitInteger(value, 30);
		// 表头高度
		value = WatchLauncher.getInstance().findCaption("Window/TablePanel/header-height");
		int headerHeight = ConfigParser.splitInteger(value, 28);

		// 初始化参数
		model = new WatchMixedTableModel();
		table = new JTable(model);
		// 渲染器只支持ShowItemCell类
		renderer = new WatchMixedTableCellRenderer();
		table.setDefaultRenderer(ShowItemCell.class, renderer);
		
//		// 设置模型！
//		table.setModel(model);
//
//		// 所有支持OBJECT类都使用这个渲染器
//		renderer = new WatchMixedTableCellRenderer();
//		table.setDefaultRenderer(Object.class, renderer);
//		// 将 rowMargin 和 columnMargin（单元格之间间距的高度和宽度）设置为 intercellSpacing
//		table.setIntercellSpacing(new Dimension(0, 0)); 

		// 参数！
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
		table.setBorder(new EmptyBorder(2, 1, 1, 1));
		// 表格单元高度与高度，宽度与宽度之间的间距
		UITools.setTableIntercellSpacing(table);
		
		
		// 调整显示字体
		__exchangeFont(WatchProperties.readTabbedTableFont());

		// 滚动条！
		JScrollPane scroll = new JScrollPane(table);
		// 设置表头高度， 修正JTable.getTableHeader().setPreferredSize(new Dimension(20, headerHeight));产生的错误，这个错误会造成表头拖动异常！
		scroll.setColumnHeader(new HeightViewport(headerHeight)); 
		FontKit.setToolTipText(scroll, tooltip);

		// 布局！
		setLayout(new BorderLayout());
		add(scroll, BorderLayout.CENTER);

		// 截获键盘复制操作
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
	 * @param e
	 */
	public void setSelectFont(Font e) {
		addThread(new FontThread(e));
	}
	
	/**
	 * 调整显示字体
	 * @param font
	 */
	private void __exchangeFont(Font font) {
		if (font != null) {
			// 标题字体，WINDOWS固定12尺寸, LINUX 14
			Font sub = UITools.createHeaderFont(font);
			table.getTableHeader().setFont(sub);
			// 表格字体
			table.setFont(font);
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
	 * 返回标题单元数目
	 * @return 标题单元数目
	 */
	public int getTitleCellCount() {
		return table.getColumnCount();
	}

	/**
	 * 显示表格数据
	 * @param title
	 * @param items
	 */
	public void showTable(ShowTitle title, Collection<ShowItem> items) {
		// 保存到SWING队列
		InfluxTableThread thread = new InfluxTableThread(title, items);
		addThread(thread);
	}

	/**
	 * 设置标题
	 * @param title
	 */
	public void setTitle(ShowTitle title) {
		// 清除全部旧记录
		clear();

		// 以同步方式放入队列中，保证标题显示后再添加单元。如果失败，再放入SWING异步队列
		InfluxTitleThread thread = new InfluxTitleThread(title);
		// 以异步方式执行，如果有错误，再改回同步
		addThread(thread);
	}

	/**
	 * 增加一行显示单元
	 * @param item
	 */
	public void addItem(ShowItem item) {
		Laxkit.nullabled(item);

		// 放入SWING队列
		InfluxItemThread thread = new InfluxItemThread(item);
		addThread(thread);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {		
		// 重新绘制渲染器
		if (renderer != null) {
			renderer.updateUI();
		}
		super.updateUI();
		// 表格单元高度与高度，宽度与宽度之间的间距
		UITools.setTableIntercellSpacing(table);
	}

}