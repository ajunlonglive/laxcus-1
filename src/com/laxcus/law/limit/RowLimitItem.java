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
 * 行记录限制操作单元
 * 
 * @author scott.liang
 * @version 1.0 4/10/2018
 * @since laxcus 1.0
 */
public final class RowLimitItem extends LimitItem {

	private static final long serialVersionUID = -1264215944659368703L;

	/** 行记录名 **/
	private RowFeature feature;

	/**
	 * 构造默认和私有的行记录限制操作单元
	 */
	private RowLimitItem() {
		super();
	}

	/**
	 * 生成行记录限制操作单元数据副本
	 * @param that RowLimitItem实例
	 */
	private RowLimitItem(RowLimitItem that) {
		super(that);
		feature = that.feature.duplicate();
	}

	/**
	 * 构造行记录限制操作单元，指定参数
	 * @param operator 操作符
	 * @param feature 行记录名
	 */
	public RowLimitItem(byte operator, RowFeature feature) {
		super(LawRank.ROW, operator);
		setFeature(feature);
	}

	/**
	 * 从可类化数据读取器中解析行记录限制操作单元
	 * @param reader 可类化数据读取器
	 */
	public RowLimitItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出行记录限制操作单元
	 * @param reader 标记化读取器
	 */
	public RowLimitItem(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置行记录名。不允许空值
	 * @param e 行记录名
	 */
	public void setFeature(RowFeature e) {
		Laxkit.nullabled(e);

		feature = e;
	}

	/**
	 * 返回行记录名
	 * @return 行记录名
	 */
	public RowFeature getFeature() {
		return feature;
	}

	/**
	 * 返回数据库名
	 * @return 数据库名
	 */
	public Fame getFame() {
		return feature.getFame();
	}
	
	/**
	 * 返回数据表名
	 * @return 数据表名
	 */
	public Space getSpace() {
		return feature.getSpace();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ feature.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", super.toString(), feature);
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
			RowLimitItem item = (RowLimitItem) that;
			ret = Laxkit.compareTo(feature, item.feature);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#duplicate()
	 */
	@Override
	public RowLimitItem duplicate() {
		return new RowLimitItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(feature);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		feature = new RowFeature(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#embrace(com.laxcus.policy.limit.LimitItem)
	 */
	@Override
	public boolean embrace(LimitItem that) {
		boolean success = false;
		// 表级只匹配同级，并且表名一致
		if (that.getClass() == RowLimitItem.class) {
			RowLimitItem item = (RowLimitItem) that;
			success = (Laxkit.compareTo(feature, item.getFeature()) == 0);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#match(com.laxcus.policy.limit.FaultItem)
	 */
	@Override
	public boolean match(FaultItem that) {
		// 表级限制单元匹配它同级同名锁定单元
		boolean success = false;
		if (that.getClass() == RowFaultItem.class) {
			RowFaultItem item = (RowFaultItem) that;
			success = (Laxkit.compareTo(feature, item.getFeature()) == 0);
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
		boolean refuse = false;

		if (that.getClass() == UserRuleItem.class) {
			refuse = true;
		} else if (that.getClass() == SchemaRuleItem.class) {
			SchemaRuleItem rule = (SchemaRuleItem) that;
			// 匹配即冲突
			refuse = (Laxkit.compareTo(feature.getFame(), rule.getFame()) == 0);
		} else if (that.getClass() == TableRuleItem.class) {
			TableRuleItem rule = (TableRuleItem) that;
			// 匹配即冲突
			refuse = (Laxkit.compareTo(feature.getSpace(), rule.getSpace()) == 0);
		} else if (that.getClass() == RowRuleItem.class) {
			RowRuleItem rule = (RowRuleItem) that;
			// 匹配即冲突
			refuse = (Laxkit.compareTo(feature, rule.getFeature()) == 0);
		}
		// 两项匹配即是冲突
		return refuse && LimitOperator.conflict(getOperator(), that.getOperator());
	}

}