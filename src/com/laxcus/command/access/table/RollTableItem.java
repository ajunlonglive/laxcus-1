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
 * 回滚处理单元结果
 * 
 * @author scott.liang
 * @version 1.0 4/12/2017
 * @since laxcus 1.0
 */
public class RollTableItem implements Classable, Cloneable, Comparable<RollTableItem>, Serializable {
	
	private static final long serialVersionUID = 6683346713874257712L;

	/** 数据块编号 **/
	private long stub;
	
	/** 成功标记 **/
	private boolean successful;

	/**
	 * 构造回滚处理单元结果
	 */
	private RollTableItem() {
		super();
	}

	/**
	 * 生成回滚处理单元结果的数据副本
	 * @param that RollTableItem实例
	 */
	private RollTableItem(RollTableItem that) {
		this();
		stub = that.stub;
		successful = that.successful;
	}

	/**
	 * 构造回滚处理单元结果，指定参数
	 * @param stub 数据块编号
	 * @param successful 成功标记
	 */
	public RollTableItem(long stub, boolean successful) {
		this();
		setStub(stub);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析回滚处理单元结果
	 * @param reader 可类化数据读取器
	 */
	public RollTableItem(ClassReader reader) {
		this();
		resolve(reader);
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
	 * 设置成功标记
	 * @param b 成功标记
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
	 * 生成当前实例的数据副本
	 * @return RollTableItem实例
	 */
	public RollTableItem duplicate() {
		return new RollTableItem(this);
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
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != RollTableItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((RollTableItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (stub >>> 32 & stub);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RollTableItem that) {
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