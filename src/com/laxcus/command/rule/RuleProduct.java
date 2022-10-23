/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rule;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 事务操作处理报告。<br>
 * 事务申请和撤销的处理结果。
 * 
 * @author scott.liang
 * @version 1.1 6/23/2015
 * @since laxcus 1.0
 */
public final class RuleProduct extends ConfirmProduct {

	private static final long serialVersionUID = 1753824787948439199L;

	/** 注册用户签名 **/
	private Siger username;
	
	/** 事务处理标识 **/
	private ProcessRuleTag tag;

	/**
	 * 构造默认和私有事务操作处理报告
	 */
	private RuleProduct() {
		super();
	}

	/**
	 * 生成事务操作处理报告的数据副本
	 * @param that RuleProduct实例
	 */
	private RuleProduct(RuleProduct that) {
		super(that);
		username = that.username;
		tag = that.tag;
	}

	/**
	 * 构造事务操作处理报告，指定参数
	 * @param username 用户签名
	 * @param tag 事务处理标识
	 * @param successful 成功标识
	 */
	public RuleProduct(Siger username, ProcessRuleTag tag, boolean successful) {
		this();
		setUsername(username);
		setTag(tag);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析事务操作处理报告
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public RuleProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户签名。不允许空指针
	 * @param e Siger实例
	 */
	public void setUsername(Siger e) {
		Laxkit.nullabled(e);

		username = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return username;
	}

	/**
	 * 设置事务处理标识。不允许空指针
	 * @param e ProcessRuleTag实例
	 */
	public void setTag(ProcessRuleTag e) {
		Laxkit.nullabled(e);

		tag = e;
	}

	/**
	 * 返回事务处理标识
	 * @return ProcessRuleTag实例
	 */
	public ProcessRuleTag getTag() {
		return tag;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public RuleProduct duplicate() {
		return new RuleProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeObject(username);
		writer.writeObject(tag);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		username = new Siger(reader);
		tag = new ProcessRuleTag(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s/%s#%s", username, tag, super.toString());
	}
}