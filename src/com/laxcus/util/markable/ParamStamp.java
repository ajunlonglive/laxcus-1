/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.markable;

import java.io.*;
import java.lang.reflect.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 参数声明标记。<br>
 * 
 * 参数编号和参数名称，在执行可类化时，首选编号，没有编号选择参数名称。
 * 
 * @author scott.liang
 * @version 1.0 10/14/2017
 * @since laxcus 1.0
 */
public final class ParamStamp extends MarkStamp implements Serializable, Cloneable, Classable, Comparable<ParamStamp> {

	private static final long serialVersionUID = 4315279310614017333L;

	/** 参数编号 **/
	private short id;

	/** 参数名称 **/
	private String name;

	/**
	 * 构造默认和私有的参数声明标记
	 */
	private ParamStamp() {
		super();
		id = MarkRecorder.INVALID; // 无效
	}

	/**
	 * 生成参数声明标记的数据副本
	 * @param that 传入的实例
	 */
	private ParamStamp(ParamStamp that) {
		super(that);
		id = that.id;
		name = that.name;
	}

	/**
	 * 构造参数声明标记，指定参数名称
	 * @param recorder 标记化参数记录器
	 * @param paramName 参数名称
	 */
	public ParamStamp(MarkRecorder recorder, String paramName) {
		this();
		setRecorder(recorder);
		setName(paramName);
	}

	/**
	 * 构造参数声明标记，指定参数域
	 * @param recorder 标记化参数记录器
	 * @param field 参数域
	 */
	public ParamStamp(MarkRecorder recorder, Field field) {
		this(recorder, field.getName());
	}

	/**
	 * 从可类化读取器中读取参数声明标记
	 * @param recorder 标记化参数记录器
	 * @param reader 可类化读取器
	 */
	public ParamStamp(MarkRecorder recorder, ClassReader reader) {
		this();
		setRecorder(recorder);
		resolve(reader);
	}

	/**
	 * 设置参数名称
	 * @param e 参数名称
	 */
	public void setName(String e) {
		// 取出编号
		java.lang.Short value = getRecorder().findParam(e);
		if (value != null) {
			id = value.shortValue();
		} else {
			name = e;
		}
	}

	/**
	 * 返回参数名称
	 * @return 返回类字符串
	 */
	public String getName() {
		if (hasMarkable()) {
			return getRecorder().findParamName(id);
		} else {
			return name;
		}
	}

	/**
	 * 判断可以标记化。<br>
	 * 
	 * 如果ID号不等于0即是可以标记化
	 * @return 返回真或者假
	 */
	public boolean hasMarkable() {
		return id != MarkRecorder.INVALID;
	}

	/**
	 * 生成数据副本
	 * @return ParamStamp实例
	 */
	public ParamStamp duplicate() {
		return new ParamStamp(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		String e = getName();
		return e.hashCode();
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
		int size = writer.size();
		boolean enabled = hasMarkable();
		if (enabled) {
			writeIdentity(id, writer);
		} else {
			writeName(name, writer);
		}
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 判断是标记码
		boolean enabled = isIdentity(reader);
		if (enabled) {
			id = readIdentity(reader);
		} else {
			name = readName(reader);
		}
		return reader.getSeek() - seek;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ParamStamp that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(id, that.id);
		if (ret == 0) {
			ret = Laxkit.compareTo(name, that.name);
		}
		return ret;
	}

}