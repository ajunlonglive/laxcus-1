/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 复制数据块单元
 * 
 * @author scott.liang
 * @version 1.0 11/10/2020
 * @since laxcus 1.0
 */
public final class CopyEntityItem implements Serializable, Cloneable, Classable, Comparable<CopyEntityItem> {

	private static final long serialVersionUID = 6279433327146521398L;

	/** 数据块编号 **/
	private long stub;

	/** 成功标识 **/
	private boolean successful;

	/**
	 * 构造默认和私有的复制数据块单元
	 */
	private CopyEntityItem() {
		super();
	}

	/**
	 * 生成复制数据块单元数据副本
	 * @param that 原本
	 */
	private CopyEntityItem(CopyEntityItem that) {
		stub = that.stub;
		successful = that.successful;
	}

	/**
	 * 构造复制数据块单元，指定数据块编号和成功标记
	 * @param stub 数据块编号
	 * @param successful 成功标记
	 */
	public CopyEntityItem(long stub, boolean successful) {
		this();
		setStub(stub);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器解析复制数据块单元
	 * @param reader 可类化数据读取器
	 */
	public CopyEntityItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置数据块编号
	 * @param e long
	 */
	public void setStub(long e) {
		stub = e;
	}

	/**
	 * 返回数据块编号
	 * @return long
	 */
	public long getStub() {
		return stub;
	}

	/**
	 * 设置成功标识
	 * @param b 成功标识
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * 产生数据副本
	 * @return CopyEntityItem实例
	 */
	public CopyEntityItem duplicate() {
		return new CopyEntityItem(this);
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
		return String.format("0x%X/%s", stub, (successful ? "Successful" : "Failed"));
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CopyEntityItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((CopyEntityItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) ((stub >>> 32) & stub);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CopyEntityItem that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(stub, that.stub);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeLong(stub);
		writer.writeBoolean(successful);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		stub = reader.readLong();
		successful = reader.readBoolean();
		return reader.getSeek() - seek;
	}

}