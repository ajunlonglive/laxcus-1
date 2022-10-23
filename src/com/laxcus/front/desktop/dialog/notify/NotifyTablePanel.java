/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.notify;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.laxcus.util.*;
import com.laxcus.util.border.*;
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
class NotifyTablePanel extends JPanel {

	private static final long serialVersionUID = 6347934141080774995L;
	
	/** 渲染器 **/
	private NotifyTableCellRenderer renderer;

	/** 终端表格模型 **/
	private NotifyTableModel model; // = new TableModel();

	/** 命令处理的显示表格 **/
	private JTable table; // = new JTable();
	
	/** 弹出菜单 **/
	private JPopupMenu rockMenu;
	

	/**
	 * 构造二维表面板
	 */
	public NotifyTablePanel() {
		super();
	}
	
	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}
	
//	private void doDeleteX() {
//		int[] rows = table.getSelectedRows();
//		for (int i = 0; i < rows.length; i++) {
//			int row = table.convertRowIndexToModel(rows[i]);
//			model.removeRow(row);
//		}
//
//		// if (rows == null || rows.length == 0) {
//		// return true;
//		// }
//		// int size = model.getRowCount();
//	}

	/**
	 * 清除表格
	 */
	private void deleteAll() {
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
			deleteAll();
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
			// 清除旧记录
			deleteAll();

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
	
	private boolean hasClear() {
		//		int rows = model.getRowCount();
		//		return rows > 0;
		// int[] rows = table.getSelectedRows();
		// return rows != null && rows.length > 0;
		//		 return model.getRowCount() > 0 || model.getColumnCount() > 0;

		return model.getRowCount() > 0;
	}
	
	private boolean hasSelectAll() {
		int size = model.getRowCount();
		if (size < 1) {
			return false;
		}
		int[] rows = table.getSelectedRows();
		if (rows == null || rows.length == 0) {
			return true;
		}
		return rows.length < size;
		// return model.getRowCount() > 0;
	}
	
	private boolean hasCopy() {
		int[] rows = table.getSelectedRows();
		return rows != null && rows.length > 0;
	}
	
	void doSelectAll() {
		int rows = model.getRowCount();
		if (rows > 0) {
			table.setRowSelectionInterval(0, rows - 1);
		}
	}

	void doClear() {
		clear();
	}
	
	void doCopy() {
		copyToClipboard();
	}

	/**
	 * 显示弹出菜单
	 * @param e
	 */
	private void showPopupMenu(MouseEvent e) {
		// 不满足SWING条件的POPUP触发，不处理
		if (!e.isPopupTrigger()) {
			return;
		}

		JMenuItem item = MenuBuilder.findMenuItemByMethod(rockMenu, "doClear");
		if (item != null) {
			item.setEnabled(hasClear());
		}
		item = MenuBuilder.findMenuItemByMethod(rockMenu, "doSelectAll");
		if (item != null) {
			item.setEnabled(hasSelectAll());
		}
		item = MenuBuilder.findMenuItemByMethod(rockMenu, "doCopy");
		if (item != null) {
			item.setEnabled(hasCopy());
		}

		int newX = e.getX();
		int newY = e.getY();
		
		Component invoker = e.getComponent(); // rockMenu.getInvoker();
		if (invoker.getClass() == JScrollPane.class) {
			JScrollPane jsp = (JScrollPane) invoker;
			JViewport port = jsp.getViewport();
			Point pt = port.getViewPosition();
			//	System.out.printf("view:%d %d, mouse:%d %d\n", pt.x, pt.y, newX, newY);

			// 调整坐标
			if (pt.x > 0) {
				newX = newX - pt.x;
				if (newX < 0) newX = 0;
			}
			if (pt.y > 0) {
				newY = newY - pt.y;
				if (newY < 0) newY = 0;
			}
		}
		
		rockMenu.show(invoker, newX, newY);
		
//		rockMenu.show(rockMenu.getInvoker(), newX, newY);
	}

	class ActionAdapter implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			click(e);
		}
	}

	/**
	 * 菜单事件
	 * @param event
	 */
	private void click(ActionEvent event) {
		Object object = event.getSource();
		if (Laxkit.isClassFrom(object, JMenuItem.class)) {
			JMenuItem source = (JMenuItem) object;
			String methodName = source.getName();
			invoke(methodName);
		}
	}

	private void invoke(String methodName) {
		if (methodName == null || methodName.isEmpty()) {
			return;
		}

		try {
			Method method = (getClass()).getDeclaredMethod(methodName, new Class<?>[0]);
			method.invoke(this, new Object[0]);
		} catch (NoSuchMethodException e) {

		} catch (IllegalArgumentException e) {

		} catch (IllegalAccessException e) {

		} catch (InvocationTargetException e) {

		}
	}

	class CommandMouseAdapter extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			showPopupMenu(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showPopupMenu(e);
		}
	}

	private final String REGEX = "^\\s*(?:[\\w\\W]+)[\\(\\[]([a-zA-Z]{1})[\\]\\)]\\s*$";
	
	/**
	 * 设置快捷键
	 * @param but
	 * @param input
	 */
	public void setMnemonic(JMenuItem but, String input) {
		if (input == null) {
			return;
		}
		Pattern pattern = Pattern.compile(REGEX);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String s = matcher.group(1);
			char w = s.charAt(0);
			but.setMnemonic(w);
		}
	}

	/**
	 * 生成菜单项
	 * @param textKey
	 * @param method
	 * @param w
	 * @return
	 */
	private JMenuItem createMenuItem(String textKey, String method) {
		String text = UIManager.getString(textKey);
		JMenuItem item = new JMenuItem(text);
		item.setName(method);
		item.addActionListener(new ActionAdapter());
		setMnemonic(item, text);
		
//		// 如果是快捷吸
//		if ((w >= 'a' && w <= 'z') || (w >= 'A' && w <= 'Z')) {
//			item.setMnemonic(w);
//		}
		
		item.setBorder(new EmptyBorder(2,4,2,4));
		return item;
	}

	/**
	 * 初始化弹出菜单
	 */
	private void initMenu() {
		//		NotifyDialog.MenuitemCopyTableText 复制[C]
		//NotifyDialog.MenuitemDeleteTableText 清除系统记录 [T]
		//NotifyDialog.MenuitemSelectAllTableText 选择全部[A]
		                                                                                   
		String[] texts = new String[] { "NotifyDialog.MenuitemCopyTableText",
				"NotifyDialog.MenuitemDeleteTableText","NotifyDialog.MenuitemSelectAllTableText" };
//		// 快捷键
//		char[] shorts = new char[] {  'C','D','A' };
		
		// 操作方法
		String[] methods = new String[] { "doCopy", "doClear","doSelectAll" };

		JMenuItem copyItem = createMenuItem(texts[0], methods[0]);
		JMenuItem mnuDelete = createMenuItem(texts[1], methods[1]);
		JMenuItem selectAllItem = createMenuItem(texts[2], methods[2]);
		
		rockMenu = new JPopupMenu();
		rockMenu.add(copyItem);
		rockMenu.add(mnuDelete);
		rockMenu.add(selectAllItem);

		rockMenu.setInvoker(table);
		table.addMouseListener(new CommandMouseAdapter());
	}

	/**
	 * 建立档案栏
	 */
	public void init() {
		// 行高度
		String value = UIManager.getString("NotifyDialog.TableRowHeight");
		int rowHeight = ConfigParser.splitInteger(value, 30);
		// 表头高度
		value = UIManager.getString("NotifyDialog.TableHeadHeight");
		int headerHeight = ConfigParser.splitInteger(value, 28);

		// 构造表
		model = new NotifyTableModel();
		table = new JTable(model);
		// 构造渲染器
		renderer = new NotifyTableCellRenderer();
		// 所有支持OBJECT类都使用这个渲染器
		table.setDefaultRenderer(ShowItemCell.class, renderer);
		// 表格单元高度与高度，宽度与宽度之间的间距
		UITools.setTableIntercellSpacing(table);

		table.setRowHeight(rowHeight); // 行高度
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowSelectionAllowed(true);
		table.setShowGrid(true);
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		table.setIntercellSpacing(new Dimension(3, 3));
		table.setColumnSelectionAllowed(true);
		table.setSurrendersFocusOnKeystroke(true);
//		FontKit.setToolTipText(table, tooltip);
		table.setBorder(new EmptyBorder(2, 2, 2, 2));
		
//		table.setIntercellSpacing(new Dimension(0, 0)); // NIMBUS界面不要空格
		
		
//		// 设置表头的最优范围，主要是定义高度
//		table.getTableHeader().setPreferredSize(new Dimension(20, headerHeight));
		
//		// 判断字体
//		Font font = FontKit.findFont(table.getFont(), tooltip);
//		if(font != null && font.equals(table.getFont())) {
//			table.setFont(font);
//		}
		
//		// 调整字体!
//		__exchangeFont(Properties.readTabbedTableFont());

		// 滚动框
		JScrollPane jsp = new JScrollPane(table);
		jsp.setColumnHeader(new HeightViewport(headerHeight)); // 修正JAVA BUG，正确显示表头高度！
		jsp.setBorder(new HighlightBorder(1));
		// 当前面板布局
		setLayout(new BorderLayout());
		add(jsp, BorderLayout.CENTER);

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
		
		initMenu();
		jsp.addMouseListener(new CommandMouseAdapter());
	}

	/**
	 * 复制内容到剪贴板
	 */
	private void copyToClipboard() {
		int[] rows = table.getSelectedRows();
		for (int i = 0; i < rows.length; i++) {
			rows[i] = table.convertRowIndexToModel(rows[i]);
		}

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

//	/**
//	 * 返回字体
//	 * @return
//	 */
//	public Font getSelectFont() {
//		return table.getFont();
//	}
//
//	/**
//	 * 设置字体
//	 * @param font
//	 */
//	public void setSelectFont(Font font) {
//		addThread(new FontThread(font));
//	}
	
//	/**
//	 * 调整字体
//	 * 
//	 * @param font
//	 */
//	private void __exchangeFont(Font font) {
//		if (font != null) {
//			// 标题字体，WINDOWS固定12尺寸,LINUX 14
//			Font sub = UITools.createHeaderFont(font);
//			table.getTableHeader().setFont(sub);
//			// 表格字体
//			table.setFont(font);
//		}
//	}

//	/**
//	 * 字体线程
//	 *
//	 * @author scott.liang
//	 * @version 1.0 8/28/2018
//	 * @since laxcus 1.0
//	 */
//	class FontThread extends SwingEvent {
//		Font font;
//
//		FontThread(Font e) {
//			super();
//			font = e;
//		}
//
//		public void process() {
//			__exchangeFont(font);
//		}
//	}

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
		
//		thread.setIntervalTime(800);
//		thread.setTouchTime(System.currentTimeMillis() + 5000);
		
		addThread(thread);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {

		// 表格单元高度与高度，宽度与宽度之间的间距
		if (table != null) {
			UITools.setTableIntercellSpacing(table);
		}

		// 更新
		super.updateUI();
		// 组件UI
		if (renderer != null) {
			renderer.updateUI();
		}
		if (rockMenu != null) {
			FontKit.updateDefaultFonts(rockMenu, true);
			rockMenu.updateUI();
		}
	}

}