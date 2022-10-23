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
 * 类声明标记。<br><br>
 * 
 * 类声明标记在类编号和类名称之间二选一，如果系统定义了类名称，
 * 那么把类名称转义成类编号，否则是字符串的类名称。<br>
 * 
 * @author scott.liang
 * @version 1.0 10/14/2017
 * @since laxcus 1.0
 */
public final class ClassStamp extends MarkStamp implements Serializable, Cloneable, Classable, Comparable<ClassStamp> {

	private static final long serialVersionUID = 8552908375598453156L;

	/** 类编号 **/
	private short id;

	/** 类名称 **/
	private String name;

	/**
	 * 构造默认和私有的类声明标记
	 */
	private ClassStamp() {
		super();
		id = MarkRecorder.INVALID; // 无效
	}

	/**
	 * 生成类声明标记的数据副本
	 * @param that 传入的实例
	 */
	private ClassStamp(ClassStamp that) {
		super(that);
		id = that.id;
		name = that.name;
	}
	
	/**
	 * 构造类声明标记，指定类名
	 * @param recorder 标记化参数记录器
	 * @param className 类名称
	 */
	public ClassStamp(MarkRecorder recorder, String className) {
		this();
		setRecorder(recorder);
		setName(className);
	}

	/**
	 * 构造类声明标记，指定类声明
	 * @param recorder 标记化参数记录器
	 * @param clazz 类声明
	 */
	public ClassStamp(MarkRecorder recorder, Class<?> clazz) {
		this(recorder, clazz.getName());
	}

	/**
	 * 构造类声明标记，指定类对象
	 * @param recorder 标记化参数记录器
	 * @param e 类对象
	 */
	public ClassStamp(MarkRecorder recorder, Object e) {
		this(recorder, e.getClass());
	}
	
	/**
	 * 从可类化读取器中读取类声明标记
	 * @param recorder 标记化参数记录器
	 * @param reader 可类化读取器
	 */
	public ClassStamp(MarkRecorder recorder, ClassReader reader) {
		this();
		setRecorder(recorder);
		resolve(reader);
	}

	/**
	 * 设置类名称
	 * @param e 类名称
	 */
	public void setName(String e) {
		java.lang.Short value = getRecorder().findClass(e);
		if (value != null) {
			id = value.shortValue();
		} else {
			name = e;
		}
	}

	/**
	 * 返回类名称
	 * @return 返回类字符串
	 */
	public String getName() {
		if (hasMarkable()) {
			return getRecorder().findClassName(id);
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
	 * @return ClassFlag实例
	 */
	public ClassStamp duplicate() {
		return new ClassStamp(this);
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
	public int compareTo(ClassStamp that) {
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