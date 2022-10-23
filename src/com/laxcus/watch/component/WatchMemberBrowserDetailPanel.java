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

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;
import com.laxcus.watch.*;
import com.laxcus.watch.pool.*;

/**
 * 用户注册/登录游览窗口。
 * 
 * @author scott.liang
 * @version 1.0 1/12/2020
 * @since laxcus 1.0
 */
public class WatchMemberBrowserDetailPanel extends JPanel {

	private static final long serialVersionUID = -1000171773451349053L;

	private WatchMemberBrowserPanel parent;

	/** 渲染器 **/
	private WatchMemberBrowserDetailCellRenderer renderer;

	/** 表格模型 **/
	private WatchMemberBrowserDetailModel tableModel;

	/** 显示表格 **/
	private JTable table;

	/** 当前注册签名 **/
	private Siger master;

	/** 标记: 用户签名、用户人数、注册地址/注册在线地址/FRONT地址 **/
	private String sigerText;
	private String personsText;

	private String registerText;
	private String register_onlineText;

	private String onlineText;
	private String frontText;

	/**
	 * 构造用户注册/登录游览窗口
	 */
	public WatchMemberBrowserDetailPanel() {
		super();
	}

	/**
	 * 设置父类面板
	 * @param e
	 */
	public void setParntPanel(WatchMemberBrowserPanel e) {
		parent = e;
	}

