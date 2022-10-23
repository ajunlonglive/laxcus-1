/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

import java.io.*;
import java.util.*;

import javax.swing.table.*;

import com.laxcus.util.*;

/**
 * 表显示标题 <br>
 * 
 * 由多个标题单元组成。
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public class ShowTitle implements Serializable, Cloneable {

	private static final long serialVersionUID = -1448010940663431809L;

	/** 标题单元 **/
	private TreeMap<java.lang.Integer, ShowTitleCell> array = new TreeMap<java.lang.Integer, ShowTitleCell>();

	/**
	 * 根据传入实例，生成一个表显示标题浅层数据副本
	 * @param that ShowTitle实例
	 */
	private ShowTitle(ShowTitle that) {
		super();
		array.putAll(that.array);
	}

	/**
	 * 构造默认的表显示标题
	 */
	public ShowTitle() {
		super();
	}

	/**
	 * 保存标题单元，不允许空指针
	 * @param e ShowTitleCell实例
	 * @return 新增加返回真，否则假
	 */
	public boolean add(ShowTitleCell e) {
		Laxkit.nullabled(e);

		return array.put(e.getIndex(), e) == null;
	}

	/**
	 * 保存一组标题单元
	 * @param a ShowTitleCell数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<ShowTitleCell> a) {
		int size = array.size();
		for (ShowTitleCell e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 返回指定下标的单元
	 * @param index
	 * @return 返回标题单元
	 */
	public ShowTitleCell get(int index) {
		return array.get(index);
	}

	/**
	 * 输出全部标题单元
	 * @return ShowTitleCell列表
	 */
	public List<ShowTitleCell> list() {
		return new ArrayList<ShowTitleCell>(array.values());
	}

	/**
	 * 统计标题单元数目
	 * @return 单元数目
	 */
	public int size() {
		return array.size();
	}
	
	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return ShowTitle实例
	 */
	public ShowTitle duplicate() {
		return new ShowTitle(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 生成表格单元
	 * @return 返回TableColumn数组
	 */
	public TableColumn[] createTableColumns() {
		// 重置标题栏
		int size = array.size();
		TableColumn[] columns = new TableColumn[size];

		Iterator<Map.Entry<Integer, ShowTitleCell>> iterator = array.entrySet().iterator();
		int index = 0;
		while (iterator.hasNext()) {
			Map.Entry<Integer, ShowTitleCell> entry = iterator.next();
			ShowTitleCell cell = entry.getValue();
			columns[index] = new TableColumn(index, cell.getWidth());
			columns[index].setHeaderValue(cell.toString());
			index++;
		}

		return columns;
	}
	
}