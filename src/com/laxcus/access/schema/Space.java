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
 * 数据表名。<br>
 * 
 * 数据表名规定：<br>
 * <1> 由数据库名和表名组成。<br>
 * <2> 数据库名和表名长度限制在1至20个字节之间。<br>
 * <3> 数据库名和数据库表名的字符串是除“ASCII控制字符、空白字符、标点字符之外，但是又包括ASCII下划线（_）的任意语言字符”，忽略大小写。<br>
 * <4> 在JNI.DB层面，字符转为UTF8编码，汉字转UTF8，最大将是60个字符，再加上半截符分割，最大达到120个字符。<br>
 * 
 * <br>
 * 每个数据表名在集群都是唯一的，不允许重复。<br><br>
 * 
 * 提示：数据表名是数据库和表名称的组合，不是表的名称！<br>
 * 
 * @author scott.liang
 * @version 1.1 03/02/2015
 * @since laxcus 1.1
 */
public final class Space implements Serializable, Cloneable, Classable, Markable, Comparable<Space> {

	private static final long serialVersionUID = -336621498995259924L;

	/** 单元格式，最大20字节 */
	private final static String REGEX = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*$";

	/** 数据表组合格式：数据库名称.表名称，限制20字节 **/
	private final static String REGEX_INPUT= "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]{1,20})\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]{1,20}+)\\s*$";

	/** 数据库名称和表名称的长度。在1-20字符之间*/
	private final static int MAX_SIZE = 20;

	/** 数据库名，字符不允许超过20个 **/
	private Naming schema;

	/** 数据表名，字符不允许超过20个 **/
	private Naming table;

	/**
	 * 判断数据表名长度有效
	 * @param size 长度
	 * @return 返回真或者假
	 */
	private static boolean validate(int size) {
		return size > 0 && size <= Space.MAX_SIZE;
	}

	/**
	 * 判断数据表名长度有效
	 * @param size 字节长度
	 * @return 有效返回真，否则假。
	 */
	public static boolean isSchemaSize(int size) {
		return Space.validate(size);
	}

	/**
	 * 判断数据表长度有效
	 * @param size 字节长度
	 * @return 有效返回真，否则假。
	 */
	public static boolean isTableSize(int size) {
		return Space.validate(size);
	}

	/**
	 * 构造一个默认和私有的数据表名
	 */
	private Space() {
		super();
	}

	/**
	 * 根据传入的数据表名实例，生成它的数据副本。
	 * @param that 数据表名实例
	 */
	private Space(Space that) {
		this();
		schema = that.schema.duplicate();
		table = that.table.duplicate();
	}

	/**
	 * 设置数据表名，包括数据库和表的名称。
	 * 设置前检查字符串参数，如果不合法，弹出PatternSyntaxException异常。
	 * @param schema 数据库名
	 * @param table 表名
	 * @throws PatternSyntaxException 
	 */
	public Space(String schema, String table)  {
		this();
		set(schema, table);
	}

	/**
	 * 使用正则表达式解析数据表名，格式不正确抛出PatternSyntaxException异常
	 * @param input "schema-name.table-name"格式文本，两侧可以留空。用正则表达式解析。
	 * @throws PatternSyntaxException
	 */
	public Space(String input)  {
		this();
		split(input);
	}

