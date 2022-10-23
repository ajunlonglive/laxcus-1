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
 * 数据表限制操作单元
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public final class TableLimitItem extends LimitItem {

	private static final long serialVersionUID = -1264215944659368703L;

	/** 数据表名 **/
	private Space space;

	/**
	 * 构造默认和私有的数据表限制操作单元
	 */
	private TableLimitItem() {
		super();
	}

	/**
	 * 生成数据表限制操作单元数据副本
	 * @param that TableLimitItem实例
	 */
	private TableLimitItem(TableLimitItem that) {
		super(that);
		space = that.space.duplicate();
	}

	/**
	 * 构造数据表限制操作单元，指定参数
	 * @param operator 操作符
	 * @param space 数据表名
	 */
	public TableLimitItem(byte operator, Space space) {
		super(LawRank.TABLE, operator);
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析数据表限制操作单元
	 * @param reader 可类化数据读取器
	 */
	public TableLimitItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出数据表限制操作单元
	 * @param reader 标记化读取器
	 */
	public TableLimitItem(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置数据表名。不允许空值
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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ space.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", super.toString(), space);
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
			TableLimitItem item = (TableLimitItem) that;
			ret = Laxkit.compareTo(space, item.space);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#duplicate()
	 */
	@Override
	public TableLimitItem duplicate() {
		return new TableLimitItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = new Space(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#embrace(com.laxcus.policy.limit.LimitItem)
	 */
	@Override
	public boolean embrace(LimitItem that) {
		boolean success = false;
		// 表级只匹配同级，并且表名一致
		if (that.getClass() == TableLimitItem.class) {
			TableLimitItem item = (TableLimitItem) that;
			success = (Laxkit.compareTo(space, item.getSpace()) == 0);
		} else if (that.getClass() == RowLimitItem.class) {
			RowLimitItem item = (RowLimitItem) that;
			success = (Laxkit.compareTo(space, item.getSpace()) == 0);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.LimitItem#match(com.laxcus.policy.limit.FaultItem)
	 */
	@Override
	public boolean match(FaultItem that) {
		// 表级限制单元匹配它同级同名锁定单元，行级锁定单元
		boolean success = false;
		if (that.getClass() == TableFaultItem.class) {
			TableFaultItem item = (TableFaultItem) that;
			success = (Laxkit.compareTo(space, item.getSpace()) == 0);
		} else if (that.getClass() == RowFaultItem.class) {
			RowFaultItem item = (RowFaultItem) that;
			success = (Laxkit.compareTo(space, item.getSpace()) == 0);
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
			refuse = (Laxkit.compareTo(space.getSchema(), rule.getFame()) == 0);
		} else if (that.getClass() == TableRuleItem.class) {
			TableRuleItem rule = (TableRuleItem) that;
			// 匹配即冲突
			refuse = (Laxkit.compareTo(space, rule.getSpace()) == 0);
		} else if (that.getClass() == RowRuleItem.class) {
			RowRuleItem rule = (RowRuleItem) that;
			// 匹配即冲突
			refuse = (Laxkit.compareTo(space, rule.getSpace()) == 0);
		}
		// 两项匹配即是冲突
		return refuse && LimitOperator.conflict(getOperator(), that.getOperator());
	}

}