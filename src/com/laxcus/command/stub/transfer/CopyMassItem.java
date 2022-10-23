/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.transfer;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * DATA主从节点数据块复制单元
 * 
 * @author scott.liang
 * @version 1.0 6/15/2019
 * @since laxcus 1.0
 */
public class CopyMassItem implements Classable, Cloneable, Serializable, Comparable<CopyMassItem> {

	private static final long serialVersionUID = 4523067850645192366L;

	/** 数据块编号 **/
	private long stub;

	/** 成功标记 **/
	private boolean successful;

	/**
	 * 构造默认的DATA主从节点数据块复制单元
	 */
	private CopyMassItem () {
		super();
	}

	/**
	 * 根据传入实例，生成DATA主从节点数据块复制单元的数据副本
	 * @param that CopyMassItem实例
	 */
	private CopyMassItem(CopyMassItem that) {
		super();
		stub = that.stub;
		successful = that.successful;
	}

	/**
	 * 构造DATA主从节点数据块复制单元，指定数据块编号和处理结果
	 * @param stub 数据块编号
	 * @param successful 成功
	 */
	public CopyMassItem(long stub, boolean successful) {
		this();
		setStub(stub);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中DATA主从节点数据块复制单元
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CopyMassItem (ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据块编号
	 * @param e long实例
	 */
	public void setStub(long e) {
		Laxkit.nullabled(e);

		stub = e;
	}

	/**
	 * 返回数据块编号
	 * @return long实例
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
	 * @return CopyMassItem实例
	 */
	public CopyMassItem duplicate() {
		return new CopyMassItem(this);
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
		if (that == null || this.getClass() != that.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((CopyMassItem ) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (stub >> 32 & stub);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("0x%x#%s", stub, (successful ? "Successful" : "Failed"));
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CopyMassItem that) {
		if (that == null) {
			return 1;
		}
		// 比较参数
		int ret = Laxkit.compareTo(stub, that.stub);
		if (ret == 0) {
			ret = Laxkit.compareTo(successful, that.successful);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		buildSuffix(writer);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		resolveSuffix(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 保存参数到可类化写入器
	 * @param writer 可类化数据写入器
	 */
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(stub);
		writer.writeBoolean(successful);
	}

	/**
	 * 从可类化读取器解析参数
	 * @param reader 可类化读取器
	 */
	protected void resolveSuffix(ClassReader reader) {
		stub = reader.readLong();
		successful = reader.readBoolean();
	}
}