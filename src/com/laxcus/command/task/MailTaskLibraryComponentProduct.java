/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 投递分布任务组件动态链接库包反馈结果
 * 
 * @author scott.liang
 * @version 1.0 10/18/2019
 * @since laxcus 1.0
 */
public class MailTaskLibraryComponentProduct extends MultiProcessProduct {

	private static final long serialVersionUID = -7628438366960422763L;

	/** 附件包投递位置，见PhaseTag中的定义 **/
	private int family;

	/**
	 * 构造投递分布任务组件动态链接库包反馈结果
	 */
	public MailTaskLibraryComponentProduct() {
		super();
	}

	/**
	 * 生成投递分布任务组件动态链接库包反馈结果数据副本
	 * @param that 投递分布任务组件动态链接库包反馈结果
	 */
	private MailTaskLibraryComponentProduct(MailTaskLibraryComponentProduct that) {
		super(that);
		family = that.family;
	}

	/**
	 * 构造投递分布任务组件动态链接库包反馈结果
	 * @param successful 成功或者否
	 */
	public MailTaskLibraryComponentProduct(boolean successful) {
		this();
		setSuccessful(successful);
	}

	/**
	 * 构造投递分布任务组件动态链接库包反馈结果
	 * @param successful 成功或者否
	 */
	public MailTaskLibraryComponentProduct(boolean successful, int family) {
		this(successful);
		setFamily(family);
	}
	
	/**
	 * 构造投递分布任务组件动态链接库包反馈结果
	 * @param successful 成功或者否
	 * @param family 类型
	 */
	public MailTaskLibraryComponentProduct(int family) {
		this();
		setFamily(family);
	}

	/**
	 * 从可类化读取器中解析投递分布任务组件动态链接库包反馈结果
	 * @param reader 可类化数据读取器
	 */
	public MailTaskLibraryComponentProduct(ClassReader reader) {
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.ConfirmProduct#duplicate()
	 */
	@Override
	public MailTaskLibraryComponentProduct duplicate() {
		return new MailTaskLibraryComponentProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(family);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		family = reader.readInt();
	}

}