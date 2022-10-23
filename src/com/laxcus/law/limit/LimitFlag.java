/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.limit;

import java.io.*;

import com.laxcus.law.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 限制请求规则标识
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public final class LimitFlag implements Serializable, Cloneable, Classable, Markable, Comparable<LimitFlag> {

	private static final long serialVersionUID = 3654961745324745009L;

	/** 限制请求级别 **/
	private byte rank;

	/** 限制操作符 **/
	private byte operator;

	/**
	 * 生成限制请求规则标识数据副本
	 * @param that LimitFlag实例
	 */
	private LimitFlag(LimitFlag that) {
		super();
		rank = that.rank;
		operator = that.operator;
	}

	/**
	 * 构造默认的限制请求规则标识
	 */
	private LimitFlag() {
		super();
	}

	/**
	 * 构造限制请求规则标识，指定参数
	 * @param rank 限制请求级别
	 * @param operator 限制操作符
	 */
	public LimitFlag(byte rank, byte operator) {
		this();
		setRank(rank);
		setOperator(operator);
	}

	/**
	 * 从可类化数据读取器中解析限制请求规则标识
	 * @param reader 可类化数据读取器
	 */
	public LimitFlag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出限制请求规则标识
	 * @param reader 标记化读取器
	 */
	public LimitFlag(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置限制操作符
	 * @param who 限制操作符
	 */
	private void setOperator(byte who) {
		if (!LimitOperator.isOperator(who)) {
			throw new IllegalValueException("illegal operator:%d", who);
		}
		operator = who;
	}

	/**
	 * 返回限制操作符
	 * @return 限制操作符
	 */
	public byte getOperator() {
		return operator;
	}

	/**
	 * 返回限制操作符的文本描述
	 * @return 限制操作符的字符串
	 */
	public String getOperatorText() {
		return LimitOperator.translate(operator);
	}

	/**
	 * 设置限制级别
	 * @param who 限制级别
	 */
	private void setRank(byte who) {
		if(!LawRank.isRank(who)) {
			throw new IllegalValueException("illegal rank:%d", who);
		}
		rank = who;
	}

	/**
	 * 返回限制级别
	 * @return 限制级别
	 */
	public byte getRank() {
		return rank;
	}

	/**
	 * 返回限制级别的文本描述
	 * @return 限制级别的文本描述
	 */
	public String getRankText() {
		return LawRank.translate(rank);
	}

	/**
	 * 判断是用户级别
	 * @return 返回真或者假
	 */
	public boolean isUserRank() {
		return LawRank.isUser(rank);
	}

	/**
	 * 判断是数据库级别
	 * @return 返回真或者假
	 */
	public boolean isSchemaRank() {
		return LawRank.isSchema(rank);
	}

	/**
	 * 判断是表级别
	 * @return 返回真或者假
	 */
	public boolean isTableRank() {
		return LawRank.isTable(rank);
	}

	/**
	 * 返回当前实例的数据副本
	 * @return LimitFlag实例
	 */
	public LimitFlag duplicate() {
		return new LimitFlag(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != LimitFlag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((LimitFlag) that) == 0;
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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return rank ^ operator;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(LimitFlag that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(rank, that.rank);
		if (ret == 0) {
			ret = Laxkit.compareTo(operator, that.operator);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s/%s", getRankText(), getOperatorText());
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.write(rank);
		writer.write(operator);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		rank = reader.read();
		operator = reader.read();
		return reader.getSeek() - seek;
	}

}