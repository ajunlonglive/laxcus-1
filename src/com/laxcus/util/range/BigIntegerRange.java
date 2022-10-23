/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.range;

import java.math.*;
import java.util.*;

import com.laxcus.util.classable.*;

/**
 * 大数值范围。
 * 
 * @author scott.liang
 * @version 1.0 1/16/2009
 * @since laxcus 1.0
 */
public final class BigIntegerRange extends Range implements Comparable<BigIntegerRange> {

	private static final long serialVersionUID = -1397187116321492026L;

	/** 大整数范围 */
	private BigInteger begin, end;

	/**
	 * 将大整数范围写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	public int build(ClassWriter writer) {
		int scale = writer.size();
		String s1 = this.begin.toString();
		String s2 = this.end.toString();
		writer.writeString(s1);
		writer.writeString(s2);
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析大整数范围
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		String s1 = reader.readString();
		String s2 = reader.readString();
		this.set(new BigInteger(s1), new BigInteger(s2));
		return reader.getSeek() - scale;
	}

	/**
	 * 使用传入的大整数范围，生成它的副本
	 * @param that BigIntegerRange实例
	 */
	private BigIntegerRange(BigIntegerRange that) {
		super(that);
		this.set(that.begin, that.end);
	}

	/**
	 * 构造一个默认的大数值范围
	 */
	public BigIntegerRange() {
		super();
		this.set(BigInteger.ZERO, BigInteger.ZERO);
	}

	/**
	 * 构造一个大整数范围，指定它的开始和结束位置
	 * @param begin 开始点
	 * @param end 结束值
	 */
	public BigIntegerRange(BigInteger begin, BigInteger end) {
		this();
		this.set(begin, end);
	}

	/**
	 * 构造一个大整数范围，用文本的形式指定它的开始和结束位置，以及进制数
	 * @param begin 开始点
	 * @param end 结束值
	 * @param radix 基数
	 */
	public BigIntegerRange(String begin, String end, int radix) {
		this();
		this.set(begin, end, radix);
	}

