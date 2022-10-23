/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.limit;

import com.laxcus.law.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 用户限制操作单元
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public final class UserLimitItem extends LimitItem {

	private static final long serialVersionUID = 7463065219705932327L;

	/**
	 * 构造默认和私有的用户限制操作单元
	 */
	private UserLimitItem() {
		super();
	}

	/**
	 * 生成用户限制操作单元数据副本
	 * @param that UserLimitItem实例
	 */
	private UserLimitItem(UserLimitItem that) {
		super(that);
	}

	/**
	 * 构造用户限制操作单元，指定限制符号
	 * @param operator 限制符号
	 */
	public UserLimitItem(byte operator) {
		super(LawRank.USER, operator);
	}

	/**
	 * 从可类化数据读取器中解析用户限制操作单元
	 * @param reader 可类化数据读取器
	 */
	public UserLimitItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出用户限制操作单元
	 * @param reader 标记化读取器
	 */
	public UserLimitItem(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#duplicate()
	 */
	@Override
	public UserLimitItem duplicate() {
		return new UserLimitItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {

	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {

	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#embrace(com.laxcus.policy.limit.LimitItem)
	 */
	@Override
	public boolean embrace(LimitItem that) {
		// 用户级是顶级，所有限制操作单元都是它的同级或者子级
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#match(com.laxcus.policy.limit.FaultItem)
	 */
	@Override
	public boolean match(FaultItem that) {
		// 用户级限制操作匹配用户级、数据库级、表级、行级的锁定
		boolean success = false;
		if (that.getClass() == UserFaultItem.class) {
			success = true;
		} else if (that.getClass() == SchemaFaultItem.class) {
			success = true;
		} else if (that.getClass() == TableFaultItem.class) {
			success = true;
		} else if (that.getClass() == RowFaultItem.class) {
			success = true;
		} 
		// 返回匹配结果
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#conflict(com.laxcus.policy.rule.RuleItem)
	 */
	@Override
	public boolean conflict(RuleItem that) {
		// 限制
		return  LimitOperator.conflict(getOperator(), that.getOperator());
	}

}