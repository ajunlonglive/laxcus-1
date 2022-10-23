/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column.attribute;

import java.io.*;

import com.laxcus.access.column.*;
import com.laxcus.access.function.table.*;
import com.laxcus.access.type.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;
import com.laxcus.util.naming.*;

/**
 * 列属性 <br><br>
 * 
 * 定义所有可以表述列的基本参数。<br>
 * 可表述列是在建立时生成的列，不包括LIKE类型列。<br>
 * 
 * @author scott.liang
 * @version 1.3 8/21/2013
 * @since laxcus 1.0
 */
public abstract class ColumnAttribute implements Classable, Markable, Serializable, Cloneable, Comparable<ColumnAttribute> {

	private static final long serialVersionUID = 7933880763723508974L;

	/** 列属性的三种状态：未定义，允许空值，不允许空值  */
	
	/** 未定义状态 */
	protected final static byte UNSTATUS = -1;

	/** 允许空值 **/
	protected static final byte ALLOW_NULL = 0;

	/** 不允许空值 **/
	protected static final byte NOT_NULL = 1;

	/** 列属性标记 */
	private ColumnAttributeTag tag = new ColumnAttributeTag();

	/** 值状态，是否允许空置或者不定义，默认是不定义 **/
	private byte nullable;

	/** 键类型: 主键，从键，或者未定义(prime key, savle key, none key) **/
	private byte key = KeyType.NONE_KEY;	;

	/** 列函数 (在生成默认列时使用)**/
	private ColumnFunction function;
	
	/** 备注，说明列的用途，注意事项之类的文字说明  **/
	private String comment;

	/**
	 * 将列属性参数写入可类化数据写入器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int seek = writer.size();
		// 列属性标记
		writer.writeObject(tag);
		// 索引键
		writer.write(key);
		// 空值标记
		writer.write(buildNullable());
		// 列函数
		buildFunction(writer);
		// 注释
		buildComment(writer);
		
		// 调用子类，写入属于它的数据
		buildSuffix(writer);		
		// 返回写入的字节长度
		return writer.size() - seek;
	}

	/**
	 * 从可类化读取器中解析列属性参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 列属性标记
		tag.resolve(reader);
		// 索引键
		setKey(reader.read());
		// 空值标记
		resolveNullable(reader.read());
		// 列函数
		resolveFunction(reader);
		// 注释
		resolveComment(reader);
		
		// 调用子类，读出属于它的数据
		resolveSuffix(reader);
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入参数构造列属性的数据副本
	 * 
	 * @param that 列属性
	 */
	protected ColumnAttribute(ColumnAttribute that) {
		super();
		// 列属性标记
		tag = that.tag.duplicate();
		// 索引键
		key = that.key;
		// 空值标记
		nullable = that.nullable;
		// 列函数
		if (that.function != null) {
			function = (ColumnFunction) that.function.duplicate();
		}
		// 备注
		if (that.comment != null) {
			comment = new String(that.comment);
		}
	}

	/**
	 * 构造列基础属性，并且指定列数据类型
	 * @param family 列数据类型
	 */
	protected ColumnAttribute(byte family) {
		super();
		key = KeyType.NONE_KEY;		
		nullable = ColumnAttribute.UNSTATUS;
		setType(family);
	}

	/**
	 * 返回列属性标记
	 * @return ColumnAttributeTag实例
	 */
	public ColumnAttributeTag getTag() {
		return tag;
	}

	/**
	 * 设置列编号
	 * @param id short类型
	 */
	public void setColumnId(short id) {
		tag.setColumnId(id);
	}

	/**
	 * 返回列编号
	 * @return short类型
	 */
	public short getColumnId() {
		return tag.getColumnId();
	}

	/**
	 * 设置列名称，列名称忽略大小写
	 * @param e 列名称的字符串描述
	 */
	public void setName(String e) {
		tag.setName(e);
	}

	/**
	 * 设置列名称，列名称忽略大小写
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 */
	public void setName(byte[] b, int off, int len) {
		tag.setName(b, off, len);
	}

	/**
	 * 返回列名称的文本
	 * @return 字符串描述
	 */
	public String getNameText() {
		return tag.getNameText();
	}

	/**
	 * 返回列名称
	 * @return 列命名
	 */
	public Naming getName() {
		return tag.getName();
	}

	/**
	 * 设置列的数据类型，见 ColumnType中定义
	 * @param b 列数据类型的字节描述
	 */
	public void setType(byte b) {
		tag.setType(b);
	}

	/**
	 * 返回列的数据类型
	 * @return 列数据类型的字节描述
	 */
	public byte getType() {
		return tag.getType();
	}

	/**
	 * 设置备注
	 * @param e
	 */
	public void setComment(String e) {
		comment = e;
	}