	/**
	 * 从可类化读取器中解析大整数范围
	 * @param reader 可类化读取器
	 */
	public BigIntegerRange(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 返回开始点
	 * @return BigInteger
	 */
	public BigInteger begin() {
		return this.begin;
	}

	/**
	 * 返回结束点
	 * @return BigInteger
	 */
	public BigInteger end() {
		return this.end;
	}

	/**
	 * 设置数据范围，开始位置必须小于等于结束位置。否则抛出"算术异常"
	 * @param begin 开始点
	 * @param end 结束点
	 */
	public void set(BigInteger begin, BigInteger end) {
		if (begin.compareTo(end) > 0) {
			throw new ArithmeticException("illegal range");
		}
		this.begin = new BigInteger(begin.toString());
		this.end = new BigInteger(end.toString());
	}

	/**
	 * 设置参数
	 * @param begin 开始点
	 * @param end 结束点
	 * @param radix 基数
	 */
	public void set(String begin, String end, int radix) {
		this.set(new BigInteger(begin, radix), new BigInteger(end, radix));
	}

	/**
	 * 返回范围尺寸
	 * @return 范围数
	 */
	public BigInteger size() {
		return end.subtract(begin).add(BigInteger.ONE);
	}

	/**
	 * 是否在范围内
	 * @param value 大整型值
	 * @return 返回真或者假
	 */
	public boolean inside(BigInteger value) {
		return (begin.compareTo(value) <= 0 && value.compareTo(end) <= 0);
	}

	/**
	 * 是否在范围内
	 * @param that BigIntegerRange实例
	 * @return 返回真或者假
	 */
	public boolean inside(BigIntegerRange that) {
		return begin.compareTo(that.begin) <= 0 && that.end.compareTo(end) <= 0;
	}

	/**
	 * 根据传入的块数，将当前范围平均分割多指定的多个子大整数范围
	 * @param blocks 块数
	 * @return 返回BigIntegerRange数组
	 */
	public BigIntegerRange[] split(final int blocks) {
		// 不能小于1
		if (blocks < 1) {
			throw new IllegalArgumentException("illegal blocks:" + blocks);
		}

		// 存储集
		ArrayList<BigIntegerRange> array = new ArrayList<BigIntegerRange>();
		// 确定一个BLOCK的范围
		BigInteger biBlocks = BigInteger.valueOf(blocks);
		BigInteger sect = end.subtract(begin).add(BigInteger.ONE);
		BigInteger field = sect.divide(biBlocks);
		if (sect.remainder(biBlocks).compareTo(BigInteger.ZERO) != 0) {
			field = field.add(BigInteger.ONE);
		}
		// 分块开始
		BigInteger previous = this.begin;
		while (true) {
			BigInteger next = previous.add(field);
			if (next.compareTo(this.end) >= 0) {
				next = this.end;
				array.add(new BigIntegerRange(previous, next));
				break;
			} else {
				if (next.compareTo(previous) > 0) {
					next = next.subtract(BigInteger.ONE);
				}
				array.add(new BigIntegerRange(previous, next));
				previous = next.add(BigInteger.ONE);
			}
		}
		// 保存数组
		BigIntegerRange[] ranges = new BigIntegerRange[array.size()];
		return array.toArray(ranges);
	}

	/*
	 * 比较两个大整数范围是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != BigIntegerRange.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return this.compareTo((BigIntegerRange) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return begin.hashCode() ^ end.hashCode();
	}

	/**
	 * 返回大整数范围的十进制格式描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s - %s", begin.toString(), end.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(BigIntegerRange that) {
		// 空值在前
		if(that == null) {
			return 1;
		}
		int ret = begin.compareTo(that.begin);
		if (ret == 0) {
			ret = end.compareTo(that.end);
		}
		return ret;
	}

	/*
	 * 根据当前大整数范围，生成一个它的副本
	 * @see com.laxcus.util.range.NumberRange#duplicate()
	 */
	@Override
	public Range duplicate() {
		return new BigIntegerRange(this);
	}

	/**
	 * 开始点否小于指定的值
	 * 
	 * @return 返回真或者假
	 */
	public boolean beginLessBy(BigInteger value) {
		return begin.compareTo(value) < 0;
	}

	/**
	 * 开始点是否等于指定的值
	 * 
	 * @return 返回真或者假
	 */
	public boolean beginEqualsBy(BigInteger value) {
		return begin.compareTo(value) == 0;
	}

	/**
	 * 开始点是否大于指定的值
	 * 
	 * @return 返回真或者假
	 */
	public boolean beginGreatBy(BigInteger value) {
		return begin.compareTo(value) > 0;
	}

	/**
	 * 结束点是否小于被比较值
	 * 
	 * @return 返回真或者假
	 */
	public boolean endLessBy(BigInteger value) {
		return end.compareTo(value) < 0;
	}

	/**
	 * 是否等于结束点值
	 * 
	 * @return 返回真或者假
	 */
	public boolean endEqualsBy(BigInteger value) {
		return end.compareTo(value) == 0;
	}

	/**
	 * 是否大于结束点值
	 * 
	 * @return 返回真或者假
	 */
	public boolean endGreatBy(BigInteger value) {
		return end.compareTo(value) > 0;
	}

	/**
	 * 判断当前Range结尾与另一个Range开始是否衔接
	 * @param that
	 * @return
	 */
	public boolean isLinkupByAfter(BigIntegerRange that) {
		return end.add(BigInteger.ONE).compareTo(that.begin()) == 0;
	}

	/**
	 * 判断另一个Range的结尾与当前Range开始是否衔接
	 * @param that
	 * @return 返回真或者假
	 */
	public boolean isLinkupByBefore(BigIntegerRange that) {
		return that.end().add(BigInteger.ONE).compareTo(this.begin) == 0;
	}

	/**
	 * 合并两个对象. 成功返回一个合并后的新对象,不成功,返回NULL
	 * @param after
	 * @param before
	 * @return
	 */
	public static BigIntegerRange incorporate(BigIntegerRange after, BigIntegerRange before) {
		// 比较两个对象是否衔
		if (!after.isLinkupByAfter(before)) return null;
		// 组成一个合并后的新对象
		return new BigIntegerRange(after.begin, before.end);
	}

}