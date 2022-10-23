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

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.laxcus.front.terminal.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.tub.servlet.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 边缘应用软件明细窗口。
 * 
 * @author scott.liang
 * @version 1.0 1/12/2020
 * @since laxcus 1.0
 */
public class TerminalLocalTubDetailPanel extends TerminalPanel {

	private static final long serialVersionUID = -1000171773451349053L;

	private TerminalLocalTubPanel parent;

	/** 渲染器 **/
	private TerminalLocalBrowserDetailCellRenderer renderer;

	/** 表格模型 **/
	private TerminalLocalBrowserDetailModel tableModel;

	/** 显示表格 **/
	private JTable table;

	/** 锚定实例 **/
	private TubTag attach;
	
	/** 容器单元 **/
	private String tagTubName;
	private String tagTubCaption;
	private String tagTubClass;
	private String tagTubCount;
	private String tagTubPID;


	/**
	 * 构造边缘应用软件明细窗口
	 */
	public TerminalLocalTubDetailPanel() {
		super();
	}

	/**
	 * 设置父类面板
	 * @param e
	 */
	public void setParentPanel(TerminalLocalTubPanel e) {
		parent = e;
	}

	/**
	 * 返回父类面板
	 * @return
	 */
	public TerminalLocalTubPanel getParentPanel() {
		return parent;
	}

	/**
	 * 加入线程
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 切换用户签名
	 * @param tubTag
	 */
	public void exchange(TubTag tubTag) {
		addThread(new ExchangeThread(tubTag));
	}

	/**
	 * 清除记录
	 */
	public void clear() {
		addThread(new ClearThread());
	}

	/**
	 * 删除用户
	 * @param tubTag
	 */
	public void update(TubTag tubTag) {
		addThread(new UpdateThread(tubTag));
	}

	/**
	 * 设置当前字体
	 * @param e
	 */
	public void setSelectFont(Font e) {
		addThread(new FontThread(e));
	}

//	/**
//	 * 生成一行显示单元
//	 * @param prefix
//	 * @param node
//	 * @return
//	 */
//	private ShowItem createSite(String prefix, Node node) {
//		ShowItem item = new ShowItem();
//		item.add(new ShowStringCell(0, prefix, prefix));
//		String str = node.toString();
//		item.add(new ShowStringCell(1, str, str));
//		return item;
//	}
//
//	/**
//	 * 生成签名单元
//	 * @param prefix
//	 * @param str
//	 * @return
//	 */
//	private ShowItem createSiger(String prefix, String str) {
//		ShowItem item = new ShowItem();
//		item.add(new ShowStringCell(0, prefix, prefix));
//		item.add(new ShowStringCell(1, str, str));
//		return item;
//	}

	/**
	 * 判断当前对象是绑定的对象
	 * @param object 传入对象
	 * @return 匹配返回真，否则假
	 */
	public boolean isAttachObject(Object object) {
		if (attach == null || object == null) {
			return false;
		}
		// 判断对象一致
		if (attach.getClass() == object.getClass()) {
			return (Laxkit.compareTo((TubTag) attach, (TubTag) object) == 0);
		}
		return false;
	}
	
