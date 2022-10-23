/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 表格表格标题单元 <br>
 * 在概念上对应数据库的“列属性”（ColumnAttribute）。表格表格标题单元下标从0，依次增加。
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public final class ShowTitleCell implements Classable, Serializable, Cloneable, Comparable<ShowTitleCell> {

	private static final long serialVersionUID = -2513795918750597699L;

	/** 基于0下标的排序编号 **/
	private int index;

	/** 显示文本 **/
	private String name;

	/** 宽度 **/
	private int width;

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeInt(index);
		writer.writeInt(width);
		writer.writeString(name);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		index = reader.readInt();
		width = reader.readInt();
		name = reader.readString();
		return reader.getSeek() - seek;
	}

	/**
	 * 构造默认和私有的表格标题单元
	 */
	private ShowTitleCell() {
		super();
		// 默认宽度80像素
		width = 80;
		// 索引无定义
		index = -1;
	}

	/**
	 * 根据传入的表格标题单元实例，生成它的数据副本
	 * @param that ShowTitleCell实例
	 */
	private ShowTitleCell(ShowTitleCell that) {
		super();
		index = that.index;
		width = that.index;
		name = that.name;
	}

	/**
	 * 从可类化读取器中解析表格标题单元参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public ShowTitleCell(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造表格标题单元，指定它的标题索引号
	 */
	public ShowTitleCell(int index) {
		this();
		setIndex(index);
	}

	/**
	 * 构造表格标题单元，指定它的标题索引号和显示文本
	 * @param index 标题索引号
	 * @param text 显示文本
	 */
	public ShowTitleCell(int index, String text) {
		this(index);
		setName(text);
	}

	/**
	 * 构造表格标题单元，指定它的全部参数。
	 * @param index 标题索引号
	 * @param text 显示文本
	 * @param width 列宽度
	 */
	public ShowTitleCell(int index, String text, int width) {
		this(index, text);
		setWidth(width);
	}

	/**
	 * 设置标题索引号
	 * @param i 标题索引号
	 */
	public void setIndex(int i) {
		index = i;
	}

	/**
	 * 返回标题索引号
	 * @return 标题索引号
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * 设置标题宽度
	 * @param i 标题宽度
	 */
	public void setWidth(int i) {
		width = i;
	}

	/**
	 * 返回标题宽度
	 * @return 标题宽度
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * 设置标题文本
	 * @param e String实例
	 */
	public void setName(String e) {
		name = e;
	}

	/**
	 * 返回标题文本
	 * @return String实例
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 返回标题的文本描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * 比较两个对象一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != ShowTitleCell.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((ShowTitleCell) that) == 0;
	}

	/**
	 * 返回标题散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return index;
	}

	/**
	 * 克隆一个当前实例的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new ShowTitleCell(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ShowTitleCell that) {
		if(that == null) {
			return 1;
		}
		return Laxkit.compareTo(index, that.index);
	}

}