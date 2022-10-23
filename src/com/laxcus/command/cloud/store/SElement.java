/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.datetime.*;

/**
 * 云存储成员，云存储的磁盘、目录、文件的基础类
 * 
 * @author scott.liang
 * @version 1.0 10/23/2021
 * @since laxcus 1.0
 */
public abstract class SElement implements Serializable, Cloneable, Classable, Comparable<SElement> {

	private static final long serialVersionUID = -7049508805440660348L;

	/** 本地路径，不做串行化 **/
	private transient File root;

	/** 路径 **/
	protected String path;

	/** 最后修改时间 **/
	protected long lastModified;

	/**
	 * 构造存储成员
	 */
	protected SElement() {
		super();
	}

	/**
	 * 构造存储成员副本
	 * @param that 副本
	 */
	protected SElement(SElement that) {
		super();
		root = that.root;
		path = that.path;
		lastModified = that.lastModified;
	}

	/**
	 * 设置根目录
	 * @param e
	 */
	public void setRoot(File e){
		root = e;
	}

	/**
	 * 返回根目录
	 * @return
	 */
	public File getRoot(){
		return root;
	}

	/**
	 * 设置最后修改时间
	 * @param date
	 */
	public void setLastModified(Date date) {
		lastModified = SimpleTimestamp.format(date);
	}

	/**
	 * 返回最后修改时间
	 * @return
	 */
	public Date getLastModified() {
		return SimpleTimestamp.format(lastModified);
	}

	/**
	 * 设置路径
	 * @param s
	 */
	public void setPath(String s) {
		path = s;
	}

	/**
	 * 返回路径
	 * @return
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * 返回指向的实际名称 
	 * @return
	 */
	public String getName() {
		if (path == null || path.trim().isEmpty()) {
			return null;
		}
		int last = path.lastIndexOf("/");
		if (last >= 0) {
			return path.substring(last + 1);
		}
		return null;
	}

	/**
	 * 生成一个本地文件实例
	 * @param root 根目录
	 * @param siger 签名
	 * @return 返回File实例
	 */
	public File getLocal(File root, Siger siger) {
		String suffix = path.replace('/', File.separatorChar);
		String prefix = siger.toString();
		String str = suffix + File.separatorChar + prefix;
		return new File(root, str);
	}

	/**
	 * 复制SElement子类对象的浅层数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个对象是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		return compareTo((SElement) that) == 0;
	}

	/**
	 * 返回底层散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SElement that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(path, that.path);
		if (ret == 0) {
			ret = Laxkit.compareTo(lastModified, that.lastModified);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeString(path);
		writer.writeLong(lastModified);
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
		path = reader.readString();
		lastModified = reader.readLong();
		resolveSuffix(reader);
		// 返回读取的数据长度
		return reader.getSeek() - seek;
	}

	/**
	 * SElement子类对象生成自己的浅层数据副本。<br>
	 * 浅层数据副本和标准数据副本的区别在于：浅层数据副本只赋值对象，而不是复制对象内容本身。
	 * @return SElement子类实例
	 */
	public abstract SElement duplicate();

	/**
	 * 将操作命令参数信息写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 * @since 1.1
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析操作命令参数信息
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	protected abstract void resolveSuffix(ClassReader reader);
}