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
 * 双浮点索引 <br>
 * 
 * @author scott.liang
 * @version 1.0 3/25/2009
 * @since laxcus 1.0
 */
public final class StubDoubleIndex extends StubIndex {

	private static final long serialVersionUID = -6313110050265319586L;

	/** 索引范围 **/
	private double begin, end;

	/**
	 * 根据传入的双浮点索引，生成它的副本
	 * @param that StubDoubleIndex实例
	 */
	private StubDoubleIndex(StubDoubleIndex that) {
		super(that);
		begin = that.begin;
		end = that.end;
	}

	/**
	 * 构造一个双浮点索引
	 */
	public StubDoubleIndex() {
		super(IndexType.DOUBLE_INDEX);
		begin = end = 0;
	}
	
	/**
	 * 从可类化读取器中解析数据
	 * @param reader 可类化读取器
	 */
	public StubDoubleIndex(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造一个双浮点索引，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public StubDoubleIndex(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 构造一个双浮点索引，并且指定它的列编号、索引范围
	 * @param columnId 列编号
	 * @param begin 开始位置
	 * @param end 结束位置
	 */
	public StubDoubleIndex(short columnId, double begin, double end) {
		this(columnId);
		setRange(begin, end);
	}

	/**
	 * 设置双浮点索引范围
	 * @param b1 开始位置
	 * @param e1 结束位置
	 */
	public void setRange(double b1, double e1) {
		if (b1 > e1) {
			String s = String.format("%f > %f", b1, e1);
			throw new IndexOutOfBoundsException(s);
		}
		begin = b1;
		end = e1;
	}

	/**
	 * 返回索引开始位置
	 * @return double
	 */
	public double getBegin() {
		return begin;
	}

	/**
	 * 返回索引结束位置
	 * @return double
	 */
	public double getEnd() {
		return end;
	}

	/**
	 * 判断在索引范围内
	 * @param value 比较值
	 * @return 返回真或者假
	 */
	public boolean inside(double value) {
		return begin <= value && value <= end;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.region.StubIndex#getRange()
	 */
	@Override
	public DoubleRange getRange() {
		return new DoubleRange(begin, end);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.region.StubIndex#createStubChart()
	 */
	@Override
	public StubDoubleChart createStubChart() {
		return new StubDoubleChart();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.region.StubIndex#duplicate()
	 */
	@Override
	public StubDoubleIndex duplicate() {
		return new StubDoubleIndex(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.range.StubIndex#buildSuffix(com.laxcus.util.BitBuffer)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeDouble(begin);
		writer.writeDouble(end);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.range.StubIndex#resolveSuffix(byte[], int, int)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		begin = reader.readDouble();
		end = reader.readDouble();
	}
}