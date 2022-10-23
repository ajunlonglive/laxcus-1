/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index;

import com.laxcus.access.type.*;
import com.laxcus.command.access.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * <code>SQL WHERE</code>嵌套检索参数。<br>
 * 如：SELECT * FROM schema.table WHERE column1=(SELECT column1 FROM schema.table WHERE column2=...)
 *
 * @author scott.liang
 * @version 1.0 5/2/2009
 * @since laxcus 1.0
 */
public final class NestedIndex extends WhereIndex {

	private static final long serialVersionUID = 6107405530306949485L;

	/** 被检索的列标识号，如果是EXISTS|NOT EXISTS语句前缀则为0 */
	private short columnId;

	/** SELECT语句 */
	private Select select;

	/**
	 * 将子查询索引写入可类化存储器。不兼容C接口
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 类型定义
		writer.write(super.getFamily());
		// 列标识(column identity)
		writer.writeShort(columnId);
		// SELECT数据
		writer.writeInstance(select);
		// 返回写入字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析SQL 子查询。不兼容C接口
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 类型定义
		setFamily(reader.read());
		// 列标识(column identity)
		columnId = reader.readShort();
		// SELECT句柄
		select = reader.readInstance(Select.class);
		// 返回解析的字节数组长度
		return reader.getSeek() - seek;
	}

	/**
	 * 使用传入参数，构造一个嵌套检索参数的副本
	 * @param that
	 */
	private NestedIndex(NestedIndex that) {
		super(that);
		columnId = that.columnId;
		if (that.select != null) {
			select = that.select.duplicate();
		}
	}

	/**
	 * 构造一个默认的嵌套检索参数
	 */
	public NestedIndex() {
		super(IndexType.NESTED_INDEX);
		columnId = 0;
	}

	/**
	 * 构造一个嵌套检索参数，设置列标识号和SELECT语句实例
	 * @param columnId
	 * @param select
	 */
	public NestedIndex(short columnId, Select select) {
		this();
		setColumnId(columnId);
		setSelect(select);
	}

	/**
	 * 设置SELECT实例
	 * @param e
	 */
	public void setSelect(Select e) {
		select = e;
	}

	/**
	 * 返回SELECT实例
	 * @return Select实例
	 */
	public Select getSelect() {
		return select;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.WhereIndex#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger issuer) {
		if (select != null) {
			select.setIssuer(issuer);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.WhereIndex#getIssuer()
	 */
	@Override
	public Siger getIssuer() {
		if (select != null) {
			return select.getIssuer();
		}
		return null;
	}

	/* 返回当前列标识号
	 * @see com.laxcus.access.index.WhereIndex#getColumnId()
	 */
	@Override
	public short getColumnId() {
		return columnId;
	}

	/* 设置当前列标识号
	 * @see com.laxcus.access.index.WhereIndex#setColumnId(short)
	 */
	@Override
	public void setColumnId(short id) {
		columnId = id;
	}

	/* 根据当前嵌套检索索引参数，生成它的数据副本
	 * @see com.laxcus.access.index.WhereIndex#duplicate()
	 */
	@Override
	public NestedIndex duplicate() {
		return new NestedIndex(this);
	}

}