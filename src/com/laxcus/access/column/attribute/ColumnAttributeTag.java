/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column.attribute;

import java.io.*;
import java.util.regex.*;

import com.laxcus.access.function.table.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;
import com.laxcus.util.naming.*;

/**
 * 列属性标记<br><br>
 * 
 * 列属性标记由列名、编号、属性三个参数组成，通过这三个参数确定每一列的唯一性。
 * 列名由ASCIC字符组成，字符范围在1-32之间。<br>
 * 
 * @author scott.liang
 * @version 1.1 3/23/2015
 * @since laxcus 1.0
 */
public final class ColumnAttributeTag implements Classable, Markable, Serializable, Cloneable, Comparable<ColumnAttributeTag> {

	private static final long serialVersionUID = -7369136540728024825L;

	/** 名称正则表达式，最大20个字符 **/
	private final static String REGEX = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*$";

	/** 列名长度。在1-20字符之间*/
	private final static int MAX_SIZE = 20;

	/** 列的数据类型，见 ColumnType定义 **/
	private byte type;

	/** 列编号。从1开始，在建表(CREATE TABLE)时自动顺序分配。0是无效或者未定义 **/
	private short columnId;
	
	/** 宽度，定义显示相关参数  **/
	private int width;

	/** 列名，忽略大小写。字符限制为英文字母，数字和下划线。即正则表达式的"\w" **/
	private Naming name;

	/**
	 * 判断有效
	 * @param len
	 * @return
	 */
	public static boolean validate(int len) {
		return 1 <= len && len <= ColumnAttributeTag.MAX_SIZE;
	}

	/**
	 * 根据传入的列属性标记实例，生成它的数据副本。
	 * @param that 列属性标记实例
	 */
	private ColumnAttributeTag(ColumnAttributeTag that) {
		this();
		type = that.type;
		columnId = that.columnId;
		width = that.width;
		name = that.name.duplicate();
	}

	/**
	 * 构造一个空的列属性标记实例
	 */
	protected ColumnAttributeTag() {
		super();
		// 无效状态
		type = 0;
		columnId = 0;
		width = 0;
	}

