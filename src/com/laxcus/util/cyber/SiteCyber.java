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
 * 节点虚拟空间
 * 一个节点的承载量，以人数为基点。
 * 
 * 节点可以支持的最多成员数，即这个成员的资源，支持在线调整。部署在GATE/CALL/DATA/WORK/BUILD节点。
 * 
 * @author scott.liang
 * @version 1.0 10/20/2019
 * @since laxcus 1.0
 */
public abstract class SiteCyber implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = -6567451737467823378L;

	/** 在集群里，机器性能各不相同，还有内存/CPU/磁盘的限制，每个节点只能支持有限人数。小于等于0不限制，适用节点。 **/
	private volatile int persons;

	/** 最大限制比例，达到阀值则报警，完全达到则显示严重报警！**/
	private volatile double threshold;

	/** 检测超时时间 **/
	private long timeout;

	/**
	 * 构造默认的节点虚拟空间
	 */
	protected SiteCyber() {
		super();
		// 默认一分钟检查一次
		setTimeout(60000);
	}
	
	/**
	 * 生成节点虚拟空间副本
	 * @param that 节点虚拟空间
	 */
	protected SiteCyber(SiteCyber that) {
		this();
		persons = that.persons;
		threshold = that.threshold;
		timeout = that.timeout;
	}

	/**
	 * 构造节点虚拟空间，指定参数
	 * @param maxPersons 最大用户数
	 * @param threshold 阀值
	 */
	protected SiteCyber(int maxPersons, double threshold) {
		this();
		setPersons(maxPersons);
		setThreshold(threshold);
	}

	/**
	 * 设置可以支持的最多用户数目。<br>
	 * 在集群里，因为计算机性能的限制，包括：内存/CPU/磁盘，每个节点只能支持有限人数。设置这个参数加以限制。<br>
	 * 这个方法针对人员注册使用的节点，包括：ACCOUNT/GATE/CALL/DATA/WORK/BUILD。<br>
	 * 
	 * @param more 用户数目
	 */
	public void setPersons(int more) {
		persons = more;
	}

	/**
	 * 返回可以支持的最多用户数目
	 * @return 整数
	 */
	public int getPersons() {
		return persons;
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
	 * 设置超时时间，不能低于10秒
	 * @param ms 毫秒时间
	 */
	public void setTimeout(long ms) {
		if (ms >= 10000) {
			timeout = ms;
		}
	}

	/**
	 * 返回超时时间
	 * @return 毫秒计
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * 判断“满员”状态
	 * @param members 成员数
	 * @return 返回真或者
	 */
	public boolean isFull(int members) {
		return persons > 0 && members >= persons;
	}

	/**
	 * 判断达到“不足”状态
	 * @param members 成员数
	 * @return 返回真或者假
	 */
	public boolean isMissing(int members) {
		boolean success = (persons > 0 && threshold > 0.0f);
		if (success) {
			double rate = ((double) members / (double) persons) * 100.0f;
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
		return String.format("%d # %.2f # %d", persons, threshold,timeout);
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
		// 成员数目
		writer.writeInt(persons);
		// 阀值
		writer.writeDouble(threshold);
		// 检测超时时间
		writer.writeLong(timeout);
		// 调用子类接口，将子类信息写入可类化存储器
		buildSuffix(writer);
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
		persons = reader.readInt();
		// 阀值
		threshold = reader.readDouble();
		// 检测超时时间
		timeout = reader.readLong();
		// 从可类化读取器中解析子类信息
		resolveSuffix(reader);
		// 返回读取的数据长度
		return reader.getSeek() - seek;
	}
	
	/**
	 * 生成副本
	 * @return VirtualCyber子类实例
	 */
	public abstract SiteCyber duplicate();

	/**
	 * 将子类参数写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中读取子类参数信息
	 * @param reader 可类化数据读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);

}