/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.traffic;

import com.laxcus.site.Node;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据传输速率报告。
 * 
 * @author scott.liang
 * @version 1.0 8/15/2018
 * @since laxcus 1.0
 */
public class TrafficProduct extends ConfirmProduct {

	private static final long serialVersionUID = 4306072145848228912L;

	/** 发出命令节点 **/
	private Node from;

	/** 接受命令节点 **/
	private Node to;

	/** 数据发送尺寸 **/
	private int sendSize;

	/** 运行时间 **/
	private long runTime;

	/** 发包数目 **/
	private int sendPackets;

	/** 超时次数 **/
	private int timeoutCount;

	/** 重传次数 **/
	private int retries;

	/**
	 * 构造默认和私有的数据传输速率报告
	 */
	private TrafficProduct() {
		super();
	}

	/**
	 * 生成数据传输速率报告的数据副本
	 * @param that 数据传输速率报告
	 */
	private TrafficProduct(TrafficProduct that) {
		super(that);
		from = that.from;
		to = that.to;
		sendSize = that.sendSize;
		runTime = that.runTime;
		sendPackets = that.sendPackets;
		timeoutCount = that.timeoutCount;
		retries = that.retries;
	}

	/**
	 * 构造数据传输速率报告，定义成功或者否
	 * @param successful 成功或者否
	 */
	public TrafficProduct(boolean successful) {
		this();
		setSuccessful(successful);
	}

	/**
	 * 构造数据传输速率报告，定义基本参数
	 * @param successful 成功
	 * @param from 发起地址
	 * @param to 目标地址
	 */
	public TrafficProduct(boolean successful, Node from, Node to) {
		this(successful);
		setFrom(from);
		setTo(to);
	}

	/**
	 * 从可类化数据读取器中解析数据传输速率报告
	 * @param reader 可类化数据读取器
	 */
	public TrafficProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置发起地址
	 * @param e
	 */
	public void setFrom(Node e) {
		Laxkit.nullabled(e);
		from = e;
	}

	/**
	 * 返回发起地址
	 * @return
	 */
	public Node getFrom() {
		return from;
	}

	/**
	 * 设置目标地址
	 * @param e
	 */
	public void setTo(Node e) {
		Laxkit.nullabled(e);
		to = e;
	}

	/**
	 * 返回目标地址
	 * @return
	 */
	public Node getTo() {
		return to;
	}

	/**
	 * 设置数据发送尺寸
	 * @param n
	 */
	public void setSendSize(int n){
		sendSize = n;
	}

	/**
	 * 返回数据发送尺寸
	 * @return
	 */
	public int getSendSize(){
		return sendSize;
	}

	/**
	 * 设置运行时间
	 * @param n
	 */
	public void setRunTime(long n) {
		runTime = n;
	}

	/**
	 * 返回运行时间
	 * @return
	 */
	public long getRunTime() {
		return runTime;
	}

	/**
	 * 设置发包数目
	 * @param n
	 */
	public void setSendPackets(int n) {
		sendPackets = n;
	}

	/**
	 * 返回发包数目
	 * @return
	 */
	public int getSendPacket() {
		return sendPackets;
	}

	/**
	 * 设置超时次数
	 * @param n
	 */
	public void setTimeoutCount(int n) {
		timeoutCount = n;
	}

	/**
	 * 返回超时次数
	 * @return
	 */
	public int getTimeoutCount() {
		return timeoutCount;
	}

	/**
	 * 设置重传次数
	 * @param n
	 */
	public void setRetries(int n) {
		retries = n;
	}

	/**
	 * 返回重传次数
	 * @return
	 */
	public int getRetries() {
		return retries;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TrafficProduct duplicate() {
		return new TrafficProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeObject(from);
		writer.writeObject(to);
		writer.writeInt(sendSize);
		writer.writeLong(runTime);
		writer.writeInt(sendPackets);
		writer.writeInt(timeoutCount);
		writer.writeInt(retries);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		from = new Node(reader);
		to = new Node(reader);
		sendSize = reader.readInt();
		runTime = reader.readLong();
		sendPackets = reader.readInt();
		timeoutCount = reader.readInt();
		retries = reader.readInt();
	}

}