	/**
	 * 从可类化数据读取器中解析列属性标记参数
	 * @param reader  可类化数据读取器
	 * @since 1.1
	 */
	public ColumnAttributeTag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出列属性标记参数
	 * @param reader 标记化读取器
	 */
	public ColumnAttributeTag(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置列编号(在数据库表定义中的排列位置，下标从1开始，0是无效值)
	 * @param id  列编号
	 */
	public void setColumnId(short id) {
		if (id < 1) {
			throw new IllegalValueException("illega column id:%d", id);
		}
		columnId = id;
	}

	/**
	 * 返回列编号
	 * @return short类型
	 */
	public short getColumnId() {
		return columnId;
	}
	
	/**
	 * 判断在允许范围内，0-255
	 * @param range 范围值
	 * @return 返回真或者假
	 */
	private final boolean allow(int range) {
		return (range >= 0 && range <= 0xff);
	}

	/**
	 * 设置精度和标度
	 * @param m 精度
	 * @param d 标度
	 */
	public void setWidth(int m, int d) {
		if (!allow(m) || !allow(d)) {
			throw new IllegalValueException("illegal value %d %d", m, d);
		}
		int value = ((m & 0xff) << 8) | (d & 0xff);
		width = width | value;
	}

	/**
	 * 设置列名
	 * @param e 列名
	 */
	public void setName(Naming e) {
		// 判断是空指针
		Laxkit.nullabled(e);
		// 判断在长度范围
		if (!ColumnAttributeTag.validate(e.length())) {
			throw new IllegalValueException("length out: %s", e);
		}
		// 赋值
		name = e;
	}

	/**
	 * 设置列名。如果是空值弹出空指针异常，如果参数不匹配，弹出正则表达式异常。
	 * @param input 输入文本
	 * @throws NullPointerException
	 */
	public void setName(String input) {
		// 检查空指针
		Laxkit.nullabled(input);
		// 判断超过范围
		Pattern pattern = Pattern.compile(ColumnAttributeTag.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		// 判断是标准的列名（不包括ASCII控制字符、符号字符的任意语言字符串）
		if (success) {
			setName(new Naming(matcher.group(1))); // 保存过滤后的参数
		}

		// 判断是列函数
		if (!success) {
			success = ColumnFunctionCreator.matches(input);
			if (success) name = new Naming(input.trim());
		}

		// 弹出异常
		if (!success) {
			throw new PatternSyntaxException(input, ColumnAttributeTag.REGEX, 0);
		}
	}

	/**
	 * 设置列名称
	 * @param b  字节数组
	 * @param off 下标
	 * @param len 有效长度
	 */
	public void setName(byte[] b, int off, int len) {
		Naming e = new Naming(b, off, len, new UTF8());
		setName(e);
	}

	/**
	 * 设置列名
	 * @param b 字节数组
	 */
	public void setName(byte[] b) {
		setName(b, 0, b.length);
	}

	/**
	 * 返回列名
	 * @return 列名命名 
	 */
	public Naming getName() {
		return name;
	}

	/**
	 * 返回列名文本
	 * @return 列名的文本格式
	 */
	public String getNameText() {
		return name.toString();
	}

	/**
	 * 返回列名的字节数组描述
	 * @return 列名字节数组
	 */
	public byte[] getNameRaw() {
		return name.getBytes(new UTF8());
	}

	/**
	 * 设置列数据类型，目前只有“可变长数据类型、数字类型、日期类型”三种，不允许LIKE类型
	 * 
	 * @param who 数据类型，见ColumnType中的定义
	 */
	public void setType(byte who) {
		// 可变长、数字、日期三种，忽略LIKE类型
		boolean success = (ColumnType.isVariable(who)
				|| ColumnType.isNumber(who) || ColumnType.isCalendar(who));
		// 不允许弹出异常
		if (!success) {
			throw new IllegalValueException("illegal type:%d", who);
		}
		type = who;
	}

	/**
	 * 返回列数据类型
	 * @return 列数据类型，见ColumnType中定义
	 */
	public byte getType() {
		return type;
	}

	/**
	 * 判断是否为可变长字节数组(二进制字节数组、媒体、字符三种)
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
		return ColumnType.isRaw(type);
	}

	/**
	 * 判断是文档类型
	 * 
	 * @return 返回真或者假
	 */
	public boolean isDocument() {
		return ColumnType.isDocument(type);
	}

	/**
	 * 判断是图像类型
	 * 
	 * @return 返回真或者假
	 */
	public boolean isImage() {
		return ColumnType.isImage(type);
	}

	/**
	 * 判断是音频类型
	 * @return 返回真或者假
	 */
	public boolean isAudio() {
		return ColumnType.isAudio(type);
	}

	/**
	 * 判断是视频类型
	 * @return 返回真或者假
	 */
	public boolean isVideo() {
		return ColumnType.isVideo(type);
	}

	/**
	 * 判断是否单字符（UTF8编码）
	 * @return 返回真或者假
	 */
	public boolean isChar() {
		return ColumnType.isChar(type);
	}

	/**
	 * 判断是否宽字符（UTF16 BIG-ENDAIN编码）
	 * @return 返回真或者假
	 */
	public boolean isWChar() {
		return ColumnType.isWChar(type);
	}

	/**
	 * 判断是否大字符（UTF32编码 ）
	 * @return 返回真或者假
	 */
	public boolean isHChar() {
		return ColumnType.isHChar(type);
	}

	/**
	 * 判断是短整型 (2字节)
	 * @return 返回真或者假
	 */
	public boolean isShort() {
		return ColumnType.isShort(type);
	}

	/**
	 * 判断是整型 (4字节)
	 * @return 返回真或者假
	 */
	public boolean isInteger() {
		return ColumnType.isInteger(type);
	}

	/**
	 * 判断是长整型 (8字节)
	 * @return 返回真或者假
	 */
	public boolean isLong() {
		return ColumnType.isLong(type);
	}

	/**
	 * 判断是单浮点(4字节)
	 * @return 返回真或者假
	 */
	public boolean isFloat() {
		return ColumnType.isFloat(type);
	}

	/**
	 * 判断是双浮点(8字节)
	 * @return 返回真或者假
	 */
	public boolean isDouble() {
		return ColumnType.isDouble(type);
	}

	/**
	 * 判断是日期(4字节)
	 * @return 返回真或者假
	 */
	public boolean isDate() {
		return ColumnType.isDate(type);
	}

	/**
	 * 判断是时间(4 字节)
	 * @return 返回真或者假
	 */
	public boolean isTime() {
		return ColumnType.isTime(type);
	}

	/**
	 * 判断是时间戳 (8 字节)
	 * @return 返回真或者假
	 */
	public boolean isTimestamp() {
		return ColumnType.isTimestamp(type);
	}

	/**
	 * 生成当前列属性标记的数据副本
	 * @return ColumnAttributeTag实例
	 */
	public ColumnAttributeTag duplicate() {
		return new ColumnAttributeTag(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != ColumnAttributeTag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((ColumnAttributeTag) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return columnId;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s#%d", name,
				ColumnType.translate(type), columnId);
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
	 * 比较两个列属性标记的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ColumnAttributeTag that) {
		// 空对象排在前面
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(columnId, that.columnId);
	}

	/**
	 * 将列属性标记参数保存到可类化写入器中。兼容C接口。
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		if (name == null) {
			throw new NullPointerException();
		}
		final int size = writer.size();
		// 列数据类型
		writer.write(type);
		// 列编号
		writer.writeShort(columnId);
		// 显示范围
		writer.writeInt(width);
		// 列名称
		byte[] b = name.getBytes(new UTF8());
		// 列长度，只有1个字节(必须!)
		writer.write((byte) (b.length & 0xFF));
		// 列名称
		writer.write(b);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中取出列属性标记参数。兼容C接口。
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 类型
		type = reader.read();
		// 标识号
		columnId = reader.readShort();
		// 显示范围
		width = reader.readInt();
		// 名称长度，只有1个字节
		int len = reader.read() & 0xFF;
		// 读名称长度
		if (len > 0) {
			byte[] b = reader.read(len);
			name = new Naming(b, new UTF8());
		}  else {
			name = null;
		}
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

}