	/**
	 * 产生一行记录
	 * @param prefix
	 * @param str
	 * @return
	 */
	private ShowItem createItem(String prefix, String str, Color foreground) {
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, prefix, prefix));
		ShowStringCell value = new ShowStringCell(1, str, str);
		if (foreground != null) {
			value.setForeground(foreground);
		}
		item.add(value);
		return item;
	}

	/**
	 * 生成一行记录
	 * @param prefix
	 * @param str
	 * @return
	 */
	private ShowItem createItem(String prefix, String str) {
		return createItem(prefix, str, null);
	}
	
	/**
	 * 切换显示
	 * @param tag
	 */
	void __exchange(TubTag tag) {
		// 清除
		__clear();
		
		java.util.List<TubToken> tokens = TubPool.getInstance().findTubs(tag.getNaming());
		int count = tokens.size();
		
		tableModel.addRow(createItem(tagTubName, tag.getNaming().toString()));
		tableModel.addRow(createItem(tagTubCaption, tag.getCaption()));
		tableModel.addRow(createItem(tagTubClass, tag.getClassName()));
		// 统计数量
		tableModel.addRow(createItem(tagTubCount, String.valueOf(count)));

		// 进程号
		if (tokens.size() > 0) {
			tableModel.addRow(createItem(" ", " "));
			for (int i = 0; i < tokens.size(); i++) {
				TubToken token = tokens.get(i);
				long pid = token.getId();
				tableModel.addRow(createItem(tagTubPID, String.valueOf(pid)));
			}
		}

		// 记录签名
		attach = tag;
	}

	/**
	 * 清除记录，但是不清除列
	 */
	void __clear() {
		tableModel.clear();
		attach = null;
	}

	/**
	 * 更新，必须是在签名一致的情况下!
	 * @param tubTag
	 */
	void __update(TubTag tubTag) {
		// 判断一致！
		boolean success = (attach != null && Laxkit.compareTo(tubTag, attach) == 0);
		if (success) {
			__exchange(tubTag);
		}
	}

	class ExchangeThread extends SwingEvent {
		TubTag tubTag;
		ExchangeThread(TubTag e) {
			super();
			tubTag = e;
		}

		public void process() {
			__exchange(tubTag);
		}
	}

	class ClearThread extends SwingEvent {
		ClearThread() {
			super();
		}

		public void process() {
			__clear();
		}
	}

	class UpdateThread extends SwingEvent {
		TubTag tubTag;

		UpdateThread(TubTag e) {
			super();
			tubTag = e;
		}

		public void process() {
			__update(tubTag);
		}
	}

	/**
	 * 修改表格和表头的字体
	 * @param font
	 */
	private void __exchangeFont(Font font) {
		if (font != null) {
			// 表头字体，WINDOWS固定12磅, LINUX 14
			Font sub = UITools.createHeaderFont(font);
			table.getTableHeader().setFont(sub);
			// 设置字体
			table.setFont(font);
		}
	}

	/**
	 * 字体线程
	 *
	 * @author scott.liang
	 * @version 1.0 1/15/2020
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

	//	/**
	//	 * 初始化面板，不要定义背景颜色
	//	 */
	//	public void init() {
	//		initTable();
	//
	//		JScrollPane scroll = new JScrollPane(table);
	////		scroll.getViewport().setBackground(Color.WHITE);
	//		setLayout(new BorderLayout(0, 0));
	//		add(scroll, BorderLayout.CENTER);
	//	}

	/**
	 * 初始化表头
	 */
	private void initHeader() {
		// 属性/参数行的宽度
		String value = findCaption("Window/LocalHubDetailPanel/key-width");
		int key_width = ConfigParser.splitInteger(value, 42);
		value =  findCaption("Window/LocalHubDetailPanel/value-width");
		int value_width = ConfigParser.splitInteger(value, 130);

		String keyText =  findCaption("Window/LocalHubDetailPanel/Header/Key/title");
		String valueText = findCaption("Window/LocalHubDetailPanel/Header/Value/title");

		// 标题！
		ShowTitle title = new ShowTitle();
		title.add(new ShowTitleCell(0, keyText, key_width));
		title.add(new ShowTitleCell(1, valueText, value_width));

		//		// 显示标题
		//		TableColumn[] columns = title.createTableColumns();
		//		for (int i = 0; i < columns.length; i++) {
		//			String name = title.get(i).getName();
		//			columns[i].setHeaderRenderer(new TerminalLocalBrowserDetailHeaderCellRenderer(name));
		////			columns[i].setHeaderRenderer(new DefaultTableCellRenderer());
		//			table.addColumn(columns[i]);
		//		}

		int count = title.size();
		for (int i = 0; i < count; i++) {
			ShowTitleCell cell = title.get(i);
			TableColumn column = new TableColumn(cell.getIndex(), cell.getWidth());
			column.setHeaderValue(cell.getName());
			// 保存
			table.addColumn(column);
		}

		// 保存标题
		tableModel.setTitle(title);
	}

	/**
	 * 初始化表格
	 */
	public void init() {
		//  取配置参数
		String titleText =  findCaption("Window/LocalHubDetailPanel/title");
		FontKit.setToolTipText(this, titleText);

		// 行高度
		String value =  findCaption("Window/LocalHubDetailPanel/row-height");
		int rowHeight = ConfigParser.splitInteger(value, 30);
		// 表头高度
		value =  findCaption("Window/LocalHubDetailPanel/header-height");
		int headerHeight = ConfigParser.splitInteger(value, 28);

		// 表模型
		tableModel = new TerminalLocalBrowserDetailModel();
		table = new JTable(tableModel);
		// 渲染器只支持ShowItemCell类
		renderer = new TerminalLocalBrowserDetailCellRenderer();
		table.setDefaultRenderer(ShowItemCell.class, renderer);
		// 初始化表头
		initHeader();

		// 表格单元高度与高度，宽度与宽度之间的间距
		UITools.setTableIntercellSpacing(table);


		//		// 设置基础参数
		//		table.setModel(tableModel);

		//		table.setTableHeader(new HeightTableHeader(table.getColumnModel(), headerHeight));

		table.setRowHeight(rowHeight); // 行高度
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowSelectionAllowed(true);
		table.setShowGrid(true);
		table.getTableHeader().setReorderingAllowed(true);
		table.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//		table.setIntercellSpacing(new Dimension(3, 3));
		//		table.setColumnSelectionAllowed(true);
		table.setSurrendersFocusOnKeystroke(true);
		table.setBorder(new EmptyBorder(1, 1, 1, 1));

		//		// 所有支持OBJECT类都使用这个渲染器
		//		renderer = new TerminalLocalBrowserDetailRowCellRenderer();
		//		table.setDefaultRenderer(Object.class, renderer);
		//		// 将 rowMargin 和 columnMargin（单元格之间间距的高度和宽度）设置为 intercellSpacing
		//		table.setIntercellSpacing(new Dimension(0, 0)); 

		//		// 设置表头的最优范围
		//		table.getTableHeader().setPreferredSize(new Dimension(20, headerHeight));

		//		// 初始化表头
		//		initHeader();
		//		
		//		System.out.printf("head renderer is %s\n", columns[0].getHeaderRenderer().getClass().getName());

		//		TableColumn es = table.getColumnModel().getColumn(0);
		//		if(es != null) {
		//			TableCellRenderer ss = es.getHeaderRenderer();
		//			System.out.printf("head renderer name is %s\n", (ss== null ? "null" : ss.getClass().getName()));
		//		}

		// 修正字体
		__exchangeFont(TerminalProperties.readLocalBrowserFont());

		// 滚动栏
		JScrollPane scroll = new JScrollPane(table);
		FontKit.setToolTipText(scroll, titleText);
		scroll.setColumnHeader(new HeightViewport(headerHeight)); // 修正JAVA BUG，显示正确的表头高度，保证不出错！
		// 界面布局！
		setLayout(new BorderLayout(0, 0));
		add(scroll, BorderLayout.CENTER);

		// 信息
		tagTubName = findCaption("Window/LocalHubDetailPanel/Tub/Name/title");
		tagTubCaption = findCaption("Window/LocalHubDetailPanel/Tub/Caption/title");
		tagTubClass = findCaption("Window/LocalHubDetailPanel/Tub/Class/title");
		tagTubCount = findCaption("Window/LocalHubDetailPanel/Tub/Count/title");
		tagTubPID = findCaption("Window/LocalHubDetailPanel/Tub/PID/title");
		
//		register_onlineText = findCaption("Window/LocalHubDetailPanel/Register-Online/title");
//		onlineText = findCaption("Window/LocalHubDetailPanel/Online/title");

		// 定位复制键
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

	//	/**
	//	 * 初始化表格
	//	 */
	//	private void initTable() {
	//		//  取配置参数
	//		String titleText = findCaption("Window/LocalHubDetailPanel/title");
	//		FontKit.setToolTipText(this, titleText);
	//		
	//		// 行高度
	//		String value = findCaption("Window/LocalHubDetailPanel/row-height");
	//		int rowHeight = ConfigParser.splitInteger(value, 30);
	//		// 表头高度
	//		value = findCaption("Window/LocalHubDetailPanel/header-height");
	//		int headerHeight = ConfigParser.splitInteger(value, 28);
	//		// 属性/参数行的宽度
	//		value = findCaption("Window/LocalHubDetailPanel/key-width");
	//		int key_width = ConfigParser.splitInteger(value, 42);
	//		value = findCaption("Window/LocalHubDetailPanel/value-width");
	//		int value_width = ConfigParser.splitInteger(value, 130);
	//		
	//		// 设置基础参数
	//		table.setModel(tableModel);
	//		table.setRowHeight(rowHeight); // 行高度
	//		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	//		table.setRowSelectionAllowed(true);
	//		table.setShowGrid(true);
	//		table.getTableHeader().setReorderingAllowed(true);
	//		table.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	//		table.setIntercellSpacing(new Dimension(3, 3));
	////		table.setColumnSelectionAllowed(true);
	//		table.setSurrendersFocusOnKeystroke(true);
	//		table.setBorder(new EmptyBorder(1, 1, 1, 1));
	//
	//		// 所有支持OBJECT类都使用这个渲染器
	//		table.setDefaultRenderer(Object.class, new TerminalLocalBrowserDetailRowCellRenderer());
	//		
	//		// 设置表头的最优范围
	//		table.getTableHeader().setPreferredSize(new Dimension(key_width, headerHeight));
	//		
	//		String keyText = findCaption("Window/LocalHubDetailPanel/Header/Key/title");
	//		String valueText = findCaption("Window/LocalHubDetailPanel/Header/Value/title");
	//		
	//		// 标题！
	//		ShowTitle title = new ShowTitle();
	//		title.add(new ShowTitleCell(0, keyText, key_width));
	//		title.add(new ShowTitleCell(1, valueText, value_width));
	//		
	//		// 显示标题
	//		TableColumn[] columns = title.createTableColumns();
	//		for (int i = 0; i < columns.length; i++) {
	//			String name = title.get(i).getName();
	//			columns[i].setHeaderRenderer(new TerminalLocalBrowserDetailHeaderCellRenderer(name));
	////			columns[i].setHeaderRenderer(new DefaultTableCellRenderer());
	//			table.addColumn(columns[i]);
	//		}
	//		// 保存标题
	//		tableModel.setTitle(title);
	//		
	////		System.out.printf("head renderer is %s\n", columns[0].getHeaderRenderer().getClass().getName());
	//		
	////		TableColumn es =	table.getColumnModel().getColumn(0);
	////		if(es != null) {
	////			TableCellRenderer ss = es.getHeaderRenderer();
	////			System.out.printf("head renderer name is %s\n", (ss== null ? "null" : ss.getClass().getName()));
	////		}
	//		
	//		// 信息
	//		sigerText = findCaption("Window/LocalHubDetailPanel/Siger/title");
	//		registerText =	findCaption("Window/LocalHubDetailPanel/Register/title");
	//		register_onlineText = findCaption("Window/LocalHubDetailPanel/Register-Online/title");
	//		onlineText = findCaption("Window/LocalHubDetailPanel/Online/title");
	//
	//		// 定位复制键
	//		table.getInputMap(JComponent.WHEN_FOCUSED).put(
	//				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK, true), "CTRL C");
	//		table.getActionMap().put("CTRL C", new AbstractAction() {
	//
	//			private static final long serialVersionUID = 1L;
	//
	//			@Override
	//			public void actionPerformed(ActionEvent e) {
	//				// 放入SWING队列
	//				ClipboardCopyThread thread = new ClipboardCopyThread();
	//				addThread(thread);
	//			}
	//		});
	//		
	//		// 读取定义字体，更新！
	//		Font font = TerminalProperties.readBrowserLocalFont();
	//		if (font != null) {
	//			setSelectFont(font);
	//		}
	//	}

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
	 * 复制内容到剪贴板
	 */
	private void copyToClipboard() {
		// 选中的行
		int[] rows = table.getSelectedRows();
		for (int i = 0; i < rows.length; i++) {
			rows[i] = table.convertRowIndexToModel(rows[i]);
		}
		// 选中的列
		int[] columns = table.getSelectedColumns();
		// 保存的缓存
		StringBuilder buff = new StringBuilder();
		for (int row : rows) {
			StringBuilder bf = new StringBuilder();
			for (int column : columns) {
				column = table.convertColumnIndexToModel(column);

				// 两个空格
				if (bf.length() > 0) bf.append("  ");

				ShowItemCell cell = tableModel.getCellAt(row, column);
				if (Laxkit.isClassFrom(cell, ShowImageCell.class)) {
					Object node = ((ShowImageCell) cell).getSymbol();
					if (node != null) bf.append(node.toString());
				} else {
					Object str = cell.visible();
					if (Laxkit.isClassFrom(str, String.class)) {
						bf.append((String) str);
					}
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

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		// 重新绘制
		if (renderer != null) {
			renderer.updateUI();
		}
		// 表格单元高度与高度，宽度与宽度之间的间距
		UITools.setTableIntercellSpacing(table);
	}

}