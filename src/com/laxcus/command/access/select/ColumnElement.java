/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.select;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * <code>SQL SELECT</code>标准显示列成员参数。<br>
 * 
 * @author scott.liang
 * @version 1.1 6/20/2012
 * @since laxcus 1.0
 */
public final class ColumnElement extends ListElement {

	private static final long serialVersionUID = 6254848005274107916L;

	/** 列标记。列参数的基本属性 **/
	private ColumnAttributeTag tag;
	
	/**
	 * 根据传入列显示成员参数，生成一个它的副本
	 * @param that
	 */
	private ColumnElement(ColumnElement that) {
		super(that);
		if (that.tag != null) {
			tag = that.tag.duplicate();
		}
	}

	/**
	 * 生成一个默认的列显示成员
	 */
	protected ColumnElement() {
		super(ListElement.COLUMN);
	}

	/**
	 * 构造一个列显示成员，并且指定它的表和列基础属性
	 * @param space 数据表名
	 * @param tag 列标记
	 */
	public ColumnElement(Space space, ColumnAttributeTag tag) {
		this();
		setSpace(space);
		setTag(tag);
	}

	/**
	 * 构造一个列显示成员，并且指定它的表、列标记、别名
	 * @param space 数据表名
	 * @param tag 列标记
	 * @param alias 别名
	 */
	public ColumnElement(Space space, ColumnAttributeTag tag, String alias) {
		this(space, tag);
		setAlias(alias);
	}
	
	/**
	 * 使用传入的可类化读取器，生成一个列显示成员对象
	 * @param reader 可类化读取器
	 */
	public ColumnElement(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置列标记
	 * @param e ColumnAttributeTag实例
	 */
	public void setTag(ColumnAttributeTag e) {
		tag = e;
	}

	/**
	 * 返回列标记
	 * @return ColumnAttributeTag实例
	 */
	public ColumnAttributeTag getTag() {
		return tag;
	}

	/*
	 * 生成当前列显示成员的副本
	 * @see com.laxcus.sql.method.select.ShowElement#duplicate()
	 */
	@Override
	public ColumnElement duplicate() {
		return new ColumnElement(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.sql.method.select.ShowElement#getColumnId()
	 */
	@Override
	public short getColumnId(){
		return tag.getColumnId();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.sql.method.select.ShowElement#getIdentity()
	 */
	@Override
	public short getIdentity() {
		return getColumnId();
	}

	/*
	 * 列成员返回它的列属性
	 * @see com.laxcus.sql.method.select.ShowElement#getFamily()
	 */
	@Override
	public byte getFamily() {
		return tag.getType();
	}

	/*
	 * 返回列成员名称或者别名
	 * @see com.laxcus.sql.method.select.ShowElement#getName()
	 */
	@Override
	public String getName() {
		String s = getAlias();
		return (s == null ? tag.getName().toString() : s);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.select.ListElement#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(tag);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.select.ListElement#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		tag = new ColumnAttributeTag(reader);
	}

}