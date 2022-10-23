/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub.index;

import com.laxcus.access.stub.chart.*;
import com.laxcus.access.type.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.range.*;

/**
 * 短整型索引 <br>
 * 
 * @author scott.liang
 * @version 1.0 3/24/2009
 * @since laxcus 1.0
 */
public final class StubShortIndex extends StubIndex {
	
	private static final long serialVersionUID = 5359977534215641487L;
	
	/** 索引范围 **/
	private short begin, end;

	/**
	 * 构造一个默认的短整型索引
	 */
	public StubShortIndex() {
		super(IndexType.SHORT_INDEX);
		begin = end = 0;
	}
	
	/**
	 * 从可类化读取器中解析数据
	 * @param reader  可类化读取器
	 */
	public StubShortIndex(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的短整型索引，生成它的副本
	 * @param that StubShortIndex实例
	 */
	private StubShortIndex(StubShortIndex that) {
		super(that);
		begin = that.begin;
		end = that.end;
	}

	/**
	 * 构造一个短整型索引，并且指定它的列编号
	 * 
	 * @param columnId 列编号
	 */
	public StubShortIndex(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 构造一个短整型索引，并且指定它的列编号、短整型范围
	 * @param columnId 列编号
	 * @param begin 开始位置
	 * @param end 结束位置
	 */
	public StubShortIndex(short columnId, short begin, short end) {
		this(columnId);
		setRange(begin, end);
	}

	/**
	 * 设置短整型索引范围
	 * @param b1 开始位置
	 * @param e1 结束位置
	 */
	public void setRange(short b1, short e1) {
		if (b1 > e1) {
			String s = String.format("%d > %d", b1, e1);
			throw new IndexOutOfBoundsException(s);
		}
		begin = b1;
		end = e1;
	}
	
	/**
	 * 返回短整型开始位置
	 * @return short
	 */
	public short getBegin() {
		return begin;
	}
	
	/**
	 * 返回短整型结束位置
	 * @return short
	 */
	public short getEnd() {
		return end;
	}

	/**
	 * 判断在索引范围内
	 * @param value 比较值
	 * @return 返回真或者假
	 */
	public boolean inside(short value) {
		return begin <= value && value <= end;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.region.StubIndex#getRange()
	 */
	@Override
	public ShortRange getRange() {
		return new ShortRange(begin, end);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.region.StubIndex#createStubChart()
	 */
	@Override
	public StubShortChart createStubChart() {
		return new StubShortChart();
	}
	
	/*
	 * 从当前短整型中，生成一个它的副本
	 * @see com.laxcus.access.index.range.StubIndex#duplicate()
	 */
	@Override
	public StubShortIndex duplicate() {
		return new StubShortIndex(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.range.StubIndex#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeShort(begin);
		writer.writeShort(end);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.range.StubIndex#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		begin = reader.readShort();
		end = reader.readShort();
	}


}