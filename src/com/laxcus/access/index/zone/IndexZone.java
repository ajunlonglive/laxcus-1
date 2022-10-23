/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.zone;

import java.io.*;
import java.math.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 列索引值区域<br><br>
 * 
 * 列索引值区域由数值范围和此范围中值的出现频率(权重)组成。<br>
 * 
 * 列索引值区域子类：ShortZone、IntegerZone、LongZone、FloatZone、DoubleZone。
 * 
 * @author scott.liang
 * @version 1.2 12/06/2015
 * @since laxcus 1.0
 */
public abstract class IndexZone implements Serializable, Cloneable, Classable {

	private static final long serialVersionUID = -7452109823730309895L;
	
	/** 索引区域类型 **/
	private byte family;

	/** 权重（在系统运行过程中，数据发生频率的统计值） **/
	private int weight;
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 类型
		writer.write(family);
		// 权重
		writer.writeInt(weight);
		// 子类数据
		buildSuffix(writer);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 类型
		setFamily(reader.read());
		// 权重
		setWeight(reader.readInt());
		// 子类数据
		resolveSuffix(reader);
		// 返回解析长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个默认的列索引值区域，指定索引区域类型
	 * @param family 索引区域类型
	 */
	protected IndexZone(byte family) {
		super();
		setFamily(family);
	}

	/**
	 * 根据传入的列索引值区域，生成它的副本
	 * @param that IndexZone实例
	 */
	protected IndexZone(IndexZone that) {
		super();
		family = that.family;
		weight = that.weight;
	}
	
	/**
	 * 返回参数类型
	 * @return 返回字节描述的列索引值类型
	 */
	public byte getFamily() {
		return family;
	}

	/**
	 * 设置列索引值类型
	 * @param who 列索引值类型
	 */
	private void setFamily(byte who) {
		// 判断合法
		if (!IndexZoneTag.isIndexZone(who)) {
			throw new IllegalValueException("illegal family: %d", who);
		}
		family = who;
	}

	/**
	 * 设置权重（数据发生频率）
	 * @param i 权重值
	 */
	public void setWeight(int i) {
		weight = i;
	}

	/**
	 * 返回权重（数据发生频率）
	 * @return 权重整型值
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * 增加统计值，如果达到最大值时，限定为整型最大值
	 * @param value 权重值
	 */
	public void addWeight(int value) {
		if (BigInteger.valueOf(weight).add(BigInteger.valueOf(value))
				.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
			this.weight = Integer.MAX_VALUE;
		} else {
			this.weight += value;
		}
	}

	/**
	 * 根据子类实例参数，克隆一个它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 根据子类实例，生成它的数据副本
	 * @return IndexZone子类实例
	 */
	public abstract IndexZone duplicate();

	/**
	 * 将子类信息写入可类化写入器
	 * @param writer 可类化写入器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化读取器中解析子类信息
	 * @param reader 可类化读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);		
	
}