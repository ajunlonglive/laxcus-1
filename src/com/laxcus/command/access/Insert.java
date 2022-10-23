/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import java.io.*;
import java.util.*;

import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据插入命令。是“INSERT INTO”和“INJECT INTO”语句化实现。<br><br>
 * 
 * “INSERT INTO”是SQL标准，只写入一行记录。<br>
 * “INJECT INTO”是LAXCUS定义，可以写入多行记录。<br>
 * 
 * @author scott.liang
 * @version 1.1 7/18/2015
 * @since laxcus 1.0
 */
public final class Insert extends Manipulate {

	private static final long serialVersionUID = 161284404060739948L;

	/** 行记录集合 */
	private ArrayList<Row> array = new ArrayList<Row>();

	/**
	 * 根据传入的数据插入命令，生成它的数据副本
	 * @param that Insert实例
	 */
	private Insert(Insert that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造一个默认的数据插入命令
	 */
	public Insert() {
		super(SQLTag.INSERT_METHOD);
	}

	/**
	 * 构造数据插入命令，指定它的数据表名
	 * @param space 数据表名
	 */
	public Insert(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析数据插入命令的数据
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public Insert(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造数据插入命令，从指定的字节数组中解析它
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 数据长度
	 */
	public Insert(byte[] b, int off, int len) {
		this(new ClassReader(b, off, len));
	}

	/**
	 * 构造数据插入命令，从磁盘读取它
	 * @param file 磁盘文件
	 * @throws IOException
	 */
	public Insert(File file) throws IOException {
		this(new ClassReader(file));
	}

	/**
	 * 调整记录存储空间尺寸
	 * @param capacity 指定的尺寸
	 */
	public void ensure(int capacity) {
		array.ensureCapacity(capacity);
	}

	/**
	 * 将记录数组空间调整为实际大小
	 */
	public void trim() {
		this.array.trimToSize();
	}

	/**
	 * 增加一条记录
	 * @param row 记录
	 */
	public void add(Row row) {
		this.array.add(row);
	}

	/**
	 * 保存一组记录
	 * @param rows
	 */
	public void addAll(List<Row> rows) {
		array.addAll(rows);
	}

	/**
	 * 输出全部行记录
	 * @return Row列表
	 */
	public List<Row> list() {
		return this.array;
	}

	/**
	 * 取出指定下标的行
	 * @param index 下标
	 * @return 返回指定行，如果没有是空指针
	 */
	public Row get(int index) {
		if (index < 0 || index >= array.size()) {
			return null;
		}
		return array.get(index);
	}

	/**
	 * 清除全部记录
	 */
	public void clear() {
		this.array.clear();
	}

	/**
	 * 判断集合是空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 统计行数
	 * @return 返回行数
	 */
	public int size() {
		return array.size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.RuleCommand#getRules()
	 */
	@Override
	public List<RuleItem> getRules() {
		RuleItem item = new TableRuleItem(RuleOperator.SHARE_WRITE, getSpace());
		ArrayList<RuleItem> a = new ArrayList<RuleItem>();
		a.add(item);
		return a;
	}

	/*
	 * 生成INSERT实例的浅层数据副本
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Insert duplicate() {
		return new Insert(this);
	}

	/**
	 * 将行记录写入可类化存储器
	 * @since 1.1
	 * @see com.laxcus.command.access.Manipulate#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 上级信息
		super.buildSuffix(writer);
		// 写入记录的尺寸
		writer.writeInt(array.size());
		// 保存每一行记录
		for (Row row : array) {
			writer.writeObject(row);
		}
	}

	/**
	 * 从可类化读取器中解析行记录
	 * @since 1.1
	 * @see com.laxcus.command.access.Manipulate#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级信息
		super.resolveSuffix(reader);
		// 记录尺寸
		int size = reader.readInt();
		// 逐一读取记录
		for (int i = 0; i < size; i++) {
			Row row = new Row(reader);
			array.add(row);
		}
	}

	/**
	 * 把INSERT命令全部参数转为字节数组输出。<br>
	 * <b>兼容C接口！！！</b>
	 * @return 返回INSERT字节数组
	 */
	public byte[] buildX() {
		int size = 0;
		// 判断行数
		int rows = array.size();
		if(rows == 0) {
			throw new IllegalValueException("cannot be 0");
		}
		short columns = (short) array.get(0).size();
		for (Row row : array) {
			// 统计尺寸
			size += row.capacity();
			// 判断列成员数目一致
			if (row.size() != columns) {
				throw new IllegalValueException("cannot be match:%d,%d", row.size(), columns);
			}
		}

		// 输出数据到缓存(需要产生校验码)
		size = size - (size % 96) + 96;
		ClassWriter writer = new ClassWriter(size);
		for (Row row : array) {
			row.buildX(writer);
		}

		// 输出数据实体
		byte[] entity = writer.effuse();
		// 重置缓存
		writer.reset();
		// 写入INSERT标识头
		writer.writeObject(getSpace());		// 数据表名
		writer.writeInt(rows);				// 行数目
		writer.writeShort(columns);			// 列数目
		writer.writeInt(entity.length);		// 数据实体尺寸
		// 再次写入数据实体
		writer.write(entity);
		// 输出数据
		return writer.effuse();
	}

}