	/**
	 * 从可类化数据读取器中解析数据表参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Space(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出数据表参数
	 * @param reader 标记化读取器
	 */
	public Space(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置数据表名。包括数据库和表两部分，长度在1-20个字符之间。
	 * @param schema_name 数据库名
	 * @param table_name 表名
	 */
	private void set(Naming schema_name, Naming table_name) {
		// 检查长度
		if (!Space.isSchemaSize(schema_name.length())) {
			throw new IllegalValueException("length out: %s", schema_name);
		} else if (!Space.isTableSize(table_name.length())) {
			throw new IllegalValueException("length out: %s", schema_name);
		}

		schema = schema_name;
		table = table_name;
	}

	/**
	 * 设置数据表名。数据库和表的名称必须是ASCII的字符，长度在1-20字符之间。
	 * @param schemaName 数据库名
	 * @param tableName 表名
	 * @throws PatternSyntaxException 
	 */
	private void set(String schemaName, String tableName) {
		// 正则表达式检查库名
		Pattern pattern = Pattern.compile(Space.REGEX);
		Matcher matcher = pattern.matcher(schemaName);
		if (!matcher.matches()) {
			throw new PatternSyntaxException(schemaName, Space.REGEX, 0);
		}
		schemaName = matcher.group(1);
		// 正则表达式检查表名
		pattern = Pattern.compile(Space.REGEX);
		matcher = pattern.matcher(tableName);
		if (!matcher.matches()) {
			throw new PatternSyntaxException(tableName, Space.REGEX, 0);
		}
		tableName = matcher.group(1);
		// 设置
		set(new Naming(schemaName), new Naming(tableName));
	}

	/**
	 * 返回数据库命名(数据库名称在集合中唯一)
	 * @return Fame实例
	 */
	public Fame getSchema() {
		return new Fame(schema.toString());
	}

	/**
	 * 返回数据库名称的字符串描述
	 * @return 字符串
	 */
	public String getSchemaText() {
		return schema.get();
	}

	/**
	 * 返回UTF8解码的数据库名称字节数组
	 * @return 字节数组
	 */
	public byte[] getSchemaRaw() {
		return schema.getBytes(new UTF8());
	}

	/**
	 * 返回数据表命名(表名在数据库名下唯一)
	 * @return Naming实例
	 */
	public Naming getTable() {
		return table;
	}

	/**
	 * 返回表名的字符串描述
	 * @return 字符串
	 */
	public String getTableText() {
		return table.get();
	}

	/**
	 * 返回UTF8解码的表名称的字节数组
	 * @return 字节数组
	 */
	public byte[] getTableRaw() {
		return table.getBytes(new UTF8());
	}

	/**
	 * 返回当前数据表名的数据副本
	 * @return Space实例
	 */
	public Space duplicate() {
		return new Space(this);
	}

	/**
	 * 比较两个数据表名是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Space.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((Space) that) == 0;
	}

	/**
	 * 返回数据表名的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return schema.hashCode() ^ table.hashCode();
	}

	/**
	 * 返回数据表名的字符串描述。格式是：schema_name.table_name
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s.%s", schema, table);
	}

	/**
	 * 根据当前数据表名实例，克隆一个它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个数据表名的排列位置
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Space that) {
		// 空对象排在前面，有效对象排在后面
		if (that == null) {
			return 1;
		}

		int ret = schema.compareTo(that.schema);
		if (ret == 0) {
			ret = table.compareTo(that.table);
		}
		return ret;
	}

	/**
	 * 使用正则表达式解析数据表名。
	 * @param input "数据库名.数据表名"格式结构。
	 * @throws PatternSyntaxException
	 */
	public void split(String input) {
		Pattern pattern = Pattern.compile(Space.REGEX_INPUT);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throw new PatternSyntaxException(input, Space.REGEX_INPUT, 0);
		}
		set(matcher.group(1), matcher.group(2));
	}

	/**
	 * 将数据表名参数写入可类化数据存储器。兼容C接口。
	 * @param writer 可类化数据存储器
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// ASCII字符
		byte[] s1 = schema.getBytes(new UTF8());
		byte[] s2 = table.getBytes(new UTF8());
		// 写入长度，各只占1个字节
		writer.write((byte) (s1.length & 0xFF));
		writer.write((byte) (s2.length & 0xFF));
		// 写入参数
		writer.write(s1);
		writer.write(s2);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化数据读取器中解析数据表名参数。兼容C接口。
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 数据库名和数据表名长度(只有一个字节)
		int schemaSize = reader.read() & 0xFF;
		int tableSize = reader.read() & 0xFF;
		// 读取数据
		byte[] s1 = reader.read(schemaSize);
		byte[] s2 = reader.read(tableSize);
		schema = new Naming(s1, new UTF8());
		table = new Naming(s2, new UTF8());
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 生成数据表名名称的字节数组，并且输出。
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 判断数据表名格式正确
	 * @param input 输入参数
	 * @return 返回真或者假。
	 */
	public static boolean validate(String input) {
		if (input == null) {
			return false;
		}
		Pattern pattern = Pattern.compile(Space.REGEX_INPUT);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
}