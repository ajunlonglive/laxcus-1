/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.select;

import java.io.Serializable;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * SQL "ORDER BY"适配器。按照<code>SQL "ORDER BY"</code>规则，重新排列每一行数据在集合中的位置。 <br>
 * 
 * @author scott.liang
 * @version 1.1 9/13/2015
 * @since laxcus 1.0
 */
public final class OrderByAdapter implements Serializable, Cloneable, Classable {

	private static final long serialVersionUID = 2174836282062863908L;
	
	/** 升序排列(ascent sort) */
	public final static byte ASC = 1;
	/** 降序排列(descent sort) */
	public final static byte DESC = 2;
	
	/** 指定数据表名 **/
	private Space space;

	/** 列编号 **/
	private short columnId;

	/** 列排序标识，默认是升充 **/
	private byte aligment = OrderByAdapter.ASC;

	/** "ORDER BY"关联实例 */
	private OrderByAdapter next;
	
	/**
	 * 构造默认和私有的实例
	 */
	private OrderByAdapter() {
		super();
	}

	/**
	 * 根据传入的"ORDER BY"实例，生成一个它的副本
	 * @param that OrderByAdapter实例
	 */
	private OrderByAdapter(OrderByAdapter that) {
		this();
		space = that.space.duplicate();
		columnId = that.columnId;
		aligment = that.aligment;
		if (that.next != null) {
			next = that.next.duplicate();
		}
	}

	/**
	 * 使用传入参数生成"ORDER BY"实例，指名它的数据表名、列编号、排序标识号
	 * @param space 数据表名
	 * @param columnId 列编号
	 * @param aligment 排序标识号
	 */
	public OrderByAdapter(Space space, short columnId, byte aligment) {
		super();
		// 对应的数据表名
		setSpace(space);
		// 保存列编号
		setColumnId(columnId);
		// 检查排序标识号
		setAligment(aligment);
	}
	
	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public OrderByAdapter(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置数据表名
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}
	
	/**
	 * 设置列编号
	 * @param who 列编号
	 */
	public void setColumnId(short who) {
		columnId = who;
	}

	/**
	 * 返回列编号
	 * @return 列编号的短整型
	 */
	public short getColumnId() {
		return columnId;
	}

	/**
	 * 返回全部列编号
	 * @return 列编号数组
	 */
	public short[] listColumnIds() {
		ArrayList<java.lang.Short> a = new ArrayList<java.lang.Short>();
		a.add(columnId);
		OrderByAdapter sub = next;
		while (sub != null) {
			a.add(sub.columnId);
			sub = sub.next;
		}

		short[] s = new short[a.size()];
		for (int i = 0; i < s.length; i++) {
			s[i] = a.get(i).shortValue();
		}
		return s;
	}

	/**
	 * 设置排列标识
	 * @param who 排列标识
	 */
	public void setAligment(byte who) {
		switch (who) {
		case OrderByAdapter.ASC:
		case OrderByAdapter.DESC:
			break;
		default:
			throw new IllegalValueException("illegal aligment:%d", who);
		}
		aligment = who;
	}

	/**
	 * 返回排列标识
	 * @return 排列标识
	 */
	public byte getAligment() {
		return aligment ;
	}

	/**
	 * 判断是升序
	 * @return 返回真或者假
	 */
	public boolean isASC() {
		return aligment == OrderByAdapter.ASC;
	}

	/**
	 * 判断是降序
	 * @return 返回真或者假
	 */
	public boolean isDESC() {
		return aligment == OrderByAdapter.DESC;
	}

	/**
	 * 在尾部追加一个关联实例
	 * @param object OrderByAdapter实例
	 */
	public void setLast(OrderByAdapter object) {
		if (next == null) {
			next = object;
		} else {
			next.setLast(object);
		}
	}

	/**
	 * 返回最后的关联
	 * @return OrderByAdapter实例
	 */
	public OrderByAdapter getLast() {
		if (next != null) {
			return next.getLast();
		}
		return this;
	}

	/**
	 * 返回下一个关联
	 * @return OrderByAdapter实例
	 */
	public OrderByAdapter getNext() {
		return next;
	}

	/**
	 * 检查是否有下一级关联
	 * @return 返回真或者假
	 */
	public boolean hasNext() {
		return next != null;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return 返回OrderByAdapter实例
	 */
	public OrderByAdapter duplicate() {
		return new OrderByAdapter(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();

		writer.writeObject(space);
		writer.writeShort(columnId);
		writer.write(aligment);
		writer.writeInstance(next);

		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();

		space = new Space(reader);
		columnId = reader.readShort();
		aligment = reader.read();
		next = reader.readInstance(OrderByAdapter.class);
		
		return reader.getSeek() - seek;
	}
}