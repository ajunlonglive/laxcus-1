/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.function.table;

import com.laxcus.util.classable.*;

/**
 * 列聚合函数，对一列或者几列进行合并/筛选处理。<br>
 * SQL中的聚合函数包括: Sum、Count、Avg等。<br>
 * 
 * @author scott.liang
 * @version 1.1 12/9/2009
 * @since laxcus 1.0
 */
public abstract class ColumnAggregateFunction extends ColumnFunction {

	private static final long serialVersionUID = 5845062404672501921L;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
	}

	/**
	 * 构造一个默认聚合函数
	 */
	protected ColumnAggregateFunction() {
		super();
		// 聚合函数不允许产生默认列
		setSupportDefault(false);
	}

	/**
	 * 构造一个聚合函数，并且指定返回类型
	 * @param resultFamily 返回类型
	 */
	protected ColumnAggregateFunction(byte resultFamily) {
		this();
		setResultFamily(resultFamily);
	}

	/**
	 * 根据传入参数生成它的副本
	 * @param that ColumnAggregateFunction实例
	 */
	protected ColumnAggregateFunction(ColumnAggregateFunction that) {
		super(that);
	}

}