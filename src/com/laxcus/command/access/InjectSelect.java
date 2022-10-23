/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.select.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.classable.*;

/**
 * 基于SELECT查询基础之上的数据插入。<br>
 * 
 * 此非SQL标准语句，但是很实用。
 * 
 * @author scott.liang
 * @version 1.0 11/27/2020
 * @since laxcus 1.0
 */
public class InjectSelect extends Manipulate {
	
	private static final long serialVersionUID = 6650976218424237961L;

	/** 显示列集合(表属性列、函数列、列计算单元) */
	private ListSheet sheet;
	
	/** 查询语句 **/
	private Select select;

	/**
	 * 构造SELECT查询基础之上的数据插入命令
	 */
	public InjectSelect() {
		super(SQLTag.INJECT_SELECT_METHOD);
	}

	/**
	 * 构造SELECT查询基础之上的数据插入命令，指定表名
	 * @param space 表名
	 */
	public InjectSelect(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析SELECT查询基础之上的数据插入命令
	 * @param reader 可类化数据读取器
	 */
	public InjectSelect(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造基于SELECT查询基础之上的数据插入的数据副本
	 * @param that 基于SELECT查询基础之上的数据插入
	 */
	private InjectSelect(InjectSelect that) {
		super(that);
		sheet = that.sheet.duplicate();
		select = that.select.duplicate();
	}

	/**
	 * 设置显示成员表
	 * @param e ListSheet实例
	 */
	public void setListSheet(ListSheet e) {
		sheet = e;
	}

	/**
	 * 返回显示成员表
	 * @return ListSheet实例
	 */
	public ListSheet getListSheet() {
		return sheet;
	}

	/**
	 * 设置查询语句
	 * @param e SELECT命令
	 */
	public void setSelect(Select e) {
		select = e;
	}

	/**
	 * 返回查询语句
	 * @return SELECT命令
	 */
	public Select getSelect() {
		return select;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.RuleCommand#getRules()
	 */
	@Override
	public List<RuleItem> getRules() {
		ArrayList<RuleItem> array = new ArrayList<RuleItem>();
		array.add(new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE, getSpace()));
		// 查询语句互斥写！
		if (select != null) {
			array.addAll(select.createTableRules(RuleOperator.EXCLUSIVE_WRITE));
		}
		return array;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public InjectSelect duplicate() {
		return new InjectSelect(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Manipulate#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeObject(sheet);
		writer.writeObject(select);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Manipulate#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		sheet = new ListSheet(reader);
		select = new Select(reader);
	}

}