/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.rule;

import java.io.*;

import com.laxcus.law.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 事务规则标识
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public final class RuleFlag implements Serializable, Cloneable, Classable, Comparable<RuleFlag> {

	private static final long serialVersionUID = -37393186750969370L;

	/** 事务处理级别 **/
	private byte rank;

	/** 事务操作符 **/
	private byte operator;

	/**
	 * 生成事务规则标识数据副本
	 * @param that RuleFlag实例
	 */
	private RuleFlag(RuleFlag that) {
		super();
		rank = that.rank;
		operator = that.operator;
	}

	/**
	 * 构造默认的事务规则标识
	 */
	private RuleFlag() {
		super();
	}

	/**
	 * 构造事务规则标识，指定参数
	 * @param rank 事务处理级别
	 * @param operator 事务操作符
	 */
	public RuleFlag(byte rank, byte operator) {
		this();
		setRank(rank);
		setOperator(operator);
	}

	/**
	 * 从可类化数据读取器中解析事务规则标识
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RuleFlag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置事务操作符
	 * @param who 事务操作符
	 */
	private void setOperator(byte who) {
		if (!RuleOperator.isOperator(who)) {
			throw new IllegalValueException("illegal operator:%d", who);
		}
		operator = who;
	}

	/**
	 * 返回事务操作符
	 * @return 事务操作符
	 */
	public byte getOperator() {
		return operator;
	}

	/**
	 * 返回事务操作符的文本描述
	 * @return 事务操作符的字符串
	 */
	public String getOperatorText() {
		return RuleOperator.translate(operator);
	}

	/**
	 * 设置事务级别
	 * @param who 事务级别
	 */
	private void setRank(byte who) {
		if(!LawRank.isRank(who)) {
			throw new IllegalValueException("illegal rank:%d", who);
		}
		rank = who;
	}

	/**
	 * 返回事务级别
	 * @return 事务级别
	 */
	public byte getRank() {
		return rank;
	}

	/**
	 * 返回事务级别的文本描述
	 * @return 事务级别的文本描述
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
	 * 判断是行级锁
	 * @return 返回真或者假
	 */
	public boolean isRowRank(){
		return LawRank.isRow(rank);
	}

	/**
	 * 返回当前实例的数据副本
	 * @return RuleFlag实例
	 */
	public RuleFlag duplicate() {
		return new RuleFlag(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != RuleFlag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((RuleFlag) that) == 0;
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
	public int compareTo(RuleFlag that) {
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