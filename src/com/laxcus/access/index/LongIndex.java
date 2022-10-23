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
 * <code>SQL WHERE</code>检索时的长整型(BIT64)检索索引值。<br>
 * 格式如: WHERE column1 >= long value
 * 
 * @author scott.liang
 * @version 1.0 5/7/2009
 * @since laxcus 1.0
 */
public final class LongIndex extends ColumnIndex {

	private static final long serialVersionUID = 7379281332238321669L;

	/** 被比较长整型值，用于定位数据区域，与长整型的列不同  **/
	private long hash;

	/**
	 * 根据传入参数，构造一个长整型检索索引的副本
	 * @param that LongIndex实例
	 */
	private LongIndex(LongIndex that) {
		super(that);
		hash = that.hash;
	}

	/**
	 * 构造一个默认的长整型检索索引
	 */
	public LongIndex() {
		super(IndexType.LONG_INDEX);
		hash = 0L;
	}

	/**
	 * 构造一个长整型检索索引，并且指定它的被比较长整型值
	 * @param hash 被比较长整型值
	 */
	public LongIndex(long hash) {
		this();
		setHash(hash);
	}

	/**
	 * 构造一个长整型检索索引，并且指定它的被比较长整型值和被比较列
	 * @param hash 被比较长整型值
	 * @param column 被比较列
	 */
	public LongIndex(long hash, Column column) {
		this(hash);
		setColumn(column);
	}

	/**
	 * 设置被比较长整型值
	 * @param param 被比较长整型值
	 */
	public void setHash(long param) {
		hash = param;
	}

	/**
	 * 返回被比较长整型值
	 * @return 被比较长整型值
	 */
	public long getHash() {
		return hash;
	}

	/*
	 * 根据长整型检索索引值，生成它的数据副本
	 * @see com.laxcus.access.index.WhereIndex#duplicate()
	 */
	@Override
	public LongIndex duplicate() {
		return new LongIndex(this);
	}

	/**
	 * 输出长整型索引到可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 类型定义
		writer.write(super.getFamily());
		// 索引值
		writer.writeLong(hash);
		// 列标识号
		writer.writeShort(getColumnId());
		// 列数据
		writer.writeObject(super.getColumn());
		// 返回长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析长整形索引参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();

		super.setFamily(reader.read());
		hash = reader.readLong();
		short columnId = reader.readShort();
		
		// 检查数据类型
		byte state = reader.current();
		switch (ColumnType.resolveType(state)) {
		case ColumnType.LONG:
		case ColumnType.TIMESTAMP:
		case ColumnType.RAW:
		case ColumnType.CHAR:
		case ColumnType.WCHAR:
		case ColumnType.HCHAR:
		case ColumnType.RCHAR:
		case ColumnType.RWCHAR:
		case ColumnType.RHCHAR:
		case ColumnType.DOCUMENT:
		case ColumnType.IMAGE:
		case ColumnType.AUDIO:
		case ColumnType.VIDEO:
			break;
		default:
			throw new ColumnException("cannot support %d!", state);
		}

		// 解析列
		Column column = ColumnCreator.resolve(reader);
		column.setId(columnId);
		// 保存它
		super.setColumn(column);

		return reader.getSeek() - seek;
	}

}