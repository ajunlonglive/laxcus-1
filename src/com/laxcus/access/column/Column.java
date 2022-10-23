/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

import java.io.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.markable.*;

/**
 * 数据列<br><br>
 * 
 * “列”是数据存储和计算的基础模型，在这里提供列编号、数据类型、空值三个参数，其余由子类去实现。<br>
 * 
 * 列编号：short类型，范围在1-0x7FFF，0是无效标识，负数是“LIKE WORD(RWord)”编号。所以，一行记录最大可以到0x7FFF。
 * 数据类型：byte类型，见Types中的定义。
 * 空值标记：布尔值。
 *
 * @author scott.liang 
 * @version 1.1 8/21/2013
 * @since laxcus 1.0
 */
public abstract class Column implements Classable, Markable, Serializable, Cloneable, Comparable<Column> {

	private static final long serialVersionUID = -2232857768232951073L;

	/** 列编号，下标从1开始计，0代表无效 ，有效范围是:1 - 0x7fff**/
	private short id;

	/** 列数据类型 **/
	private byte type;

	/** 空值(NULL)状态标记 **/
	private boolean nullable;

	/**
	 * 构造列基础类，同时指定默认值
	 */
	protected Column() {
		super();
		id = 0;
		type = 0;
		nullable = true;
	}

	/**
	 * 根据传入的列参数，生成它的数据副本
	 * @param that Column实例
	 */
	protected Column(Column that) {
		super();
		id = that.id;
		type = that.type;
		nullable = that.nullable;
	}

	/**
	 * 建立一个列，同时指定它的数据类型
	 * @param type 列数据类型
	 */
	protected Column(byte type) {
		this();
		setType(type);
	}

	/**
	 * 建立一个列，同时指定的数据类型和标识号
	 * @param type 列数据类型
	 * @param id 列编号
	 */
	protected Column(byte type, short id) {
		this(type);
		setId(id);
	}

	/**
	 * 设置列编号 (1 - 0x8fff)
	 * @param i 列编号
	 */
	public void setId(short i) {
		id = i;
	}

	/**
	 * 返回列编号，是短整型
	 * @return 列编号
	 */
	public short getId() {
		return id;
	}

	/**
	 * 设置空值状态
	 * @param b 空状态
	 */
	public void setNull(boolean b) {
		nullable = b;
	}

	/**
	 * 判断判断是空值状态
	 * @return 返回真或者假
	 */
	public boolean isNull() {
		return nullable;
	}

	/**
	 * 设置列数据类型
	 * @param who 列数据类型
	 */
	public void setType(byte who) {
		if (!ColumnType.isType(who)) {
			throw new IllegalValueException("illegal column:%d", who);
		}
		type = who;
	}

	/**
	 * 返回列数据类型
	 * @return 列数据类型
	 */
	public byte getType() {
		return type;
	}

	/**
	 * 判断是匹配的列数据类型
	 * @param who 列数据类型
	 * @return 返回真或者假
	 */
	public boolean isType(byte who) {
		return type == who;
	}

	/**
	 * 将空值标记和数据类型合并，生成列标记号（只有一个字节）
	 * @return 合并后的状态码
	 */
	protected byte buildTag() {
		return ColumnType.buildState(isNull(), getType());
	}

	/**
	 * 解析列编号和检查是否合法
	 * @param state 合并的状态码
	 * @return 正确返回真，出错弹出类型
	 * @throws ColumnException - 列异常
	 */
	protected boolean resolveTag(byte state) {
		// 空值标记
		setNull(ColumnType.isNullable(state));
		// 数据类型
		byte who = ColumnType.resolveType(state);
		// 如果不匹配，弹出异常
		if(!isType(who)) {
			throw new ColumnException("resolve error! not match type! %d - %d",
					type & 0xFF, who & 0xFF);
		}
		return true;
	}

	/**
	 * 判断是可变长类型，包括二进制数组、字符、媒体类型
	 * @return 条件成立返回真，否则假。
	 */
	public boolean isVariable() {
		return (isRaw() || isWord() || isMedia());
	}

	/**
	 * 判断是字符类型，包括CHAR、WCHAR、HCHAR
	 * @return 返回真或者假
	 */
	public boolean isWord() {
		return (isChar() || isWChar() || isHChar());
	}

	/**
	 * 判断是LIKE关键字，包括LIKE-CHAR、LIKE-WCHAR、LIKE-HCHAR
	 * @return 返回真或者假
	 */
	public boolean isRWord() {
		return (isRChar() || isRWChar() || isRHChar());
	}

	/**
	 * 判断是媒体类型
	 * @return 返回真或者假
	 */
	public boolean isMedia() {
		return (isDocument() || isImage() || isAudio() || isVideo());
	}

	/**
	 * 判断是二进制数组类型
	 * @return 返回真或者假
	 */
	public final boolean isRaw() {
		return ColumnType.isRaw(type);
	}

	/**
	 * 判断是图像
	 * @return 返回真或者假
	 */
	public final boolean isImage() {
		return ColumnType.isImage(type);
	}

