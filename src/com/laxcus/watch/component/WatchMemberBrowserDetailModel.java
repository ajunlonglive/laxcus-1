/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import com.laxcus.util.*;
import com.laxcus.util.display.show.*;

/**
 * LAXCUS集群成员表格模式
 * 
 * @author scott.liang
 * @version 1.0 1/12/2020
 * @since laxcus 1.0
 */
public class WatchMemberBrowserDetailModel extends DefaultTableModel {

	private static final long serialVersionUID = -6950417496204302683L;

	/** 标题栏 **/
	private ShowTitle title;

	/** 数据栏 **/
	private ArrayList<ShowItem> array = new ArrayList<ShowItem>();

	/**
	 * 默认的运行时参数表格模式
	 */
	public WatchMemberBrowserDetailModel() {
		super();
	}

	/**
	 * 设置标题
	 * @param e
	 */
	public void setTitle(ShowTitle e) {
		title = e;
	}

	/**
	 * 返回标题
	 * @return
	 */
	public ShowTitle getTitle() {
		return title;
	}

	/**
	 * 清除全部数据
	 */
	public void clear() {
		int size = getRowCount();
		for (int index = 0; index < size; index++) {
			removeRow(0);
		}
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

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		if (title == null) {
			return super.getColumnCount();
		}
		return title.size();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#removeRow(int)
	 */
	@Override
	public void removeRow(int index) {
		if (index >= 0 && index < array.size()) {
			array.remove(index);
		}
		super.removeRow(index);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		if (column < 0 || column >= title.size()) {
			return null;
		}
		ShowTitleCell cell = title.get(column);
		return cell.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#findColumn(java.lang.String)
	 */
	@Override
	public int findColumn(String name) {
		if (title != null) {
			for (ShowTitleCell e : title.list()) {
				if (Laxkit.compareTo(name, e.getName(), false) == 0) {
					return e.getIndex();
				}
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (array.isEmpty()) {
			return null;
		}

		ShowItem item = array.get(0);
		ShowItemCell cell = item.get(columnIndex);
		return (cell != null ? cell.getClass() : null);
	}

	/**
	 * 保存一行数据
	 * @see javax.swing.table.DefaultTableModel#addRow(java.lang.Object[])
	 */
	@Override
	public void addRow(Object[] a) {
		ShowItem item = new ShowItem();
		for (int i = 0; i < a.length; i++) {
			if (!Laxkit.isClassFrom(a[i], ShowItemCell.class)) {
				throw new ClassCastException("cannot be cast to ShowItemCell");
			}
			item.add((ShowItemCell) a[i]);
		}
		addRow(item);
	}

	/**
	 * 保存一行数据
	 * @param item
	 */
	public void addRow(ShowItem item) {
		Laxkit.nullabled(item);

		array.add(item);

		// 交给上级片
		super.addRow(item.toArray());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int column) {
		return getCellAt(row, column);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object value, int row, int column) {
		if (row < 0 || row >= array.size()) {
			return;
		}

		// 必须继承自ShowItemCell
		if (!Laxkit.isClassFrom(value, ShowItemCell.class)) {
			throw new ClassCastException("cannot be cast to ShowItemCell");
		}

		ShowItem item = array.get(row);
		item.add((ShowItemCell) value);

		// 交给上级处理
		super.setValueAt(value, row, column);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		ShowItemCell cell = getCellAt(row, column);
		return (cell == null ? false : cell.isEditable());
	}

}