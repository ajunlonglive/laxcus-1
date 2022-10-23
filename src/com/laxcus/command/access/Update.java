/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.schema.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.classable.*;

/**
 * <code>SQL UPDATE</code>语句实现
 * 
 * @author scott.liang
 * @version 1.1 7/18/2015
 * @since laxcus 1.0
 */
public final class Update extends Query {

	private static final long serialVersionUID = -5615400413964109461L;

	/** 被更新的列 **/
	private Map<java.lang.Short, Column> array = new TreeMap<java.lang.Short, Column>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Query#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 列成员数目
		writer.writeInt(array.size());
		// 列集合
		Iterator<Map.Entry<java.lang.Short, Column>> iterator = array.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<java.lang.Short, Column> entry = iterator.next();
			short columnId = entry.getKey();
			Column column = entry.getValue();
			// 保存列编号
			writer.writeShort(columnId);
			// 保存列实例
			writer.writeObject(column);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Query#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 列成员数目
		int size = reader.readInt();
		// 列集合
		for (int i = 0; i < size; i++) {
			short columnId = reader.readShort();
			Column column = ColumnCreator.resolve(reader);
			// 设置列编号
			column.setId(columnId);
			// 保存一列
			add(column);
		}
	}

	/**
	 * 根据传入的UPDATE实例，生成它的副本
	 * @param that Update实例
	 */
	private Update(Update that) {
		super(that);
		array.putAll(that.array);
	}

	/**
	 * 生成一个默认的UPDATE对象实例
	 */
	public Update() {
		super(SQLTag.UPDATE_METHOD);
	}

	/**
	 * 生成UPDATE对象实例，并且设置它更新的表
	 * @param space 数据表名
	 */
	public Update(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析"SQL UPDATE"语句参数
	 * @param reader 可类数据读取器
	 * @since 1.1
	 */
	public Update(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 增加一列更新参数
	 * @param e Column实例
	 */
	public void add(Column e) {
		if (e == null) {
			throw new NullPointerException();
		} else if (e.getId() < 1) {
			throw new ColumnException("illegal id %d", e.getId());
		}
		array.put(e.getId(), e);
	}

	/**
	 * 输出更新列
	 * @return Column列表
	 */
	public List<Column> values() {
		return new ArrayList<Column>(array.values());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Update duplicate() { 
		return new Update(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.RuleCommand#getRules()
	 */
	@Override
	public List<RuleItem> getRules() {
		return createTableRules(RuleOperator.EXCLUSIVE_WRITE);
	}

}