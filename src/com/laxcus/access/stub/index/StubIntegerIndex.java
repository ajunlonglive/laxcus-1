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
 * 整形索引 <br>
 * 
 * @author scott.liang
 * @version 1.0 3/24/2009
 * @since laxcus 1.0
 */
public final class StubIntegerIndex extends StubIndex {

	private static final long serialVersionUID = 3072558883255276058L;

	/** 索引范围 **/
	private int begin, end;

	/**
	 * 构造一个整形索引
	 */
	public StubIntegerIndex() {
		super(IndexType.INTEGER_INDEX);
		begin = end = 0;
	}

	/**
	 * 从可类化读取器解析整型范围参数
	 * @param reader 可类化读取器
	 */
	public StubIntegerIndex(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的整形索引，生成它的副本
	 * @param that StubIntegerIndex实例
	 */
	private StubIntegerIndex(StubIntegerIndex that) {
		super(that);
		begin = that.begin;
		end = that.end;
	}

	/**
	 * 构造一个整形索引，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public StubIntegerIndex(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 构造一个整形索引，并且指定它的列编号、索引范围
	 * @param columnId 列编号
	 * @param begin 开始位置
	 * @param end 结束位置
	 */
	public StubIntegerIndex(short columnId, int begin, int end) {
		this(columnId);
		setRange(begin, end);
	}

	/**
	 * 设置整形索引
	 * @param b1 开始位置
	 * @param e1 结束位置
	 */
	public void setRange(int b1, int e1) {
		if (b1 > e1) {
			String s = String.format("%d > %d", b1, e1);
			throw new IndexOutOfBoundsException(s);
		}
		begin = b1;
		end = e1;
	}

	/**
	 * 返回范围开始位置
	 * @return int
	 */
	public int getBegin() {
		return begin;
	}

	/**
	 * 返回范围结束位置
	 * @return int
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * 判断在索引范围内
	 * @param value 比较值
	 * @return 返回真或者假
	 */
	public boolean inside(int value) {
		return begin <= value && value <= end;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.stub.index.StubIndex#getRange()
	 */
	@Override
	public IntegerRange getRange() {
		return new IntegerRange(begin, end);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.region.StubIndex#createStubChart()
	 */
	@Override
	public StubIntegerChart createStubChart() {
		return new StubIntegerChart();
	}

	/*
	 * 生成当前整型索引范围的副本
	 * @see com.laxcus.access.index.range.StubIndex#duplicate()
	 */
	@Override
	public StubIntegerIndex duplicate() {
		return new StubIntegerIndex(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.range.StubIndex#buildSuffix(com.laxcus.util.BitBuffer)
	 */
	@Override
	public void buildSuffix(ClassWriter buff) {
		buff.writeInt(begin);
		buff.writeInt(end);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.range.StubIndex#resolveSuffix(byte[], int, int)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		begin = reader.readInt();
		end = reader.readInt();
	}

}