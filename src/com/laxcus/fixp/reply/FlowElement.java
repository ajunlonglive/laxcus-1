/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import com.laxcus.echo.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 异步数据流成员 <br>
 * 记录数据流在接收过程中的参数，返回合适的UDP流量分配方案。
 * 
 * @author scott.liang
 * @version 1.0 9/10/2020
 * @since laxcus 1.0
 */
public class FlowElement implements Comparable<FlowElement> {
	
	/** 流量控制标记符 **/
	private FlowFlag flag;

	/** 分配给线程的可用SOCKET缓存 **/
	private int capacity;

	/** 流量方案 **/
	private FlowSketch sketch;

	/** 刷新时间，无论成功或者丢包 **/
	private long refreshTime;

	/**
	 * 构造异步数据流成员
	 * @param code 异步通信码，是MD5标识
	 * @param wide 来自公网或者否
	 */
	public FlowElement(Address address, CastCode code) {
		super();
		setFlag(new FlowFlag(address.duplicate(), code.duplicate()));
		refreshTime();
	}

	/**
	 * 设置流量控制标记符
	 * @param e 流量控制标记符
	 */
	public void setFlag(FlowFlag e) {
		Laxkit.nullabled(e);
		flag = e;
	}

	/**
	 * 返回流量控制标记符
	 * @return 流量控制标记符
	 */
	public FlowFlag getFlag() {
		return flag;
	}

	/**
	 * 返回通信码
	 * @return
	 */
	public CastCode getCode(){
		return flag.getCode();
	}

	/**
	 * 刷新时间
	 */
	private final void refreshTime() {
		refreshTime = System.currentTimeMillis();
	}

	/**
	 * 返回IP地址
	 * @return 返回真或者假
	 */
	public Address getAddress() {
		return flag.getAddress();
	}

	/**
	 * 判断是公网地址
	 * @return 返回真或者假 
	 */
	public boolean isWideAddress() {
		return ReplyUtil.isWideAddress(flag.getAddress());
	}

	/**
	 * 设置管理池分配的SOCKET缓存空间尺寸
	 * @param len 容量
	 */
	private void setCapacity(int len) {
		capacity = len;
	}

	/**
	 * 返回SOCKET缓存空间尺寸
	 * @return 整数
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * 设置UDP流量方案，指定SOCKET缓存容量，只有在这个容量中使用数据
	 * 
	 * @param capacity 分配的SOCKET缓存容量
	 * @param sameMembers 同地址现有成员
	 */
	public void createSketch(int capacity, int sameMembers) {
		// 记录分配的容量
		setCapacity(capacity);
		// 生成实例
		sketch = new FlowSketch();
		// 分配UDP流量方案
		sketch.createDefault(isWideAddress(), capacity, sameMembers);
	}

	/**
	 * 返回当前的UDP流量方案
	 * @return FlowSketch实例
	 */
	public FlowSketch getSketch() {
		return sketch;
	}

	/**
	 * 收到数据，激活操作
	 */
	public void active() {
		// 更新时间
		refreshTime();
		// 记录参数
	}

	/**
	 * 丢包，降频处理!
	 * @param count 丢包统计值
	 */
	public void lose(int count) {
		// 记录出错数目

		// 下调参数
		sketch.lessen(isWideAddress(), count);

//		Logger.error(this, "lose", "发生丢包，下调流量控制阀值！%s ", sketch);
	}

	/**
	 * 通过UDP数据流统计单元，判断超时
	 * @param ms 毫秒
	 * @return 返回真或者假
	 */
	public boolean isTimeout(long ms) {
		return System.currentTimeMillis() - refreshTime >= ms;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		return compareTo((FlowElement) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return flag.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FlowElement that) {
		if (that == null) {
			return -1;
		}
		// 以下参数要逐一判断
		return Laxkit.compareTo(flag, that.flag);
	}

}