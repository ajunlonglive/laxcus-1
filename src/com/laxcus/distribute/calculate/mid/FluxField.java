/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.calculate.mid;

import com.laxcus.distribute.mid.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.range.*;

/**
 * DIFFUSE/CONVERGE分布计算域。<br><br>
 * 
 * FluxField是FROM/TO计算过程中，一个DATA/WORK节点上，基于一个“模”值产生的数据映像。FluxField被包含在FluxArea中，是FluxArea的子集。<br><br>
 * 
 * 与FluxField关联的实体数据，被FluxTrustorPool托管，保存在内存或者硬盘里，并被WORK节点的TO阶段调用器使用，按要求传输到WORK节点，WORK对数据进行计算。在完成后，被WORK/TO调用器通过FluxTrustorPool删除它。<br><br>
 * 
 * <B>
 * 模值(MOD)的含义：<br>
 * 1. 模值是一个64位有符号长整数，用于数据分片，是数据分割/合并/重组时的最基本参考依据。<br>
 * 2. 一个模值在一个FluxArea中唯一，不同FluxArea允许有相同的模值。<br>
 * 3. 相同的模值，它所映射的实体数据是一致的。<br>
 * 4. 相邻的模值，它所映射的实体数据是相邻的。<br>
 * 5. 模值在DATA.FROM/WORK.TO阶段产生，CALL.BALANCE依此进行计算，来达到平衡数据资源的目的。<br></B>
 * 
 * @author scott.liang
 * @version 1.1 10/10/2015
 * @since laxcus 1.0
 */
public final class FluxField extends MiddleZone implements Comparable<FluxField> {

	private static final long serialVersionUID = -8319360298379437595L;

	/**
	 * 模值，64位有符号长整数。这个确定每个分组在网络中的唯一性，非常重要!!!
	 */
	private long mod;

	/** 
	 * 托管在中间数据存取池中的实体数据范围。实体数据由FluxTrustorPool管理，可以选择保存在内存或者硬盘中。 
	 */
	private long seek, size;

	/** 本片区域的数据成员数目 ，如SELECT检索后的记录数 **/
	private int elements;

	/** 会话对象迭代编号，默认是-1，无迭代。对应SessionObject.iterateIndex **/
	private int iterateIndex;

	/**
	 * 根据传入参数生成一个DIFFUSE/CONVERGE分布计算域副本
	 * @param that FluxField实例
	 */
	private FluxField(FluxField that) {
		super(that);
		mod = that.mod;
		seek = that.seek;
		size = that.size;
		elements = that.elements;
		iterateIndex = that.iterateIndex;
	}

	/**
	 * 构造一个DIFFUSE/CONVERGE分布计算域
	 */
	public FluxField() {
		super();
		mod = 0L; 			// 模值
		seek = size = 0L;	// 数据范围
		elements = 0;		// 数据成员数
		iterateIndex = -1; 	// 默认无迭代
	}

	/**
	 * 从可类化数据读取器中解析DIFFUSE/CONVERGE分布计算域
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FluxField(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置模值
	 * @param i 模值
	 */
	public void setMod(long i) {
		mod = i;
	}

	/**
	 * 返回模值 
	 * @return 模值
	 */
	public long getMod() {
		return mod;
	}

	/**
	 * 设置基于0的数据下标
	 * @param i 数据下标
	 */
	public void setSeek(long i) {
		if (i < 0L) {
			throw new IllegalValueException("illegal offset:%d", i);
		}
		seek = i;
	}

	/**
	 * 返回基于0的数据下标
	 * @return 数据下标
	 */
	public long getSeek() {
		return seek;
	}

	/**
	 * 设置数据长度
	 * @param i 数据长度
	 */
	public void setSize(long i) {
		if (i < 0L) {
			throw new IllegalValueException("illegal size:%d", i);
		}
		size = i;
	}

	/**
	 * 返回数据长度
	 * @return 数据长度
	 */
	public long getSize() {
		return size;
	}

	/**
	 * 返回数据范围
	 * @return LongRange实例
	 */
	public LongRange getRange() {
		return new LongRange(seek, seek + size - 1);
	}

	/**
	 * 磁盘文件中数据块的总长度
	 * @return 数据块的总长度
	 */
	public long length() {
		return size;
	}

	/**
	 * 数据成员数目(SELECT检索记录数或者其它数值)
	 * @param i 成员数目
	 */
	public void setElements(int i) {
		elements = i;
	}

	/**
	 * 返回数据中的成员数目
	 * @return 成员数目
	 */
	public int getElements() {
		return elements;
	}

	/**
	 * 设置迭代编号
	 * @param index 迭代编号
	 */
	public void setIterateIndex(int index) {
		iterateIndex = index;
	}

	/**
	 * 返回迭代编号
	 * @return 迭代编号
	 */
	public int getIterateIndex() {
		return iterateIndex;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || getClass() != that.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((FluxField) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (mod ^ seek ^ size);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FluxField that) {
		// 空对象排在前面
		if (that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(mod, that.mod);
		if (ret == 0) {
			ret = Laxkit.compareTo(seek, that.seek);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(size, that.size);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d/%d,%d/%d%d", mod, seek, size, elements, iterateIndex);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MiddleZone#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 写入参数
		writer.writeLong(mod);
		writer.writeLong(seek);
		writer.writeLong(size);
		writer.writeInt(elements);
		writer.writeInt(iterateIndex);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MiddleZone#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 读参数
		mod = reader.readLong();
		seek = reader.readLong();
		size = reader.readLong();
		elements = reader.readInt();
		iterateIndex = reader.readInt();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MiddleZone#duplicate()
	 */
	@Override
	public FluxField duplicate() {
		return new FluxField(this);
	}

}