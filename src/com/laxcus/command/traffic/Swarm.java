/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.traffic;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据传输速率测试。<br>
 * 命令从WATCH节点发出，目标的指定的任意节点。
 * 
 * @author scott.liang
 * @version 1.0 8/10/2018
 * @since laxcus 1.0
 */
public class Swarm extends Command {

	private static final long serialVersionUID = 2133982047971852576L;

	/** 传输的数据尺寸 **/
	private int length;

	/** FIXP数据包长度 **/
	private int packetSize;

	/** FIXP数据子包长度 **/
	private int subPacketSize;
	
	/** FIXP子包发送发送间隔，默认是0 **/
	private int sendInterval;

	/** 目标站点 **/
	private Node site;

	/**
	 * 构造默认的数据传输速率测试命令
	 */
	public Swarm() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析数据传输速率测试
	 * @param reader 可类化数据读取器
	 */
	public Swarm(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成数据传输速率测试副本
	 * @param that 数据传输速率测试
	 */
	private Swarm(Swarm that) {
		super(that);
		length = that.length;
		packetSize = that.packetSize;
		subPacketSize = that.subPacketSize;
		sendInterval = that.sendInterval;
		site = that.site;
	}

	/**
	 * 设置传输的数据尺寸
	 * @param len
	 */
	public void setLength(int len) {
		if (len < 0) {
			throw new IllegalValueException("illegal value:%d", len);
		}
		length = len;
	}

	/**
	 * 返回传输的数据尺寸
	 * @return
	 */
	public int getLength() {
		return length;
	}

	/**
	 * 设置数据包长度。一个FIXP数据包包含N个FIXP子包
	 * @param len
	 */
	public void setPacketSize(int len) {
		if (len < 0) {
			throw new IllegalValueException("illegal value:%d", len);
		}
		packetSize = len;
	}

	/**
	 * 返回数据包长度
	 * @return
	 */
	public int getPacketSize() {
		return packetSize;
	}

	/**
	 * 设置子包数据长度
	 * @param len
	 */
	public void setSubPacketSize(int len) {
		if (len < 0) {
			throw new IllegalValueException("illegal value:%d", len);
		}
		subPacketSize = len;
	}

	/**
	 * 返回子包数据长度
	 * @return
	 */
	public int getSubPacketSize() {
		return subPacketSize;
	}
	
	/**
	 * 设置FIXP子包之间的发送间隔，以毫秒为单位。通过迟延发送，避免接收端压力过大，减少丢包概率
	 * @param ms 以毫秒为单位的时间
	 */
	public void setSendInterval(int ms) {
		sendInterval = ms;
	}

	/**
	 * 返回 FIXP子包之间的发送间隔，以毫秒为单位。通过迟延发送，避免接收端压力过大，减少丢包概率
	 * @return 以毫秒为单位的时间
	 */
	public int getSendInterval() {
		return sendInterval;
	}

	/**
	 * 设置目标站点
	 * @param e
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);
		site = e;
	}

	/**
	 * 返回目标站点
	 * @return
	 */
	public Node getSite() {
		return site;
	}
	
	/**
	 * 判断是投递到注册节点
	 * @return 返回真或者假
	 */
	public boolean isHub() {
		return site == null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Swarm duplicate() {
		return new Swarm(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(length);
		writer.writeInt(packetSize);
		writer.writeInt(subPacketSize);
		writer.writeInt(sendInterval);
		writer.writeInstance(site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		length = reader.readInt();
		packetSize = reader.readInt();
		subPacketSize = reader.readInt();
		sendInterval = reader.readInt();
		site = reader.readInstance(Node.class);
	}

}