/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index;

import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * <code>SQL JOIN ON</code>的检索
 * 
 * @author scott.liang
 * @version 1.1 3/14/2012
 * @since laxcus 1.0
 */
public final class OnIndex extends WhereIndex implements Comparable<OnIndex> {

	private static final long serialVersionUID = 5769258321624960606L;

	/** 左/右 比较关系( >、<>、>、>=、<、<=) **/
	private byte compare;

	/** 左侧比较列 **/
	private Dock left;

	/** 右侧比较列 **/
	private Dock right;

	/**
	 * 根据传入的OnIndex实例，生成它的副本
	 * @param that
	 */
	protected OnIndex(OnIndex that) {
		super(that);
		setLeft(that.left);
		setCompare(that.compare);
		setRight(that.right);
	}

	/**
	 * 建立OnIndex实例，并且指定左侧列、比较运算符、右侧列 
	 * @param left - 左侧列空间
	 * @param compare - 比较运算符
	 * @param right - 右侧列空间
	 */
	public OnIndex(Dock left, byte compare, Dock right) {
		super(IndexType.ON_INDEX);
		setLeft(left);
		setCompare(compare);
		setRight(right);
	}

	/**
	 * 设置比较值
	 * @param who
	 */
	public void setCompare(byte who) {
		switch (who) {
		case CompareOperator.EQUAL:
		case CompareOperator.NOT_EQUAL:
		case CompareOperator.LESS:
		case CompareOperator.LESS_EQUAL:
		case CompareOperator.GREATER:
		case CompareOperator.GREATER_EQUAL:
			compare = who;
			break; // 以上都允许
		default:
			throw new IllegalValueException("illegal operator:%d", who);
		}
	}

	/**
	 * 返回比较值
	 * @return 比较值
	 */
	public byte getCompare() {
		return compare;
	}

	/**
	 * 设置左侧列空间
	 * @param e 列空间
	 */
	public void setLeft(Dock e) {
		Laxkit.nullabled(e);

		left = e;
	}

	/**
	 * 返回左侧列空间
	 * @return 列空间
	 */
	public Dock getLeft() {
		return left;
	}

	/**
	 * 设置右侧列空间
	 * @param e 列空间
	 */
	public void setRight(Dock e) {
		Laxkit.nullabled(e);

		right = e;
	}

	/**
	 * 返回右侧列空间
	 * @return 列空间
	 */
	public Dock getRight() {
		return right;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != OnIndex.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((OnIndex) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return left.hashCode() ^ compare ^ right.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(OnIndex that) {
		// 空值在前
		if (that == null) {
			return 1;
		}

		int ret = left.compareTo(that.left);
		if (ret == 0) {
			ret = Laxkit.compareTo(compare, that.compare);
		}
		if (ret == 0) {
			ret = right.compareTo(that.right);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.WhereIndex#duplicate()
	 */
	@Override
	public OnIndex duplicate() {
		return new OnIndex(this);
	}

	/**
	 * 此方法无效
	 * @see com.laxcus.access.index.WhereIndex#getColumnId()
	 */
	@Override
	public short getColumnId() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * 此方法无效
	 * @see com.laxcus.access.index.WhereIndex#setColumnId(short)
	 */
	@Override
	public void setColumnId(short id) {

	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int scale = writer.size();
		writer.write(compare);
		writer.writeObject(left);
		writer.writeObject(right);
		return writer.size() - scale;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int scale = reader.getSeek();

		compare = reader.read();
		left = new Dock(reader);
		right = new Dock(reader);

		return reader.getSeek() - scale;
	}

}