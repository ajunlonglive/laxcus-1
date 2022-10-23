/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.manage;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 应用软件根集合 <br><br>
 * 
 * software root，保存BootItem的转换结果，做为类保存！<br><br>
 * 
 * 组成：<br>
 * 1. 软件包的散列码，具有唯一性 <br>
 * 2. 磁盘文件路径 <br>
 * 3. 应用令牌单元（采用级联方式串接）<br><br>
 * 
 * 执行从根单元开始！！！<br>
 * 
 * @author scott.liang
 * @version 1.0 8/3/2021
 * @since laxcus 1.0
 */
public class WRoot implements Classable, Cloneable, Comparable<WRoot>  {
	
	/** 软件包内容签名，必须有！**/
	private SHA256Hash hash;

	/** 磁盘文件路径。如果没有定义时，是系统应用，系统应用打包在启动包中；用户应用在磁盘文件上。**/
	private File path;
	
	/** 软件包的字节数组 **/
	private byte[] content;
	
	/** 绑定菜单 **/
	private String attchMenu;
	
	/** 版本 **/
	private String version;
	
	/** 根单元 **/
	private WElement element;
	
	/**
	 * 构造默认和私有应用软件标记
	 */
	public WRoot() {
		super();
	}

	/**
	 * 根据传入的应用软件标记，生成它的数据副本
	 * @param that 应用软件标记
	 */
	private WRoot(WRoot that) {
		this();
		
		hash = that.hash;
		path = that.path;
		content = that.content;
		attchMenu = that.attchMenu;
		version = that.version;
		element = that.element;
	}

	/**
	 * 构造应用软件标记，指定参数
	 * @param file 启动类路径
	 * @param hash 软件包内容签名
	 */
	public WRoot(File file, SHA256Hash hash) {
		this();
		setPath(file);
		setHash(hash);
	}

	/**
	 * 从可类化数据读取器中解析应用软件标记
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public WRoot(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置应用软件路径，允许空指针
	 * @param e File实例
	 */
	public void setPath(File e) {
		path = e;
	}

	/**
	 * 返回应用软件路径
	 * @return File实例，或者空指针
	 */
	public File getPath() {
		return path;
	}
	
	/**
	 * 判断有路径
	 * @return 返回真或者假
	 */
	public boolean hasPath() {
		return path != null;
	}

	/**
	 * 设置包字节数组
	 * @param b
	 */
	public void setContent(byte[] b) {
		content = b;
	}

	/**
	 * 返回包字节数组
	 * @return
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * 判断有内容
	 * @return 返回真或者假
	 */
	public boolean hasContent() {
		return content != null;
	}
	
	/**
	 * 判断是系统应用。系统应用保存在内存里，没有磁盘路径
	 * @return
	 */
	public boolean isSystem() {
		return path == null;
	}

	/**
	 * 判断是用户级应用。用户级应用保存在磁盘上，有磁盘路径
	 * @return
	 */
	public boolean isUser() {
		return path != null;
	}

	/**
	 * 设置软件包内容签名
	 * @param e SHA256散列码
	 */
	public void setHash(SHA256Hash e) {
		Laxkit.nullabled(e);

		hash = e;
	}

	/**
	 * 返回软件包内容签名
	 * @return SHA256散列码
	 */
	public SHA256Hash getHash() {
		return hash;
	}

	/**
	 * 关联菜单
	 * @param s
	 */
	public void setAttachMenu(String s) {
		attchMenu = s;
	}
	
	/**
	 * 返回关联菜单
	 * @return
	 */
	public String getAttachMenu() {
		return attchMenu;
	}
	
	/**
	 * 设置图标位置
	 * @param s
	 */
	public void setVersion(String s) {
		version = s;
	}

	/**
	 * 返回图标位置
	 * @return
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * 设置成员实例 
	 * @param e
	 */
	public void setElement(WElement e){
		Laxkit.nullabled(e);
		element = e;
	}
	
	/**
	 * 返回成员实例
	 * @return
	 */
	public WElement getElement(){
		return element;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return WRoot实例
	 */
	public WRoot duplicate() {
		return new WRoot(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != WRoot.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((WRoot) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hash.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (path != null) {
			return String.format("%s{%s}", path, hash);
		}
		return hash.toString();
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
	public int compareTo(WRoot that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(hash, that.hash);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		
		writer.writeObject(hash);
		writer.writeFile(path);
		writer.writeByteArray(content);
		writer.writeString(attchMenu);
		writer.writeString(version);
		writer.writeDefault(element);
		
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();

		hash = new SHA256Hash(reader);
		path = reader.readFile();
		content = reader.readByteArray();
		attchMenu = reader.readString();
		version = reader.readString();
		element = (WElement) reader.readDefault();

		return reader.getSeek() - seek;
	}

}