/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 投递分布任务组件包反馈结果
 * 
 * @author scott.liang
 * @version 1.0 10/10/2019
 * @since laxcus 1.0
 */
public class MailTaskComponentProduct extends ConfirmProduct {

	private static final long serialVersionUID = -4484266933220870735L;

	/**
	 * 构造投递分布任务组件包反馈结果
	 */
	public MailTaskComponentProduct() {
		super();
	}

	/**
	 * 生成投递分布任务组件包反馈结果数据副本
	 * @param that 投递分布任务组件包反馈结果
	 */
	private MailTaskComponentProduct(MailTaskComponentProduct that) {
		super(that);
	}

	/**
	 * 构造投递分布任务组件包反馈结果
	 * @param successful 成功或者否
	 */
	public MailTaskComponentProduct(boolean successful) {
		this();
		setSuccessful(successful);
	}

	/**
	 * 从可类化读取器中解析投递分布任务组件包反馈结果
	 * @param reader 可类化数据读取器
	 */
	public MailTaskComponentProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.ConfirmProduct#duplicate()
	 */
	@Override
	public MailTaskComponentProduct duplicate() {
		return new MailTaskComponentProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
	}

}