	/**
	 * 返回备注册
	 * @return
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * 判断是否为可变长字节数组(包括二进制字节和字符)
	 * 
	 * @return
	 */
	public boolean isVariable() {
		return (isRaw() || isMedia() || isWord());
	}

	/**
	 * 判断是否是字符串类型(可变长文字)
	 * 
	 * @return 返回真或者假
	 */
	public boolean isWord() {
		return (isChar() || isWChar() || isHChar());
	}

	/**
	 * 判断是媒体类型
	 * 
	 * @return 返回真或者假
	 */
	public boolean isMedia() {
		return (isDocument() || isImage() || isAudio() || isVideo());
	}

	/**
	 * 日期/时间类型
	 * 
	 * @return 返回真或者假
	 */
	public boolean isCalendar() {
		return (isDate() || isTime() || isTimestamp());
	}

	/**
	 * 固定长度无小数十进制数
	 * 
	 * @return 返回真或者假
	 */
	public boolean isIntegral() {
		return (isShort() || isInteger() || isLong());
	}

	/**
	 * 固定长度有小数十进制数
	 * 
	 * @return 返回真或者假
	 */
	public boolean isDecimal() {
		return (isFloat() || isDouble());
	}

	/**
	 * 十进制数字，包括浮点数和整数
	 * 
	 * @return 返回真或者假
	 */
	public boolean isNumber() {
		return (isShort() || isInteger() || isLong() || isFloat() || isDouble());
	}

	/**
	 * 判断是二进制字节数组类型
	 * 
	 * @return 返回真或者假
	 */
	public boolean isRaw() {
		return ColumnType.isRaw(getType());
	}

	/**
	 * 判断是文档类型
	 * 
	 * @return 返回真或者假
	 */
	public boolean isDocument() {
		return ColumnType.isDocument(getType());
	}

	/**
	 * 判断是图像类型
	 * 
	 * @return 返回真或者假
	 */
	public boolean isImage() {
		return ColumnType.isImage(getType());
	}

	/**
	 * 判断是音频类型
	 * @return 返回真或者假
	 */
	public boolean isAudio() {
		return ColumnType.isAudio(getType());
	}

	/**
	 * 判断是视频类型
	 * @return 返回真或者假
	 */
	public boolean isVideo() {
		return ColumnType.isVideo(getType());
	}

	/**
	 * 判断是否单字符（UTF8编码）
	 * @return 返回真或者假
	 */
	public boolean isChar() {
		return ColumnType.isChar(getType());
	}

	/**
	 * 判断是否宽字符（UTF16 BIG-ENDAIN编码）
	 * @return 返回真或者假
	 */
	public boolean isWChar() {
		return ColumnType.isWChar(getType());
	}

	/**
	 * 判断是否大字符（UTF32编码 ）
	 * @return 返回真或者假
	 */
	public boolean isHChar() {
		return ColumnType.isHChar(getType());
	}

	/**
	 * 判断是短整型 (2字节)
	 * @return 返回真或者假
	 */
	public boolean isShort() {
		return ColumnType.isShort(getType());
	}

	/**
	 * 判断是整型 (4字节)
	 * @return 返回真或者假
	 */
	public boolean isInteger() {
		return ColumnType.isInteger(getType());
	}

	/**
	 * 判断是长整型 (8字节)
	 * @return 返回真或者假
	 */
	public boolean isLong() {
		return ColumnType.isLong(getType());
	}

	/**
	 * 判断是单浮点(4字节)
	 * @return 返回真或者假
	 */
	public boolean isFloat() {
		return ColumnType.isFloat(getType());
	}

	/**
	 * 判断是双浮点(8字节)
	 * @return 返回真或者假
	 */
	public boolean isDouble() {
		return ColumnType.isDouble(getType());
	}

	/**
	 * 判断是日期(4字节)
	 * @return 返回真或者假
	 */
	public boolean isDate() {
		return ColumnType.isDate(getType());
	}

	/**
	 * 判断是时间(4 字节)
	 * @return 返回真或者假
	 */
	public boolean isTime() {
		return ColumnType.isTime(getType());
	}

	/**
	 * 判断是时间戳 (8 字节)
	 * @return 返回真或者假
	 */
	public boolean isTimestamp() {
		return ColumnType.isTimestamp(getType());
	}

	/**
	 * 设置索引键类型 (主键或者从键)
	 * @param b 键类型
	 */
	public void setKey(byte b) {
		key = b;
	}

	/**
	 * 返回索引键类型
	 * @return 键类型的字节描述
	 */
	public byte getKey() {
		return key;
	}

	/**
	 * 判断是主键或者从键
	 * @return 返回真或者假
	 */
	public boolean isKey() {
		return isPrimeKey() || isSlaveKey();
	}

	/**
	 * 判断是主键
	 * @return 返回真或者假
	 */
	public boolean isPrimeKey() {
		return KeyType.isPrimeKey(key);
	}

