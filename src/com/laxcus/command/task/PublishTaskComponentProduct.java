/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import java.io.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 构造发布分布任务组件处理结果
 * 
 * @author scott.liang
 * @version 1.0 10/8/2019
 * @since laxcus 1.0
 */
public class PublishTaskComponentProduct extends ConfirmProduct {

	private static final long serialVersionUID = 4027634039311277633L;

	/** 来源地址，允许空指针 **/
	private Node remote;
	
	/** 本地磁盘文件路径，允许空指针 **/
	private String path;

	/**
	 * 构造默认的发布分布任务组件处理结果
	 */
	public PublishTaskComponentProduct() {
		super();
	}
	
	/**
	 * 构造发布分布任务组件处理结果
	 * @param successful 发送成功或者否
	 */
	public PublishTaskComponentProduct(boolean successful) {
		this();
		setSuccessful(successful);
	}
	
	/**
	 * 构造发布分布任务组件处理结果
	 * @param path 文件路径
	 */
	public PublishTaskComponentProduct(String path) {
		this();
		setPath(path);
	}

	/**
	 * 构造发布分布任务组件处理结果
	 * @param path 文件
	 * @param successful 发送成功或者否
	 */
	public PublishTaskComponentProduct(String path, boolean successful) {
		this(path);
		setSuccessful(successful);
	}

	/**
	 * 构造发布分布任务组件处理结果
	 * @param file 文件名
	 */
	public PublishTaskComponentProduct(File file) {
		this();
		setPath(file);
	}

	/**
	 * 构造发布分布任务组件处理结果
	 * @param file 文件
	 * @param successful 发送成功或者否
	 */
	public PublishTaskComponentProduct(File file, boolean successful) {
		this(file);
		setSuccessful(successful);
	}

	/**
	 * 构造发布分布任务组件处理结果
	 * @param remote 目标地址
	 * @param file 文件
	 * @param successful 发送成功或者否
	 */
	public PublishTaskComponentProduct(Node remote, File file, boolean successful) {
		this(file, successful);
		setRemote(remote);
	}

	/**
	 * 生成发布分布任务组件处理结果数据副本
	 * @param that 发布分布任务组件处理结果实例
	 */
	private PublishTaskComponentProduct(PublishTaskComponentProduct that) {
		super(that);
		path = that.path;
		remote = that.remote;
	}

	/**
	 * 从可类化读取器中解析发布分布任务组件处理结果
	 * @param reader 可类化数据读取器
	 */
	public PublishTaskComponentProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置文件路径
	 * @param e 文件路径
	 */
	public void setPath(String e) {
		path = e;
	}

	/**
	 * 返回文件路径
	 * @return 文件路径
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 设置文件路径，允许空指针
	 * @param e 文件路径
	 */
	public void setPath(File e) {
		setPath(Laxkit.canonical(e));
	}
	
	/**
	 * 设置来源节点
	 * @param e Node实例
	 */
	public void setRemote(Node e) {
		remote = e;
	}

	/**
	 * 返回来源节点
	 * @return Node实例
	 */
	public Node getRemote() {
		return remote;
	}

	/**
	 * 返回来源节点
	 * @return 字符串
	 */
	public String getRemoteText() {
		return (remote != null ? remote.toString() : "");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PublishTaskComponentProduct duplicate() {
		return new PublishTaskComponentProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeString(path);
		writer.writeInstance(remote);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		path = reader.readString();
		remote = reader.readInstance(Node.class);
	}

}