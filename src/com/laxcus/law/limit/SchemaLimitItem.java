/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.limit;

import com.laxcus.access.schema.*;
import com.laxcus.law.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 数据库限制操作单元
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public final class SchemaLimitItem extends LimitItem {

	private static final long serialVersionUID = -3045330495836664474L;

	/** 数据库名称 **/
	private Fame fame;

	/**
	 * 构造默认和私有的数据库限制操作单元
	 */
	private SchemaLimitItem() {
		super();
	}

	/**
	 * 生成数据库限制操作单元数据副本
	 * @param that SchemaLimitItem实例
	 */
	private SchemaLimitItem(SchemaLimitItem that) {
		super(that);
		fame = that.fame.duplicate();
	}

	/**
	 * 构造数据库限制操作单元，指定参数
	 * @param operator 操作符
	 * @param fame 数据库名称
	 */
	public SchemaLimitItem(byte operator, Fame fame) {
		super(LawRank.SCHEMA, operator);
		setFame(fame);
	}

	/**
	 * 从可类化数据读取器中解析数据库限制操作单元
	 * @param reader 可类化数据读取器
	 */
	public SchemaLimitItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出数据库限制操作单元
	 * @param reader 标记化读取器
	 */
	public SchemaLimitItem(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 设置数据库名。不允许空值
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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ fame.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", super.toString(), fame);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(LimitItem that) {
		// 去上级比较
		int ret = super.compareTo(that);
		// 当前比较
		if (ret == 0) {
			SchemaLimitItem item = (SchemaLimitItem) that;
			ret = Laxkit.compareTo(fame, item.fame);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#duplicate()
	 */
	@Override
	public SchemaLimitItem duplicate() {
		return new SchemaLimitItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(fame);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		fame = new Fame(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#embrace(com.laxcus.policy.limit.LimitItem)
	 */
	@Override
	public boolean embrace(LimitItem that) {
		boolean success = false;
		// 数据库级包含同级和表级，并且数据库名一致
		if (that.getClass() == SchemaLimitItem.class) {
			SchemaLimitItem item = (SchemaLimitItem) that;
			success = (Laxkit.compareTo(fame, item.getFame()) == 0);
		} else if (that.getClass() == TableLimitItem.class) {
			TableLimitItem item = (TableLimitItem) that;
			success = (Laxkit.compareTo(fame, item.getFame()) == 0);
		} else if (that.getClass() == RowLimitItem.class) {
			RowLimitItem item = (RowLimitItem) that;
			success = (Laxkit.compareTo(fame, item.getFame()) == 0);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#match(com.laxcus.policy.limit.FaultItem)
	 */
	@Override
	public boolean match(FaultItem that) {
		// 数据库限制单元匹配它的同级同名、表级同名、行级同名
		boolean success = false;
		
		if (that.getClass() == SchemaFaultItem.class) {
			SchemaFaultItem item = (SchemaFaultItem) that;
			success = (Laxkit.compareTo(fame, item.getFame()) == 0);
		} else if (that.getClass() == TableFaultItem.class) {
			TableFaultItem item = (TableFaultItem) that;
			success = (Laxkit.compareTo(fame, item.getFame()) == 0);
		} else if (that.getClass() == RowFaultItem.class) {
			RowFaultItem item = (RowFaultItem) that;
			success = (Laxkit.compareTo(fame, item.getFame()) == 0);
		}
		// 返回结果
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#conflict(com.laxcus.policy.rule.RuleItem)
	 */
	@Override
	public boolean conflict(RuleItem that) {
		boolean refuse = false;

		if (that.getClass() == UserRuleItem.class) {
			refuse = true;
		} else if (that.getClass() == SchemaRuleItem.class) {
			SchemaRuleItem rule = (SchemaRuleItem) that;
			// 匹配即冲突
			refuse = (Laxkit.compareTo(fame, rule.getFame()) == 0);
		} else if (that.getClass() == TableRuleItem.class) {
			TableRuleItem rule = (TableRuleItem) that;
			// 匹配即冲突
			refuse = (Laxkit.compareTo(fame, rule.getFame()) == 0);
		} else if (that.getClass() == RowRuleItem.class) {
			RowRuleItem rule = (RowRuleItem) that;
			// 匹配即冲突
			refuse = (Laxkit.compareTo(fame, rule.getFame()) == 0);
		}
		// 两项匹配即冲突
		return refuse && LimitOperator.conflict(getOperator(), that.getOperator());
	}

}