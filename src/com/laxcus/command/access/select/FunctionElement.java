/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.select;

import com.laxcus.access.function.table.*;
import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * <code>SQL SELECT</code>显示列的函数成员。<br>
 * 
 * @author scott.liang
 * @version 1.1 6/23/2012
 * @since laxcus 1.0
 */
public final class FunctionElement extends ListElement {

	private static final long serialVersionUID = 6761748771331243898L;

	/** 函数成员的编号，在对应表的编号范围之外 **/
	private short functionId;

	/** 列函数 */
	private ColumnFunction function;

	/**
	 * 根据传入参数构造当前对象的副本
	 * @param that FunctionElement实例
	 */
	private FunctionElement(FunctionElement that) {
		super(that);
		// 函数成员编号
		functionId = that.functionId;
		// 函数
		if (that.function != null) {
			function = (ColumnFunction) that.function.duplicate();
		}
	}

	/**
	 * 构造一个默认的显示函数列
	 */
	public FunctionElement() {
		super(ListElement.FUNCTION);
	}

	/**
	 * 根据传入参数构造显示函数列
	 * @param space 表名
	 * @param functionId 函数编号
	 * @param function 函数
	 * @param alias 别名
	 */
	public FunctionElement(Space space, short functionId, ColumnFunction function, String alias) {
		this();
		setSpace(space);
		setFunctionId(functionId);
		setFunction(function);
		setAlias(alias);
	}

	/**
	 * 使用传入的可类化读取器，生成一个显示函数成员对象
	 * @param reader 可类化读取器
	 */
	public FunctionElement(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置列函数在显示列集合的编号(临时的)
	 * @param id 函数编号
	 */
	public void setFunctionId(short id) {
		functionId = id;
	}

	/**
	 * 设置列函数
	 * @param e ColumnFunction子类实例
	 */
	public void setFunction(ColumnFunction e) {
		function = e;
	}

	/**
	 * 返回列函数
	 * @return ColumnFunction子类实例
	 */
	public ColumnFunction getFunction() {
		return function;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.select.ListElement#duplicate()
	 */
	@Override
	public FunctionElement duplicate() {
		return new FunctionElement(this);
	}

	/*
	 * 返回函数操作的列编号
	 * @see com.laxcus.sql.method.select.ShowElement#getColumnId()
	 */
	@Override
	public short getColumnId() {
		// 如果是聚合函数或者列函数，返回它对应的列编号
		if (function != null) {
			if (function instanceof ColumnAggregateFunction) {
				return ((ColumnAggregateFunction) function).getColumnId();
			} else if (function instanceof ColumnFunction) {
				return ((ColumnFunction) function).getColumnId();
			}
		}
		return 0;
	}

	/*
	 * 返回函数编号(临时)
	 * @see com.laxcus.sql.method.select.ShowElement#getIdentity()
	 */
	@Override
	public short getIdentity() {
		return functionId;
	}

	/*
	 * 返回函数计算结果列的属性
	 * @see com.laxcus.sql.method.select.ShowElement#getFamily()
	 */
	@Override
	public byte getFamily() {
		return function.getResultFamily();
	}

	/*
	 * 返回函数原语或者函数别名
	 * @see com.laxcus.sql.method.select.ShowElement#getName()
	 */
	@Override
	public String getName() {
		// 如果没有别名，显示它的函数原语
		String s = getAlias();
		return (s == null ? function.getPrimitive() : s);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.select.ListElement#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeShort(functionId);
		writer.writeDefault(function);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.select.ListElement#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 函数在集合中的编号
		functionId = reader.readShort();
		// 函数
		function = (ColumnFunction) reader.readDefault();
	}

}