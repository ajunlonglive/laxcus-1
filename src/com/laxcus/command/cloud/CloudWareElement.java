/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 云应用成员<br><br>
 * 
 * 以命名方式记录一个成员，包括：<br>
 * 1. 阶段命名 <br>
 * 2. 附件文件名 <br>
 * 3. 动态库文件名 <br>
 * 
 * @author scott.liang
 * @version 1.0 2/9/2020
 * @since laxcus 1.0
 */
public class CloudWareElement implements Classable, Cloneable, Serializable , Comparable<CloudWareElement>{
	
	private static final long serialVersionUID = -8263204483980984634L;
	
	/** 阶段命名 **/
	private Phase phase;

	/** 附件名 **/
	private TreeSet<FileKey> assists = new TreeSet<FileKey>();
	
	/** 动态库名称 **/
	private TreeSet<FileKey> links = new TreeSet<FileKey>();
	
	/**
	 * 构造默认的云应用成员
	 */
	private CloudWareElement() {
		super();
	}

	/**
	 * 构造云应用成员，指定分布任务组件类型 
	 * @param taskFamily 分布任务组件类型
	 */
	public CloudWareElement(Phase phase) {
		this();
		setPhase(phase);
	}
	
	/**
	 * 生成云应用成员副本
	 * @param that 云应用成员
	 */
	private CloudWareElement(CloudWareElement that) {
		this();
		phase = that.phase;
		assists.addAll(that.assists);
		links.addAll(that.links);
	}

	/**
	 * 从可类化读取器中解析云应用成员
	 * @param reader 可类化数据读取器
	 */
	public CloudWareElement(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置阶段命名
	 * @param e
	 */
	public void setPhase(Phase e) {
		Laxkit.nullabled(e);
		phase = e;
	}
	
	/**
	 * 返回阶段命名
	 * @return
	 */
	public Phase getPhase() {
		return phase;
	}
	
	/**
	 * 保存JAR附件文件标记
	 * @param key JAR标记
	 * @return 保存成功返回真，否则假
	 */
	public boolean addJar(FileKey key) {
		Laxkit.nullabled(key);
		return assists.add(key);
	}
	
	/**
	 * 删除JAR附件文件标记
	 * @param key JAR标记
	 * @return 删除成功返回真，否则假
	 */
	public boolean removeJar(FileKey key) {
		Laxkit.nullabled(key);
		return assists.remove(key);
	}

	/**
	 * 输出JAR包
	 * @return
	 */
	public List<FileKey> getJars() {
		return new ArrayList<FileKey>(assists);
	}
	
	/**
	 * 保存动态链接库文件标记
	 * @param key 动态链接库文件
	 * @return 保存成功返回真，否则假
	 */
	public boolean addLibrary(FileKey key) {
		Laxkit.nullabled(key);
		return links.add(key);
	}
	
	/**
	 * 删除动态链接库文件标记
	 * @param key 动态链接库文件
	 * @return 删除成功返回真，否则假
	 */
	public boolean removeLibrary(FileKey key) {
		Laxkit.nullabled(key);
		return links.remove(key);
	}

	/**
	 * 输出动态链接库文件
	 * @return FileKey集合
	 */
	public List<FileKey> getLibraries() {
		return new ArrayList<FileKey>(links);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();

		// 阶段命名
		writer.writeObject(phase);
		// 附件
		writer.writeInt(assists.size());
		for (FileKey e : assists) {
			writer.writeObject(e);
		}
		// 动态链接库
		writer.writeInt(links.size());
		for (FileKey e : links) {
			writer.writeObject(e);
		}

		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();

		// 阶段命名
		phase = new Phase(reader);
		// 附件
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			FileKey e = new FileKey(reader);
			assists.add(e);
		}
		// 动态链接库
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			FileKey e = new FileKey(reader);
			links.add(e);
		}

		// 统计读取字节数
		return reader.getSeek() - seek;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return CloudWareElement实例
	 */
	public CloudWareElement duplicate() {
		return new CloudWareElement(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CloudWareElement.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((CloudWareElement) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return phase.hashCode();
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
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CloudWareElement that) {
		if (that == null) {
			return 1;
		}

		return Laxkit.compareTo(phase, that.phase);
	}

}