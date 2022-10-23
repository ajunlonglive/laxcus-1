/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.cyber;

import java.io.*;

import com.laxcus.util.classable.*;

/**
 * 成员虚拟空间。
 * 一个节点的承载量，以人数为基点。
 * 
 * 节点可以支持的最多成员数，即这个成员的资源，支持在线调整。部署在GATE/CALL/DATA/WORK/BUILD节点。
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public final class PersonStamp implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = 338475569770349984L;

	/** 允许的最多人数 **/
	private int maxPersons;

	/** 最大限制比例，达到阀值则报警，完全达到则显示严重报警！ **/
	private double threshold;

	/** 当前成员数 **/
	private int realPersons;

	/**
	 * 构造默认的成员虚拟空间
	 */
	public PersonStamp() {
		super();
	}

	/**
	 * 生成成员虚拟空间副本
	 * @param that 成员虚拟空间
	 */
	private PersonStamp(PersonStamp that) {
		this();
		maxPersons = that.maxPersons;
		threshold = that.threshold;
		realPersons = that.realPersons;
	}

	/**
	 * 从可类化读取器解析成员虚拟空间
	 * @param reader 可类化数据读取器
	 */
	public PersonStamp(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造成员虚拟空间，指定参数
	 * @param maxPersons 最大用户数
	 * @param threshold 阀值
	 * @param realPersons 当前用户数
	 */
	public PersonStamp(int maxPersons, double threshold, int realPersons) {
		this();
		setMaxPersons(maxPersons);
		setThreshold(threshold);
		setRealPersons(realPersons);
	}

	/**
	 * 可以承载的最大用户数。如果是0表示没有限制
	 * @param more 成员数
	 */
	public void setMaxPersons(int more) {
		maxPersons = more;
	}

	/**
	 * 返回可以支持的最多用户数目
	 * @return 整数
	 */
	public int getMaxPersons() {
		return maxPersons;
	}

	/**
	 * 设置用户数目阀值
	 * @param f 用户数目阀值
	 */
	public void setThreshold(double f) {
		threshold = f;
	}

	/**
	 * 返回用户数目阀值
	 * @return 浮点数
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * 设置当前成员数，不能低于10秒
	 * @param more 成员数
	 */
	public void setRealPersons(int more) {
		realPersons = more;
	}

	/**
	 * 返回当前成员数
	 * @return 成员数
	 */
	public int getRealPersons() {
		return realPersons;
	}

	/**
	 * 判断达到“满员”状态
	 * @return 返回真或者假
	 */
	public boolean isFull() {
		return maxPersons > 0 && realPersons >= maxPersons;
	}

	/**
	 * 判断达到“空间不足”状态
	 * @return 返回真或者假
	 */
	public boolean isMissing() {
		boolean success = (maxPersons > 0 && threshold > 0.0f);
		if (success) {
			double rate = ((double) realPersons / (double) maxPersons) * 100.0f;
			success = (rate >= threshold);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d # %.2f # %d", maxPersons, threshold, realPersons);
	}

	/**
	 * 生成副本
	 * @return Tik副本
	 */
	public PersonStamp duplicate() {
		return new PersonStamp(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 最大成员数目
		writer.writeInt(maxPersons);
		// 阀值
		writer.writeDouble(threshold);
		// 当前成员数
		writer.writeInt(realPersons);
		// 返回写入的数据长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 成员数目
		maxPersons = reader.readInt();
		// 阀值
		threshold = reader.readDouble();
		// 当前成员数
		realPersons = reader.readInt();
		// 返回读取的数据长度
		return reader.getSeek() - seek;
	}
}