/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 建立账号报告<br>
 * 
 * 当成功时，结果中包含ENTRANCE站点，是FRONT节点登录入口。
 * 
 * @author scott.liang
 * @version 1.0 9/17/2012
 * @since laxcus 1.0
 */
public class CreateUserProduct extends ProcessUserProduct {

	private static final long serialVersionUID = 1222285009812852643L;

	/** 内网地址 **/
	private Node entranceInner;

	/** 外网地址 **/
	private Node entranceOuter;

	/**
	 * 根据传入的参数，生成它的数据副本
	 * @param that CreateUserProduct实例
	 */
	private CreateUserProduct(CreateUserProduct that) {
		super(that);
		entranceInner = that.entranceInner;
		entranceOuter= that.entranceOuter;
	}

	/**
	 * 构造建立账号命令的处理结果
	 */
	public CreateUserProduct() {
		super();
	}

	/**
	 * 建立建立账号报告，指定用户签名
	 * @param siger 用户签名
	 */
	public CreateUserProduct(Siger siger) {
		this();
		setUsername(siger);
	}

	/**
	 * 建立建立账号报告，指定用户签名
	 * @param siger 用户签名
	 * @param successful 成功标识
	 */
	public CreateUserProduct(Siger siger, boolean successful) {
		this(siger);
		setSuccessful(successful);
	}

	/** 
	 * 构造建立账号命令的处理结果，指定一个ENTRANCE站点
	 * @param entrance Node实例
	 */
	public CreateUserProduct(Node inner, Node outer) {
		this();
		setEntrance(inner, outer);
	}

	/**
	 * 设置用户登录入口。当不成功的时候，允许空指针。
	 * @param e Node实例
	 */
	public void setEntrance(Node inner, Node outer) {
		entranceInner = inner;
		entranceOuter = outer;
	}

	/**
	 * 返回用户登录入口的内部地址
	 * @return Node实例
	 */
	public Node getEntranceInner() {
		return entranceInner;
	}

	/**
	 * 返回用户登录入口的外部地址
	 * @return Node实例
	 */
	public Node getEntranceOuter() {
		return entranceOuter;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CreateUserProduct duplicate() {
		return new CreateUserProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInstance(entranceInner);
		writer.writeInstance(entranceOuter);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		entranceInner = reader.readInstance(Node.class);
		entranceOuter = reader.readInstance(Node.class);
	}

}