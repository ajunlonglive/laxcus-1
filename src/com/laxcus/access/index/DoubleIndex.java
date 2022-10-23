/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index;

import com.laxcus.access.column.*;
import com.laxcus.access.type.*;
import com.laxcus.util.classable.*;

/**
 * <code>SQL WHERE</code>检索时的双浮点(BIT64)检索索引值。<br>
 * 基本格式： WHERE column name > double value
 * 
 * @author scott.liang
 * @version 1.0 5/2/2009
 * @since laxcus 1.0
 */
public final class DoubleIndex extends ColumnIndex {

	private static final long serialVersionUID = -2491051309447776128L;

	/** 双浮点被比较值，区别与实际的列参数 **/
	private double hash;

	/**
	 * 使用传入的双浮点检索索引参数，生成它的数据副本
	 * @param that DoubleIndex实例
	 */
	private DoubleIndex(DoubleIndex that) {
		super(that);
		hash = that.hash;
	}

	/**
	 * 构造一个默认的双浮点检索索引
	 */
	public DoubleIndex() {
		super(IndexType.DOUBLE_INDEX);
		hash = 0.0f;
	}

	/**
	 * 构造一个双浮点的检索索引，并且指定它的被比较值
	 * @param hash  被比较值
	 */
	public DoubleIndex(double hash) {
		this();
		setHash(hash);
	}

	/**
	 * 构造一个双浮点的检索索引，并且指定它的被比较值和比较列
	 * @param hash  被比较值
	 * @param column 列实例
	 */
	public DoubleIndex(double hash, Column column) {
		this(hash);
		setColumn(column);
	}

	/**
	 * 设置双浮点检索索引的被比较被比较值
	 * @param param 被比较值
	 */
	public void setHash(double param) {
		hash = param;
	}

	/**
	 * 返回双浮点检索索引的被比较被比较值
	 * @return 被比较值
	 */
	public double getHash() {
		return hash;
	}

	/*
	 * 根据当前双浮点的检索索引参数，生成它的数据副本
	 * @see com.laxcus.access.index.WhereIndex#duplicate()
	 */
	@Override
	public DoubleIndex duplicate() {
		return new DoubleIndex(this);
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
		// 索引值
		writer.writeDouble(hash);
		// 列标识
		writer.writeShort(getColumnId());
		// 列数据(输出数据到缓存)
		writer.writeObject(super.getColumn());
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
		hash = reader.readDouble();
		short columnId = reader.readShort();
		// 只定义双浮点列
		byte state = reader.current();
		if (ColumnType.resolveType(state) != ColumnType.DOUBLE) {
			throw new ColumnException("cannot support %d!", state);
		}

		Column column = ColumnCreator.resolve(reader);
		column.setId(columnId);
		// 保存它
		setColumn(column);

		// 返回解析长度
		return reader.getSeek() - seek;
	}

}