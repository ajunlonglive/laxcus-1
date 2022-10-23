/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
 * WATCH站点表格模型
 * 
 * @author scott.liang
 * @version 1.2 02/16/2018
 * @since laxcus 1.0
 */
public class WatchMixedRuntimeModel extends AbstractTableModel {

	private static final long serialVersionUID = -1970906976335213838L;

	/** 列头 **/
	private TreeSet<ShowTitleCell> columns = new TreeSet<ShowTitleCell>();

	/** 数据实例 **/
	private ArrayList<ShowItem> array = new ArrayList<ShowItem>();

	/** 图标 **/
	private Icon TOP_ICON;
	private Icon LOG_ICON;

	private Icon HOME_ICON;
	private Icon DATA_ICON;
	private Icon WORK_ICON;
	private Icon BUILD_ICON;
	private Icon CALL_ICON;

	private Icon BANK_ICON;
	private Icon ACCOUNT_ICON;
	private Icon HASH_ICON;
	private Icon GATE_ICON;
	private Icon ENTRANCE_ICON;

	/**
	 * 构造默认的WATCH站点表格模型
	 */
	public WatchMixedRuntimeModel() {
		super();
		loadIcons();
	}

	/**
	 * 加载图标
	 */
	private void loadIcons() {
		ResourceLoader loader = new ResourceLoader("conf/watch/image/sites/");
		int width = 16, height = 16;
		TOP_ICON = loader.findImage("top.png", width, height); // TOP图标
		LOG_ICON = loader.findImage("log.png", width, height); // LOG图标

		HOME_ICON = loader.findImage("home.png", width, height);// HOME图标
		DATA_ICON = loader.findImage("data.png", width, height); // DATA图标
		BUILD_ICON = loader.findImage("build.png", width, height); // BUILD图标
		WORK_ICON = loader.findImage("work.png", width, height); // WORK图标
		CALL_ICON = loader.findImage("call.png", width, height); // CALL图标

		BANK_ICON = loader.findImage("bank.png", width, height); // BANK图标
		ACCOUNT_ICON = loader.findImage("account.png", width, height); // ACCOUNT图标
		HASH_ICON = loader.findImage("hash.png", width, height); // HASH图标
		GATE_ICON = loader.findImage("gate.png", width, height); // GATE图标
		ENTRANCE_ICON = loader.findImage("entrance.png", width, height); // ENTRANCE图标
	}

	/**
	 * 保存标题
	 * @param e
	 */
	public void addColumn(ShowTitleCell e) {
		columns.add(e);
	}

