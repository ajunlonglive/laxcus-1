/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.rule;

import com.laxcus.law.*;
import com.laxcus.law.forbid.*;
import com.laxcus.law.limit.*;
import com.laxcus.util.classable.*;

/**
 * 用户事务规则单元。
 * 
 * @author scott.liang
 * @version 1.1 4/1/2015
 * @since laxcus 1.0
 */
public final class UserRuleItem extends RuleItem {

	private static final long serialVersionUID = -8665807864980490695L;

	/**
	 * 构造用户事务规则单元
	 * @param that UserRuleItem实例
	 */
	private UserRuleItem(UserRuleItem that) {
		super(that);
	}

	/**
	 * 构造用户事务规则单元，指定操作符
	 * @param operator 操作符
	 */
	public UserRuleItem(byte operator) {
		super(LawRank.USER, operator);
	}

	/**
	 * 从可类化数据读取器中解析用户事务规则单元
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public UserRuleItem(ClassReader reader) {
		super();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#createForbidItem()
	 */
	@Override
	public ForbidItem createForbidItem() {
		return new UserForbidItem();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#createFaultItem()
	 */
	@Override
	public FaultItem createFaultItem() {
		return new UserFaultItem();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#conflict(com.laxcus.policy.rule.RuleItem)
	 */
	@Override
	public boolean conflict(RuleItem that) {
		// 在用户级，交给操作符去判断冲突
		return RuleOperator.conflict(getOperator(), that.getOperator());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#duplicate()
	 */
	@Override
	public UserRuleItem duplicate() {
		return new UserRuleItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// TODO Auto-generated method stub

	}

}
