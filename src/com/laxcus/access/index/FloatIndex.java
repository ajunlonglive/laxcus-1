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
 * <code>SQL WHERE</code>检索时的单浮点(BIT32)检索索引值。<br>
 * 格式如: WHERE column1 = float value
 * 
 * @author scott.liang
 * @version 1.0 5/11/2009
 * @since laxcus 1.0
 */
public final class FloatIndex extends ColumnIndex {

	private static final long serialVersionUID = 5745454421641811539L;

	/** 被比较单浮点值，区别与列参数 **/
	private float hash;

	/**
	 * 根据传入参数，构造一个单浮点的检索索引的副本
	 * @param that
	 */
	private FloatIndex(FloatIndex that) {
		super(that);
		hash = that.hash;
	}

	/**
	 * 构造一个默认的单浮点的检索索引
	 */
	public FloatIndex() {
		super(IndexType.FLOAT_INDEX);
		hash = 0.0f;
	}

	/**
	 * 构造一个单浮点的检索索引，并且指定它的被比较值
	 * @param hash 被比较单浮点值
	 */
	public FloatIndex(float hash) {
		this();
		setHash(hash);
	}

	/**
	 * 构造一个单浮点的检索索引，并且指定它的被比较值和比较列
	 * @param hash 被比较单浮点值
	 * @param column 被比较列
	 */
	public FloatIndex(float hash, Column column) {
		this(hash);
		super.setColumn(column);
	}

	/**
	 * 设置单浮点检索索引的被比较被比较值
	 * @param param 被比较单浮点值
	 */
	public void setHash(float param) {
		hash = param;
	}

	/**
	 * 返回单浮点检索索引的被比较被比较值
	 * @return 被比较单浮点值
	 */
	public float getHash() {
		return hash;
	}

	/*
	 * 根据当前单浮点的检索索引参数，生成它的数据副本
	 * @see com.laxcus.access.index.WhereIndex#duplicate()
	 */
	@Override
	public FloatIndex duplicate() {
		return new FloatIndex(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 类型定义
		writer.write(super.getFamily());
		// 索引值
		writer.writeFloat(hash);
		// 列标识(column identity)
		writer.writeShort(getColumnId());
		// 列数据(输出数据到缓存)
		writer.writeObject(super.getColumn());

		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();

		super.setFamily(reader.read());
		hash = reader.readFloat();
		short columnId = reader.readShort();
		// 只允许单浮点列
		byte state = reader.current();
		if (ColumnType.resolveType(state) != ColumnType.FLOAT) {
			throw new ColumnException("cannot support %d!", state);
		}

		Column column = ColumnCreator.resolve(reader);
		column.setId(columnId);
		// 保存它
		super.setColumn(column);

		return reader.getSeek() - seek;
	}

}