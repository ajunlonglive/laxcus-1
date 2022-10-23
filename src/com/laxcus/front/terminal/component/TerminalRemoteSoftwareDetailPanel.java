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

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.front.pool.*;
import com.laxcus.front.terminal.*;
import com.laxcus.law.cross.*;
import com.laxcus.site.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.task.contact.near.*;
import com.laxcus.task.establish.end.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.event.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;
import com.laxcus.util.skin.*;

/**
 * 云端应用软件明细面板。
 * 
 * @author scott.liang
 * @version 1.0 1/12/2020
 * @since laxcus 1.0
 */
public class TerminalRemoteSoftwareDetailPanel extends JPanel {

	private static final long serialVersionUID = -1000171773451349053L;

	private TerminalRemoteSoftwarePanel parent;

	/** 渲染器 **/
	private TerminalRemoteSoftwareDetailCellRenderer renderer;

	/** 表格模型 **/
	private TerminalRemoteSoftwareDetailModel tableModel;

	/** 显示表格 **/
	private JTable table;

	/** 绑定对象 **/
	private Object attach;

	/** 关键字 **/
	private String tagCloudTask; // 云端应用
	private String tagLocalTask;	// 本地应用
	private String tagPosition;		// 位置
	private String tagNotFound;  // 没有找到

	private String tagTable; // 数据表
	private String tagCreateTime; // 创建时间
	
	private String tagAuthorizer; // 授权人
	private String tagAuthorizerOperator; // 授权项
	
	/** 列图标 **/
	private Icon primeKey;
	private Icon subKey;
	private Icon column;


	/**
	 * 构造云端应用软件明细面板
	 */
	public TerminalRemoteSoftwareDetailPanel() {
		super();
	}

	/**
	 * 设置父类面板
	 * @param e
	 */
	public void setParentPanel(TerminalRemoteSoftwarePanel e) {
		parent = e;
	}

	/**
	 * 返回父类面板
	 * @return
	 */
	public TerminalRemoteSoftwarePanel getParentPanel() {
		return parent;
	}
	
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
		if (attach.getClass() != object.getClass()) {
			return false;
		}
		
		boolean success = false;
		// 判断对象
		if (object.getClass() == Space.class) {
			success = (Laxkit.compareTo((Space) attach, (Space) object) == 0);
		} else if (object.getClass() == Phase.class) {
			success = (Laxkit.compareTo((Phase) attach, (Phase) object) == 0);
		} else if (object.getClass() == PassiveItem.class) {
			Space s1 = ((PassiveItem) attach).getSpace();
			Space s2 = ((PassiveItem) object).getSpace();
			success = (Laxkit.compareTo(s1, s2) == 0);
		}

