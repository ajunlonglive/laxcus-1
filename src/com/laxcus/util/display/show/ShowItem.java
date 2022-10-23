/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

import java.util.*;
import java.io.*;

import com.laxcus.util.*;

/**
 * 表数据项 <br>
 * 由多个数据单元组成，长度必须与ShowTitle保持一致，概念上对应数据库的“行（row）”。
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public class ShowItem implements Cloneable, Serializable {

	private static final long serialVersionUID = 7592551547896715494L;

	/** 数据单元 **/
	private TreeMap<Integer, ShowItemCell> array = new TreeMap<Integer, ShowItemCell>();

	/**
	 * 根据传入实例生成浅层数据副本
	 * @param that ShowItem实例
	 */
	private ShowItem(ShowItem that) {
		super();
		array.putAll(that.array);
	}

	/**
	 * 构造默认的表数据项
	 */
	public ShowItem() {
		super();
	}

	/**
	 * 构造默认的表数据项，指定一批单元
	 * @param a
	 */
	public ShowItem(ShowItemCell[] a) {
		this();
		addAll(a);
	}

	/**
	 * 保存数据单元，不允许空指针
	 * @param e ShowItemCell实例
	 * @return 新增返回真，否则假
	 */
	public boolean add(ShowItemCell e) {
		Laxkit.nullabled(e);

		return array.put(e.getIndex(), e) == null;
	}

	/**
	 * 保存一组数据单元
	 * @param e 数据单元
	 * @return 新增成员数目
	 */
	public int addAll(ShowItem e) {
		Laxkit.nullabled(e);
		int size = array.size();
		array.putAll(e.array);
		return array.size() - size;
	}

	/**
	 * 保存一组数据单元
	 * @param a ShowItemCell子类数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<ShowItemCell> a) {
		int size = array.size();
		for (ShowItemCell e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一组数据单元
	 * @param a ShowItemCell子类数组
	 * @return 返回新增成员数目
	 */
	public int addAll(ShowItemCell[] a) {
		int size = array.size();
		for (ShowItemCell e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 返回指定下标的列
	 * @param index 索引
	 * @return 显示单元
	 */
	public ShowItemCell get(int index) {
		return array.get(index);
	}

	/**
	 * 返回类定义
	 * @param index 列索引
	 * @return 列实例
	 */
	public Class<?> getColumnClass(int index) {
		Object e = get(index);
		return (e == null ? null : e.getClass());
	}

	/**
	 * 输出全部数据单元
	 * @return ShowItemCell列表
	 */
	public List<ShowItemCell> list() {
		return new ArrayList<ShowItemCell>(array.values());
	}
	
	/**
	 * 清除数据单元
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 输出数组
	 * @return 单元数组
	 */
	public ShowItemCell[] toArray() {
		ShowItemCell[] a = new ShowItemCell[array.size()];
		return array.values().toArray(a);
	}

	/**
	 * 输出单元向量
	 * @return
	 */
	public Vector<ShowItemCell> toVector() {
		return new Vector<ShowItemCell>(array.values());
		// Vector a= new Vector();
		// a.addAll(array);
		// return a;
	}

	/**
	 * 统计数据单元数目
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
	 * @return ShowItem实例
	 */
	public ShowItem duplicate() {
		return new ShowItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

}