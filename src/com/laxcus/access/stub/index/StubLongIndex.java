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
 * 长整型索引
 * 
 * @author scott.liang
 * @version 1.0 3/24/2009
 * @since laxcus 1.0
 */
public final class StubLongIndex extends StubIndex {

	private static final long serialVersionUID = -5273921697558901144L;

	/** 索引范围 **/
	private long begin, end;

	/**
	 * 根据传入的长整型索引，生成它的副本
	 * @param that
	 */
	private StubLongIndex(StubLongIndex that) {
		super(that);
		begin = that.begin;
		end = that.end;
	}

	/**
	 * 构造一个长整型索引
	 */
	public StubLongIndex() {
		super(IndexType.LONG_INDEX);
		begin = end = 0L;
	}

	/**
	 * 从可类化读取器中解析长整型数据
	 * @param reader 可类化读取器
	 */
	public StubLongIndex(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造一个长整型索引，并且设置它的列编号
	 * @param columnId 列编号
	 */
	public StubLongIndex(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 构造一个长整型索引，并且设置它的列编号、索引范围
	 * @param columnId 列编号
	 * @param begin 开始位置
	 * @param end 结束位置
	 */
	public StubLongIndex(short columnId, long begin, long end) {
		this(columnId);
		setRange(begin, end);
	}

	/**
	 * 设置索引范围
	 * @param b1 开始位置
	 * @param e1 结束位置
	 */
	public void setRange(long b1, long e1) {
		if (b1 > e1) {
			String s = String.format("%d > %d", b1, e1);
			throw new IndexOutOfBoundsException(s);
		}
		begin = b1;
		end = e1;
	}

	/**
	 * 返回索引开始位置
	 * @return long
	 */
	public long getBegin() {
		return begin;
	}

	/**
	 * 返回索引结束位置
	 * @return long
	 */
	public long getEnd() {
		return end;
	}

	/**
	 * 判断在索引范围内
	 * @param value 比较值
	 * @return 返回真或者假
	 */
	public boolean inside(long value) {
		return begin <= value && value <= end;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.region.StubIndex#getRange()
	 */
	@Override
	public LongRange getRange() {
		return new LongRange(begin, end);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.region.StubIndex#createStubChart()
	 */
	@Override
	public StubLongChart createStubChart() {
		return new StubLongChart();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.stub.index.StubIndex#duplicate()
	 */
	@Override
	public StubLongIndex duplicate() {
		return new StubLongIndex(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.stub.index.StubIndex#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeLong(begin);
		writer.writeLong(end);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.stub.index.StubIndex#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		begin = reader.readLong();
		end = reader.readLong();
	}
}