	/**
	 * 判断是音频
	 * @return 返回真或者假
	 */
	public final boolean isAudio() {
		return ColumnType.isAudio(type);
	}

	/**
	 * 判断是视频
	 * @return 返回真或者假
	 */
	public final boolean isVideo() {
		return ColumnType.isVideo(type);
	}

	/**
	 * 判断是可变长字符类型(UTF8编码)
	 * @return 返回真或者假
	 */
	public final boolean isChar() {
		return ColumnType.isChar(type);
	}

	/**
	 * 判断是单字符(UTF8)LIKE类型
	 * @return 返回真或者假
	 */
	public final boolean isRChar() {
		return ColumnType.isRChar(type);
	}

	/**
	 * 判断是可变长宽字符类型(UTF16 BigEndian编码)
	 * @return 返回真或者假
	 */
	public final boolean isWChar() {
		return ColumnType.isWChar(type);
	}

	/**
	 * 判断是宽字符(UTF16 BIG-ENDIAN)的LIKE类型
	 * @return 返回真或者假
	 */
	public final boolean isRWChar() {
		return ColumnType.isRWChar(type);
	}

	/**
	 * 判断是可变长大字符类型(UTF32编码)
	 * @return 返回真或者假
	 */
	public final boolean isHChar() {
		return ColumnType.isHChar(type);
	}

	/**
	 * 判断是大字符(UTF32)LIKE关键字
	 * @return 返回真或者假
	 */
	public final boolean isRHChar() {
		return ColumnType.isRHChar(type);
	}

	/**
	 * 判断是否是数值类型
	 * @return 返回真或者假
	 */
	public final boolean isNumber() {
		return (isShort() || isInteger() || isLong() || isFloat() || isDouble());
	}

	/**
	 * 判断是否日历类型
	 * @return 返回真或者假
	 */
	public final boolean isCalendar() {
		return (isDate() || isTime() || isTimestamp());
	}

	/**
	 * 判断是短型值
	 * @return 返回真或者假
	 */
	public final boolean isShort() {
		return ColumnType.isShort(type);
	}

	/**
	 * 判断是整型
	 * @return 返回真或者假
	 */
	public final boolean isInteger() {
		return ColumnType.isInteger(type);
	}

	/**
	 * 判断是长整型
	 * @return 返回真或者假
	 */
	public final boolean isLong() {
		return ColumnType.isLong(type);
	}

	/**
	 * 是否单浮点类型
	 * @return 返回真或者假
	 */
	public final boolean isFloat() {
		return ColumnType.isFloat(type);
	}

	/**
	 * 是否双浮点类型
	 * @return 返回真或者假
	 */
	public final boolean isDouble() {
		return ColumnType.isDouble(type);
	}

	/**
	 * 是否日期类型
	 * @return 返回真或者假
	 */
	public final boolean isDate() {
		return ColumnType.isDate(type);
	}

	/**
	 * 是否时间类型
	 * @return 返回真或者假
	 */
	public final boolean isTime() {
		return ColumnType.isTime(type);
	}

	/**
	 * 是否时间戳类型
	 * @return 返回真或者假
	 */
	public final boolean isTimestamp() {
		return ColumnType.isTimestamp(type);
	}

	/**
	 * 判断是文档
	 * @return 返回真或者假
	 */
	public final boolean isDocument() {
		return ColumnType.isDocument(type);
	}

	/**
	 * 按照列编号进行排序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Column that) {
		// 按照规则，空对象排在前面
		if(that == null) {
			return 1;
		}
		// 列编号是short类型，like列的标识号是负数， short转成int后，所有like列将排在后面
		return Laxkit.compareTo((id & 0xFFFF), (that.id & 0xFFFF));
	}

	/**
	 * 根据实际子类，克隆一个它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 将列数据转化为字节数组后输出。兼容C接口。
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer); // 调用子类
		return writer.effuse();
	}

	/**
	 * 从传入的字节数组中解析一列参数。兼容C接口。
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 解析字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader); // 调用子类
	}

	/**
	 * 由子类实现，生成当时子类实例的数据副本
	 * @return Column子类实例
	 */
	public abstract Column duplicate();

	/**
	 * 根据列中的数据进行比较排序，低者在前(<0)，高者在后(>0)，相同为0。<br>
	 * 数值型按照值大小排序，二进制数组按照字节序列比较，字符串按照字典序比较
	 * @param that 传入被比较的列实例
	 * @return 字典排序值
	 */
	public abstract int compare(Column that);
	
	/**
	 * 根据列数据，按照指定的升序或者降序排序
	 * @param that 传入被比较的列实例
	 * @param asc 升序或者否
	 * @return 字典排序值
	 */
	public abstract int compare(Column that, boolean asc);

	/**
	 * 统计列中的参数转为字节数组后的尺寸(以字节为单位)
	 * @return 字节尺寸
	 */
	public abstract int capacity();

	/**
	 * 根据列属性，产生模值
	 * @param attribute
	 * @return
	 */
	public abstract SHA256Hash hash(ColumnAttribute attribute);
}