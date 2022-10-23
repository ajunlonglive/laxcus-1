/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.parameter;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;
import com.laxcus.util.naming.*;

/**
 * 自定义参数。<br><br>
 * 
 * 自定义参数的格式由系统规定，内容由用户定义，用在各种分布处理中。自定义参数格式是：<br>
 * 
 * 自定义参数名称(数据类型)=数字型数值|'字符串数值'|'日期/时间'|布尔值(false|true) <br>
 * 
 * 多个自定义参数之间用逗号(,)分隔。<br><br>
 * 数据类型关键字: [bool|char|string|raw|short|int|long|float|double|date|time|timestamp] <br><br>
 * 
 * 实例数据类型(instance|classable)是在程序中设置，不在图形/字符界面输入。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public abstract class TaskParameter implements Serializable, Cloneable, Classable, Markable, Comparable<TaskParameter> {

	private static final long serialVersionUID = -4396660962581017735L;

	/** 参数数据类型 */
	private byte type;

	/** 参数名称，忽略大小写 */
	private Naming name;

	/**
	 * 构造默认的自定义参数，指定数据类型
	 * 
	 * @param type 数据类型
	 */
	protected TaskParameter(byte type) {
		super();
		setType(type);
	}

	/**
	 * 根据传入的自定义参数，生成它的副本
	 * @param that TaskParameter实例
	 */
	protected TaskParameter(TaskParameter that) {
		super();
		type = that.type;
		name = that.name;
	}

	/**
	 * 设置参数名称
	 * @param e 字符串
	 */
	public void setName(String e) {
		if(e == null || e.isEmpty()) {
			throw new NullPointerException();
		}
		name = new Naming(e);
	}

	/**
	 * 设置参数名称
	 * @param e 参数名称
	 */
	public void setName(Naming e) {
		Laxkit.nullabled(e);

		name = e;
	}

	/**
	 * 返回参数名称
	 * @return 命名实例
	 */
	public Naming getName() {
		return name;
	}

	/**
	 * 返回参数名称的文本描述
	 * @return 字符串
	 */
	public String getNameText() {
		return name.get();
	}

	/**
	 * 返回参数类型，见TaskParameterType定义
	 * @return 参数类型的字节描述
	 */
	public byte getType() {
		return type;
	}

	/**
	 * 设置参数类型，见TaskParameterType定义
	 * @param who 参数类型
	 */
	private void setType(byte who) {
		if(!TaskParameterType.isValueType(who)) {
			throw new IllegalValueException("illegal value: %d", who);
		}
		type = who;
	}

	/**
	 * 判断是布尔类型
	 * @return 返回真或者假
	 */
	public boolean isBoolean() {
		return TaskParameterType.isBoolean(type);
	}

	/**
	 * 判断是字节数组类型
	 * @return 返回真或者假
	 */
	public boolean isRaw() {
		return TaskParameterType.isRaw(type);
	}

	/**
	 * 判断是字符串类型
	 * @return 返回真或者假
	 */
	public boolean isString() {
		return TaskParameterType.isString(type);
	}

	/**
	 * 判断是短整型
	 * @return 返回真或者假
	 */
	public boolean isShort() {
		return TaskParameterType.isShort(type);
	}

	/**
	 * 判断是整型
	 * @return 返回真或者假
	 */
	public boolean isInteger() {
		return TaskParameterType.isInteger(type);
	}

	/**
	 * 判断是长整型
	 * @return 返回真或者假
	 */
	public boolean isLong() {
		return TaskParameterType.isLong(type);
	}

	/**
	 * 判断是单浮点值
	 * @return 返回真或者假
	 */
	public boolean isFloat() {
		return TaskParameterType.isFloat(type);
	}

	/**
	 * 判断是双浮点值
	 * @return 返回真或者假
	 */
	public boolean isDouble() {
		return TaskParameterType.isDouble(type);
	}

	/**
	 * 判断是日期类型
	 * @return 返回真或者假
	 */
	public boolean isDate() {
		return TaskParameterType.isDate(type);
	}

	/**
	 * 判断是时间类型
	 * @return 返回真或者假
	 */
	public boolean isTime() {
		return TaskParameterType.isTime(type);
	}

	/**
	 * 判断是时间戳类型
	 * @return 返回真或者假
	 */
	public boolean isTimestamp() {
		return TaskParameterType.isTimestamp(type);
	}

	/**
	 * 判断是串行化对象类型
	 * @return 返回真或者假
	 */
	public boolean isSerializable() {
		return TaskParameterType.isSerializable(type);
	}

	/**
	 * 判断是可类化接口类型
	 * @return 返回真或者假
	 */
	public boolean isClassable() {
		return TaskParameterType.isClassable(type);
	}
	
	/**
	 * 判断是命令类型
	 * @return 返回真或者假
	 */
	public boolean isCommand() {
		return TaskParameterType.isCommand(type);
	}

	/**
	 * 将数据类型，标题、数据值输出到可类化写入器
	 * @param writer 可类化写入器
	 * @return 返回写入的字节长度
	 */
	@Override
	public int build(ClassWriter w) {
		final int size = w.size();

		ClassWriter writer = new ClassWriter();
		// 数据类型
		writer.write(type);
		// 标题名称
		writer.writeInstance(name);
		// 保存子类参数信息
		buildSuffix(writer);

		// 读取内容
		byte[] b = writer.effuse();
		w.writeInt(b.length);
		w.write(b);

		// 返回写入的数据长度
		return w.size() - size;
	}

	/**
	 * 从可类化读取器中解析数据类型、参数标题、数据值
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader r) {
		final int seek = r.getSeek();

		// 从可类化读取器中读取Command字节流
		int len = r.readInt();
		byte[] b = r.read(len);

		ClassReader reader = new ClassReader(b);
		// 数据类型
		setType(reader.read());
		// 标题名称
		name = reader.readInstance(Naming.class);
		// 解析参数
		resolveSuffix(reader);

		// 返回解析长度
		return r.getSeek() - seek;
	}

//	/**
//	 * 将数据类型，标题、数据值输出到可类化写入器
//	 * @param writer 可类化写入器
//	 * @return 返回写入的字节长帿
//	 */
//	@Override
//	public int build(ClassWriter writer) {
//		final int size = writer.size();
//		// 数据类型
//		writer.write(type);
//		// 标题名称
//		writer.writeInstance(name);
//		// 保存子类参数信息
//		buildSuffix(writer);
//		// 返回长度
//		return writer.size() - size;
//	}
//
//	/**
//	 * 从可类化读取器中解析数据类型、参数标题、数据便
//	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
//	 */
//	@Override
//	public int resolve(ClassReader reader) {
//		final int seek = reader.getSeek();
//		// 数据类型
//		setType(reader.read());
//		// 标题名称
//		name = reader.readInstance(Naming.class);
//		// 解析参数
//		resolveSuffix(reader);
//		// 返回解析长度
//		return reader.getSeek() - seek;
//	}

	/**
	 * 调用子类实现接口，生成一个它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || !Laxkit.isClassFrom(that, TaskParameter.class)) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较一致
		return compareTo((TaskParameter) that) == 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return type ^ name.hashCode();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TaskParameter that) {
		if (that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(type, that.type);
		if (ret == 0) {
			ret = Laxkit.compareTo(name, that.name);
		}
		return ret;
	}

	/**
	 * 子类生成自己实例的数据副本
	 * @return TaskParameter子类实例
	 */
	public abstract TaskParameter duplicate();

	/**
	 * 将子类的参数写入可类化写入器
	 * @param writer 可类化写入器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化读取器中解析子类的参数
	 * @param reader 可类化读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);
}