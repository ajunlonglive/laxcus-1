/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.guide.archive;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 引导包 <br>
 * 
 * 包含软件名称和文件的内容，从压缩包中提取，写入磁盘
 * 
 * @author scott.liang
 * @version 1.1 7/23/2015
 * @since laxcus 1.0
 */
public final class BootComponent implements Classable, Cloneable, Serializable, Comparable<BootComponent> { 

	private static final long serialVersionUID = 6496606983363997177L;

	/** 软件名称 **/
	private Naming ware;

	/** 字节内容 **/
	private byte[] content;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		ware = null;
		content = null;
	}

	/**
	 * 根据传入的引导包，生成它的副本
	 * @param that 引导包
	 */
	private BootComponent(BootComponent that) {
		this();
		ware = that.ware;
		content = that.content;
	}

	/**
	 * 构造一个默认的引导包
	 */
	public BootComponent() {
		super();
	}

	/**
	 * 构造引导包，指定参数
	 * @param tag 软件名称
	 * @param content 数据内容
	 */
	public BootComponent(Naming tag, byte[] content) {
		this();
		setWare(tag);
		setContent(content);
	}

	/**
	 * 从可类化数据读取器中解析引导包参数
	 * @param reader 可类化数据读取器
	 */
	public BootComponent(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置软件名称
	 * @param e Naming实例
	 */
	public void setWare(Naming e) {
		ware = e;
	}

	/**
	 * 返回软件名称
	 * @return Naming实例
	 */
	public Naming getWare() {
		return ware;
	}

	/**
	 * 设置GTC文件内容
	 * @param b 字节数组
	 */
	public void setContent(byte[] b) {
		content = b;
	}

	/**
	 * 返回DTC文件内容
	 * @return 字节数组
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * 生成当前实例的一个数据副本
	 * @return BootComponent实例
	 */
	public BootComponent duplicate() {
		return new BootComponent(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ware.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ware.hashCode();
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
		if (that == null || that.getClass() != BootComponent.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((BootComponent) that) == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(BootComponent that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(ware, that.ware);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 写入参数
		writer.writeObject(ware);
		writer.writeByteArray(content);
		// 返回写入的数据长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 解析参数
		ware = new Naming(reader);
		content = reader.readByteArray();
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

}