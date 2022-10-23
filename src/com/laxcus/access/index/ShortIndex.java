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
 * <code>SQL WHERE</code>检索时的短整型(BIT16)检索索引值。<br>
 * 格式如：WHERE column1 > short value 
 * 
 * @author scott.liang
 * @version 1.0 5/2/2009
 * @since laxcus 1.0
 */
public final class ShortIndex extends ColumnIndex {

	private static final long serialVersionUID = 2850445449306695728L;

	/** 被比较短整型值，用于定位数据区域 */
	private short hash;

	/**
	 * 使用传入参数，建立一个短整型索引检查值的副本
	 * @param that
	 */
	private ShortIndex(ShortIndex that) {
		super(that);
		hash = that.hash;
	}

	/**
	 * 构造一个短整型索引检索值
	 */
	public ShortIndex() {
		super(IndexType.SHORT_INDEX);
		hash = 0;
	}

	/**
	 * 构造一个短整型索引检索值，并且指定的被比较短整型值
	 * @param hash 被比较短整型值
	 */
	public ShortIndex(short hash) {
		this();
		setHash(hash);
	}

	/**
	 * 构造一个短整型索引检索值，并且指定被比较短整型值和被比较列
	 * @param hash 被比较短整型值
	 * @param column 被比较列
	 */
	public ShortIndex(short hash, Column column) {
		this(hash);
		setColumn(column);
	}

	/**
	 * 设置被比较短整型值
	 * @param num 被比较短整型值
	 */
	public void setHash(short num) {
		hash = num;
	}

	/**
	 * 返回被比较短整型值
	 * @return 被比较短整型值
	 */
	public short getHash() {
		return hash;
	}

	/*
	 * 根据当前参数，生成一个短整型索引比较副本
	 * @see com.laxcus.access.index.WhereIndex#duplicate()
	 */
	@Override
	public ShortIndex duplicate() {
		return new ShortIndex(this);
	}
	
	/**
	 * 输出短整型索引数据流到缓存
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();

		// 类型定义
		writer.write(super.getFamily());
		// 索引值
		writer.writeShort(hash);
		// 列标识号
		writer.writeShort(getColumnId());
		// 列数据(输出数据到缓存)
		writer.writeObject(super.getColumn());

		return writer.size() - size;
	}
	
	/**
	 * 解析数据流，返回解析字节长度
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();

		super.setFamily(reader.read());
		hash = reader.readShort();
		short columnId = reader.readShort();
		// 只允许short
		byte state = reader.current();
		if (ColumnType.resolveType(state) != ColumnType.SHORT) {
			throw new ColumnException("cannot support %d!", state);
		}

		Column column = ColumnCreator.resolve(reader);
		column.setId(columnId);
		// 保存它
		super.setColumn(column);

		return reader.getSeek() - seek;
	}

}