/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index;

import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 双列索引
 * 
 * @author scott.liang
 * @version 1.0 12/13/2020
 * @since laxcus 1.0
 */
public class DockIndex extends WhereIndex {
	
	private static final long serialVersionUID = -4710232560210477147L;

	/** 左侧列 **/
	private Dock left;
	
	/** 右侧列 **/
	private Dock right;

	/**
	 * 使用传入的双列索引索引参数，生成它的数据副本
	 * @param that DockIndex实例
	 */
	private DockIndex(DockIndex that) {
		super(that);
		left = that.left;
		right = that.right;
	}

	/**
	 * 构造一个默认的双列索引索引
	 */
	public DockIndex() {
		super(IndexType.DOCK_INDEX);
	}

	/**
	 * 构造一个双浮点的检索索引，并且指定它的被比较值
	 * @param left 左侧列
	 * @param right 右侧列
	 */
	public DockIndex(Dock left, Dock right) {
		this();
		setLeft(left);
		setRight(right);
	}

	/**
	 * 设置左列
	 * @param e
	 */
	public void setLeft(Dock e) {
		Laxkit.nullabled(e);
		left = e;
	}

	/**
	 * 返回左列
	 * @return
	 */
	public Dock getLeft() {
		return left;
	}

	/**
	 * 设置右列
	 * @param e
	 */
	public void setRight(Dock e) {
		Laxkit.nullabled(e);
		right = e;
	}

	/**
	 * 返回右列
	 * @return
	 */
	public Dock getRight() {
		return right;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.WhereIndex#getColumnId()
	 */
	@Override
	public short getColumnId() {
		return left.getColumnId();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.WhereIndex#setColumnId(short)
	 */
	@Override
	public void setColumnId(short id) {

	}

	/*
	 * 根据当前双列索引参数，生成它的数据副本
	 * @see com.laxcus.access.index.WhereIndex#duplicate()
	 */
	@Override
	public DockIndex duplicate() {
		return new DockIndex(this);
	}

	/**
	 * 输出双浮点索引数据流到缓存
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 类型定义
		writer.write(getFamily());
		// 左列
		writer.writeObject(left);
		// 右列
		writer.writeObject(right);
		// 返回解析长度
		return writer.size() - size;
	}

	/**
	 * 解析双浮点数据流，返回解析字节长度
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		// 定位
		final int seek = reader.getSeek();

		setFamily(reader.read());
		left = new Dock(reader);
		right = new Dock(reader);

		// 返回解析长度
		return reader.getSeek() - seek;
	}

}