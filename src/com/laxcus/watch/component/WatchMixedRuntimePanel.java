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
import java.util.Timer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.event.*;
import com.laxcus.util.lock.*;
import com.laxcus.util.skin.*;
import com.laxcus.watch.*;

/**
 * 分布站点运行时显示面板
 * 
 * @author scott.liang
 * @version 1.0 4/2/2012
 * @since laxcus 1.0
 */
public class WatchMixedRuntimePanel extends JPanel {

	private static final long serialVersionUID = 1533226553595884364L;
	
	/** 渲染器 **/
	private WatchMixedRuntimeCellRenderer renderer;

	/** 重要说明！JTable table = new JTable(), WatchMixedRuntimeModel model = new WatchMixedRuntimeModel() 必须在类声明中构造！否则在显示时会出错！原因暂时不明！没有查SWING源码，可能是JAVA SWING的BUG!  **/

	/** 命令处理的显示表格 **/
	private JTable table;

	/** 处理结果模型 **/
	private WatchMixedRuntimeModel model;

	/** 站点参数处理任务 **/
	private SiteTask siteTask = new SiteTask();

	/**
	 * 构造分布站点运行时显示面板
	 */
	public WatchMixedRuntimePanel(){
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
	 * 修改窗口字体
	 * @param font
	 */
	private void __exchangeFont(Font font){
		if(font != null){
			// 表头字体，固定12磅
			Font sub = UITools.createHeaderFont(font);
			table.getTableHeader().setFont(sub);
			// 设置字体
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
	 * 清除全部数据
	 */
	public void clear() {
		// 放入SWING组件队列
		addThread(new ClearThread());
	}

	class ClearThread extends SwingEvent {
		ClearThread() { super(); }
		public void process() {
			// 清除记录
			siteTask.clear();
			// 删除界面记录
			model.clear();
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
	
//	private void doTableGap() {
//		if(table != null) {
//			// 将 rowMargin 和 columnMargin（单元格之间间距的高度和宽度）设置为 intercellSpacing
//			table.setIntercellSpacing(new Dimension(1, 1)); 
//		}
//	}

	/**
	 * 建立档案栏
	 */
	public void init() {
		Timer timer = WatchLauncher.getInstance().getTimer();
		timer.schedule(siteTask, 0, 2000); // 2秒钟触发一次
		
		String tooltip = WatchLauncher.getInstance().findCaption("Window/RuntimePanel/title");

		// 行高度
		String value = WatchLauncher.getInstance().findCaption("Window/RuntimePanel/row-height");
		int rowHeight = ConfigParser.splitInteger(value, 30);
		// 表头高度
		value = WatchLauncher.getInstance().findCaption("Window/RuntimePanel/header-height");
		int headerHeight = ConfigParser.splitInteger(value, 28);
		
		// 命令处理的显示表格
		model = new WatchMixedRuntimeModel();
		table = new JTable(model);
		// 构造渲染器
		renderer = new WatchMixedRuntimeCellRenderer();
		table.setDefaultRenderer(ShowItemCell.class, renderer);
		// 初始化标题
		initHeader();		

//		// 定义模型！
//		table.setModel(model);
//		// 初始化标题
//		initHeader();
//		// 构造渲染器
//		renderer = new WatchMixedRuntimeCellRenderer();
////		// 所有支持OBJECT类都使用这个渲染器
////		table.setDefaultRenderer(Object.class, renderer);
//		table.setDefaultRenderer(ShowItemCell.class, renderer);
		
		table.setRowHeight(rowHeight); // 行高度
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowSelectionAllowed(true);
		table.setShowGrid(true);
		table.getTableHeader().setReorderingAllowed(true);
		
		table.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		table.setIntercellSpacing(new Dimension(3, 3));
		table.setColumnSelectionAllowed(true);
		table.setSurrendersFocusOnKeystroke(true);
		FontKit.setToolTipText(table, tooltip);
		table.setBorder(new EmptyBorder(2, 1, 1, 1));
		
//		// 将 rowMargin 和 columnMargin（单元格之间间距的高度和宽度）设置为 intercellSpacing
//		table.setIntercellSpacing(new Dimension(0, 0)); 
		UITools.setTableIntercellSpacing(table);
		
		// 自己的字体
		__exchangeFont(WatchProperties.readTabbedRuntimeFont());

		JScrollPane scroll = new JScrollPane(table);
		// 这是一个JAVA BUG的修改方法，修正表头高度，取代JTable.getTableHeader().setPreferredSize(new Dimension(width, height));否则拖动表头会出现显示异常
		scroll.setColumnHeader(new HeightViewport(headerHeight));
		// 设置字体
		FontKit.setToolTipText(scroll, tooltip);
		
		// 窗口布局！
		setLayout(new BorderLayout(0,0));
		add(scroll, BorderLayout.CENTER);
		
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
		
		// 初始化排序
		initSorts();

//		// 初始化排序
//		TableRowSorter<WatchMixedRuntimeModel> sorter = new TableRowSorter<WatchMixedRuntimeModel>(model);
//		table.setRowSorter(sorter);
//		// 节点地址和CPU使用率
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.ADDRESS.getIndex(), new NodeComparator());
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.CPU_RATE.getIndex(), new RateComparator());
//		// 系统内存
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.SYS_USABLE_MEMORY.getIndex(), new MemoryComparator());
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.SYS_MAX_MEMORY.getIndex(), new MemoryComparator());
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.SYS_MEMORY_RATE.getIndex(), new RateComparator());
//		// 虚拟机内存
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.VM_USABLE_MEMORY.getIndex(), new MemoryComparator());
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.VM_MAX_MEMORY.getIndex(), new MemoryComparator());
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.VM_MEMORY_RATE.getIndex(), new RateComparator());
//		// 磁盘
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.USABLE_DISK.getIndex(), new DiskComparator());
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.MAX_DISK.getIndex(), new DiskComparator());
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.DISK_RATE.getIndex(), new RateComparator());
//		// 其它参数
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.THREADS.getIndex(), new IntegerComparator());
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.COMMANDS.getIndex(), new IntegerComparator());
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.INVOKERS.getIndex(), new IntegerComparator());
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.OS.getIndex(), new StringComparator());
//		sorter.setComparator(WatchMixedRuntimeSiteIndex.CPU.getIndex(), new StringComparator());
	}
	
	/**
	 * 初始化排序
	 */
	private void initSorts() {
		// 初始化排序
		TableRowSorter<WatchMixedRuntimeModel> sorter = new TableRowSorter<WatchMixedRuntimeModel>(model);
		table.setRowSorter(sorter);
		// 节点地址和CPU使用率
		sorter.setComparator(WatchMixedRuntimeSiteIndex.ADDRESS.getIndex(), new NodeComparator());
		sorter.setComparator(WatchMixedRuntimeSiteIndex.CPU_RATE.getIndex(), new RateComparator());
		// 系统内存
		sorter.setComparator(WatchMixedRuntimeSiteIndex.SYS_USABLE_MEMORY.getIndex(), new MemoryComparator());
		sorter.setComparator(WatchMixedRuntimeSiteIndex.SYS_MAX_MEMORY.getIndex(), new MemoryComparator());
		sorter.setComparator(WatchMixedRuntimeSiteIndex.SYS_MEMORY_RATE.getIndex(), new RateComparator());
		// 虚拟机内存
		sorter.setComparator(WatchMixedRuntimeSiteIndex.VM_USABLE_MEMORY.getIndex(), new MemoryComparator());
		sorter.setComparator(WatchMixedRuntimeSiteIndex.VM_MAX_MEMORY.getIndex(), new MemoryComparator());
		sorter.setComparator(WatchMixedRuntimeSiteIndex.VM_MEMORY_RATE.getIndex(), new RateComparator());
		// 磁盘
		sorter.setComparator(WatchMixedRuntimeSiteIndex.USABLE_DISK.getIndex(), new DiskComparator());
		sorter.setComparator(WatchMixedRuntimeSiteIndex.MAX_DISK.getIndex(), new DiskComparator());
		sorter.setComparator(WatchMixedRuntimeSiteIndex.DISK_RATE.getIndex(), new RateComparator());
		// 其它参数
		sorter.setComparator(WatchMixedRuntimeSiteIndex.MEMBERS.getIndex(), new StringComparator());
		sorter.setComparator(WatchMixedRuntimeSiteIndex.THREADS.getIndex(), new IntegerComparator());
		sorter.setComparator(WatchMixedRuntimeSiteIndex.COMMANDS.getIndex(), new IntegerComparator());
		sorter.setComparator(WatchMixedRuntimeSiteIndex.INVOKERS.getIndex(), new IntegerComparator());
		sorter.setComparator(WatchMixedRuntimeSiteIndex.OS.getIndex(), new StringComparator());
		sorter.setComparator(WatchMixedRuntimeSiteIndex.CPU.getIndex(), new StringComparator());
	}

	/**
	 * 节点地址的排序
	 * 
	 * @author scott.liang
	 * @version 1.0 4/20/2018
	 * @since laxcus 1.0
	 */
	class NodeComparator implements Comparator<ShowItemCell> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(ShowItemCell e1, ShowItemCell e2) {
			int ret = -1;
			Object o1 = e1.getSymbol();
			Object o2 = e2.getSymbol();
			// 检查地址匹配
			if (Laxkit.isClassFrom(o1, Node.class) && Laxkit.isClassFrom(o2, Node.class)) {
				Node n1 = (Node) o1;
				Node n2 = (Node) o2;
				ret =  Laxkit.compareTo(n1, n2);
			}
			return ret;
		}
	}

	/**
	 * CPU/内存的使用率
	 * 
	 * @author scott.liang
	 * @version 1.0 4/20/2018
	 * @since laxcus 1.0
	 */
	class RateComparator implements Comparator<ShowItemCell> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(ShowItemCell e1, ShowItemCell e2) {
			Object str1 = e1.visible();
			Object str2 = e2.visible();
			int ret = -1;
			// 检查字符串的比率
			if (Laxkit.isClassFrom(str1, String.class) && Laxkit.isClassFrom(str2, String.class)) {
				double n1 = ConfigParser.splitRate((String)str1, -1f);
				double n2 = ConfigParser.splitRate((String)str2, -1f);
				ret =  Laxkit.compareTo(n1, n2);
			}
			return ret;
		}
	}

	/**
	 * 内存比较
	 * 
	 * @author scott.liang
	 * @version 1.0 4/20/2018
	 * @since laxcus 1.0
	 */
	class MemoryComparator implements Comparator<ShowItemCell> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(ShowItemCell e1, ShowItemCell e2) {
			Object str1 = e1.visible();
			Object str2 = e2.visible();
			int ret = -1;
			// 检查字符串的比率
			if (Laxkit.isClassFrom(str1, String.class) && Laxkit.isClassFrom(str2, String.class)) {
				double n1 = ConfigParser.splitDoubleCapacity((String) str1, -1f);
				double n2 = ConfigParser.splitDoubleCapacity((String) str2, -1f);
				ret = Laxkit.compareTo(n1, n2);
			}
			return ret;
		}
	}

	/**
	 * 磁盘比较
	 * 
	 * @author scott.liang
	 * @version 1.0 4/20/2018
	 * @since laxcus 1.0
	 */
	class DiskComparator implements Comparator<ShowItemCell> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(ShowItemCell e1, ShowItemCell e2) {
			Object str1 = e1.visible();
			Object str2 = e2.visible();
			int ret = -1;
			// 检查字符串的比率
			if (Laxkit.isClassFrom(str1, String.class) && Laxkit.isClassFrom(str2, String.class)) {
				double n1 = ConfigParser.splitDoubleCapacity((String)str1, -1f);
				double n2 = ConfigParser.splitDoubleCapacity((String)str2, -1f);
				ret =  Laxkit.compareTo(n1, n2);
			}
			return ret;
		}
	}

	/**
	 * 命令/调用器的统计数字
	 * 
	 * @author scott.liang
	 * @version 1.0 4/20/2018
	 * @since laxcus 1.0
	 */
	class IntegerComparator implements Comparator<ShowItemCell> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(ShowItemCell e1, ShowItemCell e2) {
			Object str1 = e1.visible();
			Object str2 = e2.visible();
			int ret = -1;
			// 检查字符串的比率
			if (Laxkit.isClassFrom(str1, String.class) && Laxkit.isClassFrom(str2, String.class)) {
				int n1 = ConfigParser.splitInteger((String) str1, -1);
				int n2 = ConfigParser.splitInteger((String) str2, -1);
				ret = Laxkit.compareTo(n1, n2);
			}
			return ret;
		}
	}

	/**
	 * 字符串参数比较器
	 * 
	 * @author scott.liang
	 * @version 1.0 4/20/2018
	 * @since laxcus 1.0
	 */
	class StringComparator implements Comparator<ShowItemCell> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(ShowItemCell e1, ShowItemCell e2) {
			Object str1 = e1.visible();
			Object str2 = e2.visible();
			int ret = -1;
			// 比较字符串
			if (Laxkit.isClassFrom(str1, String.class) && Laxkit.isClassFrom(str2, String.class)) {
				ret = Laxkit.compareTo((String)str1, (String)str2);
			}
			return ret;
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
				if (bf.length() > 0) bf.append(" ");

				TableColumn element = table.getTableHeader().getColumnModel().getColumn(column);
				String name = (String) element.getHeaderValue();

				// 列定位到实际位置，没有返回-1
				column = model.findColumn(name);
				if(column < 0) continue;

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

	/**
	 * 设置表对
	 */
	private void initHeader() {
		// "resources.xml"中的标签
		WatchMixedRuntimeSiteIndex[] indexes = WatchMixedRuntimeSiteIndex.total();
		for (WatchMixedRuntimeSiteIndex e : indexes) {
			String describe = e.getConfigDescribe();
			String title = String.format("Window/RuntimePanel/%s/title", describe);
			String width = String.format("Window/RuntimePanel/%s/width", describe);

			// 标题和宽度
			String s1 = WatchLauncher.getInstance().findCaption(title);
			String s2 = WatchLauncher.getInstance().findCaption(width);
			int columnWidth = ConfigParser.splitInteger(s2, 120);

			ShowTitleCell cell = new ShowTitleCell(e.getIndex(), s1, columnWidth);

			TableColumn column = new TableColumn(cell.getIndex(), cell.getWidth());
			column.setHeaderValue(cell.getName());
			
			// 显示站点参数的标题
			model.addColumn(cell);
			table.addColumn(column);
		}
	}

	/**
	 * 显示/更新节点状态
	 * @param runtime
	 */
	public void show(SiteRuntime runtime) {
		Laxkit.nullabled(runtime);

		// 暂存参数，定时批量显示
		siteTask.show(runtime);
	}

	/**
	 * 删除节点记录
	 * @param node
	 */
	public void drop(Node node) {
		Laxkit.nullabled(node);

		// 暂存参数，定时批量删除
		siteTask.drop(node);
	}
	
	/**
	 * 显示插入的行
	 */
	class InsertSiteRuntimeThread extends SwingEvent {
		/** 行数 **/
		ArrayList<Integer> rows = new ArrayList<Integer>();

		InsertSiteRuntimeThread() {
			super();
		}

		public void add(int row) {
			rows.add(row);
		}

		public int size() {
			return rows.size();
		}

		public void process() {
			for (int row : rows) {
				model.refreshInsertRow(row);
			}
		}
	}
	
	/**
	 * 刷新一行记录
	 */
	class UpdateSiteRuntimeThread extends SwingEvent {
		/** 行数 **/
		ArrayList<Integer> rows = new ArrayList<Integer>();

		UpdateSiteRuntimeThread() {
			super();
		}

		public void add(int row) {
			rows.add(row);
		}

		public int size() {
			return rows.size();
		}

		public void process() {
			for (int row : rows) {
				model.refreshUpdateRow(row);
			}
		}
	}
	
	/**
	 * 删除一行记录
	 */
	class RemoveSiteRuntimeThread extends SwingEvent {
		/** 行数 **/
		ArrayList<Integer> rows = new ArrayList<Integer>();

		RemoveSiteRuntimeThread() {
			super();
		}

		public void add(int row) {
			rows.add(row);
		}

		public int size() {
			return rows.size();
		}

		public void process() {
			for (int row : rows) {
				model.removeRow(row);
			}
		}
	}
	
	/**
	 * 站点参数显示/删除任务
	 *
	 * @author scott.liang
	 * @version 1.0 8/22/2018
	 * @since laxcus 1.0
	 */
	class SiteTask extends TimerTask {

		SingleLock lock = new SingleLock();

		ArrayList<SiteRuntime> infuses = new ArrayList<SiteRuntime>();

		ArrayList<Node> effuses = new ArrayList<Node>();
		
		/**
		 * 清除全部记录
		 */
		public void clear() {
			lock.lock();
			try {
				infuses.clear();
				effuses.clear();
			} finally {
				lock.unlock();
			}
		}

		/**
		 * 保存参数
		 * @param e
		 */
		public void show(SiteRuntime e) {
			lock.lock();
			try {
				infuses.add(e);
			} finally {
				lock.unlock();
			}
		}
		
		/**
		 * 排序
		 */
		private void sort() {
			lock.lock();
			try {
				Collections.sort(infuses);
			} finally {
				lock.unlock();
			}
		}

		private SiteRuntime popSiteRuntime() {
			lock.lock();
			try {
				if (infuses.size() > 0) {
					return infuses.remove(0);
				}
			} catch (Throwable e) {
				Logger.fatal(e);
			} finally {
				lock.unlock();
			}
			return null;
		}
		
		/**
		 * 删除
		 * @param node
		 */
		public void drop(Node node) {
			lock.lock();
			try {
				effuses.add(node);
			} finally {
				lock.unlock();
			}
		}
		
		/**
		 * 弹出节点
		 * @return
		 */
		private Node popNode() {
			lock.lock();
			try {
				if (effuses.size() > 0) {
					return effuses.remove(0);
				}
			} catch (Throwable e) {
				Logger.fatal(e);
			} finally {
				lock.unlock();
			}
			return null;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
			int inlen = infuses.size();
			int eflen = effuses.size();

			int size = inlen + eflen;
			if (size == 0) {
				return;
			}
			
			// 排序
			if (inlen > 0) {
				sort();
			}
			
			InsertSiteRuntimeThread insert = new InsertSiteRuntimeThread();
			UpdateSiteRuntimeThread update = new UpdateSiteRuntimeThread();
			RemoveSiteRuntimeThread remove = new RemoveSiteRuntimeThread();

			// 取出增加
			for (int i = 0; i < inlen; i++) {
				SiteRuntime runtime = popSiteRuntime();
				if (runtime == null) {
					continue;
				}

				int row = model.findRow(runtime.getNode());
				ShowItem item = model.createShowItem(runtime);
				
				if (row < 0) {
					row = model.insertRow(item);
					insert.add(row);
				} else {
					boolean success = model.replaceRow(row, item);
					if (success) {
						update.add(row);
					}
				}
			}
			
			// 取出删除的
			for (int i = 0; i < eflen; i++) {
				Node node = popNode();
				if (node == null) {
					continue;
				}
				// 找到行下标
				int row = model.findRow(node);
				if (row >= 0) {
					remove.add(row);
				}
			}
			
			// 批量保存后，放进去，必须按照这个顺序处理
			if (insert.size() > 0) {
				addThread(insert);
			}
			if (update.size() > 0) {
				addThread(update);
			}
			if (remove.size() > 0) {
				addThread(remove);
			}
		}
		
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