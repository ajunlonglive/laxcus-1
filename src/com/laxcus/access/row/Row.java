/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.row;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 数据行。<br><br>
 * 
 * 行是“列”的数据集合，对应NSM格式，由任意个“列”组成。<br>
 * LAXCUS任意层面的数据输入和输出（不包括JNI DB内部的数据处理），都“行”为单位进行。<br><br>
 * 
 * 行的特点：<br>
 * 1. 存储在“行”中的“列”，呈一种松散排列的结构，以进入的先后顺序为准。如果需要将列排序，请调用aligment()方法，它将按照列编号进行排列。<br>
 * 2. 本处的“列”，与属性表（Table）对列的定义不一定匹配。即一行中的“列”可以与属性表（ Table）中的列属性匹配，也可以是任意多种无关联的“列”的组合。<br>
 * 3. 检查“行”中“中”与要求是否一致，请调用check(Sheet)方法，它将依据Sheet中定义的列属性，与本行中的列属性逐一比较。<br>
 * 
 * @author scott.liang
 * @version 1.1 9/17/2015
 * @since laxcus 1.0
 */
public final class Row implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = 6817332802456538927L;

	/** 行标记 **/
	private RowTag tag = new RowTag();

	/** 列成员数组(不做排序，以存储的先后顺序为准) **/
	private ArrayList<Column> array = new ArrayList<Column>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 头标识
		writer.writeObject(tag);
		// 列成员数目
		writer.writeInt(array.size());
		// 写入列成员
		for (Column column : array) {
			writer.writeObject(column);
		}
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 头标识
		tag.resolve(reader);
		// 解析列成员
		int size = reader.readInt();
		// 列成员
		for (int i = 0; i < size; i++) {
			Column column = ColumnCreator.resolve(reader);
			array.add(column);
		}
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的行，生成一个它的副本
	 * @param that 行实例
	 */
	private Row(Row that) {
		super();
		tag.set(that.tag);
		array.addAll(that.array);
		trim();
	}

	/**
	 * 构造默认的行记录
	 */
	public Row() {
		super();
	}

	/**
	 * 构造行记录，指定行标记
	 * @param tag 行标记
	 */
	public Row(RowTag tag) {
		this();
		setTag(tag);
		// 分配列成员空间
		if (tag.columns > 0) {
			array.ensureCapacity(tag.columns);
		}
	}

	/**
	 * 建立一行记录，并且指定列成员存储空间尺寸
	 * @param size 列成员数目
	 */
	public Row(int size) {
		this();
		if (size > 0) {
			array.ensureCapacity(size);
		}
	}

	/**
	 * 从可类化读取器中解析“行”参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public Row(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置行标记
	 * 
	 * @param e 行标记
	 */
	public void setTag(RowTag e) {
		tag.set(e);
	}

	/**
	 * 返回行标记
	 * 
	 * @return RowTag实例
	 */
	public RowTag getTag() {
		return tag;
	}

	/**
	 * 将数组空间调整为实际大小(删除剩余空间)
	 */
	public void trim() {
		array.trimToSize();
	}

	/**
	 * 判断当前是否空状态
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 返回列成员数
	 * @return 列成员数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 返回全部列集合
	 * @return 列集合
	 */
	public List<Column> list() {
		return array;
	}

	/**
	 * 清除全部列
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 计算一行数据的字节长度（包括头标记11字节和全部列的字节长度)
	 * 
	 * @return 返回字节长度
	 */
	public int capacity() {
		int size = 0;
		for (Column column : array) {
			size += column.capacity();
		}
		return tag.volume() + size;
	}

	/**
	 * 返回对应列排列位置的列编号集合，
	 * @return 列编号列表
	 */
	public List<java.lang.Short> keys() {
		List<java.lang.Short> a = new ArrayList<java.lang.Short>(array.size());
		for(Column column : array) {
			a.add(column.getId());
		}
		return a;
	}

	/**
	 * 根据列编号查找对应的列
	 * @param columnId 列编号
	 * @return 列实例
	 */
	public Column find(short columnId) {
		// 列编号是从1开始，首先按照下标找
		if (columnId > 0 && columnId <= array.size()) {
			Column column = array.get(columnId - 1);
			if (column.getId() == columnId) {
				return column;
			}
		}
		// 按照存储顺序检索
		for (Column column : array) {
			if (column.getId() == columnId) {
				return column;
			}
		}
		return null;
	}

	/**
	 * 根据下标要求返回指定的列
	 * @param index 列数组下标
	 * @return 列实例
	 */
	public Column get(int index) {
		if (index < 0 || index >= array.size()) {
			return null;
		}
		return array.get(index);
	}

	/**
	 * 保存一列数据(不需要排序)
	 * @param e 列实例
	 * @return 成功返回真，否则假
	 * @throws NullPointerException
	 */
	public boolean add(Column e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批列
	 * @param a 列列表
	 * @return 返回新增列数目
	 */
	public int add(List<Column> a) {
		int size = array.size();
		for (Column column : a) {
			add(column);
		}
		return array.size() - size;
	}

	/**
	 * 删除指定列编号的列
	 * 
	 * @param columnId 列编号
	 * @return 成功返回真，否则假
	 */
	public boolean remove(short columnId) {
		int count = 0;
		for (int index = 0; index < array.size(); index++) {
			Column column = array.get(index);
			if (column.getId() == columnId) {
				array.remove(index);
				index = -1;
				count++;
			}
		}
		return count > 0;
	}

	/**
	 * 按照列编号进行排序
	 */
	public void aligment() {
		Collections.sort(array);
	}

	/**
	 * 根据Sheet表，检查列与表中的属性定义是否一致。如果出错，弹出ColumnException
	 * 
	 * @param sheet 列属性序列表
	 * @throws ColumnException
	 */
	public void check(Sheet sheet) {
		int size = array.size();
		if (size != sheet.size()) {
			throw new ColumnException("size not match %d - %d!", size, sheet.size());
		}
		for (int index = 0; index < size; index++) {
			Column column = array.get(index);
			ColumnAttribute attribute = sheet.get(index);
			if (attribute.getColumnId() != column.getId()) {
				throw new ColumnException("not match identity, %d,%d", attribute.getColumnId(), column.getId());
			} else if (attribute.getType() != column.getType()) {
				throw new ColumnException("not match family, %d,%d", attribute.getType(), column.getType());
			}
		}
	}

	/**
	 * 根据列编号和列类型定义，判断列存在
	 * @param columnId 列编号
	 * @param family 列类型定义
	 * @return 返回真或者假
	 */
	public boolean hasColumn(short columnId, byte family) {
		Column column = find(columnId);
		boolean success = (column != null);
		if (success) {
			success = (column.getType() == family);
		}
		return success;
	}

	/**
	 * 根据列编号置换一列，要求旧列必须存在
	 * @param column 被置换的列
	 * @return 置换成功返回真，否则假
	 */
	public boolean replace(Column column) {
		short columnId = column.getId();
		// 删除一列
		boolean success = remove(columnId);
		// 增加一列
		if (success) {
			success = add(column);
		}
		return success;
	}

	/**
	 * 生成数据副本
	 * @return Row实例
	 */
	public Row duplicate() {
		return new Row(this);
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
	 * 指定数据空间长度和产生校验码参数，生成一行数据的数据流
	 * 
	 * @param capacity 指定内存空间
	 * @param buildCRC32 生成CRC32校验码
	 * @param writer 可类化数据写入器
	 * 
	 * @return 返回写入的字节数组长度
	 */
	public int buildX(int capacity, boolean buildCRC32, ClassWriter writer) {
		// 如果未定义数据流空间长度时
		if(capacity < 1) {
			capacity = capacity();
			int left = capacity % 32;
			if (left > 0) capacity = capacity - left + 32;
		}

		ClassWriter buff = new ClassWriter(capacity);
		// 全部列转成数据流
		for (Column column : array) {
			column.build(buff);
		}
		// 取出数据流
		byte[] data = buff.effuse();

		// 状态位(有效)
		tag.status = RowTag.VALID;
		// 列成员数
		tag.columns = (short) array.size();
		// 一行记录的长度(包括头信息)
		tag.length = tag.volume() + data.length;

		// 如果要求生成CRC32校验码
		if (buildCRC32) {
			CRC32 sum = new CRC32();
			byte[] b = Laxkit.toBytes(tag.length);
			sum.update(b, 0, b.length);
			b = Laxkit.toBytes(tag.columns);
			sum.update(b, 0, b.length);
			sum.update(data, 0, data.length);
			tag.checksum = (int) sum.getValue();
		} else {
			tag.checksum = 0; // 校验码默认0，不设置
		}

		// 生成头标记
		writer.write(tag.status);
		writer.writeInt(tag.checksum);
		writer.writeInt(tag.length);
		writer.writeShort(tag.columns);
		// 列数据流写入缓存
		writer.write(data, 0, data.length);

		return tag.length;
	}

	/**
	 * 将"行“记录信息输出到可类化存储器。
	 * @param writer 可类化存储器
	 * @return 写入的字节长度
	 */
	public int buildX(ClassWriter writer) {
		return buildX(0, true, writer);
	}

	/**
	 * 生成"行"数据流，返回字节数据
	 * @return 字节数组
	 */
	public byte[] buildX() {
		int maxlen = capacity();
		int left = maxlen % 32;
		if(left > 0) maxlen = maxlen - left + 32;

		ClassWriter writer = new ClassWriter(maxlen);
		buildX(maxlen, true, writer);
		return writer.effuse();
	}

	/**
	 * 依据表属性，计算一行记录的全部散列值，返回SHA256散列码
	 * @param sheet 数据表，必须一致
	 * @return 返回SHA256散列码，或者空值
	 */
	public SHA256Hash hash(Sheet sheet) {
		int size = sheet.size();
		if (size != array.size()) {
			throw new IllegalValueException("%d != %d", size, array.size());
		}
		
		ClassWriter buff = new ClassWriter();
		for (int index = 0; index < size; index++) {
			ColumnAttribute attribute = sheet.get(index);
			Column column = array.get(index);
			// 如果属性不一致，弹出错误
			if (attribute.getType() != column.getType()) {
				throw new IllegalValueException("illegal type! index %d, %s != %s", 
						index, ColumnType.translate(attribute.getType()), ColumnType.translate(column.getType()));
			}
			
			// 保存计算的MD5散列码
			SHA256Hash hash = column.hash(attribute);
			buff.write(hash.get());
		}
		// 生成字节流
		byte[] b = buff.effuse();
		return Laxkit.doSHA256Hash(b);
	}

}