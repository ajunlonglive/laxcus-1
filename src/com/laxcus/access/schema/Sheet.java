/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.schema;

import java.io.*;

import java.util.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.util.classable.*;

/**
 * 列属性序列表，列属性按照要求的下标排列和存储。<BR>
 * Table是对<code>"CREATE TABLE"</code>语句的映射，按照列属性的编号进行排列。<BR>
 * Sheet是根据用户的临时定义，如<code>“SELECT”</code>中指定的列，按照要求的列和列下标关系排列。<BR><br>
 * 
 * <code>Sheet</code>主要用于各种随机显示过程中的处理。<br><BR>
 * 
 * @author scott.liang 
 * @version 1.1 03/03/2014
 * @since laxcus 1.0
 */
public final class Sheet implements Serializable, Cloneable {

	private static final long serialVersionUID = -8824362584015056007L;

	/** 列属性分配的下标位置(不是列编号) -> 列属性。列下标从0开始。 **/
	private Map<Integer, ColumnAttribute> attributes = new TreeMap<Integer, ColumnAttribute>();

	/**
	 * 根据传入Sheet对象，生成它的浅层数据副本
	 * @param that Sheet实例
	 */
	private Sheet(Sheet that) {
		this();
		attributes.putAll(that.attributes);
	}

	/**
	 * 生成一个默认的列属性对象集合
	 */
	public Sheet() {
		super();
	}

	/**
	 * 保存一列属性，下标从0开始
	 * @param index 指定下标
	 * @param attribute 列属性
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(int index, ColumnAttribute attribute) {
		return attributes.put(index, attribute) == null;
	}

	/**
	 * 根据下标，返回对应的列属性，如果没有返回NULL
	 * @param index 指定下标
	 * @return 列属性实例
	 */
	public ColumnAttribute get(int index) {
		return attributes.get(index);
	}

	/**
	 * 根据列编号，查找对应的属性
	 * @param columnId 列编号
	 * @return 列属性实例
	 */
	public ColumnAttribute find(short columnId) {
		for (ColumnAttribute attribute : attributes.values()) {
			if (attribute.getColumnId() == columnId) {
				return attribute;
			}
		}
		return null;
	}

	/**
	 * 返回全部列属性
	 * @return 列属性列表
	 */
	public Collection<ColumnAttribute> values() {
		return attributes.values();
	}

	/**
	 * 清除全部列属性
	 */
	public void clear() {
		attributes.clear();
	}

	/**
	 * 检查属性集合是否空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return attributes.isEmpty();
	}

	/**
	 * 返回列属性成员数
	 * @return 成员数目
	 */
	public int size() {
		return attributes.size();
	}

	/**
	 * 返回浅层数据副本
	 * @return Sheet实例
	 */
	public Sheet duplicate() {
		return new Sheet(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 将数据写入可类化存储器
	 * @param writer 可类化存储器
	 * @return 返回写入的字节长度
	 */
	public int build(ClassWriter writer) {
		// 排序
		ArrayList<Integer> array = new ArrayList<Integer>(attributes.keySet());
		Collections.sort(array);

		int size = writer.size();
		// 成员数目
		writer.writeInt(attributes.size());
		// 写入每一个成员
		for (int index : array) {
			ColumnAttribute attribute = attributes.get(index);
			writer.writeObject(attribute.getTag());
		}
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析属性表参数
	 * @param table 表名
	 * @param reader 可类化读取器
	 * @return 返回解析的字节长度
	 */
	public int resolve(Table table, ClassReader reader) {
		int scale = reader.getSeek();
		// 成员数
		int size = reader.readInt();
		// 列下标编号，从0开始
		int index = 0;
		// 解析每一个成员，找到它在表中对应的列属性，并且保存
		for (int i = 0; i < size; i++) {
			ColumnAttributeTag seal = new ColumnAttributeTag(reader);
			// 从表中查找列属性
			ColumnAttribute attribute = table.find(seal.getColumnId());
			if (attribute == null || attribute.getName().compareTo(seal.getName()) != 0) {
				throw new ColumnAttributeException("cannot find %s",
						seal.getName());
			}
			// 按照下标保存记录
			add(index, attribute);
			index++;
		}
		// 返回读取的字节长度
		return reader.getSeek() - scale;
	}

	/**
	 * 把属性表转为字节数组输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 从字节数组中解析属性表参数
	 * @param table 数据表
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 解析的字节长度
	 */
	public int resolve(Table table, byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(table, reader);
	}

	//	/**
	//	 * 生成数据流
	//	 * @return
	//	 */
	//	public byte[] build1() {
	//		ArrayList<Integer> a = new ArrayList<Integer>(attributes.keySet());
	//		Collections.sort(a);
	//		ByteArrayOutputStream buff = new ByteArrayOutputStream();
	//		for (int offset : a) {
	//			ColumnAttribute attribute = attributes.get(offset);
	//			short columnId = attribute.getColumnId();
	//			String name = attribute.getName();
	//			byte[] b = Laxkit.toBytes(columnId);
	//			buff.write(b, 0, b.length);
	//			b = name.getBytes();
	//			byte sz = (byte) b.length;
	//			buff.write(sz);
	//			buff.write(b, 0, b.length);
	//		}
	//
	//		byte[] data = buff.toByteArray();
	//
	//		ByteArrayOutputStream all = new ByteArrayOutputStream();
	//		short count = (short) attributes.size();
	//		byte[] b = Laxkit.toBytes(count);
	//		all.write(b, 0, b.length);
	//		b = Laxkit.toBytes(data.length);
	//		all.write(b, 0, b.length);
	//		all.write(data, 0, data.length);
	//
	//		return all.toByteArray();
	//	}
	//
	//	/**
	//	 * 解析数据流，存储到当前序列集合中
	//	 * 
	//	 * @param table
	//	 * @param b
	//	 * @param off
	//	 * @return
	//	 */
	//	public int resolve1(Table table, byte[] b, int off, int length) {
	//		int seek = off;
	//
	//		short count = Laxkit.toShort(b, seek, 2);
	//		seek += 2;
	//		int size = Laxkit.toInteger(b, seek, 4);
	//		seek += 4;
	//
	//		int index = 0;
	//		for (short i = 0; i < count; i++) {
	//			short columnId = Laxkit.toShort(b, seek, 2);
	//			seek += 2;
	//			byte len = b[seek];
	//			seek += 1;
	//			byte[] byte_name = new byte[len];
	//			System.arraycopy(b, seek, byte_name, 0, byte_name.length);
	//			seek += byte_name.length;
	//			String name = new String(byte_name);
	//			// 从表中查找列属性
	//			ColumnAttribute attribute = table.find(columnId);
	//			if (attribute == null || !attribute.getName().equalsIgnoreCase(name)) {
	//				throw new ColumnAttributeException("cannot find %s", name);
	//			}
	//			// 按照下标保存记录
	//			add(index, attribute);
	//			index++;
	//		}
	//		if(seek - 6 != size) {
	//			return -1;
	//		}
	//		return seek - off;
	//	}

}