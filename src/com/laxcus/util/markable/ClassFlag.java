/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.markable;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 类符号。<br><br>
 * 
 * 包含两个参数：修饰符（字段类型）和类声明标记。<br><br>
 * 
 * 类声明标记是可选参数，允许留空。
 * 
 * @author scott.liang
 * @version 1.0 8/22/2017
 * @since laxcus 1.0
 */
public class ClassFlag implements Serializable, Cloneable, Classable, Comparable<ClassFlag> {

	private static final long serialVersionUID = 3015629728529156383L;

	/** 标记化记录器 **/
	private MarkRecorder recorder ;

	/** 修饰符，见MarkType。变量类型，即：byte, boolean, char, short, byte[], char[], String[] .... **/
	private byte type;

	/** 类声明标记 **/
	private ClassStamp stamp;

	/**
	 * 设置标记化记录器，不允许空指针
	 * @param e 标记化记录器
	 */
	protected void setRecorder(MarkRecorder e) {
		Laxkit.nullabled(e);
		recorder = e;
	}

	/**
	 * 返回标记化记录器
	 * @return 标记化记录器
	 */
	protected MarkRecorder getRecorder() {
		return recorder;
	}

	/**
	 * 构造默认的类符号
	 */
	protected ClassFlag() {
		super();
	}

	/**
	 * 从传入的实例生成类符号的数据副本
	 * @param that 传入的类符号实例
	 */
	protected ClassFlag(ClassFlag that) {
		this();
		type = that.type;
		stamp = that.stamp;
		recorder = that.recorder;
	}

	/**
	 * 构造类符号，指定参数
	 * @param recorder 标记化参数记录器
	 * @param type 修饰符（类型）
	 */
	public ClassFlag(MarkRecorder recorder, byte type) {
		this();
		setRecorder(recorder);
		setType(type);
	}

	/**
	 * 构造类符号，指定参数
	 * @param recorder 标记化参数记录器
	 * @param type 修饰符
	 * @param clazz 类名称（可选）
	 */
	public ClassFlag(MarkRecorder recorder, byte type, Class<?> clazz) {
		this(recorder, type);
		setClassName(clazz);
	}

	/**
	 * 从可类化读取器中解析参数
	 * @param recorder 标记化参数记录器
	 * @param reader 可类化读取器
	 */
	public ClassFlag(MarkRecorder recorder, ClassReader reader) {
		this();
		setRecorder(recorder);
		resolve(reader);
	}

	/**
	 * 设置类名称
	 * @param clazz 类定义
	 */
	public void setClassName(Class<?> clazz) {
		// 尝试保存这个类定义，如果继承Markable接口且没有保存时
		getRecorder().load(clazz);

		// 保存类名称
		String clazzName = clazz.getName();
		setClassName(clazzName);
	}

	/**
	 * 设置类名称。<br>
	 * 类名称允许空指针，但是不能够是空字符串。
	 * 
	 * @param className 类名称
	 */
	private void setClassName(String className) {
		if (className != null && className.isEmpty()) {
			throw new NullPointerException();
		}
		stamp = new ClassStamp(recorder, className);
	}

	/**
	 * 返回类名称
	 * @return 字符串或者空指针
	 */
	public String getClassName() {
		if (stamp != null) {
			return stamp.getName();
		}
		return null;
	}

	/**
	 * 设置修饰符。见MarkType中定义
	 * @param who 修饰符
	 */
	public void setType(byte who) {
		if (!MarkType.isType(who)) {
			throw new IllegalValueException("illegal mark type: %d", who);
		}
		type = who;
	}

	/**
	 * 返回修饰符。见MarkType中定义
	 * @return 修饰符
	 */
	public byte getType() {
		return type;
	}

	/**
	 * 判断是字节
	 * @return 返回真或者假
	 */
	public boolean isByte() {
		return MarkType.isByte(type);
	}

	/**
	 * 判断是布尔
	 * @return 返回真或者假
	 */
	public boolean isBoolean() {
		return MarkType.isBoolean(type);
	}

	/**
	 * 判断是字符
	 * @return 返回真或者假
	 */
	public boolean isChar() {
		return MarkType.isChar(type);
	}

	/**
	 * 判断是短整型
	 * @return 返回真或者假
	 */
	public boolean isShort() {
		return MarkType.isShort(type);
	}

	/**
	 * 判断是整型
	 * @return 返回真或者假
	 */
	public boolean isInteger() {
		return MarkType.isInteger(type);
	}

