/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.rule;

import com.laxcus.access.schema.*;
import com.laxcus.law.*;
import com.laxcus.law.forbid.*;
import com.laxcus.law.limit.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据库事务规则单元。<br>
 * 
 * @author scott.liang
 * @version 1.1 4/1/2015
 * @since laxcus 1.0
 */
public final class SchemaRuleItem extends RuleItem {

	private static final long serialVersionUID = -5795896801119718760L;

	/** 数据库名 **/
	private Fame fame;

	/**
	 * 根据传入的数据库事务规则单元，生成它的数据副本
	 * @param that SchemaRuleItem实例
	 */
	private SchemaRuleItem(SchemaRuleItem that) {
		super(that);
		fame = that.fame;
	}

	/**
	 * 构造数据库事务规则单元，指定操作符
	 * @param operator 操作符
	 */
	public SchemaRuleItem(byte operator) {
		super(LawRank.SCHEMA, operator);
	}

	/**
	 * 构造数据库事务规则单元，指定操作符和数据库名
	 * @param operator 操作符
	 * @param fame 数据库名称
	 */
	public SchemaRuleItem(byte operator, Fame fame) {
		this(operator);
		setFame(fame);
	}

	/**
	 * 从可类化数据读取器中解析数据库事务规则单元
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SchemaRuleItem(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 设置数据库名
	 * @param e Fame实例
	 */
	public void setFame(Fame e) {
		Laxkit.nullabled(e);

		fame = e;
	}

	/**
	 * 返回数据库名
	 * @return Fame实例
	 */
	public Fame getFame() {
		return fame;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s<%s>", super.toString(), fame);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RuleItem that) {
		// 空值在前
		if (that == null) {
			return 1;
		}

		// 比较上级对象
		int ret = super.compareTo(that);
		// 对象一致时比较数据库名称
		if (ret == 0 && that.getClass() == SchemaRuleItem.class) {
			// 比较数据库名
			SchemaRuleItem item = (SchemaRuleItem) that;
			ret = Laxkit.compareTo(fame, item.fame);
		}
		// 返回结果
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#createForbidItem()
	 */
	@Override
	public ForbidItem createForbidItem() {
		return new SchemaForbidItem(fame);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#createFaultItem()
	 */
	@Override
	public FaultItem createFaultItem() {
		return new SchemaFaultItem(fame);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#conflict(com.laxcus.policy.rule.RuleItem)
	 */
	@Override
	public boolean conflict(RuleItem that) {
		// 数据库或者表一致
		boolean refuse = false;

		// 用户、数据库、表冲突判断
		if (that.getClass() == UserRuleItem.class) {
			refuse = true;
		} else if (that.getClass() == SchemaRuleItem.class) {
			SchemaRuleItem item = (SchemaRuleItem) that;
			// 匹配即冲突
			refuse = (Laxkit.compareTo(fame, item.fame) ==0);
		} else if (that.getClass() == TableRuleItem.class) {
			TableRuleItem item = (TableRuleItem) that;
			// 匹配即冲突
			refuse = (Laxkit.compareTo(fame, item.getFame()) == 0);
		}

		// 2项匹配，冲突
		return refuse && RuleOperator.conflict(getOperator(), that.getOperator());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#duplicate()
	 */
	@Override
	public SchemaRuleItem duplicate() {
		return new SchemaRuleItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(fame);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		fame = new Fame(reader);
	}

}