/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.log;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 日志节点上的日志监听地址
 * 
 * @author scott.liang
 * @version 1.0 6/12/2009
 * @since laxcus 1.0
 */
public final class LogNode implements Serializable, Cloneable, Classable, Comparable<LogNode> {

	private static final long serialVersionUID = 1489919267207305527L;

	/** 日志节点类型 **/
	private byte family;

	/** 监听端口 **/
	private int port;

	/**
	 * 根据传入的日志监听地址，生成它的数据副本
	 * @param that LogNode实例
	 */
	private LogNode(LogNode that) {
		super();
		family = that.family;
		port = that.port;
	}

	/**
	 * 初始化并且设置节点参数
	 * @param family 日志节点类型
	 * @param port 监听端口
	 */
	public LogNode(byte family, int port) {
		super();
		setFamily(family);
		setPort(port);
	}

	/**
	 * 从可类化读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public LogNode(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 设置节点类型
	 * 
	 * @param who 节点类型
	 */
	public void setFamily(byte who) {
		if (!SiteTag.isSite(who)) {
			throw new IllegalValueException("illegal site:%d", who);
		}
		family = who;
	}

	/**
	 * 返回节点类型
	 * 
	 * @return 节点类型
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 设置监听端口
	 * @param i 监听端口
	 */
	public void setPort(int i) {
		port = i;
	}

	/**
	 * 返回监听端口
	 * @return 监听端口
	 */
	public int getPort() {
		return port;
	}

	/**
	 * 返回监听端口的字符串描述
	 * @return 监听端口的字符串
	 */
	public String getTag() {
		if (SiteTag.isSite(family)) {
			return SiteTag.translate(family);
		}
		return null;
		//		switch (family) {
		//		case SiteTag.TOP_SITE:
		//			return "top";
		//		case SiteTag.ACCOUNT_SITE:
		//			return "account";
		//		case SiteTag.HASH_SITE:
		//			return "hash";
		//		case SiteTag.GATE_SITE:
		//			return "gate";
		//		case SiteTag.DIRECT_SITE:
		//			return "direct";
		//		case SiteTag.HOME_SITE:
		//			return "home";
		//		case SiteTag.AID_SITE:
		//			return "aid";
		//		case SiteTag.ARCHIVE_SITE:
		//			return "archive";
		//		case SiteTag.WATCH_SITE:
		//			return "watch";
		//		case SiteTag.FRONT_SITE:
		//			return "front";
		//		case SiteTag.DATA_SITE:
		//			return "data";
		//		case SiteTag.WORK_SITE:
		//			return "work";
		//		case SiteTag.CALL_SITE:
		//			return "call";
		//		case SiteTag.BUILD_SITE:
		//			return "build";
		//		}
		//		return null;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return LogNode实例
	 */
	public LogNode duplicate() {
		return new LogNode(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != LogNode.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((LogNode) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return family ^ port;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(LogNode that) {
		// 空对象在前
		if(that == null) {
			return 1;
		}
		// 比较
		int ret = Laxkit.compareTo(family, that.family);
		if (ret == 0) {
			ret = Laxkit.compareTo(port, that.port);
		}
		return ret;
	}

	/**
	 * 将日志监听地址写入可类化写入器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.write(family);
		writer.writeInt(port);
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析日志监听地址
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		family = reader.read();
		port = reader.readInt();
		return reader.getSeek() - seek;
	}

}