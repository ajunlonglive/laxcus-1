/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.select;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * SQL "GROUP BY"适配器。按照<code>SQL "GROUP BY"</code>规则进行分组处理。<br>
 * 即相同键值的数据合并在一起，加上HAVING语句控制输出。<br>
 * 
 * @author scott.liang
 * @version 1.1 9/13/2015
 * @since laxcus 1.0
 */
public final class GroupByAdapter implements Serializable, Cloneable, Classable {

	private static final long serialVersionUID = 1280316118802447470L;

	/** 所属的数据表名称 **/
	private Space space;

	/** 列分组标识集合(允许一个及以上的列编号) **/
	private List<java.lang.Short> array = new ArrayList<java.lang.Short>();

	/** "HAVING"子句实例 */
	private Situation situation;
	
	/**
	 * 构造默认和私有类
	 */
	private GroupByAdapter() {
		super();
	}

	/**
	 * 根据传入的"GROUP BY"分组，生成它的数据副本
	 * @param that GroupByAdapter实例
	 */
	private GroupByAdapter(GroupByAdapter that) {
		this();
		// 设置数据表名称
		setSpace(that.space);
		// 列分组时的标识号集合
		for (short columnId : that.array) {
			addColumnId(columnId);
		}
		if (that.situation != null) {
			situation = that.situation.duplicate();
		}
	}

	/**
	 * 构造一个默认的"GROUP BY"分组实例，同时指定数据表名
	 */
	public GroupByAdapter(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中，生成"GROUP BY"实例
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public GroupByAdapter(ClassReader reader) {
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
	 * 检查某一个列编号是否存在
	 * @param columnId 列编号
	 * @return 返回真或者假
	 */
	public boolean inside(short columnId) {
		return array.contains(columnId);
	}

	/**
	 * 增加一个列编号
	 * @param columnId
	 */
	public void addColumnId(short columnId) {
		if (array.contains(columnId)) {
			throw new IllegalValueException("overlap column id:%d", columnId);
		}
		array.add(columnId);
	}

	/**
	 * 返回列编号集合
	 * @return 列编号数组
	 */
	public short[] getColumnIds() {
		short[] s = new short[array.size()];
		for (int index = 0; index < s.length; index++) {
			s[index] = array.get(index).shortValue();
		}
		return s;
	}

	/**
	 * 设置“HAVING”实例
	 * @param e Situation实例
	 */
	public void setSituation(Situation e) {
		situation = e;
	}

	/**
	 * 返回“HAVING”实例
	 * @return Situation实例
	 */
	public Situation getSituation() {
		return situation;
	}

	/**
	 * 生成当前对象的数据副本
	 * @return GroupByAdapter实例
	 */
	public GroupByAdapter duplicate() {
		return new GroupByAdapter(this);
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
		final int size = writer.size();

		// 数据表名
		writer.writeObject(space);
		// 列编号集合
		writer.writeInt(array.size());
		for (int i = 0; i < array.size(); i++) {
			writer.writeShort(array.get(i));
		}
		// HAVING子句
		writer.writeInstance(situation);

		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();

		// 数据表名
		space = new Space(reader);
		// 列编号集合
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			short columnId = reader.readShort();
			array.add(columnId);
		}
		// HAVING子句比较条件
		situation = reader.readInstance(Situation.class);

		// 返回字节长度
		return reader.getSeek() - seek;
	}

}