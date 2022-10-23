/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.login;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 注册站点命令的应答结果
 * 
 * @author scott.liang
 * @version 1.0 12/03/2017
 * @since laxcus 1.0
 */
public class LoginSiteProduct extends ConfirmProduct {

	private static final long serialVersionUID = 2458541219345741235L;

	/** 节点地址 **/
	private Node node;
	
	/**
	 * 构造默认的注册站点命令的应答结果
	 */
	private LoginSiteProduct() {
		super();
	}

	/**
	 * 构造注册站点命令的应答结果，指定参数
	 * @param node 节点地址
	 * @param successful 成功或者否
	 */
	public LoginSiteProduct(Node node, boolean successful) {
		this();
		setSuccessful(successful);
		setNode(node);
	}

	/**
	 * 从可类化数据读取器中解析注册站点命令的应答结果
	 * @param reader 可类化数据读取器
	 */
	public LoginSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成注册站点命令的应答结果的数据副本
	 * @param that 注册站点命令的应答结果
	 */
	private LoginSiteProduct(LoginSiteProduct that) {
		super(that);
		node = that.node;
	}
	
	/**
	 * 设置站点地址实例，不允许空指针
	 * @param e 站点地址实例
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);
		node = e;
	}
	
	/**
	 * 返回站点地址实例
	 * @return 站点地址实例
	 */
	public Node getNode() {
		return node;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.SuccessfulProduct#toString()
	 */
	@Override
	public String toString() {
		if (node != null) {
			return node.toString();
		}
		return "none node";
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public LoginSiteProduct duplicate() {
		return new LoginSiteProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeObject(node);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		node = new Node(reader);
	}

}
