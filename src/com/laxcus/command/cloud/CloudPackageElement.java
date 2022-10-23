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

/**
 * 云应用软件包成员<br><br>
 * 
 * 记录一个阶段的云应用，包括：<br>
 * 1. 引导文件，只允许一个 <br>
 * 2. 附件文件，任意多个 <br>
 * 3. 动态库文件，任意多个 <br>
 * 
 * @author scott.liang
 * @version 1.0 2/9/2020
 * @since laxcus 1.0
 */
public class CloudPackageElement implements Classable, Cloneable, Serializable , Comparable<CloudPackageElement>{
	
	private static final long serialVersionUID = 1210400602903831946L;

	/** 标签名称 **/
	private String mark;

	/** 启动引导文件 **/
	private FileKey boot;

	/** 附件文件 **/
	private TreeSet<FileKey> assists = new TreeSet<FileKey>();
	
	/** 动态库名称 **/
	private TreeSet<FileKey> links = new TreeSet<FileKey>();
	
	/**
	 * 构造默认的云应用软件包成员
	 */
	private CloudPackageElement() {
		super();
	}
	
	/**
	 * 构造默认的云应用软件包成员，指定标签名称
	 * @param mark 标签名称
	 */
	public CloudPackageElement(String mark) {
		this();
		setMark(mark);
	}
	
	/**
	 * 生成云应用软件包成员副本
	 * @param that 云应用软件包成员
	 */
	private CloudPackageElement(CloudPackageElement that) {
		this();
		mark = that.mark;
		boot = that.boot;
		assists.addAll(that.assists);
		links.addAll(that.links);
	}

	/**
	 * 从可类化读取器中解析云应用软件包成员
	 * @param reader 可类化数据读取器
	 */
	public CloudPackageElement(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置标签名称，不允许空指针
	 * @param e 字符串
	 */
	public void setMark(String e) {
		Laxkit.nullabled(e);
		mark = e;
	}
	
	/**
	 * 返回标签名称
	 * @return 字符串
	 */
	public String getMark() {
		return mark;
	}
	
	/**
	 * 设置引导文件
	 * @param e
	 */
	public void setBoot(FileKey e) {
		Laxkit.nullabled(e);
		boot = e;
	}

	/**
	 * 返回引导文件
	 * @return 引导文件
	 */
	public FileKey getBoot() {
		return boot;
	}
	
	/**
	 * 判断有引导文件
	 * @return 返回真或者假
	 */
	public boolean hasBoot() {
		return boot != null;
	}
	
	/**
	 * 保存JAR附件文件标记
	 * @param key JAR标记
	 * @return 保存成功返回真，否则假
	 */
	public boolean addAssist(FileKey key) {
		Laxkit.nullabled(key);
		return assists.add(key);
	}
	
	/**
	 * 删除JAR附件文件标记
	 * @param key JAR标记
	 * @return 删除成功返回真，否则假
	 */
	public boolean removeAssist(FileKey key) {
		Laxkit.nullabled(key);
		return assists.remove(key);
	}

	/**
	 * 输出JAR包
	 * @return
	 */
	public List<FileKey> getAssists() {
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
		
		// 标签
		writer.writeString(mark);
		// 引导文件
		writer.writeObject(boot);
		// 附件
		writer.writeInt(assists.size());
		for(FileKey e : assists) {
			writer.writeObject(e);
		}
		// 动态链接库
		writer.writeInt(links.size());
		for(FileKey e : links) {
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
		
		// 标签
		mark = reader.readString();

		// 引导文件
		boot = new FileKey(reader);
		
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
	 * @return CloudPacketElement实例
	 */
	public CloudPackageElement duplicate() {
		return new CloudPackageElement(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CloudPackageElement.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((CloudPackageElement) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return boot.hashCode();
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
	public int compareTo(CloudPackageElement that) {
		if (that == null) {
			return 1;
		}

		return Laxkit.compareTo(boot, that.boot);
	}

}