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
import com.laxcus.util.datetime.*;
import com.laxcus.util.markable.*;

/**
 * 资源共享标识
 * 
 * @author scott.liang
 * @version 1.0 8/16/2017
 * @since laxcus 1.0
 */
public final class CrossFlag implements Serializable, Cloneable, Classable, Markable, Comparable<CrossFlag> {

	private static final long serialVersionUID = 1084509711644552208L;

	/** 数据表名 **/
	private Space space;

	/** 共享资源操作符 **/
	private int operator;
	
	/** 建立时间 **/
	private long createTime;

	/**
	 * 生成当前资源共享标识的数据副本
	 * @param that CrossFlag实例
	 */
	private CrossFlag(CrossFlag that) {
		super();
		space = that.space.duplicate();
		operator = that.operator;
		createTime = that.createTime;
	}

	/**
	 * 构造默认的资源共享标识
	 */
	private CrossFlag() {
		super();
		refreshCreateTime();
	}

	/**
	 * 构造资源共享标识，指定参数
	 * @param space 数据表名
	 * @param operator 共享资源操作符
	 */
	public CrossFlag(Space space, int operator) {
		this();
		setSpace(space);
		setOperator(operator);
	}

	/**
	 * 从可类化数据读取器中解析资源共享标识
	 * @param reader 可类化数据读取器
	 */
	public CrossFlag(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出资源共享标识
	 * @param reader 标记化读取器
	 */
	public CrossFlag(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 判断被操作标识符原标识要求
	 * @param that 被比较共享标识
	 * @return 允许返回真，否则假
	 */
	public boolean allow(CrossFlag that) {
		boolean success = (Laxkit.compareTo(space, that.space) == 0);
		if (success) {
			success = CrossOperator.allow(operator, that.operator);
		}
		return success;
	}

	/**
	 * 设置共享资源操作符
	 * @param who 共享资源操作符
	 */
	public void setOperator(int who) {
		if (!CrossOperator.isOperator(who)) {
			throw new IllegalValueException("illegal operator:%d", who);
		}
		operator = who;
	}

	/**
	 * 返回共享资源操作符
	 * @return 共享资源操作符
	 */
	public int getOperator() {
		return operator;
	}

	/**
	 * 返回操作符的文本描述
	 * @return String
	 */
	public String getOperatorText() {
		return CrossOperator.translate(operator);
	}

	/**
	 * 设置数据表名
	 * @param e 数据表名
	 */
	private void setSpace(Space e) {
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
	 * 判断是SELECT操作
	 * @return 返回真或者假
	 */
	public boolean isSelect() {
		return CrossOperator.isSelect(operator);
	}

	/**
	 * 判断是INSERT操作
	 * @return 返回真或者假
	 */
	public boolean isInsert() {
		return CrossOperator.isInsert(operator);
	}

	/**
	 * 判断是DELETE操作
	 * @return 返回真或者假
	 */
	public boolean isDelete() {
		return CrossOperator.isDelete(operator);
	}
	
	/**
	 * 判断是UPDATE操作
	 * @return 返回真或者假
	 */
	public boolean isUpdate() {
		return CrossOperator.isUpdate(operator);
	}
	
	/**
	 * 判断是SELECT, INSERT, DELETE, UPDATE操作
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return CrossOperator.isAll(operator);
	}
	
	/**
	 * 更新建立时间
	 */
	public void refreshCreateTime() {
		createTime = SimpleTimestamp.currentTimeMillis();
	}

	/**
	 * 返回建立时间
	 * @return
	 */
	public long getCreateTime() {
		return createTime;
	}

	/**
	 * 返回当前实例的数据副本
	 * @return CrossFlag实例
	 */
	public CrossFlag duplicate() {
		return new CrossFlag(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CrossFlag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((CrossFlag) that) == 0;
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
		return space.hashCode() ^ operator;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CrossFlag that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(space, that.space);
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
		return String.format("%s#%s", space, getOperatorText());
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(space);
		writer.writeInt(operator);
		writer.writeLong(createTime);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		space = new Space(reader);
		operator = reader.readInt();
		createTime = reader.readLong();
		return reader.getSeek() - seek;
	}

}