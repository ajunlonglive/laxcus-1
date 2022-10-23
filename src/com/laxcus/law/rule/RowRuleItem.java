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
 * 行事务规则单元
 * 
 * @author scott.liang
 * @version 1.0 4/9/2018
 * @since laxcus 1.0
 */
public final class RowRuleItem extends RuleItem {

	private static final long serialVersionUID = 4211976756875093365L;

	/** 行记录特称 **/
	private RowFeature feature;

	/**
	 * 根据传入的行事务规则单元，生成它的数据副本
	 * @param that RowRuleItem实例
	 */
	private RowRuleItem(RowRuleItem that) {
		super(that);
		feature = that.feature;
	}

	/**
	 * 构造行事务规则单元，指定操作符
	 * @param operator 操作符
	 */
	public RowRuleItem(byte operator) {
		super(LawRank.ROW, operator);
	}

	/**
	 * 构造行事务规则单元，指定操作符和数据表名
	 * @param operator 操作符
	 * @param feature 行记录特称
	 */
	public RowRuleItem(byte operator, RowFeature feature) {
		this(operator);
		setFeature(feature);
	}

	/**
	 * 从可类化数据读取器中解析行事务规则单元
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RowRuleItem(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 设置行记录特称，不允许空指定针
	 * @param e 行记录特称
	 */
	public void setFeature(RowFeature e) {
		Laxkit.nullabled(e);
		feature = e;
	}

	/**
	 * 返回行记录特称
	 * @return 行记录特称实例
	 */
	public RowFeature getFeature() {
		return feature;
	}

	/**
	 * 返回数据库名
	 * @return 数据库名
	 */
	public Fame getFame(){
		return feature.getFame();
	}

	/**
	 * 返回表名
	 * @return 表名
	 */
	public Space getSpace(){
		return feature.getSpace();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.law.rule.RuleItem#toString()
	 */
	@Override
	public String toString() {
		return feature.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.law.rule.RuleItem#compareTo(com.laxcus.law.rule.RuleItem)
	 */
	@Override
	public int compareTo(RuleItem that) {
		// 空值在前
		if (that == null) {
			return 1;
		}

		// 比较上级对象
		int ret = super.compareTo(that);
		if (ret == 0 && that.getClass() == RowRuleItem.class) {
			RowRuleItem item = (RowRuleItem) that;
			ret = Laxkit.compareTo(feature, item.feature);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.law.rule.RuleItem#createFaultItem()
	 */
	@Override
	public FaultItem createFaultItem() {
		return new RowFaultItem(feature);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.law.rule.RuleItem#createForbidItem()
	 */
	@Override
	public ForbidItem createForbidItem() {
		return new RowForbidItem(feature);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.law.rule.RuleItem#conflict(com.laxcus.law.rule.RuleItem)
	 */
	@Override
	public boolean conflict(RuleItem that) {
		boolean refuse = false;
		// 用户、数据库、表冲突判断
		if (that.getClass() == UserRuleItem.class) {
			refuse = true;
		} else if (that.getClass() == SchemaRuleItem.class) {
			SchemaRuleItem item = (SchemaRuleItem) that;
			// 匹配即冲突
			refuse = (Laxkit.compareTo(getFame(), item.getFame()) == 0);
		} else if (that.getClass() == TableRuleItem.class) {
			TableRuleItem item = (TableRuleItem) that;
			// 匹配即冲突
			refuse = (Laxkit.compareTo(getSpace(), item.getSpace()) == 0);
		} else if (that.getClass() == RowRuleItem.class) {
			RowRuleItem item = (RowRuleItem) that;
			// 匹配即冲突
			refuse = (Laxkit.compareTo(feature, item.feature) == 0);
		}

		// 3项匹配，冲突
		return refuse && RuleOperator.conflict(getOperator(), that.getOperator());	
	}

	/* (non-Javadoc)
	 * @see com.laxcus.law.rule.RuleItem#duplicate()
	 */
	@Override
	public RowRuleItem duplicate() {
		return new RowRuleItem(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.law.rule.RuleItem#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(feature);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.law.rule.RuleItem#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		feature = new RowFeature(reader);
	}

}