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
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.event.*;
import com.laxcus.util.net.*;
import com.laxcus.util.skin.*;
import com.laxcus.watch.*;

/**
 * 运行时记录
 * 
 * @author scott.liang
 * @version 1.0 12/23/2019
 * @since laxcus 1.0
 */
public class WatchSiteBrowserDetailPanel extends JPanel {

	private static final long serialVersionUID = 195119350906969883L;

	private final int SITE_INDEX = 0;
	private final int TIME_INDEX = 1;

	private final int CPU_INDEX = 2;
	private final int VM_MEM_INDEX = 3;
	private final int SYS_MEM_INDEX = 4;
	private final int SYS_DISK_INDEX = 5;

	private final int REGISTER_MEMBERS_INDEX = 6;
	private final int ONLINE_MEMBERS_INDEX = 7;

	private final int IDLE_TASKS_INDEX = 8;
	private final int RUN_TASKS_INDEX = 9;
	private final int THREADS_INDEX = 10;

	private final int COMMAND_TCP_PORT = 11;
	private final int COMMAND_TCP_BUFFER = 12;
	private final int COMMAND_UDP_PORT = 13;
	private final int COMMAND_UDP_BUFFER = 14;
	
	private final int DATA_SUCKER_PORT = 15;
	private final int MASSIVE_MI = 16;
	private final int DATA_SUCKER_BUFFER = 17;
	
	private final int DATA_DISPATCHER_PORT = 18;
	private final int MASSIVE_MO = 19;
	private final int DATA_DISPATCHER_BUFFER = 20;
	
	private final int CPU_TYPE_INDEX = 21;
	private final int OS_INDEX = 22;

	private final int LAST_INDEX = OS_INDEX;

	/** 父类窗口 **/
	private WatchSiteBrowserPanel parnet;

	/** 渲染器 **/
	private WatchSiteBrowserDetailCellRenderer renderer;

	/** 终端表格模型 **/
	private WatchSiteBrowserDetailModel model;

	/** 显示表格 **/
	private JTable table;

	/** 当前节点地址 **/
	private Node focus;

