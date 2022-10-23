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
 * 数据表事务规则单元。<br>
 * 
 * @author scott.liang
 * @version 1.1 4/1/2015
 * @since laxcus 1.0
 */
public final class TableRuleItem extends RuleItem {

	private static final long serialVersionUID = -2957970751408200627L;

	/** 数据表名 **/
	private Space space;

	/**
	 * 根据传入的数据表事务规则单元，生成它的数据副本
	 * @param that TableRuleItem实例
	 */
	private TableRuleItem(TableRuleItem that) {
		super(that);
		space = that.space;
	}

	/**
	 * 构造数据表事务规则单元，指定操作符
	 * @param operator 操作符
	 */
	public TableRuleItem(byte operator) {
		super(LawRank.TABLE, operator);
	}

	/**
	 * 构造数据表事务规则单元，指定操作符和数据表名
	 * @param operator 操作符
	 * @param space 数据表名
	 */
	public TableRuleItem(byte operator, Space space) {
		this(operator);
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析数据表事务规则单元
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TableRuleItem(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 设置数据表名
	 * @param e 数据表名
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return 数据表名
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 返回数据库名
	 * @return 数据库名
	 */
	public Fame getFame() {
		return space.getSchema();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s<%s>", super.toString(), space);
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
		// 当前对象一致时，进一步比较
		if (ret == 0 && that.getClass() == TableRuleItem.class) {
			TableRuleItem item = (TableRuleItem) that;
			ret = Laxkit.compareTo(space, item.space);
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
		return new TableForbidItem(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#createFaultItem()
	 */
	@Override
	public FaultItem createFaultItem() {
		return new TableFaultItem(space);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#conflict(com.laxcus.policy.rule.RuleItem)
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
			refuse = (Laxkit.compareTo(space.getSchema(), item.getFame()) == 0);
		} else if (that.getClass() == TableRuleItem.class) {
			TableRuleItem item = (TableRuleItem) that;
			// 匹配即冲突
			refuse = (Laxkit.compareTo(space, item.space) == 0);
		}

		// 2项匹配，冲突
		return refuse && RuleOperator.conflict(getOperator(), that.getOperator());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#duplicate()
	 */
	@Override
	public TableRuleItem duplicate() {
		return new TableRuleItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.rule.RuleItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = new Space(reader);
	}
}