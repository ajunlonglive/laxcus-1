/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.cross;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 共享单元
 * 
 * @author scott.liang
 * @version 1.0 8/16/2017
 * @since laxcus 1.0
 */
public abstract class CrossItem implements Classable, Markable, Cloneable, Serializable, Comparable<CrossItem> {

	private static final long serialVersionUID = 6011927673808246455L;

	/** 共享标识 **/
	private CrossFlag flag;

	/**
	 * 构造默认的共享单元
	 */
	protected CrossItem() {
		super();
	}

	/**
	 * 生成共享单元的数据副本
	 * @param that 共享单元
	 */
	protected CrossItem(CrossItem that) {
		this();
		flag = that.flag;
	}

	/**
	 * 设置共享标识
	 * @param e CrossFlag实例
	 */
	public void setFlag(CrossFlag e) {
		Laxkit.nullabled(e);
		
		flag = e;
	}

	/**
	 * 返回共享标识
	 * @return CrossFlag实例
	 */
	public CrossFlag getFlag() {
		return flag;
	}
	
	/**
	 * 返回操作符
	 * @return 操作符
	 */
	public int getOperator() {
		return flag.getOperator();
	}
	
	/**
	 * 设置操作符
	 * @param who 共享操作符
	 */
	public void setOperator(int who) {
		flag.setOperator(who);
	}

	/**
	 * 返回数据表名
	 * @return 数据表名
	 */
	public Space getSpace() {
		return flag.getSpace();
	}

	/**
	 * 返回数据库名
	 * @return 数据库名
	 */
	public Fame getSchema() {
		return flag.getSpace().getSchema();
	}

	/**
	 * 判断传入共享标识符合当前标准
	 * @param that 传入的共享标识
	 * @return 返回真或者假
	 */
	public boolean allow(CrossFlag that) {
		return flag.allow(that);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CrossItem that) {
		// 空值排在前面
		if (that == null) {
			return 1;
		}
		// 比较
		return Laxkit.compareTo(flag, that.flag);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(flag);
		buildSuffix(writer);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		flag = new CrossFlag(reader);
		resolveSuffix(reader);
		return reader.getSeek() - seek;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return flag.hashCode();
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
	 * 生成当前共享单元的数据副本
	 * @return CrossItem实例
	 */
	public abstract CrossItem duplicate();

	/**
	 * 将子类数据写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析子类数据
	 * @param reader 可类化数据读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);

}