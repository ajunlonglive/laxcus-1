/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 服务器系统信息检测单元
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public final class CheckSystemInfoItem implements Classable, Cloneable, Serializable, Comparable<CheckSystemInfoItem> {

	private static final long serialVersionUID = -5327331416078006183L;

	/** 站点地址 **/
	private Node node;

	/** 成功标识 **/
	private boolean successful;
	
	/** 节点版本 **/
	private Version version;

	/** CPU信息单元 **/
	private CPUInfoItem cpuInfo;

	/** 内存信息单元 **/
	private MemoryInfoItem memInfo;
	
	/** 磁盘信息单元 **/
	private DiskInfoItem diskInfo;
	
	/** JAVA虚拟机信息 **/
	private JREInfoItem jreInfo;
	
	/**
	 * 构造默认的被刷新处理单元
	 */
	public CheckSystemInfoItem() {
		super();
		successful = false;
	}

	/**
	 * 根据传入实例，生成服务器系统信息检测单元的数据副本
	 * @param that CheckSystemInfoItem实例
	 */
	private CheckSystemInfoItem(CheckSystemInfoItem that) {
		super();
		node = that.node;
		successful = that.successful;
		version = that.version;
		cpuInfo = that.cpuInfo;
		memInfo = that.memInfo;
		diskInfo = that.diskInfo;
		jreInfo = that.jreInfo;
	}

	/**
	 * 构造服务器系统信息检测单元，指定站点地址和处理结果
	 * @param node 站点地址
	 * @param successful 成功
	 */
	public CheckSystemInfoItem(Node node, boolean successful) {
		this();
		setSite(node);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中服务器系统信息检测单元
	 * @param reader 可类化数据读取器
	 */
	public CheckSystemInfoItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置站点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		node = e;
	}

	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return node;
	}

	/**
	 * 设置成功标识
	 * @param b 成功标识
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断是成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return successful;
	}
	
	/**
	 * 设置版本
	 * @param e
	 */
	public void setVersion(Version e) {
		version = e;
	}
	
	/**
	 * 返回版本
	 * @return
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * 设置CPU信息，允许空指针
	 * @param e CPU信息
	 */
	public void setCPUInfo(CPUInfoItem e) {
		cpuInfo = e;
	}

	/**
	 * 返回CPU信息，允许空指针
	 * @return CPU信息
	 */
	public CPUInfoItem getCPUInfo() {
		return cpuInfo;
	}

	/**
	 * 设置内存信息
	 * @param e 内存信息
	 */
	public void setMemInfo(MemoryInfoItem e) {
		memInfo = e;
	}

	/**
	 * 返回内存信息
	 * @return 内存信息
	 */
	public MemoryInfoItem getMemInfo() {
		return memInfo;
	}
	
	/**
	 * 设置磁盘信息
	 * @param e 磁盘信息
	 */
	public void setDiskInfo(DiskInfoItem e) {
		diskInfo = e;
	}

	/**
	 * 返回磁盘信息
	 * @return 磁盘信息
	 */
	public DiskInfoItem getDiskInfo() {
		return diskInfo;
	}
	
	/**
	 * 设置JRE信息
	 * @param e
	 */
	public void setJREInfo(JREInfoItem e) {
		jreInfo = e;
	}
	
	/**
	 * 返回JRE信息
	 * @return
	 */
	public JREInfoItem getJREInfo() {
		return jreInfo;
	}
	
	/**
	 * 生成当前实例的数据副本
	 * @return CheckSystemInfoItem实例
	 */
	public CheckSystemInfoItem duplicate() {
		return new CheckSystemInfoItem(this);
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
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || getClass() != that.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((CheckSystemInfoItem ) that) == 0;
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", node, (successful ? "Successful" : "Failed"));
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CheckSystemInfoItem that) {
		if (that == null) {
			return 1;
		}
		// 比较参数
		return Laxkit.compareTo(node, that.node);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		buildSuffix(writer);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		resolveSuffix(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 保存参数
	 * @param writer 
	 */
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(node);
		writer.writeBoolean(successful);
		writer.writeInstance(version);
		writer.writeInstance(cpuInfo);
		writer.writeInstance(memInfo);
		writer.writeInstance(diskInfo);
		writer.writeInstance(jreInfo);
	}

	/**
	 * 解析参数
	 * @param reader
	 */
	protected void resolveSuffix(ClassReader reader) {
		node = new Node(reader);
		successful = reader.readBoolean();
		version = reader.readInstance(Version.class);
		cpuInfo = reader.readInstance(CPUInfoItem.class);
		memInfo = reader.readInstance(MemoryInfoItem.class);
		diskInfo = reader.readInstance(DiskInfoItem.class);
		jreInfo = reader.readInstance(JREInfoItem.class);
	}

}