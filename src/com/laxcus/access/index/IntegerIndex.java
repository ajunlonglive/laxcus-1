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
 * <code>SQL WHERE</code>检索时的整型(BIT32)检索索引值。<br>
 * 格式如: WHERE column1 < int value
 * 
 * @author scott.liang
 * @version 1.0 5/3/2009
 * @since laxcus 1.0
 */
public final class IntegerIndex extends ColumnIndex {

	private static final long serialVersionUID = -5150650384022906969L;

	/** 被比较整型值，用于定位数据区域，区别被比较的列参数 */
	private int hash;

	/**
	 * 根据传入参数，构造一个整型检索索引的副本
	 * @param that
	 */
	private IntegerIndex(IntegerIndex that) {
		super(that);
		hash = that.hash;
	}

	/**
	 * 构造一个整型检索索引值
	 */
	public IntegerIndex() {
		super(IndexType.INTEGER_INDEX);
		hash = 0;
	}

	/**
	 * 构造一个整型检索索引值，并且指定它的被比较整型值
	 * @param hash 被比较整型值
	 */
	public IntegerIndex(int hash) {
		this();
		setHash(hash);
	}

	/**
	 * 构造一个整型检索索引值，并且指定它的被比较整型值和被比较列
	 * @param hash 被比较整型值
	 * @param column 被比较列
	 */
	public IntegerIndex(int hash, Column column) {
		this(hash);
		super.setColumn(column);
	}

	/**
	 * 设置被比较整型值
	 * @param param 被比较整型值
	 */
	public void setHash(int param) {
		hash = param;
	}

	/**
	 * 返回被比较整型值
	 * @return 被比较整型值
	 */
	public int getHash() {
		return hash;
	}

	/**
	 * 根据当前参数，生成长整型检索的索引数据副本
	 * @see com.laxcus.access.index.WhereIndex#duplicate()
	 */
	@Override
	public IntegerIndex duplicate() {
		return new IntegerIndex(this);
	}

	/**
	 * 输出整型索引数据流
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 类型定义
		writer.write(super.getFamily());
		// 索引值
		writer.writeInt(hash);
		// 列标识
		writer.writeShort(getColumnId());
		// 列数据(输出数据到缓存)
		writer.writeObject(super.getColumn());

		return writer.size() - size;
	}

	/**
	 * 解析整形索引数据流
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();

		super.setFamily(reader.read());
		hash = reader.readInt();
		short columnId = reader.readShort();
		// 限制三种类型
		byte state = reader.current();
		switch(ColumnType.resolveType(state)) {
		case ColumnType.INTEGER:
		case ColumnType.DATE:
		case ColumnType.TIME:
			break;
		default:
			throw new ColumnException("cannot support %d!", state);
		}

		Column column = ColumnCreator.resolve(reader);
		column.setId(columnId);
		// 保存它
		super.setColumn(column);

		// 返回解析的长度
		return reader.getSeek() - seek;
	}
	
}