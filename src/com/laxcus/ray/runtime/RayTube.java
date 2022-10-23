/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.runtime;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * WATCH监视站点。<br>
 * 
 * 由被监视节点地址和最后刷新时间组成。
 * 
 * @author scott.liang
 * @version 1.0 4/10/2018
 * @since laxcus 1.0
 */
public final class RayTube implements Cloneable, Serializable, Comparable<RayTube>, Classable {

	private static final long serialVersionUID = 8336741086906501408L;

	/** 默认是1分钟 **/
	public static final long defaultTimeout = 60000;

	/** 最小延时时间，10秒 **/
	private static volatile long minTimeout = 10000;

	/** 被监视站点超时时间，默认1分钟。超时后，WATCH站点启动请求新状态操作 **/
	private static volatile long timeout = RayTube.defaultTimeout;
	
	/**
	 * 设置最小时间
	 */
	public static long setMinTimeout(long ms) {
		if (ms >= 1000) {
			RayTube.minTimeout = ms;
		}
		return RayTube.minTimeout;
	}

	/**
	 * 最小时间
	 * @return
	 */
	public static long getMinTimeout() {
		return RayTube.minTimeout;
	}

	/**
	 * 设置被监视站点超时时间，必须大于等于最小延时时间。单位：毫秒，小于等于0是无效超时，恢复到默认值。
	 * @param ms 被监视站点超时时间
	 */
	public static void setTimeout(long ms) {
		if (ms <= 0) {
			RayTube.timeout = RayTube.defaultTimeout;
		} else if (ms >= RayTube.minTimeout) {
			RayTube.timeout = ms;
		} else {
			RayTube.timeout = RayTube.minTimeout;
		}
	}

	/**
	 * 返回被监视站点超时时间。单位：毫秒。
	 * @return 被监视站点超时时间
	 */
	public static long getTimeout() {
		return RayTube.timeout;
	}

	/** 被监视节点地址 **/
	private Node node;

	/** 最后一次使用时间 **/
	private long refreshTime;

	/**
	 * 构造WATCH监视站点，指定操作符
	 */
	private RayTube() {
		super();
		refreshTime = 0;// 0值，立即启动更新
	}

	/**
	 * 根据传入的WATCH监视站点，生成它的数据副本
	 * @param that WatchTube实例
	 */
	private RayTube(RayTube that) {
		this();
		node = that.node;
		refreshTime = that.refreshTime;
	}

	/**
	 * 构造WATCH监视站点，指定节点地址
	 * @param node 节点地址
	 */
	public RayTube(Node node) {
		this();
		setNode(node);
	}

	/**
	 * 从可类化数据读取器中解析WATCH监视站点
	 * @param reader 可类化数据读取器
	 */
	public RayTube(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 设置节点地址，不允许空指定针
	 * @param e 节点地址
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);
		node = e.duplicate();
	}

	/**
	 * 返回节点地址
	 * @return 节点地址实例
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * 判断超时
	 * 
	 * @return 返回真或者假
	 */
	public boolean isTimeout() {
		return System.currentTimeMillis() - refreshTime >= RayTube.timeout;
	}

	/**
	 * 返回最后调用时间
	 * @return 长整型的系统时间
	 */
	public long getRefreshTime() {
		return refreshTime;
	}

	/**
	 * 更新使用时间
	 */
	public void refresh() {
		refreshTime = System.currentTimeMillis();
	}

	/**
	 * 返回WATCH监视站点副本
	 * @return WATCH监视站点
	 */
	public RayTube duplicate() {
		return new RayTube(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone(){
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return node.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.law.rule.RuleItem#toString()
	 */
	@Override
	public String toString() {
		return node.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RayTube that) {
		// 空值在前
		if (that == null) {
			return 1;
		}

		// 比较对象
		return Laxkit.compareTo(node, that.node);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(node);
		writer.writeLong(refreshTime);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		node = new Node(reader);
		refreshTime = reader.readLong();
		return reader.getSeek() - seek;
	}

}