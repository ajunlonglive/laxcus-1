/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块标识 <br><br>
 * 
 * 参数组成：<br>
 * 1. 数据表名 <br>
 * 2. 数据块编号 <br><br>
 * 
 * 两个参数组合，确定每个数据块的唯一性。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/17/2015
 * @since laxcus 1.0
 */
public final class StubFlag implements Serializable, Cloneable, Classable, Comparable<StubFlag> {

	private static final long serialVersionUID = 5050612737641252218L;

	/** 数据表名 **/
	private Space space;

	/** 数据块编号 **/
	private long stub;
	
	/**
	 * 根据传入的数据块标识实例，生成它的浅层数据副本
	 * @param that
	 */
	private StubFlag(StubFlag that) {
		this();
		space = that.space;
		stub = that.stub;
	}

	/**
	 * 构造数据块标识
	 */
	public StubFlag() {
		super();
	}

	/**
	 * 构造数据块标识，指定全部参数
	 * @param space 数据表名
	 * @param stub 数据块编号
	 */
	public StubFlag(Space space, long stub) {
		this();
		setSpace(space);
		setStub(stub);
	}

	/**
	 * 从可类化数据读取器中解析数据块标识
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public StubFlag(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置数据表名
	 * @param e 数据表名
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return 数据表名
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置数据块编号
	 * @param e 数据块编号
	 */
	public void setStub(long e) {
		stub = e;
	}

	/**
	 * 返回数据块编号
	 * @return 数据块编号
	 */
	public long getStub() {
		return stub;
	}
	
	/**
	 * 生成数据块标识的浅层数据副本
	 * @return StubFlag实例
	 */
	public StubFlag duplicate() {
		return new StubFlag(this);
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%X", space, stub);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != StubFlag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((StubFlag) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (space.hashCode() ^ stub);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StubFlag that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(space, that.space);
		if (ret == 0) {
			ret = Laxkit.compareTo(stub, that.stub);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(space);
		writer.writeLong(stub);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		space = new Space(reader);
		stub = reader.readLong();
		return reader.getSeek() - seek;
	}

}
