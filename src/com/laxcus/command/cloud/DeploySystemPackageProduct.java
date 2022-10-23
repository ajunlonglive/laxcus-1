/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 构造发布系统包处理结果
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class DeploySystemPackageProduct extends ConfirmProduct {

	private static final long serialVersionUID = 4027634039311277633L;

	/** 来源地址，允许空指针 **/
	private Node remote;
	
	/** 部署的单元数目 **/
	private int elements;
	
	/**
	 * 构造默认的发布系统包处理结果
	 */
	public DeploySystemPackageProduct() {
		super();
	}
	
	/**
	 * 构造发布系统包处理结果
	 * @param successful 发送成功或者否
	 */
	public DeploySystemPackageProduct(boolean successful) {
		this();
		setSuccessful(successful);
	}
	
	/**
	 * 构造发布系统包处理结果
	 * @param remote 目标地址
	 * @param file 文件
	 * @param successful 发送成功或者否
	 */
	public DeploySystemPackageProduct(Node remote, boolean successful) {
		this( successful);
		setRemote(remote);
	}

	/**
	 * 生成发布系统包处理结果数据副本
	 * @param that 发布系统包处理结果实例
	 */
	private DeploySystemPackageProduct(DeploySystemPackageProduct that) {
		super(that);
		remote = that.remote;
		elements = that.elements;
	}

	/**
	 * 从可类化读取器中解析发布系统包处理结果
	 * @param reader 可类化数据读取器
	 */
	public DeploySystemPackageProduct(ClassReader reader) {
		this();
		resolve(reader);
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
	

	/**
	 * 设置成员数目
	 * @param i 成员数目
	 */
	public void setElements(int i) {
		elements = i;
	}

	/**
	 * 返回成员数目
	 * @return 成员数目
	 */
	public int getElements() {
		return elements;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DeploySystemPackageProduct duplicate() {
		return new DeploySystemPackageProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInstance(remote);
		writer.writeInt(elements);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		remote = reader.readInstance(Node.class);
		elements = reader.readInt();
	}

}