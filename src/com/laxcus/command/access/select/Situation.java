/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.select;

import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.function.table.*;
import com.laxcus.access.row.*;
import com.laxcus.access.type.*;
import com.laxcus.command.access.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * <code>SQL GROUP BY "HAVING"</code>子句的类实现<br>
 * 
 * @author scott.liang
 * @version 1.1 3/22/2012
 * @since laxcus 1.0
 */
public final class Situation extends Gradation {

	private static final long serialVersionUID = -5265205670029389626L;

	/** SQL聚合函数(必须是聚合函数) **/
	private ColumnAggregateFunction function;

	/** 被比较的值 */
	private Column value;

	/** 同级比较 单元 */
	private List<Situation> partners = new ArrayList<Situation>(3);

	/** 关联比较单元。与当前对象链接起来，形成单向链表关系 **/
	private Situation next;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Gradation#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 1. SQL函数
		writer.writeDefault(function);
		// 2. 列值
		writer.writeDefault(value);
		// 3.同级成员
		writer.writeInt(partners.size());
		for (int i = 0; i < partners.size(); i++) {
			writer.writeObject(partners.get(i));
		}
		// 4. 子级成员
		writer.writeInstance(next);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Gradation#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 1. SQL函数
		function = (ColumnAggregateFunction) reader.readDefault();
		// 2. 列值
		value = (Column) reader.readDefault();
		// 3. 同级成员
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Situation e = new Situation(reader);
			partners.add(e);
		}
		// 4. 子级成员
		next = reader.readInstance(Situation.class);
	}

	/**
	 * 构造默认的HAVING子句实例
	 */
	public Situation() {
		super();
	}

	/**
	 * 从可类化读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public Situation(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 使用传入参数生成它的副本
	 * @param that Situation实例
	 */
	private Situation(Situation that) {
		super(that);
		// 函数
		if (that.function != null) {
			function = (ColumnAggregateFunction) that.function.clone();
		}
		// 比较列
		if (that.value != null) {
			value = that.value.duplicate();
		}
		// 同级成员
		for (Situation partner : that.partners) {
			partners.add((Situation) partner.clone());
		}
		// 子级
		if (that.next != null) {
			next = (Situation) that.next.clone();
		}
	}

	/**
	 * 生成HAVING子句实例，同时指定聚合函数、比较符号、被比较值
	 * @param function 聚合函数
	 * @param compare 比较关系符
	 * @param value 被比较值
	 */
	public Situation(ColumnAggregateFunction function, byte compare, Column value) {
		this();
		setFunction(function);
		setCompare(compare);
		setValue(value);
	}

	/**
	 * 设置聚合函数
	 * @param e ColumnAggregateFunction子类实例
	 */
	public void setFunction(ColumnAggregateFunction e) {
		function = e;
	}

	/**
	 * 返回聚合函数
	 * @return ColumnAggregateFunction子类实例
	 */
	public ColumnAggregateFunction getFunction() {
		return function;
	}

	/**
	 * 设置被比较值
	 * @param e Column实例
	 */
	public void setValue(Column e) {
		value = e;
	}

	/**
	 * 返回比较值
	 * @return Column实例
	 */
	public Column getValue() {
		return value;
	}

	/**
	 * <code>GROUP BY "HAVING"</code>筛选操作。<br>
	 * 通过传入的记录集合，进行函数计算与预定义结果比较，判断条件是否成立。<br>
	 * @param rows  行列表
	 * @return 条件成立返回“真”，否则“假”。
	 */
	public boolean sifting(List<Row> rows) {
		// 使用聚合函数，生成一个结果值
		Column result = function.makeup(rows);
		boolean ret = false;
		// 计算结果
		switch (super.getCompare()) {
		case CompareOperator.EQUAL:
			if (value.isRaw()) {
				// 如果是二进制数组类型，考虑包装，忽略索引比较
				ret = (((Raw) value).compare((Raw) result,
						function.getPacking(), true) == 0);
			} else if (value.isWord()) {
				// 如果是字符类型，考虑包装和大小写敏感，忽略索引比较
				//注: 如果有Packing，被比较值(value)也必须经过了压缩和加密的处理				
				ret = (((Word) value).compare((Word) result,
						function.getPacking(), function.isSentient(), true) == 0);
			} else {
				ret = (result.compare(value) == 0);
			}
			break;
		case CompareOperator.NOT_EQUAL:
			if (value.isRaw()) {
				ret = (((Raw) value).compare((Raw) result,
						function.getPacking(), true) != 0);
			} else if(value.isWord()) {
				ret = (((Word) value).compare((Word) result,
						function.getPacking(), function.isSentient(), true) == 0);
			} else {
				ret = (result.compare(value) != 0);
			}
			break;
		case CompareOperator.LIKE:
			// 模糊匹配，只限字符类型(函数计算结果里是否包括指定的关键字)
			if (result.isWord() && value.isRWord()) {
				ret = ((Word) result).likeIn((RWord) value, function.getPacking(), 
						function.isSentient(), true);
			}
			break;
		case CompareOperator.LESS:
			ret = (result.compare(value) < 0);
			break;
		case CompareOperator.LESS_EQUAL:
			ret = (result.compare(value) <= 0);
			break;
		case CompareOperator.GREATER:
			ret = (result.compare(value) > 0);
			break;
		case CompareOperator.GREATER_EQUAL:
			ret = (result.compare(value) >= 0);
			break;
		}
		return ret;
	}

	/**
	 * 设置同级比较单元
	 * @param e - HAVING比较单元
	 */
	public void addPartner(Situation e) {
		Laxkit.nullabled(e);

		partners.add(e);
	}

	/**
	 * 返回同级比较单元集合
	 * @return - HAVING集合
	 */
	public List<Situation> getPartners() {
		return new ArrayList<Situation>(partners);
	}

	/**
	 * 关联一个检索条件。把它绑定到最后，形成单向链表的格式
	 * @param that Situation实例
	 */
	public void attach(Situation that) {
		if (this.next == null) {
			this.next = that;
		} else {
			this.next.attach(that);
		}
	}

	/**
	 * 返回下一级比较单元，如果没有是空指针
	 * @return Situation实例，或者空指针
	 */
	public Situation next() {
		return this.next;
	}

	/**
	 * 返回单向链表中的最后一个有效检索，如果没有子级，就是对象自己。
	 * @return Situation实例
	 */
	public Situation lastEnabled() {
		if(next != null) {
			return next.lastEnabled();
		}
		return this;
	}

	/**
	 * 根据当前HAVING比较条件，生成一个它的副本
	 * @see com.laxcus.command.access.Gradation#duplicate()
	 */
	@Override
	public Situation duplicate() {
		return new Situation(this);
	}

}