	/**
	 * 保存一组标题
	 * @param a
	 */
	public void addColumns(Collection<ShowTitleCell> a) {
		columns.addAll(a);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return columns.size();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		for (ShowTitleCell e : columns) {
			if (e.getIndex() == column) {
				return e.getName();
			}
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#findColumn(java.lang.String)
	 */
	@Override
	public int findColumn(String name) {
		for (ShowTitleCell e : columns) {
			if (Laxkit.compareTo(name, e.getName(), false) == 0) {
				return e.getIndex();
			}
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return array.size();
	}

	/**
	 * 返回单元列
	 * @param row 行
	 * @param column 列
	 * @return 返回单元列
	 */
	public ShowItemCell getCellAt(int row, int column) {
		if (row < 0 || row >= array.size()) {
			return null;
		}

		ShowItem item = array.get(row);
		return item.get(column);
	}

	/**
	 * 插入一行记录
	 * @param item
	 * @return
	 */
	protected int insertRow(ShowItem item) {
		int rows = array.size();
		array.add(item);
		return rows;
	}

	/**
	 * 刷新一行插入记录
	 * @param rows
	 */
	public void refreshInsertRow(int rows) {
		fireTableRowsInserted(rows, rows);
	}

	/**
	 * 更新一行记录。更新时，忽略图标刷新。
	 * 
	 * @param row
	 * @param item
	 */
	protected void refreshUpdateRow(int row) {
		// 更新一行
		fireTableRowsUpdated(row, row);
	}

	/**
	 * 更换一行参数
	 * @param row
	 * @param item
	 * @return 更换成功返回真，否则假
	 */
	protected boolean replaceRow(int row, ShowItem item) {
		// 更新参数
		ShowItem that = ((row >= 0 && row < array.size()) ? array.get(row) : null);
		boolean success = (that != null);
		if (success) {
			that.clear();
			that.addAll(item);
		}
		return success;
	}

	/**
	 * 删除一行记录
	 * @param row 指定行
	 */
	public void removeRow(int row) {
		if (row >= 0 && row < array.size()) {
			array.remove(row);
			// <FROM - TO>的范围
			fireTableRowsDeleted(row, row);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int column) {
		return getCellAt(row, column);
	}

	/**
	 * 清除全部数据
	 */
	public void clear() {
		int rows = array.size();
		array.clear();
		if (rows > 0) {
			fireTableRowsDeleted(0, rows - 1);
		}
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
	 * 计算内存使用率
	 * @param total 已经使用的
	 * @param max 全部
	 * @return 返回浮点数
	 */
	private double doRate(long total, long max) {
		double rate = 0.0f;
		// 内存必须在于0
		if (total > 0 && max > 0) {
			rate = ((double) total / (double) max) * 100.0f;
		}
		return rate;
	}
	
	/**
	 * 显示内存使用率
	 * @param totalMemory
	 * @param maxMemory
	 * @return 字符串描述的内存比率
	 */
	private String printMemoryRate(long totalMemory, long maxMemory) {
		//		double rate = 0.0f;
		//		// 内存必须在于0
		//		if (totalMemory > 0 && maxMemory > 0) {
		//			rate = ((double) totalMemory / (double) maxMemory) * 100;
		//		}

		double rate = doRate(totalMemory, maxMemory);
		return printRate(rate);
	}

	/**
	 * 显示内存
	 * @param memory 内存容量
	 * @return 返回字符串描述
	 */
	private String printMemory(long memory) {
		// 小于等于0是无效值
		if (memory <= 0) {
			return "0";
		}
		return ConfigParser.splitCapacity(memory, 2);
	}

	/**
	 * 已经占用的磁盘空间
	 * @param cmd 命令
	 * @return 字符串描述
	 */
	private String printUsedDisk(SiteRuntime cmd) {
		return ConfigParser.splitCapacity(cmd.getSysUsedDisk(), 2);
	}

	/**
	 * 全部磁盘空间
	 * @param cmd 命令
	 * @return 字符串描述
	 */
	private String printMaxDisk(SiteRuntime cmd) {
		return ConfigParser.splitCapacity(cmd.getSysMaxDisk(), 2);
	}

	/**
	 * 磁盘使用比率
	 * @return
	 */
	private String printDiskRate(SiteRuntime cmd) {
		long maxDisk = cmd.getSysMaxDisk();
		long usableDisk = cmd.getSysUsedDisk();
		double rate = ((double) usableDisk / (double) maxDisk) * 100.0f;
		return printRate(rate);
	}
	
	/**
	 * 注册成员和在线成员
	 * @param cmd 命令
	 * @return 返回字符串
	 */
	private String printMembers(SiteRuntime cmd) {
		int reg = cmd.getRegisterMembers();
		int online = cmd.getOnlineMembers();

		String text = "";
		if (reg < 0) {
			text = "--";
		} else {
			text = String.format("%d", reg);
		}
		if (online < 0) {
			text = String.format("%s/--", text);
		} else {
			text = String.format("%s/%d", text, online);
		}
		return text;
	}

	/**
	 * 生成站点图标
	 * @param column 列索引
	 * @param node 节点
	 * @return
	 */
	private ShowItemCell createSiteIcon(int column, Node node, String signature) {
		Icon icon = null;
		switch (node.getFamily()) {
		case SiteTag.TOP_SITE:
			icon = TOP_ICON;
			break;
		case SiteTag.HOME_SITE:
			icon = HOME_ICON;
			break;
		case SiteTag.LOG_SITE:
			icon = LOG_ICON;
			break;
		case SiteTag.DATA_SITE:
			icon = DATA_ICON;
			break;
		case SiteTag.BUILD_SITE:
			icon = BUILD_ICON;
			break;
		case SiteTag.WORK_SITE:
			icon = WORK_ICON;
			break;
		case SiteTag.CALL_SITE:
			icon = CALL_ICON;
			break;
		case SiteTag.BANK_SITE:
			icon = BANK_ICON;
			break;
		case SiteTag.ACCOUNT_SITE:
			icon = ACCOUNT_ICON;
			break;
		case SiteTag.HASH_SITE:
			icon = HASH_ICON;
			break;
		case SiteTag.GATE_SITE:
			icon = GATE_ICON;
			break;
		case SiteTag.ENTRANCE_SITE:
			icon = ENTRANCE_ICON;
			break;
		}

		if (icon == null) {
			return new ShowStringCell(column, "cannot be find icon!");
		}

		// 生成图标实例
		ShowImageCell cell = new ShowImageCell(column, icon);
		String tooltip = node.toString();
		if (signature != null) {
			tooltip = String.format("%s - %s", tooltip, signature);
		}
		cell.setTooltip(tooltip);
		cell.setSymbol(node);
		return cell;
	}

	//	/**
	//	 * 生成所在列
	//	 * @param column 列编号，对应WatchSiteIndex
	//	 * @param status 状态命令
	//	 * @return 生成一个单元
	//	 */
	//	private ShowItemCell createItemCell(int column, SiteRuntime status) {
	//		switch(column) {
	//		// 节点
	//		case 0:
	//			return createSiteIcon(column, status.getNode());
	//		// CPU
	//		case 1:
	//			return new ShowStringCell(column, printRate(status.getCPURate()), Color.RED);
	//		// 内存
	//		case 2:
	//			return new ShowStringCell(column, printMemory(status.getTotalMemory()) );
	//		case 3:
	//			return new ShowStringCell(column, printMemory(status.getMaxMemory()) );
	//		case 4:
	//			return new ShowStringCell(column, printMemoryRate(status.getTotalMemory(), status.getMaxMemory()), Color.RED);
	//		// 磁盘
	//		case 5:
	//			return new ShowStringCell(column, printUsedDisk(status));
	//		case 6:
	//			return new ShowStringCell(column, printTotalDisk(status));
	//		case 7:
	//			return new ShowStringCell(column, printDiskRate(status), Color.RED);
	//		// 线程/命令
	//		case 8:
	//			return new ShowIntegerCell(column, status.getThreads());
	//		case 9:
	//			return new ShowIntegerCell(column, status.getCommands());
	//		case 10:
	//			return new ShowIntegerCell(column, status.getInvokers());
	//		// 操作系统/CPU类型
	//		case 11:
	//			return new ShowStringCell(column, status.getOS());
	//		case 12:
	//			return new ShowStringCell(column, status.getCPU());
	//		}
	//		return null;
	//	}

//	/**
//	 * 判断是“溢出”
//	 * @return 如果参数是“溢出(full)”状态，返回指定红色，否则是空指针
//	 */
//	private Color fullColor(boolean full) {
//		Color foreground = null;
//		if (full) {
//			foreground = Skins.findSiteRuntimeFullText(); 
//		}
//		return foreground;
//	}
//	
//	/**
//	 * 判断是“不足”
//	 * @return 如果参数是“不足(missing)”状态，返回指定红色，否则是空指针
//	 */
//	private Color missinColor(boolean missing) {
//		Color foreground = null;
//		if (missing) {
//			foreground = Skins.findSiteRuntimeFullText(); 
//		}
//		return foreground;
//	}
	
	/**
	 * 判断参数达到“限制”状态
	 * @param limit 真或者否
	 * @return 如果参数达到限制状态，返回指定红色，否则是空指针
	 */
	private Color isLimitColor(boolean limit) {
		Color foreground = null;
		if (limit) {
			foreground = Skins.findSiteRuntimeFullText(); 
		}
		return foreground;
	}
	
	/**
	 * 生成所在列
	 * @param column 列编号，对应WatchSiteIndex
	 * @param runtime 节点运行时
	 * @return 生成一个单元
	 */
	private ShowItemCell createItemCell(int column, SiteRuntime runtime) {
		// 节点
		if (WatchMixedRuntimeSiteIndex.ADDRESS.isIndex(column)) {
			return createSiteIcon(column, runtime.getNode(), runtime.getSignature());
		} 
		// CPU使用率
		else if (WatchMixedRuntimeSiteIndex.CPU_RATE.isIndex(column)) {
			Color color = isLimitColor(runtime.isCPUFull());
			String text = printRate(runtime.getCPURate());
			return new ShowStringCell(column, text, text, color);
		}
		// 系统内存
		else if (WatchMixedRuntimeSiteIndex.SYS_USABLE_MEMORY.isIndex(column)) {
			Color color = isLimitColor(runtime.isSysMemoryMissing());
			String text = printMemory(runtime.getSysUsedMemory());
			return new ShowStringCell(column, text, text, color);
		} else if (WatchMixedRuntimeSiteIndex.SYS_MAX_MEMORY.isIndex(column)) {
			Color color = isLimitColor(runtime.isSysMemoryMissing());
			String text = printMemory(runtime.getSysMaxMemory());
			return new ShowStringCell(column, text, text, color);
		} else if (WatchMixedRuntimeSiteIndex.SYS_MEMORY_RATE.isIndex(column)) {
			Color color = isLimitColor(runtime.isSysMemoryMissing());
			String text = printMemoryRate(runtime.getSysUsedMemory(), runtime.getSysMaxMemory());
			return new ShowStringCell(column, text, text, color);
		}
		// 虚拟机内存
		else if (WatchMixedRuntimeSiteIndex.VM_USABLE_MEMORY.isIndex(column)) {
			Color color = isLimitColor(runtime.isVmMemoryMissing());
			String text = printMemory(runtime.getVmUsedMemory());
			return new ShowStringCell(column, text, text, color);
		} else if (WatchMixedRuntimeSiteIndex.VM_MAX_MEMORY.isIndex(column)) {
			Color color = isLimitColor(runtime.isVmMemoryMissing());
			String text = printMemory(runtime.getVmMaxMemory());
			return new ShowStringCell(column, text, text, color);
		} else if (WatchMixedRuntimeSiteIndex.VM_MEMORY_RATE.isIndex(column)) {
			Color color = isLimitColor(runtime.isVmMemoryMissing());
			String text = printMemoryRate(runtime.getVmUsedMemory(), runtime.getVmMaxMemory());
			return new ShowStringCell(column, text, text, color);
		}
		// 系统磁盘
		else if (WatchMixedRuntimeSiteIndex.USABLE_DISK.isIndex(column)) {
			Color color = isLimitColor(runtime.isSysDiskMissing());
			String text = printUsedDisk(runtime);
			return new ShowStringCell(column, text, text, color);
		} else if (WatchMixedRuntimeSiteIndex.MAX_DISK.isIndex(column)) {
			Color color = isLimitColor(runtime.isSysDiskMissing());
			String text = printMaxDisk(runtime);
			return new ShowStringCell(column, text, text, color);
		} else if (WatchMixedRuntimeSiteIndex.DISK_RATE.isIndex(column)) {
			Color color = isLimitColor(runtime.isSysDiskMissing());
			String text = printDiskRate(runtime);
			return new ShowStringCell(column, text, text, color);
		} 
		// 成员
		else if (WatchMixedRuntimeSiteIndex.MEMBERS.isIndex(column)) {
			String text = printMembers(runtime);
			return new ShowStringCell(column, text, text);
		}
		// 启用线程
		else if (WatchMixedRuntimeSiteIndex.THREADS.isIndex(column)) {
			String text = String.format("%d", runtime.getThreads());
			return new ShowStringCell(column, text, text);
		} 
		// 命令/调用器数目
		else if (WatchMixedRuntimeSiteIndex.COMMANDS.isIndex(column)) {
			String text = String.format("%d", runtime.getCommands());
			return new ShowStringCell(column, text, text);
		} else if (WatchMixedRuntimeSiteIndex.INVOKERS.isIndex(column)) {
			String text = String.format("%d", runtime.getInvokers());
			return new ShowStringCell(column, text, text);
		} 
		// 操作系统/CPU类型
		else if (WatchMixedRuntimeSiteIndex.OS.isIndex(column)) {
			String os =  runtime.getOS();
			return new ShowStringCell(column, os, os);
		} else if (WatchMixedRuntimeSiteIndex.CPU.isIndex(column)) {
			String cpu = runtime.getCPU();
			return new ShowStringCell(column, cpu, cpu);
		}

		return null;
	}

	/**
	 * 根据节点地址，找到实例所在下标
	 * @param node 节点地址
	 * @return 返回所在下标，没有返回-1
	 */
	protected int findRow(Node node) {
		Iterator<ShowItem> iterator = array.iterator();
		for (int index = 0; iterator.hasNext(); index++) {
			ShowItem item = iterator.next();
			for (ShowItemCell cell : item.list()) {
				Object symbol = cell.getSymbol();
				if (symbol == null || symbol.getClass() != Node.class) {
					continue;
				}
				// 判断匹配
				Node that = (Node) symbol;
				if (Laxkit.compareTo(that, node) == 0) {
					return index;
				}
			}
		}
		return -1;
	}

	/**
	 * 生成一个显示单元
	 * @param runtime
	 * @return
	 */
	protected ShowItem createShowItem(SiteRuntime runtime){
		ShowItem item = new ShowItem();
		for (int n = 0; n < columns.size(); n++) {
			item.add(createItemCell(n, runtime));
		}
		return item;
	}

	//	/**
	//	 * 设置一行状态
	//	 * @param status
	//	 */
	//	public void addSiteRuntime(SiteRuntime status) {
	//		int row = findRow(status.getNode());
	//		if (row < 0) {
	//			addRow(status);
	//		} else {
	//			updateRow(row, status);
	//		}
	//	}
	//
	//	/**
	//	 * 删除一行节点状态
	//	 * @param node
	//	 */
	//	public void removeSiteRuntime(Node node) {
	//		int index = findRow(node);
	//		if (index >= 0) {
	//			removeRow(index);
	//		}
	//	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int column) {
		Object e = getValueAt(0, column);
		return (e != null ? e.getClass() : null);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		ShowItemCell cell = getCellAt(row, column);
		return (cell != null ? cell.isEditable() : false);
	}

}