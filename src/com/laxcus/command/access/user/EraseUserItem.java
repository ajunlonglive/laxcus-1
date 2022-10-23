/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 撤销用户的处理单元
 * 
 * @author scott.liang
 * @version 1.0 6/1/2019
 * @since laxcus 1.0
 */
public class EraseUserItem implements Classable, Cloneable, Serializable, Comparable<EraseUserItem> {

	private static final long serialVersionUID = 6380860552699821364L;

	/** 用户基点 **/
	private Seat seat;

	/** 成功标记 **/
	private boolean successful;

	/**
	 * 构造默认的撤销用户处理单元
	 */
	private EraseUserItem () {
		super();
	}

	/**
	 * 根据传入实例，生成撤销用户的处理单元的数据副本
	 * @param that CancelUserItem实例
	 */
	private EraseUserItem(EraseUserItem that) {
		super();
		seat = that.seat;
		successful = that.successful;
	}

	/**
	 * 构造撤销用户的处理单元，指定用户基点和处理结果
	 * @param seat 用户基点
	 * @param successful 成功
	 */
	public EraseUserItem(Seat seat, boolean successful) {
		this();
		setSeat(seat);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中撤销用户的处理单元
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public EraseUserItem (ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户基点
	 * @param e Seat实例
	 */
	public void setSeat(Seat e) {
		Laxkit.nullabled(e);

		seat = e;
	}

	/**
	 * 返回用户基点
	 * @return Seat实例
	 */
	public Seat getSeat() {
		return seat;
	}
	
	/**
	 * 返回签名
	 * @return Siger实例 
	 */
	public Siger getSiger() {
		return seat.getSiger();
	}

	/**
	 * 返回地址
	 * @return Node实例
	 */
	public Node getSite() {
		return seat.getSite();
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
	 * @return CancelUserItem实例
	 */
	public EraseUserItem duplicate() {
		return new EraseUserItem(this);
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
		return compareTo((EraseUserItem ) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return seat.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", seat, (successful ? "Successful" : "Failed"));
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EraseUserItem that) {
		if (that == null) {
			return 1;
		}
		// 比较参数
		int ret = Laxkit.compareTo(seat, that.seat);
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
		writer.writeObject(seat);
		writer.writeBoolean(successful);
	}

	/**
	 * 从可类化读取器解析参数
	 * @param reader 可类化读取器
	 */
	protected void resolveSuffix(ClassReader reader) {
		seat = new Seat(reader);
		successful = reader.readBoolean();
	}
}