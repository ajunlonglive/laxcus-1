/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.function.table;

import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 列函数。与列关联的函数
 * 
 * @author scott.liang
 * @version 1.0 6/13/2009
 * @since laxcus 1.0
 */
public abstract class ColumnFunction extends TableFunction {

	private static final long serialVersionUID = 5009287300563849892L;

	/** 列编号，与ColumnAttribute中的列编号一致。默认是0，未定义 **/
	private short columnId;

	/** 计算结果的数值类型，对应ColumnAttribute中的类型定义 **/
	private byte resultFamily;

	/** 数据封装（加密和压缩，针对可变长数据类型) **/
	private Packing packing;

	/** 大小写是否敏感，默认是TRUE(敏感)，针对字符类型数据 **/
	private boolean sentient;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.TableFunction#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 列编号
		writer.writeShort(columnId);
		// 函数计算结果类型
		writer.write(resultFamily);
		// 数据封装
		writer.writeInstance(packing);
		// 大小写敏感
		writer.writeBoolean(sentient);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.TableFunction#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 列编号
		columnId = reader.readShort();
		// 函数计算结果类型
		resultFamily = reader.read();
		// 数据封装
		packing = reader.readInstance(Packing.class);
		// 大小写
		sentient = reader.readBoolean();
	}

	/**
	 * 构造列函数
	 */
	protected ColumnFunction() {
		super();
		// 未定义
		columnId = 0;
		// 计算结果类型不定义
		resultFamily = 0;
		// 默认大小写敏感
		setSentient(true);
	}

	/**
	 * 构造列函数，并且指定返回类型
	 * @param family 结果类型
	 */
	protected ColumnFunction(byte family) {
		this();
		setResultFamily(family);
	}

	/**
	 * 生成传入列函数的数据副本
	 * @param that ColumnFunction实例
	 */
	protected ColumnFunction(ColumnFunction that) {
		super(that);
		columnId = that.columnId;
		resultFamily = that.resultFamily;
		sentient = that.sentient;
		setPacking(that.packing);
	}

	/**
	 * 设置列编号
	 * @param i 列编号
	 */
	public void setColumnId(short i) {
		columnId = i;
	}

	/**
	 * 返回列编号
	 * @return 列编号
	 */
	public short getColumnId() {
		return columnId;
	}

	/**
	 * 设置数据封装(只针对可变长数组)
	 * @param e Packing实例
	 */
	public void setPacking(Packing e) {
		packing = e;
	}

	/**
	 * 返回数据封装
	 * @return Packing实例，或者空指针
	 */
	public Packing getPacking() {
		return packing;
	}

	/**
	 * 大小写敏感 (CASE or NOTCASE)
	 * @param b 大小写敏感
	 */
	public void setSentient(boolean b) {
		sentient = b;
	}

	/**
	 * 是大小写敏感
	 * @return 返回真或者假
	 */
	public boolean isSentient() {
		return sentient;
	}

	/**
	 * 返回返回值数据类型
	 * @return 返回值数据类型
	 */
	public byte getResultFamily() {
		return resultFamily;
	}

	/**
	 * 设置返回值的数据类型
	 * @param b 返回值数据类型
	 */
	protected void setResultFamily(byte b) {
		resultFamily = b;
	}

	/**
	 * 检查被检索列与当前设置的返回类型是否匹配
	 * @param rows 记录列表
	 * @param columnId 列编号
	 */
	protected void check(List<Row> rows, short columnId) {
		for (Row row : rows) {
			Column column = row.find(columnId);
			if(column == null) continue; // 没找到忽略
			if (column.getType() != getResultFamily()) {
				throw new IllegalValueException("cannot be match:%d,%d", column.getType(), getResultFamily());
			}
		}
	}

	/**
	 * 判断是二进制数据类型
	 * @return 返回真或者假
	 */
	public boolean isRaw() {
		return ColumnType.isRaw(resultFamily);
	}

	/**
	 * 判断是单字符
	 * @return 返回真或者假
	 */
	public boolean isChar() {
		return ColumnType.isChar(resultFamily);
	}

	/**
	 * 判断是宽字符
	 * @return 返回真或者假
	 */
	public boolean isWChar() {
		return ColumnType.isWChar(resultFamily);
	}

	/**
	 * 判断是大字符
	 * @return 返回真或者假
	 */
	public boolean isHChar() {
		return ColumnType.isHChar(resultFamily);
	}

	/**
	 * 判断是字符类型
	 * @return 返回真或者假
	 */
	public boolean isWord() {
		return (isChar() || isWChar() || isHChar());
	}

	/**
	 * 判断是可变长数组类型
	 * @return 返回真或者假
	 */
	public boolean isVariable() {
		return (isRaw() || isWord() || isDocument() || isImage() || isAudio() || isVideo());
	}

	/**
	 * 判断返回结果是SHORT类型
	 * @return 返回真或者假
	 */
	public boolean isShort() {
		return ColumnType.isShort(resultFamily);
	}

	/**
	 * 判断返回结果是INT类型
	 * @return 返回真或者假
	 */
	public boolean isInteger() {
		return ColumnType.isInteger(resultFamily);
	}

	/**
	 * 判断返回结果是LONG类型
	 * @return 返回真或者假
	 */
	public boolean isLong() {
		return ColumnType.isLong(resultFamily);
	}

	/**
	 * 判断返回结果是FLOAT类型
	 * @return 返回真或者假
	 */
	public boolean isFloat() {
		return ColumnType.isFloat(resultFamily);
	}

	/**
	 * 判断返回结果是DOUBLE类型
	 * @return 返回真或者假
	 */
	public boolean isDouble() {
		return ColumnType.isDouble(resultFamily);
	}

	/**
	 * 判断返回结果是DATE类型
	 * @return 返回真或者假
	 */
	public boolean isDate() {
		return ColumnType.isDate(resultFamily);
	}

	/**
	 * 判断返回结果是TIME类型
	 * @return 返回真或者假
	 */
	public boolean isTime() {
		return ColumnType.isTime(resultFamily);
	}

	/**
	 * 判断返回结果是TIMESTAMP类型
	 * @return 返回真或者假
	 */
	public boolean isTimestamp() {
		return ColumnType.isTimestamp(resultFamily);
	}

	/**
	 * 判断是文档
	 * @return 返回真或者假
	 */
	public boolean isDocument() {
		return ColumnType.isDouble(resultFamily);
	}

	/**
	 * 判断是图像
	 * @return 返回真或者假
	 */
	public boolean isImage() {
		return ColumnType.isImage(resultFamily);
	}

	/**
	 * 判断是音频
	 * @return 返回真或者假
	 */
	public boolean isAudio() {
		return ColumnType.isAudio(resultFamily);
	}

	/**
	 * 判断是视频
	 * @return 返回真或者假
	 */
	public boolean isVideo() {
		return ColumnType.isVideo(resultFamily);
	}

	/**
	 * 根据输入语句，判断函数匹配
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public abstract boolean matches(String input);

	/**
	 * 按照子类提供的正则表达式，解析SQL描述语句，生成子类实例。如果条件不成立，返回NULL。<br>
	 * @param table 数据库表配置
	 * @param primitive 列函数命令描述(原语)
	 * @return ColumnFunction实例
	 */
	public abstract ColumnFunction create(Table table, String primitive);

	/**
	 * 根据传入的记录集合，产生一列数据
	 * @param rows 记录集合
	 * @return Column实例
	 */
	public abstract Column makeup(List<Row> rows);

	/**
	 * 返回一个默认列，是支持由子类决定，判断依据是isSupportDefault方法
	 * @return Column实例
	 */
	public abstract Column getDefault();

}