	/**
	 * 判断是从键
	 * @return 返回真或者假
	 */
	public boolean isSlaveKey() {
		return KeyType.isSlaveKey(key);
	}

	/**
	 * 判断没有定义键类型（不是主键和从键）
	 * @return 返回真或者假
	 */
	public boolean isNoneKey() {
		return KeyType.isNoneKey(key);
	}

	/**
	 * 设置默认函数
	 * @param e 列函数实例
	 */
	public void setFunction(ColumnFunction e) {
		function = e;
	}

	/**
	 * 返回默认函数
	 * @return 列函数实例
	 */
	public ColumnFunction getFunction() {
		return function;
	}

	/**
	 * 设置当前列属列是否允许空值
	 * @param b  是否空值
	 * @return 设置成功返回真，否则假
	 */
	public boolean setNull(boolean b) {
		if (b) {
			if (nullable == ColumnAttribute.UNSTATUS || nullable == ColumnAttribute.ALLOW_NULL) {
				nullable = ColumnAttribute.ALLOW_NULL;
				return true;
			}
		} else {
			if (nullable == ColumnAttribute.UNSTATUS || nullable == ColumnAttribute.NOT_NULL) {
				nullable = ColumnAttribute.NOT_NULL;
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断当前列属性允许空值
	 * @return 返回真或者假
	 */
	public boolean isNullable() {
		return nullable == ColumnAttribute.ALLOW_NULL;
	}

	/** 
	 * 判断当前列属性处于“无设置”状态，或者“不允许空值”状态
	 * 
	 * @return 返回真或者假
	 */
	protected boolean isSetStatus() {
		return nullable == ColumnAttribute.UNSTATUS || nullable == ColumnAttribute.NOT_NULL;
	}

	/**
	 * 判断空值标记，返回状态值
	 * @return 返回标记，0或者1
	 */
	protected byte buildNullable() {
		return (byte) ((nullable == ColumnAttribute.UNSTATUS || nullable == ColumnAttribute.ALLOW_NULL) ? 1 : 0);
	}

	/**
	 * 判断空值标记，设置空值状态
	 * @param b 标记，0或者1
	 */
	protected void resolveNullable(byte b) {
		nullable = (b == 1 ? ColumnAttribute.ALLOW_NULL : ColumnAttribute.NOT_NULL);
	}
	
	/**
	 * 生成备注
	 * @param writer 可类化写入器
	 */
	private void buildComment(ClassWriter writer) {
		byte[] b = (comment == null ? null : new UTF8().encode(comment));
		int len = (b == null ? 0 : b.length);
		writer.writeInt(len);
		if (len > 0) {
			writer.write(b);
		}
	}

	/**
	 * 解析备注
	 * @param reader 可类化读取器
	 */
	private void resolveComment(ClassReader reader) {
		int len = reader.readInt();
		if (len > 0) {
			byte[] b = reader.read(len);
			comment = new UTF8().decode(b);
		} else {
			comment = null;
		}
	}

	/**
	 * 将函数保存到可类化数据写入器中。兼容C接口
	 * @param writer
	 */
	protected void buildFunction(ClassWriter writer) {
		if (function == null) {
			writer.writeInt(0);
		} else {
			ClassWriter cw = new ClassWriter();
			cw.writeDefault(function);
			byte[] b = cw.effuse();
			writer.writeInt(b.length);
			writer.write(b);
		}
	}

	/**
	 * 从可类化数据读取器中解析默认函数。兼容C接口
	 * @param reader
	 */
	protected void resolveFunction(ClassReader reader) {
		int size = reader.readInt();
		if (size == 0) {
			function = null;
		} else {
			byte[] b = reader.read(size);
			ClassReader in = new ClassReader(b);
			function = (ColumnFunction) in.readDefault();
		}
	}

	/**
	 * 比较两个列属性的排序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ColumnAttribute that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		return tag.compareTo(that.tag);
	}

	/**
	 * 调用子类实例，克隆一个它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return tag.toString();
	}

	/**
	 * 将列属性数据转化为字节数组后输出。兼容C接口。
	 * @return
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer); // 调用子类
		return writer.effuse();
	}

	/**
	 * 从传入的字节数组中解析一列属性参数。兼容C接口。
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader); // 调用子类
	}

	/**
	 * 根据实际子类的属性定义，返回它的副本
	 * @return ColumnAttribute实例
	 */
	public abstract ColumnAttribute duplicate();

	/**
	 * 根据基本条件生成一个默认列。条件是如果有函数用函数生成，否则取默认值。
	 * @return Column实例
	 */
	public abstract Column getDefault();

	/**
	 * 将子类的参数写入可类化数据写入器
	 * @param writer  可类化数据写入器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化读取器中解析子类的参数
	 * @param reader  可类化读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);	

}