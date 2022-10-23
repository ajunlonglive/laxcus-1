/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import java.util.*;
import java.math.*;

/**
 * 随机数生成器 <br>
 * 
 * 这个类是对 java.util.Random 的补充，用来产生一个指定数字范围内的随机数。
 * 
 * @author scott.liang
 * @version 1.0 7/28/2015
 * @since laxcus 1.0
 */
public final class Rand {

	/** 种子值 **/
	private long seed;

	/**
	 * 构造随机数生成器，指定一个种子值
	 * 
	 * @param value 种子值
	 */
	public Rand(long value) {
		super();
		seed = value;
	}

	/**
	 * 构造默认的随机数生成器
	 */
	public Rand() {
		this(System.currentTimeMillis());
	}

	/**
	 * int转成BigInteger格式
	 * 
	 * @param value
	 * @return
	 */
	private BigInteger valueOf(int value) {
		return BigInteger.valueOf(value);
	}

	/**
	 * long转成BigInteger格式
	 * 
	 * @param value
	 * @return
	 */
	private BigInteger valueOf(long value) {
		return BigInteger.valueOf(value);
	}

	/**
	 * 产生一个指定范围内的随机数
	 * @param from 开始数字
	 * @param to 结尾数字（包括这个数字）
	 * @return BigInteger实例
	 */
	public BigInteger next(BigInteger from, BigInteger to) {
		if (from.compareTo(to) == 1) {
			throw new IndexOutOfBoundsException(from + ">" + to);
		}

		// this is: end - begin + 1
		BigInteger range = to.subtract(from).add(BigInteger.ONE);

		// random number
		Random rnd = new Random(seed);
		BigInteger value = valueOf(rnd.nextInt());

		// this is : value % range
		BigInteger left = value.mod(range);

		return from.add(left);
	}

	/**
	 * 在一个指定的整形范围内，产生一个随机数
	 * @param from 开始数字
	 * @param to 结尾数字（包括这个数字）
	 * @return 返回指定范围内的随机数
	 */
	public int nextInt(int from, int to) {
		BigInteger value = next(valueOf(from), valueOf(to));
		return value.intValue();
	}

	/**
	 * 在一个指定的长整型范围内，产生一个随机数
	 * @param from 开始数字
	 * @param to 结尾数字（包括这个数字）
	 * @return 返回指定范围内的随机数
	 */
	public long nextLong(long from, long to) {
		BigInteger value = next(valueOf(from), valueOf(to));
		return value.longValue();
	}

//	public void test() {
//		int[] froms = new int[] { -1, -20, -0,1, -12, 5};
//		int[] tos = new int[] { 0, -10, -0,1 ,-12, 6};
//		
//		for(int n =0; n < 10; n++)
//		for (int i = 0; i < froms.length; i++) {
//			int value = Laxkit.random(froms[i], tos[i]);
//			System.out.printf("[%d %d] %d\n", froms[i], tos[i], value);
//		}
//	}
//
//	public static void main(String[] args) {
//		Rand rnd = new Rand();
//		rnd.test();
//	}

}