	/**
	 * 构造默认的节点运行时明细面板
	 */
	public WatchSiteBrowserDetailPanel() {
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
	 * 设置父类面板
	 * @param e WatchSiteBrowserPanel实例
	 */
	public void setParntPanel(WatchSiteBrowserPanel e) {
		parnet = e;
	}

	/**
	 * 返回父类面板
	 * @return WatchSiteBrowserPanel实例
	 */
	public WatchSiteBrowserPanel getParentPanel() {
		return parnet;
	}


	/**
	 * 切换运行时
	 * @author scott.liang
	 * @version 1.0 12/23/2019
	 * @since laxcus 1.0
	 */
	class ExchangeThread extends SwingEvent {
		SiteRuntime runtime;

		ExchangeThread(SiteRuntime e) {
			super();
			runtime = e;
		}

		public void process() {
			if (runtime != null) {
				setSiteRuntime(runtime);
			} else {
				erase();// 清除
			}
		}
	}

	/**
	 * 清除全部参数
	 *
	 * @author scott.liang
	 * @version 1.0 12/26/2019
	 * @since laxcus 1.0
	 */
	class ClearSiteThread extends SwingEvent {
		ClearSiteThread() { super(); }
		public void process() {
			erase();
			// 清除焦点
			focus = null;
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
	 * 修正表头和表格字体
	 * @param font
	 */
	private void __exchangeFont(Font font) {
		if (font != null) {
			// 表头字体，WINDOWS固定12磅, LINUX 14
			Font sub = UITools.createHeaderFont(font);
			table.getTableHeader().setFont(sub);
			// 表格字体
			table.setFont(font);
		}
	}

	/**
	 * 设置当前字体
	 * @param e
	 */
	public void setSelectFont(Font e) {
		addThread(new FontThread(e));
	}

	/**
	 * 清除全部旧的记录
	 */
	public void clear() {
		addThread(new ClearSiteThread());
	}

	/**
	 * 切换节点运行时。
	 * 发生在用户切换节点显示状态
	 * @param cmd 运行时命令
	 */
	public void exchange(SiteRuntime cmd) {
		Laxkit.nullabled(cmd); // 不允许空指针

		addThread(new ExchangeThread(cmd));
	}

	/**
	 * 修改运行参数。
	 * 服务器节点更新数据时，同时修改这里
	 * @param cmd 运行时命令
	 */
	public void modify(SiteRuntime cmd) {
		Laxkit.nullabled(cmd); // 不允许空指针

		// 判断地址一致！
		boolean success = (focus != null && Laxkit.compareTo(cmd.getNode(), focus) == 0);
		if (success) {
			exchange(cmd);
		}
	}

	/**
	 * 删除地址信息，地址必须一致
	 * @param node
	 */
	public void drop(Node node) {
		Laxkit.nullabled(node); // 检测空指针，发生属于程序员错误
		// 判断地址一致
		boolean success = (focus != null && Laxkit.compareTo(node, focus) == 0);
		if (success) {
			clear();
		}
	}

	/**
	 * 达到最限制的颜色
	 * @param limit 限制
	 * @return 返回限制颜色值，不达到是空指针
	 */
	private Color isLimitColor(boolean limit) {
		Color foreground = null;
		if (limit) {
			foreground = Skins.findSiteRuntimeFullText(); 
		}
		return foreground;
	}

	private String printMembers(int value) {
		if(value < 0) {
			return "--";
		}
		return String.format("%d", value);
	}

	/**
	 * 产生SOCKET缓存显示
	 * @param buff
	 * @return
	 */
	private String doSocketBuffer(SocketBuffer buff) {
		if (buff == null) {
			return "0 / 0";
		}
		String receive = ConfigParser.splitCapacity(buff.getReceive());
		String send = ConfigParser.splitCapacity(buff.getSend());
		return String.format("%s / %s", receive, send);
	}
	
	/**
	 * 产生SOCKET缓存显示
	 * @param buff
	 * @param slaves 从属参数
	 * @return
	 */
	private String doSocketBuffer(SocketBuffer buff, int slaves) {
		if (buff == null) {
			return "0 / 0";
		}
		if (slaves < 0) slaves = 0;
		int readSize = (buff.getReceive() + buff.getReceive() * slaves);
		int writeSize = (buff.getSend() + buff.getSend() * slaves);
		
		String receive = ConfigParser.splitCapacity(readSize); // buff.getReceive());
		String send = ConfigParser.splitCapacity(writeSize); // buff.getSend());
		return String.format("%s / %s", receive, send);
	}
	
	/**
	 * 生成SOCKET端口
	 * @param port
	 * @param reflectPort
	 * @return
	 */
	private String doSocketPort(int port, int reflectPort) {
		if (reflectPort > 0) {
			return String.format("%d (%d)", port, reflectPort);
		} else {
			return String.format("%d", port);
		}
	}

	/**
	 * 修改节点运行时
	 * @param cmd
	 */
	private void setSiteRuntime(SiteRuntime cmd) {
		// 记录前节点地址
		focus = cmd.getNode();
		// 节点
		setParam(SITE_INDEX, focus.toString());
		// 记录时间 
		SimpleDateFormat style = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
		String str = style.format(new Date(cmd.getRecordTime()));
		setParam(TIME_INDEX, str);

		// CPU占比
		str = printRate(cmd.getCPURate());
		Color color = isLimitColor(cmd.isCPUFull());
		setParam(CPU_INDEX, str, color);

		// 虚拟机内存
		str = printValueRate(cmd.getVmUsedMemory(), cmd.getVmMaxMemory());
		color = isLimitColor(cmd.isVmMemoryMissing());
		setParam(VM_MEM_INDEX, str, color);

		// 系统内存
		str = printValueRate(cmd.getSysUsedMemory(), cmd.getSysMaxMemory());
		color = isLimitColor(cmd.isSysMemoryMissing());
		setParam(SYS_MEM_INDEX, str, color);

		// 系统磁盘
		str = printValueRate(cmd.getSysUsedDisk(), cmd.getSysMaxDisk());
		color = isLimitColor(cmd.isSysDiskMissing());
		setParam(SYS_DISK_INDEX, str, color);

		// 注册成员
		str = printMembers(cmd.getRegisterMembers());
		setParam(REGISTER_MEMBERS_INDEX, str);
		// 在线成员
		str = printMembers(cmd.getOnlineMembers());
		setParam(ONLINE_MEMBERS_INDEX, str);

		// 等待中的任务
		setParam(IDLE_TASKS_INDEX, cmd.getCommands());
		// 执行中的任务
		setParam(RUN_TASKS_INDEX, cmd.getInvokers());
		// 节点的线程
		setParam(THREADS_INDEX, cmd.getThreads());

		// SOCKET端口/缓存
		SiteHost host = focus.getHost();
		setParam(COMMAND_TCP_PORT, doSocketPort(host.getTCPort(), host.getReflectTCPort()));
		setParam(COMMAND_TCP_BUFFER, doSocketBuffer(cmd.getCommandStreamBuffer()));
		setParam(COMMAND_UDP_PORT, doSocketPort(host.getUDPort(), host.getReflectUDPort()));
		setParam(COMMAND_UDP_BUFFER, doSocketBuffer(cmd.getCommandPacketBuffer()));

		// 数据监听端口/缓存
		setParam(DATA_SUCKER_PORT, doSocketPort(cmd.getReplySuckerPort(), cmd.getReplyReflectSuckerPort()));
		setParam(MASSIVE_MI, cmd.getMI());
		setParam(DATA_SUCKER_BUFFER, doSocketBuffer(cmd.getReplySuckerBuffer(), cmd.getMI()));
		
		setParam(DATA_DISPATCHER_PORT, doSocketPort(cmd.getReplyDispatcherPort(), cmd.getReplyReflectDispatcherPort()));
		setParam(MASSIVE_MO, cmd.getMO());
		setParam(DATA_DISPATCHER_BUFFER, doSocketBuffer(cmd.getReplyDispatcherBuffer(), cmd.getMO()));

		// CPU类型
		setParam(CPU_TYPE_INDEX, cmd.getCPU());
		// 操作系统类型
		setParam(OS_INDEX, cmd.getOS());
	}

	/**
	 * 打印CPU使用率
	 * @param rate
	 * @return
	 */
	private String printRate(double rate) {
		return String.format("%.2f", rate) + "%";
	}

	/**
	 * 显示内存
	 * @param memory 内存容量
	 * @return 返回字符串描述
	 */
	private String printValue(long memory) {
		// 小于等于0是无效值
		if (memory <= 0) {
			return "0";
		}
		return ConfigParser.splitCapacity(memory, 2);
	}

	/**
	 * 显示内存使用率
	 * @param total
	 * @param max
	 * @return 字符串描述的内存比率
	 */
	private String printRate(long total, long max) {
		double rate = 0.0f;
		// 内存必须在于0
		if (total > 0 && max > 0) {
			rate = ((double) total / (double) max) * 100;
		}
		return printRate(rate);
	}

	/**
	 * 显示参数比值
	 * @param total 可用的
	 * @param max 最大
	 * @return 字符串比值
	 */
	private String printValueRate(long total, long max) {
		String s1 = printValue(total);
		String s2 = printValue(max);
		String s3 = printRate(total, max);

		return String.format("(%s/%s)=%s", s1, s2, s3);
	}

	/**
	 * 修改参数
	 * @param row 所在行
	 * @param text 文本信息
	 * @param foreground 前景颜色
	 */
	private void setParam(int row, String text, Color foreground) {
		if (row >= model.getRowCount()) {
			return;
		}

		ShowStringCell cell = new ShowStringCell(0, text, foreground);
		cell.setTooltip(text);
		model.setValueAt(cell, row, 0);
	}

	/**
	 * 修改参数
	 * @param row 所在行
	 * @param text 显示文本
	 */
	private void setParam(int row, String text) {
		setParam(row, text, null);
	}

	/**
	 * 修改参数
	 * @param row 所在行
	 * @param value 数值
	 */
	private void setParam(int row, int value) {
		String text = String.valueOf(value);
		setParam(row, text, null);
	}

	/**
	 * 清除记录
	 */
	private void erase() {
		for (int row = SITE_INDEX; row <= LAST_INDEX; row++) {
			setParam(row, "");
		}
	}


	//	/**
	//	 * 初始化面板
	//	 */
	//	public void init() {
	//		initTable();
	//
	//		JScrollPane scroll = new JScrollPane(table);
	////		scroll.getViewport().setBackground(Color.WHITE);
	//		setLayout(new BorderLayout(0,0));
	//		add(scroll, BorderLayout.CENTER);
	//	}

	/**
	 * 初始化表头
	 */
	private void initHeader() {
		// 键/值的宽度
		String value = WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/key-width");
		int key_width = ConfigParser.splitInteger(value, 80);
		value = WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/value-width");
		int value_width = ConfigParser.splitInteger(value, 130);

		String keyText = WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/Header/Key/title");
		String valueText = WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/Header/Value/title");

		// 标题！
		ShowTitle title = new ShowTitle();
		title.add(new ShowTitleCell(0, valueText, value_width));
		title.add(new ShowTitleCell(1, keyText, key_width));

		int count = title.size();
		for (int i = 0; i < count; i++) {
			ShowTitleCell cell = title.get(i);
			TableColumn column = new TableColumn(cell.getIndex(), cell.getWidth());
			column.setHeaderValue(cell.getName());
			// 保存
			table.addColumn(column);
		}

		// 保存标题
		model.setTitle(title);
	}

	/**
	 * 初始化面板
	 */
	public void init() {
		//  取配置参数
		String titleText = WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/title");
		FontKit.setToolTipText(this, titleText);

		//  取配置参数，行高度
		String value = WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/row-height");
		int height = ConfigParser.splitInteger(value, 30);
		// 表头高度
		value = WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/header-height");
		int headerHeight = ConfigParser.splitInteger(value, 28);

		// 模型
		model = new WatchSiteBrowserDetailModel();
		table = new JTable(model);
		// 渲染器只支持ShowItemCell类
		renderer = new WatchSiteBrowserDetailCellRenderer();
		table.setDefaultRenderer(ShowItemCell.class, renderer);

		//		// 设置基础参数
		//		table.setModel(model);

		table.setRowHeight(height); // 行高度
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowSelectionAllowed(true);
		table.setShowGrid(true);
		table.getTableHeader().setReorderingAllowed(true);
		table.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//		table.setIntercellSpacing(new Dimension(3, 3));
		//		table.setColumnSelectionAllowed(true);
		table.setSurrendersFocusOnKeystroke(true);
		table.setBorder(new EmptyBorder(1, 1, 1, 1));

		//		// 表头的最优宽度
		//		table.getTableHeader().setPreferredSize(new Dimension(20, headerHeight));

		//		// 所有支持OBJECT类都使用这个渲染器
		//		renderer = new WatchSiteBrowserDetailCellRenderer();
		//		table.setDefaultRenderer(Object.class, renderer);

		// 表格单元高度与高度，宽度与宽度之间的间距
		UITools.setTableIntercellSpacing(table);

		// 初始化表头
		initHeader();

		// 修正字体
		__exchangeFont(WatchProperties.readBrowserSiteFont());

		// 设置滚动面板
		JScrollPane scroll = new JScrollPane(table);
		scroll.setColumnHeader(new HeightViewport(headerHeight)); // 修正JAVA BUG，显示正确的表头高度，保证不出错！
		FontKit.setToolTipText(scroll, titleText);
		setLayout(new BorderLayout(0,0));
		add(scroll, BorderLayout.CENTER);

		String[] tags = { "Site", "Time", "CPU", "VM-Memory", "Sys-Memory",
				"Sys-Disk", "RegisterMembers", "OnlineMembers", "Commands",
				"Invokers", "Threads", 
				
				"Command-TCP-Port", "Command-TCP-Buffer", 
				"Command-UDP-Port", "Command-UDP-Buffer", 
				"Data-Sucker-Port", "Data-Sucker-MI", "Data-Sucker-Buffer",
				"Data-Dispatcher-Port", "Data-Dispatcher-MO", "Data-Dispatcher-Buffer",
				"CPU-Type", "OS-Type" };
		
		String[] keys = new String[tags.length];
		String[] tooltips = new String[tags.length];
		// 获取参数
		for (int i = 0; i < tags.length; i++) {
			String s1 = String.format("Window/SiteBrowserDetailPanel/%s/title", tags[i]);
			String s2 = String.format("Window/SiteBrowserDetailPanel/%s/tooltip", tags[i]);
			keys[i] = WatchLauncher.getInstance().findCaption(s1);
			
			// 工具提示
			tooltips[i] = WatchLauncher.getInstance().findCaption(s2);
			if (tooltips[i] == null) {
				tooltips[i] = keys[i];
			}
		}
		for (int i = 0; i < keys.length; i++) {
			ShowItem item = new ShowItem();
//			item.add(new ShowStringCell(1, keys[i], keys[i]));
			item.add(new ShowStringCell(1, keys[i], tooltips[i]));
			item.add(new ShowStringCell(0, ""));
			model.addRow(item);
		}

		// 定位复制键
		table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK, true), "CTRL C");

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
	//		String titleText = WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/title");
	//		FontKit.setToolTipText(this, titleText);
	//
	//		//  取配置参数，行高度
	//		String value = WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/row-height");
	//		int height = ConfigParser.splitInteger(value, 30);
	//		// 表头高度
	//		value = WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/header-height");
	//		int headerHeight = ConfigParser.splitInteger(value, 28);
	//		// 键/值的宽度
	//		value = WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/key-width");
	//		int key_width = ConfigParser.splitInteger(value, 80);
	//		value = WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/value-width");
	//		int value_width = ConfigParser.splitInteger(value, 130);
	//		
	//		// 设置基础参数
	//		table.setModel(model);
	//		table.setRowHeight(height); // 行高度
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
	//		// 表头的最优宽度
	//		table.getTableHeader().setPreferredSize(new Dimension(key_width, headerHeight));
	//
	//		// 所有支持OBJECT类都使用这个渲染器
	//		table.setDefaultRenderer(Object.class, new WatchSiteBrowserDetailCellRenderer());
	//		
	//		String keyText = WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/Header/Key/title");
	//		String valueText = WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/Header/Value/title");
	//		
	//		// 标题！
	//		ShowTitle title = new ShowTitle();
	//		title.add(new ShowTitleCell(1, keyText, key_width));
	//		title.add(new ShowTitleCell(0, valueText, value_width));
	//		
	//		// 显示标题
	//		TableColumn[] columns = title.createTableColumns();
	//		for (int i = 0; i < columns.length; i++) {
	//			//	columns[i].setHeaderRenderer(null);
	//			String name = title.get(i).getName();
	//			columns[i].setHeaderRenderer(new WatchSiteBrowserDetailHeaderCellRenderer(name));
	//			table.addColumn(columns[i]);
	//		}
	//		// 保存标题
	//		model.setTitle(title);
	//
	//		String[] tags = { "Site", "Time", "CPU", "VM-Memory", "Sys-Memory",
	//				"Sys-Disk", "Commands", "Invokers", "Threads", "CPU-Type", "OS-Type" };
	//		String[] keys = new String[tags.length];
	//
	//		// 获取参数
	//		for (int i = 0; i < tags.length; i++) {
	//			String s1 = String.format("Window/SiteBrowserDetailPanel/%s/title", tags[i]);
	//			keys[i] = WatchLauncher.getInstance().findCaption(s1);
	//		}
	//		for (int i = 0; i < keys.length; i++) {
	//			ShowItem item = new ShowItem();
	//			item.add(new ShowStringCell(1, keys[i]));
	//			item.add(new ShowStringCell(0, ""));
	//			model.addRow(item);
	//		}
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
	//		// 定义字体
	//		Font font = WatchProperties.readBrowserSiteFont();
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
				if(bf.length() >0) bf.append("  ");

				ShowItemCell cell = model.getCellAt(row, column);
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
		// 重新绘制
		if (renderer != null) {
			renderer.updateUI();
		}
		// 更新
		super.updateUI();
		// 表格单元高度与高度，宽度与宽度之间的间距
		UITools.setTableIntercellSpacing(table);
	}

}