	/**
	 * 判断是长整型
	 * @return 返回真或者假
	 */
	public boolean isLong() {
		return MarkType.isLong(type);
	}

	/**
	 * 判断是单浮点
	 * @return 返回真或者假
	 */
	public boolean isFloat() {
		return MarkType.isFloat(type);
	}

	/**
	 * 判断是双浮点
	 * @return 返回真或者假
	 */
	public boolean isDouble() {
		return MarkType.isDouble(type);
	}

	/**
	 * 判断是字符串
	 * @return 返回真或者假
	 */
	public boolean isString() {
		return MarkType.isString(type);
	}

	/**
	 * 判断是字节数组
	 * @return 返回真或者假
	 */
	public boolean isByteArray() {
		return MarkType.isByteArray(type);
	}

	/**
	 * 判断是布尔数组
	 * @return 返回真或者假
	 */
	public boolean isBooleanArray() {
		return MarkType.isBooleanArray(type);
	}

	/**
	 * 判断是字符数组
	 * @return 返回真或者假
	 */
	public boolean isCharArray() {
		return MarkType.isCharArray(type);
	}

	/**
	 * 判断是短整型数组
	 * @return 返回真或者假
	 */
	public boolean isShortArray() {
		return MarkType.isShortArray(type);
	}

	/**
	 * 判断是整型数组
	 * @return 返回真或者假
	 */
	public boolean isIntegerArray() {
		return MarkType.isIntegerArray(type);
	}

	/**
	 * 判断是长整型数组
	 * @return 返回真或者假
	 */
	public boolean isLongArray() {
		return MarkType.isLongArray(type);
	}

	/**
	 * 判断是单浮点数组
	 * @return 返回真或者假
	 */
	public boolean isFloatArray() {
		return MarkType.isFloatArray(type);
	}

	/**
	 * 判断是双浮点数组
	 * @return 返回真或者假
	 */
	public boolean isDoubleArray() {
		return MarkType.isDoubleArray(type);
	}

	/**
	 * 判断是字符串数组
	 * @return 返回真或者假
	 */
	public boolean isStringArray() {
		return MarkType.isStringArray(type);
	}

	/**
	 * 判断是标记化对象
	 * @return 返回真或者假
	 */
	public boolean isMarkable() {
		return MarkType.isMarkable(type);
	}

	/**
	 * 判断是树集
	 * @return 返回真或者假
	 */
	public boolean isTreeSet() {
		return MarkType.isTreeSet(type);
	}

	/**
	 * 判断是二叉树
	 * @return 返回真或者假
	 */
	public boolean isTreeMap() {
		return MarkType.isTreeMap(type);
	}

	/**
	 * 判断是数组列表
	 * @return 返回真或者假
	 */
	public boolean isArrayList() {
		return MarkType.isArrayList(type);
	}

	/**
	 * 判断是串行化对象
	 * @return 返回真或者假
	 */
	public boolean isSerialable() {
		return MarkType.isSerialable(type);
	}

	/**
	 * 生成当前类符号的数据副本
	 * @return ClassTag实例
	 */
	public ClassFlag duplicate() {
		return new ClassFlag(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != ClassFlag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((ClassFlag) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (stamp != null) {
			return type ^ stamp.hashCode();
		} else {
			return type;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (stamp != null) {
			return String.format("%d/%s", type, stamp);
		} else {
			return String.format("%d", type);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ClassFlag that) {
		if (that == null) {
			return 1;
		}

		int	ret = Laxkit.compareTo(type, that.type);
		if (ret == 0) {
			ret = Laxkit.compareTo(stamp, that.stamp);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int pos = writer.size();

		// 判断“ClassStamp”有效
		boolean enabled = (stamp != null);
		// 两种条件，有选择写入
		if (enabled) {
			byte id = (byte) (type ^ 0x80); // 用“异域操作”插入一个有效符
			// 带有效符的变量类型
			writer.write(id);
			// 写入类声明标记
			writer.writeObject(stamp);
		} else {
			// 变量类型
			writer.write(type);
		}

		// 返回写入字节长度
		return writer.size() - pos;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();

		byte id = reader.read();
		// 用“与”操作判断“ClassStamp”参数有效
		if ((id & 0x80) == 0x80) {
			type = (byte) (id & 0x7F);
			stamp = new ClassStamp(getRecorder(), reader);
		} else {
			type = id;
		}

		// 返回读取长度
		return reader.getSeek() - seek;
	}

}