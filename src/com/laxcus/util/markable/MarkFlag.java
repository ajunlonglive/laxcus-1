/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.markable;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 标记符号。<br><br>
 * 
 * 记录一个变量的基础信息，出现在每个变量之前，有3个参数：变量修饰符、类声明标记（这两个由ClassTag提供）、参数声明标记。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 8/22/2017
 * @since laxcus 1.0
 */
public class MarkFlag extends ClassFlag {

	private static final long serialVersionUID = -294652125711495158L;

	/** 参数声明标记 **/
	private ParamStamp param;

	/**
	 * 构造默认和私有的标记符号
	 */
	private MarkFlag() {
		super();
	}

	/**
	 * 从传入的实例生成标记符号的数据副本
	 * @param that 传入的标记符号实例
	 */
	private MarkFlag(MarkFlag that) {
		super(that);
		param = that.param;
	}

	/**
	 * 构造标记符号，指定参数
	 * @param recorder 标记化参数记录器
	 * @param paramName 名字
	 * @param type 数据类型
	 */
	public MarkFlag(MarkRecorder recorder, String paramName, byte type) {
		this();
		setRecorder(recorder);
		setParamName(paramName);
		setType(type);
	}

	/**
	 * 构造标记符号，指定参数
	 * @param recorder 标记化参数记录器
	 * @param paramName 变量名
	 * @param type 数据类型
	 * @param clazz 类定义
	 */
	public MarkFlag(MarkRecorder recorder, String paramName, byte type, Class<?> clazz) {
		this(recorder, paramName, type);
		setClassName(clazz);
	}

	/**
	 * 从可类化读取器中解析参数
	 * @param recorder 标记化参数记录器
	 * @param reader 可类化读取器
	 */
	public MarkFlag(MarkRecorder recorder, ClassReader reader) {
		this();
		setRecorder(recorder);
		resolve(reader);
	}

	/**
	 * 设置参数变量名称，不允许空指针
	 * @param e
	 */
	public void setParamName(String e) {
		Laxkit.nullabled(e);

		param = new ParamStamp(getRecorder(), e);
	}

	/**
	 * 返回参数变量名称
	 * @return 字符串
	 */
	public String getParamName() {
		return param.getName();
	}

	/**
	 * 生成当前标记符号的数据副本
	 * @return MarkTag实例
	 */
	public MarkFlag duplicate() {
		return new MarkFlag(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.markable.ClassTag#compareTo(com.laxcus.util.markable.ClassTag)
	 */
	@Override
	public int compareTo(ClassFlag that) {
		if (that == null) {
			return 1;
		}

		// 上级参数比较
		int ret = super.compareTo(that);
		// 对象一致时比较
		if (ret == 0 && that.getClass() == MarkFlag.class) {
			MarkFlag e = (MarkFlag) that;
			ret = Laxkit.compareTo(param, e.param);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != MarkFlag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((MarkFlag) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		return hash ^ param.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = super.toString();
		return String.format("%s/%s", param, str);
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
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int pos = writer.size();
		super.build(writer);
		// 写入参数名称
		writer.writeObject(param);
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
		super.resolve(reader);
		// 参数名称
		param = new ParamStamp(getRecorder(), reader);
		// 返回读取长度
		return reader.getSeek() - seek;
	}

}