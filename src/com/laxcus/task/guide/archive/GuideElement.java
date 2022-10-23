/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.guide.archive;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;

/**
 * 启动引导任务组件成员，包括“引导包”、“JAR附件包”、“动态链接库”的属性类
 * 
 * @author scott.liang
 * @version 1.0 6/17/2020
 * @since laxcus 1.0
 */
public class GuideElement implements Comparable<GuideElement>, Cloneable {

	/** 文件里的“GUIDE-INF/guides.xml”文本内容 **/
	private byte[] configure;
	
	/** 所在目录 **/
	private File root;

	/** 引导类 **/
	private FileKey boot;

	/** 辅助附件包（*.jar格式）**/
	private ArrayList<FileKey> assists = new ArrayList<FileKey>();
	
	/** 动态链接库文件属性 **/
	private ArrayList<FileKey> links = new ArrayList<FileKey>();

	/**
	 * 构造默认的启动引导任务组件成员
	 */
	public GuideElement() {
		super();
	}
	
	/**
	 * 生成当前启动引导任务组件成员的数据副本
	 * @param that 当前启动引导任务组件成员
	 */
	private GuideElement(GuideElement that) {
		this();
		configure = that.configure;
		root = that.root;
		boot = that.boot;
		assists.addAll(that.assists);
		links.addAll(that.links);
	}

	/**
	 * 构造启动引导任务组件成员，指定磁盘目录
	 * @param root
	 */
	public GuideElement(File root) {
		this();
		setRoot(root);
	}

	/**
	 * 设置分布任务组件所在的磁盘目录，不允许空指针！！！
	 * @param e 磁盘目录
	 */
	public void setRoot(File e) {
		Laxkit.nullabled(e);
		root = e;
	}

	/**
	 * 返回分布任务组件群在的磁盘目录
	 * @return 磁盘目录
	 */
	public File getRoot() {
		return root;
	}

	/**
	 * 设置配置内容
	 * @param b
	 */
	public void setConfigure(byte[] b) {
		configure = b;
	}

	/**
	 * 返回配置内容
	 * @return 字节数组
	 */
	public byte[] getConfigure() {
		return configure;
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return boot == null && assists.isEmpty() && links.isEmpty();
	}
	
	/**
	 * 返回当前成员数目
	 * @return 成员数目
	 */
	public int size() {
		return (boot != null ? 1 : 0) + assists.size() + links.size();
	}

	/**
	 * 设置分布计算组件引导包，不允许空指针！
	 * @param e 分布计算组件引导包
	 */
	public void setBoot(FileKey e) {
		Laxkit.nullabled(e);
		boot = e;
	}

	/**
	 * 返回分布计算组件引导包
	 * @return 分布计算组件引导包
	 */
	public FileKey getBoot() {
		return boot;
	}

	/**
	 * 保存JAR文件标记
	 * @param key JAR标记
	 * @return 保存成功返回真，否则假
	 */
	public boolean addJAR(FileKey key) {
		Laxkit.nullabled(key);
		return assists.add(key);
	}
	
	/**
	 * 删除JAR文件标记
	 * @param key JAR标记
	 * @return 删除成功返回真，否则假
	 */
	public boolean removeJAR(FileKey key) {
		Laxkit.nullabled(key);
		return assists.remove(key);
	}

	/**
	 * 输出JAR包
	 * @return
	 */
	public List<FileKey> getJARs() {
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

	/**
	 * 生成数据副本
	 * @return 数据副本
	 */
	public GuideElement duplicate() {
		return new GuideElement(this);
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
	public int compareTo(GuideElement that) {
		if (that == null) {
			return 1;
		}
		// 比较工作区和路径
		return Laxkit.compareTo(root, that.root);
	}

}