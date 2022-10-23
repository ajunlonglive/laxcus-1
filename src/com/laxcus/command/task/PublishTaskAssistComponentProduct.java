/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 构造发布分布任务组件应用附件处理结果
 * 
 * @author scott.liang
 * @version 1.0 10/8/2019
 * @since laxcus 1.0
 */
public class PublishTaskAssistComponentProduct extends MultiProcessProduct {

	private static final long serialVersionUID = 5299753144789597124L;

	/** 来源地址，允许空指针 **/
	private Node remote;

	/** 本地磁盘文件，允许空指针 **/
	private String path;

	/** 附件包投递位置，见PhaseTag中的定义 **/
	private int family;

	/**
	 * 构造默认的发布分布任务组件应用附件处理结果
	 */
	public PublishTaskAssistComponentProduct() {
		super();
	}

	/**
	 * 构造发布分布任务组件应用附件处理结果
	 * @param path 文件名
	 * @param family 阶段类型
	 */
	public PublishTaskAssistComponentProduct(String path, int family) {
		this();
		setPath(path);
		setFamily(family);
	}

	/**
	 * 构造发布分布任务组件应用附件处理结果
	 * @param path 文件
	 * @param family 阶段类型
	 * @param successful 发送成功或者否
	 */
	public PublishTaskAssistComponentProduct(String path, int family, boolean successful) {
		this(path, family);
		setSuccessful(successful);
	}
	
	/**
	 * 构造发布分布任务组件应用附件处理结果
	 * @param file 文件名
	 * @param family 阶段类型
	 */
	public PublishTaskAssistComponentProduct(File file, int family) {
		this();
		setPath(file);
		setFamily(family);
	}

	/**
	 * 构造发布分布任务组件应用附件处理结果
	 * @param file 文件
	 * @param family 阶段类型
	 * @param successful 发送成功或者否
	 */
	public PublishTaskAssistComponentProduct(File file, int family, boolean successful) {
		this(file, family);
		setSuccessful(successful);
	}
	
	/**
	 * 构造发布分布任务组件应用附件处理结果
	 * @param file 文件
	 * @param family 阶段类型
	 * @param successful 发送成功或者否
	 */
	public PublishTaskAssistComponentProduct(Node remote, File file, int family, boolean successful) {
		this(file, family, successful);
		setRemote(remote);
	}

	/**
	 * 生成发布分布任务组件应用附件处理结果数据副本
	 * @param that 发布分布任务组件应用附件处理结果实例
	 */
	private PublishTaskAssistComponentProduct(PublishTaskAssistComponentProduct that) {
		super(that);
		path = that.path;
		family = that.family;
	}

	/**
	 * 从可类化读取器中解析发布分布任务组件应用附件处理结果
	 * @param reader 可类化数据读取器
	 */
	public PublishTaskAssistComponentProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置投递阶段类型
	 * @param who 投递阶段类型
	 */
	public void setFamily(int who) {
		if (!PublishTaskAxes.isPhase(who)) {
			throw new IllegalValueException("illegal family %d", who);
		}
		family = who;
	}

	/**
	 * 返回投递阶段类型
	 * @return 投递阶段类型
	 */
	public int getFamily() {
		return family;
	}
	
	/**
	 * 设置文件路径
	 * @param e 字符串
	 */
	public void setPath(String e) {
		path = e;
	}

	/**
	 * 设置文件实例，允许空指针
	 * @param e 文件实例
	 */
	public void setPath(File e) {
		path = (e != null ? Laxkit.canonical(e) : null);
	}

	/**
	 * 返回文件实例
	 * @return 文件实例
	 */
	public String getPath() {
		return path;
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
	 * @return Node实例
	 */
	public String getRemoteText() {
		return (remote != null ? remote.toString() : "");
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PublishTaskAssistComponentProduct duplicate() {
		return new PublishTaskAssistComponentProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(family);
		writer.writeString(path);
		writer.writeInstance(remote);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		family = reader.readInt();
		path = reader.readString();
		remote = reader.readInstance(Node.class);
	}

}