		return success;
	}

	/**
	 * 加入线程
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 切换显示的对象
	 * @param object
	 */
	public void exchange(Object object) {
		addThread(new ExchangeThread(object));
	}

	/**
	 * 清除记录
	 */
	public void clear() {
		addThread(new ClearThread());
	}

	/**
	 * 更新显示对象
	 * @param object
	 */
	public void update(Object object) {
		addThread(new UpdateThread(object));
	}

	/**
	 * 设置当前字体
	 * @param e
	 */
	public void setSelectFont(Font e) {
		addThread(new FontThread(e));
	}

	
	private ShowItem createItem(Icon icon, String title, String str, Color foreground) {
		ShowItem item = new ShowItem();
		ShowImageCell key = new ShowImageCell(0, icon);
		key.setText(title);
		key.setTooltip(title);
		item.add(key);
		ShowStringCell value = new ShowStringCell(1, str, str);
		if (foreground != null) {
			value.setForeground(foreground);
		}
		item.add(value);
		return item;
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
	 * 显示组件
	 * @param phase
	 */
	private void __exchangePhase(Phase phase) {
		tableModel.addRow(createItem(tagCloudTask, phase.toString(true)));
		
		// 服务器地址参数
		StaffOnFrontPool pool = TerminalLauncher.getInstance().getStaffPool();
		NodeSet nodes = pool.findTaskSites(phase);
		if (nodes != null) {
			for (Node node : nodes.show()) {
				tableModel.addRow(createItem(tagPosition, node.toString()));
			}
		}
		
		// 空格
		tableModel.addRow(createItem(" ", " "));

		// 本地关联组件
		Phase sub = phase.duplicate();
		boolean exists = false;
		if (PhaseTag.isConduct(sub.getFamily())) {
			sub.setFamily(PhaseTag.PUT);
			exists = PutTaskPool.getInstance().contains(sub);
		} else if (PhaseTag.isEstablish(sub.getFamily())) {
			sub.setFamily(PhaseTag.END);
			exists = EndTaskPool.getInstance().contains(sub);
		} else if (PhaseTag.isContact(sub.getFamily())) {
			sub.setFamily(PhaseTag.NEAR);
			exists = NearTaskPool.getInstance().contains(sub);
		}
		if (exists) {
			tableModel.addRow(createItem(tagLocalTask, sub.toString(true)));
		} else {
			// 红绯 是关键字，在color.txt配置文件中定义，启动时已经导入ColorTemplate
			Color color = ColorTemplate.findColor("红绯", Color.RED);
			tableModel.addRow(createItem(tagLocalTask, tagNotFound, color));
		}
	}

	/***
	 * 显示表记录
	 * @param element
	 */
	private void printTable(Table element) {
		if (element == null) {
			return;
		}

		java.util.Date date = SimpleTimestamp.format(element.getCreateTime());
		tableModel.addRow(createItem(tagCreateTime, Laxkit.toString(date)));
		// 空格
		tableModel.addRow(createItem(" ", " "));

		Collection<ColumnAttribute> values = element.list();
		for (ColumnAttribute attribute : values) {
			Icon icon = null;
			String name = attribute.getNameText();
			if (attribute.isPrimeKey()) {
				icon = primeKey;
			} else if (attribute.isKey()) {
				icon = subKey;
			} else {
				icon = column;
			}

			// 表类型
			String type = ColumnType.translate(attribute.getType());
			ShowItem sub = createItem(icon, name, type, null);
			tableModel.addRow(sub);
		}
	}
	
	/**
	 * 显示自有表信息
	 * @param space 表名
	 */
	private void __exchangeTable(Space space) {
		tableModel.addRow(createItem(tagTable, space.toString()));
		// 服务器地址参数
		StaffOnFrontPool pool = TerminalLauncher.getInstance().getStaffPool();
		NodeSet nodes = pool.findTableSites(space);
		if (nodes != null) {
			for (Node node : nodes.show()) {
				tableModel.addRow(createItem(tagPosition, node.toString()));
			}
		}

		// 查找表
		Table element = pool.findTable(space);
		if(element != null) {
			printTable(element);
		}
	}
	
	/**
	 * 显示共享表信息
	 * @param item
	 */
	private void __exchangePassiveItem(PassiveItem item) {
		Space space = item.getSpace();
		tableModel.addRow(createItem(tagTable, space.toString()));
		tableModel.addRow(createItem(tagAuthorizer, item.getAuthorizer().toString()));
		tableModel.addRow(createItem(tagAuthorizerOperator, item.getFlag().getOperatorText()));

		// 服务器地址参数
		StaffOnFrontPool pool = TerminalLauncher.getInstance().getStaffPool();
		NodeSet nodes = pool.findTableSites(space);
		if (nodes != null) {
			for (Node node : nodes.show()) {
				tableModel.addRow(createItem(tagPosition, node.toString()));
			}
		}

		// 查找授权表
		Table element = pool.findPassiveTable(space);
		if (element != null) {
			printTable(element);
		}
	}
	
	/**
	 * 切换显示
	 * @param object
	 */
	void __exchange(Object object) {
		// 清除
		__clear();

		if (object.getClass() == Space.class) {
			__exchangeTable((Space) object);
			attach = object;
		} else if (object.getClass() == Phase.class) {
			__exchangePhase((Phase) object);
			attach = object;
		} else if (object.getClass() == PassiveItem.class) {
			__exchangePassiveItem((PassiveItem) object);
			attach = object;
		}
	}

	/**
	 * 清除记录，但是不清除列
	 */
	void __clear() {
		tableModel.clear();
		attach = null;
	}

	//	/**
	//	 * 更新，必须是在签名一致的情况下!
	//	 * @param siger
	//	 */
	//	void __update(Object siger) {
	//		// 判断一致！
	//		boolean success = (master != null && Laxkit.compareTo(siger, master) == 0);
	//		if (success) {
	//			__exchange(siger);
	//		}
	//		
	//		boolean success = (that != null);
	//	}

	/**
	 * 更新，必须是在签名一致的情况下!
	 * @param siger
	 */
	void __update(Object that) {
		// 不一致就忽略！
		if (attach == null || attach.getClass() != that.getClass()) {
			return;
		}

		boolean success = false;
		if(Laxkit.isClassFrom(attach, Space.class) && Laxkit.isClassFrom(that, Space.class)) {
			Space first = (Space) attach;
			Space second = (Space) that;
			success = (Laxkit.compareTo(first, second) == 0);
		} else if(Laxkit.isClassFrom(attach, Phase.class) && Laxkit.isClassFrom(that, Phase.class)) {
			Phase first = (Phase)attach;
			Phase second = (Phase)that;
			success = (Laxkit.compareTo(first, second) == 0);
		} else if(Laxkit.isClassFrom(attach, PassiveItem.class) && Laxkit.isClassFrom(that, PassiveItem.class)) {
			PassiveItem first = (PassiveItem)attach;
			PassiveItem second = (PassiveItem)that;
			success = (Laxkit.compareTo(first.getAuthorizer(), second.getAuthorizer()) == 0 && 
					Laxkit.compareTo(first.getSpace(), second.getSpace()) == 0);
		}
		// 以上条件成立，更新
		if (success) {
			__exchange(that);
		}
	}
	
	class ExchangeThread extends SwingEvent {
		Object object;
		ExchangeThread(Object e) {
			super();
			object = e;
		}

		public void process() {
			__exchange(object);
		}
	}

	class ClearThread extends SwingEvent {
		ClearThread(){super();}
		public void process() {
			__clear();
		}
	}

	class UpdateThread extends SwingEvent {
		Object object;

		UpdateThread(Object e) {
			super();
			object = e;
		}

		public void process() {
			__update(object);
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
		String value = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/key-width");
		int key_width = ConfigParser.splitInteger(value, 42);
		value =  TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/value-width");
		int value_width = ConfigParser.splitInteger(value, 130);

		String keyText =  TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/Header/Key/title");
		String valueText =  TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/Header/Value/title");

		// 标题！
		ShowTitle title = new ShowTitle();
		title.add(new ShowTitleCell(0, keyText, key_width));
		title.add(new ShowTitleCell(1, valueText, value_width));

		//		// 显示标题
		//		TableColumn[] columns = title.createTableColumns();
		//		for (int i = 0; i < columns.length; i++) {
		//			String name = title.get(i).getName();
		//			columns[i].setHeaderRenderer(new TerminalRemoteSoftwareDetailHeaderCellRenderer(name));
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
		String titleText = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/title");
		FontKit.setToolTipText(this, titleText);

		// 行高度
		String value = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/row-height");
		int rowHeight = ConfigParser.splitInteger(value, 30);
		// 表头高度
		value = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/header-height");
		int headerHeight = ConfigParser.splitInteger(value, 28);

		// 表模型
		tableModel = new TerminalRemoteSoftwareDetailModel();
		table = new JTable(tableModel);
		// 渲染器只支持ShowItemCell类
		renderer = new TerminalRemoteSoftwareDetailCellRenderer();
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
		//		renderer = new TerminalRemoteSoftwareDetailRowCellRenderer();
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
		__exchangeFont(TerminalProperties.readRemoteSoftwareFont());

		// 滚动栏
		JScrollPane scroll = new JScrollPane(table);
		FontKit.setToolTipText(scroll, titleText);
		scroll.setColumnHeader(new HeightViewport(headerHeight)); // 修正JAVA BUG，显示正确的表头高度，保证不出错！
		// 界面布局！
		setLayout(new BorderLayout(0, 0));
		add(scroll, BorderLayout.CENTER);

		// 属性名
		tagAuthorizer = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/Authorizer/title");
		tagAuthorizerOperator = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/AuthorizerOperator/title");

		tagTable = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/Table/title");
		tagCreateTime = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/CreateTime/title");

		tagCloudTask = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/CloudTask/title");
		tagLocalTask = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/LocalTask/title");
		tagPosition = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/Position/title");
		tagNotFound = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/NotFound/title");
			
		// 列图标
		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/object/");
		primeKey =	loader.findImage("prime_key.png");
		subKey = loader.findImage("sub_key.png");
		column = loader.findImage("column.png");
		
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
	//		String titleText = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/title");
	//		FontKit.setToolTipText(this, titleText);
	//		
	//		// 行高度
	//		String value = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/row-height");
	//		int rowHeight = ConfigParser.splitInteger(value, 30);
	//		// 表头高度
	//		value = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/header-height");
	//		int headerHeight = ConfigParser.splitInteger(value, 28);
	//		// 属性/参数行的宽度
	//		value = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/key-width");
	//		int key_width = ConfigParser.splitInteger(value, 42);
	//		value = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/value-width");
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
	//		table.setDefaultRenderer(Object.class, new TerminalRemoteSoftwareDetailRowCellRenderer());
	//		
	//		// 设置表头的最优范围
	//		table.getTableHeader().setPreferredSize(new Dimension(key_width, headerHeight));
	//		
	//		String keyText = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/Header/Key/title");
	//		String valueText = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/Header/Value/title");
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
	//			columns[i].setHeaderRenderer(new TerminalRemoteSoftwareDetailHeaderCellRenderer(name));
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
	//		sigerText = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/Siger/title");
	//		registerText =	TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/Register/title");
	//		register_onlineText = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/Register-Online/title");
	//		onlineText = TerminalLauncher.getInstance().findCaption("Window/RemoteSoftwareDetailPanel/Online/title");
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
	//		Font font = TerminalProperties.readSoftwareRemoteFont();
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