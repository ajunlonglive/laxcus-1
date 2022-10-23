/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.schema;

import java.io.*;
import java.util.regex.*;

import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;
import com.laxcus.util.naming.*;

/**
 * 数据库名称 <br>
 * 
 * 数据库名称规定：<br>
 * <1> 数据库名长度限制在1至20个字符之间。<br>
 * <2> 数据库名的字符串可以是除ASCII控制字符、标点符号、空白字符之外的任意语种字符，且忽略大小写。<br>
 * 
 * @author scott.liang
 * @version 1.1 03/02/2015
 * @since laxcus 1.1
 */
public final class Fame implements Serializable, Cloneable, Classable, Markable, Comparable<Fame> {

	private static final long serialVersionUID = 5678708910604955738L;

	/** 单元格式，最大20字节 */
	private final static String REGEX = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]{1,20})\\s*$";

	/** 数据库名称的长度。在1-20字符之间*/
	private final static int MAX_SIZE = 20;

	/** 数据库名，ASCII字符描述，不允许超过20个字符 **/
	private Naming schema;

	/**
	 * 判断长度有效
	 * @param size 字节长度
	 * @return 返回真或者假
	 */
	private static boolean validate(int size) {
		return size > 0 && size <= Fame.MAX_SIZE;
	}

	/**
	 * 判断数据库名长度有效
	 * @param size 字节长度
	 * @return 有效返回真，否则假。
	 */
	public static boolean isSchemaSize(int size) {
		return Fame.validate(size);
	}

	/**
	 * 构造一个默认和私有的数据库名
	 */
	private Fame() {
		super();
	}

	/**
	 * 根据传入的数据库名实例，生成它的数据副本。
	 * @param that 数据库名实例
	 */
	private Fame(Fame that) {
		this();
		schema = that.schema.duplicate();
	}

	/**
	 * 使用正则表达式解析数据库名，格式不正确抛出PatternSyntaxException异常
	 * @param input 数据库名称格式文本，两侧可以留空。用正则表达式判断。
	 * @throws PatternSyntaxException
	 */
	public Fame(String input)  {
		this();
		setSchema(input);
	}

	/**
	 * 构造数据库名，指定命名
	 * @param e 数据库名称
	 */
	public Fame(Naming e) {
		this(e.get());
	}

	/**
	 * 从可类化数据读取器中解析数据库名参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Fame(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出数据库名参数
	 * @param reader 标记化读取器
	 */
	public Fame(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 设置数据库名
	 * @param e 数据库名
	 */
	private void setSchema(Naming e) {
		Laxkit.nullabled(e);
		// 检查长度
		if (!Fame.isSchemaSize(e.length())) {
			throw new IllegalValueException("length out: %s", e);
		}
		// 设置值
		schema = e;
	}

	/**
	 * 使用正则表达式判断数据库名。字符限制在20个。
	 * @param input 数据库名称。
	 * @throws PatternSyntaxException
	 */
	public void setSchema(String input) {
		Pattern pattern = Pattern.compile(Fame.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throw new PatternSyntaxException(input, Fame.REGEX, 0);
		}
		// 取出数据库名
		setSchema(new Naming(matcher.group(1)));
	}

	/**
	 * 返回数据库名称(数据库名称在集合中唯一)
	 * @return 返回Naming实例
	 */
	public Naming getSchema() {
		return schema;
	}

	/**
	 * 返回数据库名称的字符串描述
	 * @return 返回数据库的字符串文本
	 */
	public String getSchemaText() {
		return schema.get();
	}

	/**
	 * 返回UTF8解码的数据库名称字节数组
	 * @return 返回UTF8编码
	 */
	public byte[] getSchemaRaw() {
		return schema.getBytes(new UTF8());
	}

	/**
	 * 返回当前数据库名的数据副本
	 * @return Fame实例
	 */
	public Fame duplicate() {
		return new Fame(this);
	}

	/**
	 * 比较两个数据库名是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Fame.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((Fame) that) == 0;
	}

	/**
	 * 返回数据库名的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return schema.hashCode() ;
	}

	/**
	 * 返回数据库名的字符串描述。格式是：schema_name.table_name
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return schema.toString();
	}

	/**
	 * 根据当前数据库名实例，克隆一个它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个数据库名的排列位置
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Fame that) {
		// 空对象排在前面，有效对象排在后面
		if (that == null) {
			return 1;
		}
		return schema.compareTo(that.schema);
	}

	/**
	 * 将数据库名参数写入可类化数据存储器。兼容C接口。
	 * @param writer 可类化数据存储器
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// ASCII字符
		byte[] s1 = schema.getBytes(new UTF8());
		// 写入长度，各只占1个字节
		writer.write((byte) (s1.length & 0xFF));
		// 写入参数
		writer.write(s1);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化数据读取器中解析数据库名参数。兼容C接口。
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 长度(只有一个字节)
		int schemaSize = reader.read() & 0xFF;
		// 读取数据
		byte[] s1 = reader.read(schemaSize);
		this.setSchema(new Naming(s1, new UTF8()));
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 生成数据库名名称的字节数组，并且输出。
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 判断数据库名格式正确
	 * @param input 输入参数
	 * @return 返回真或者假。
	 */
	public static boolean validate(String input) {
		if (input == null) {
			return false;
		}
		Pattern pattern = Pattern.compile(Fame.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
}