	/**
	 * 返回父类面板
	 * @return
	 */
	public WatchMemberBrowserPanel getParntPanel() {
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
	 * @param siger
	 */
	public void exchange(Siger siger) {
		addThread(new ExchangeThread(siger));
	}

	/**
	 * 清除记录
	 */
	public void clear() {
		addThread(new ClearThread());
	}

	/**
	 * 更新用户状态
	 * @param siger
	 */
	public void update(Siger siger) {
		addThread(new UpdateThread(siger));
	}

	/**
	 * 设置当前字体
	 * @param e
	 */
	public void setSelectFont(Font e) {
		addThread(new FontThread(e));
	}

	/**
	 * 生成一行显示单元
	 * @param prefix
	 * @param node
	 * @return
	 */
	private ShowItem createSite(String prefix, Node node) {
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, prefix, prefix));
		String str = node.toString();
		item.add(new ShowStringCell(1, str, str));
		return item;
	}

	/**
	 * 生成签名单元
	 * @param prefix
	 * @param str
	 * @return
	 */
	private ShowItem createSiger(String prefix, String str) {
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, prefix, prefix));
		item.add(new ShowStringCell(1, str, str));
		return item;
	}

	/**
	 * 判断存在
	 * @param socks
	 * @param node
	 * @return
	 */
	private boolean online(java.util.List<FrontSeat> socks, Node node) {
		for (FrontSeat e : socks) {
			boolean match = (Laxkit.compareTo(e.getGateway(), node) == 0 || Laxkit
					.compareTo(e.getFront(), node) == 0);
			if (match) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 切换显示
	 * @param siger
	 */
	void __exchange(Siger siger) {
		// 清除
		__clear();

		// 如果都不存在，忽略后面的操作
		boolean b1 = RegisterMemberBasket.getInstance().contains(siger);
		boolean b2 = FrontMemberBasket.getInstance().contains(siger);
		if (!b1 && !b2) {
			return;
		}

		// 找到注册/在线地址
		java.util.List<Node> registers = RegisterMemberBasket.getInstance().find(siger);
		java.util.List<FrontSeat> onlines = FrontMemberBasket.getInstance().find(siger);
		// 排序
		java.util.Collections.sort(registers);
		java.util.Collections.sort(onlines);
		// CALL/GATE/FRONT地址（用来消重!）
		java.util.TreeSet<Node> gateways = new java.util.TreeSet<Node>();
		java.util.TreeSet<Node> fronts = new java.util.TreeSet<Node>();
		for (FrontSeat seat : onlines) {
			gateways.add(seat.getGateway());
			fronts.add(seat.getFront());
		}

		// 找到明文，或者是SHA256
		String username = RegisterMemberBasket.getInstance().findPlainText(siger);
		if (username == null) {
			username = siger.toString();
		}
		// 用户签名
		tableModel.addRow(createSiger(sigerText, username));

		// 在线人数
		int persons = fronts.size();
		tableModel.addRow(createSiger(personsText, String.valueOf(persons)));

		// 注册地址/注册在线地址
		for (Node node : registers) {
			// 判断
			boolean exists = online(onlines, node);
			if (exists) {
				ShowItem item = createSite(register_onlineText, node);
				tableModel.addRow(item);
			} else {
				ShowItem item = createSite(registerText, node);
				tableModel.addRow(item);
			}
		}
		// 在线地址
		for (Node node : gateways) {
			boolean exists = registers.contains(node);
			if (!exists) {
				ShowItem e = createSite(onlineText, node);
				tableModel.addRow(e);
			}
		}
		// FRONT地址
		for (Node node : fronts) {
			ShowItem e = createSite(frontText, node);
			tableModel.addRow(e);
		}

		// 记录签名
		master = siger;
	}

	/**
	 * 清除记录，但是不清除列
	 */
	void __clear() {
		tableModel.clear();
		master = null;
	}

	/**
	 * 更新，必须是在签名一致的情况下!
	 * @param siger
	 */
	void __update(Siger siger) {
		// 判断一致！
		boolean success = (master != null && Laxkit.compareTo(siger, master) == 0);
		if (success) {
			__exchange(siger);
		}
	}

	class ExchangeThread extends SwingEvent {
		Siger siger;
		ExchangeThread(Siger e) {
			super();
			siger = e;
		}

		public void process() {
			__exchange(siger);
		}
	}

	class ClearThread extends SwingEvent {
		ClearThread() { super(); }
		public void process() {
			__clear();
		}
	}

	class UpdateThread extends SwingEvent {
		Siger siger;

		UpdateThread(Siger e) {
			super();
			siger = e;
		}

		public void process() {
			__update(siger);
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

	/**
	 * 初始化表头
	 */
	private void initHeader() {
		// 属性/参数行的宽度
		String value = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/key-width");
		int key_width = ConfigParser.splitInteger(value, 42);
		value = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/value-width");
		int value_width = ConfigParser.splitInteger(value, 130);

		String keyText = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/Header/Key/title");
		String valueText = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/Header/Value/title");

		// 标题！
		ShowTitle title = new ShowTitle();
		title.add(new ShowTitleCell(0, keyText, key_width));
		title.add(new ShowTitleCell(1, valueText, value_width));

		//		// 显示标题
		//		TableColumn[] columns = title.createTableColumns();
		//		for (int i = 0; i < columns.length; i++) {
		//			String name = title.get(i).getName();
		//			columns[i].setHeaderRenderer(new WatchMemberBrowserDetailHeaderCellRenderer(name));
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
		String titleText = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/title");
		FontKit.setToolTipText(this, titleText);

		// 行高度
		String value = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/row-height");
		int rowHeight = ConfigParser.splitInteger(value, 30);
		// 表头高度
		value = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/header-height");
		int headerHeight = ConfigParser.splitInteger(value, 28);

		// 表模型
		tableModel = new WatchMemberBrowserDetailModel();
		table = new JTable(tableModel);
		// 渲染器只支持ShowItemCell类
		renderer = new WatchMemberBrowserDetailCellRenderer();
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
		//		renderer = new WatchMemberBrowserDetailRowCellRenderer();
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
		__exchangeFont(WatchProperties.readBrowserMemberFont());

		// 滚动栏
		JScrollPane scroll = new JScrollPane(table);
		FontKit.setToolTipText(scroll, titleText);
		scroll.setColumnHeader(new HeightViewport(headerHeight));

		// 界面布局！
		setLayout(new BorderLayout(0, 0));
		add(scroll, BorderLayout.CENTER);

		// 信息
		sigerText = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/Siger/title");
		personsText = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/Persons/title");
		registerText =	WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/Register/title");
		register_onlineText = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/Register-Online/title");
		onlineText = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/Online/title");
		frontText = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/Front/title");

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
	//		String titleText = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/title");
	//		FontKit.setToolTipText(this, titleText);
	//		
	//		// 行高度
	//		String value = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/row-height");
	//		int rowHeight = ConfigParser.splitInteger(value, 30);
	//		// 表头高度
	//		value = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/header-height");
	//		int headerHeight = ConfigParser.splitInteger(value, 28);
	//		// 属性/参数行的宽度
	//		value = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/key-width");
	//		int key_width = ConfigParser.splitInteger(value, 42);
	//		value = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/value-width");
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
	//		table.setDefaultRenderer(Object.class, new WatchMemberBrowserDetailRowCellRenderer());
	//		
	//		// 设置表头的最优范围
	//		table.getTableHeader().setPreferredSize(new Dimension(key_width, headerHeight));
	//		
	//		String keyText = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/Header/Key/title");
	//		String valueText = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/Header/Value/title");
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
	//			columns[i].setHeaderRenderer(new WatchMemberBrowserDetailHeaderCellRenderer(name));
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
	//		sigerText = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/Siger/title");
	//		registerText =	WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/Register/title");
	//		register_onlineText = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/Register-Online/title");
	//		onlineText = WatchLauncher.getInstance().findCaption("Window/MemberBrowserDetailPanel/Online/title");
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
	//		Font font = WatchProperties.readBrowserMemberFont();
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