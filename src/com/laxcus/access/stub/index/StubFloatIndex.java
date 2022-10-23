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
 * 单浮点索引。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/25/2009
 * @since laxcus 1.0
 */
public final class StubFloatIndex extends StubIndex {

	private static final long serialVersionUID = 5509080877223550963L;

	/** 索引范围 **/
	private float begin, end;

	/**
	 * 构造一个单浮点索引
	 */
	public StubFloatIndex() {
		super(IndexType.FLOAT_INDEX);
		begin = end = 0f;
	}
	
	/**
	 * 从可类化读取器解析浮点范围
	 * @param reader 可类化读取器
	 */
	public StubFloatIndex(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的单浮点索引，生成它的副本
	 * @param that StubFloatIndex实例
	 */
	private StubFloatIndex(StubFloatIndex that) {
		super(that);
		begin = that.begin;
		end = that.end;
	}

	/**
	 * 构造一个单浮点索引，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public StubFloatIndex(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 构造一个单浮点索引，并且指定它的列编号、索引范围
	 * @param columnId 列编号
	 * @param begin 开始位置
	 * @param end 结束位置
	 */
	public StubFloatIndex(short columnId, float begin, float end) {
		this(columnId);
		setRange(begin, end);
	}

	/**
	 * 设置索引范围
	 * @param b1 开始位置
	 * @param e1 结束位置
	 */
	public void setRange(float b1, float e1) {
		if (b1 > e1) {
			String s = String.format("%f > %f", b1, e1);
			throw new IndexOutOfBoundsException(s);
		}
		begin = b1;
		end = e1;
	}

	/**
	 * 返回索引开始位置
	 * @return float
	 */
	public float getBegin() {
		return begin;
	}

	/**
	 * 返回索引结束位置
	 * @return float
	 */
	public float getEnd() {
		return end;
	}

	/**
	 * 判断在索引范围内
	 * @param value 比较值
	 * @return 返回真或者假
	 */
	public boolean inside(float value) {
		return begin <= value && value <= end;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.region.StubIndex#getRange()
	 */
	@Override
	public FloatRange getRange() {
		return new FloatRange(begin, end);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.region.StubIndex#createStubChart()
	 */
	@Override
	public StubFloatChart createStubChart() {
		return new StubFloatChart();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.region.StubIndex#duplicate()
	 */
	@Override
	public StubFloatIndex duplicate() {
		return new StubFloatIndex(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.range.StubIndex#buildSuffix(com.laxcus.util.BitBuffer)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeFloat(begin);
		writer.writeFloat(end);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.range.StubIndex#resolveSuffix(byte[], int, int)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		begin = reader.readFloat();
		end = reader.readFloat();
	}
}