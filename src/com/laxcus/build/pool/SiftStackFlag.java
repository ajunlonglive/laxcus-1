/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.pool;

import java.io.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * SIFT存取堆栈标识<br>
 * 
 * SIFT存取堆栈标识由EchoInvoker的工作编号（JOBID），和堆栈的任务编号组成，以此建立与EchoInvoker的对应关系，以及判断堆栈的唯一性。
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class SiftStackFlag implements Classable, Cloneable, Serializable, Comparable<SiftStackFlag> {
	
	private static final long serialVersionUID = -7832475351686400072L;

	/** 异步调用器编号 **/
	private long invokerId;

	/** SiftStack任务编号 **/
	private long taskId;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 异步调用器编号
		writer.writeLong(invokerId);
		// SiftStack任务编号
		writer.writeLong(taskId);
		// 返回写入的字节长度
		return writer.size() - scale;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 异步调用器编号
		invokerId = reader.readLong();
		// SiftStack任务编号
		taskId = reader.readLong();
		// 返回读取的字节长度
		return reader.getSeek() - scale;
	}

	/**
	 * 构造一个默认和私有的SIFT存取堆栈标识
	 */
	private SiftStackFlag() {
		super();
		invokerId = InvokerIdentity.INVALID;
		taskId = -1L;
	}

	/**
	 * 根据传入的SIFT存取堆栈标识，生成它的数据副本
	 * @param that - SIFT存取堆栈标识
	 */
	private SiftStackFlag(SiftStackFlag that) {
		super();
		invokerId = that.invokerId;
		taskId = that.taskId;
	}

	/**
	 * 构造SIFT存取堆栈标识，指定工作编号和SiftStack任务编号
	 * @param invokerId - 工作编号
	 * @param taskId - SiftStack任务编号
	 */
	public SiftStackFlag(long invokerId, long taskId) {
		this();
		setInvokerId(invokerId);
		setTaskId(taskId);
	}

	/**
	 * 从可类化读取器中解析SIFT存取堆栈标识参数
	 * @param reader
	 */
	public SiftStackFlag(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 设置异步调用器编号
	 * @param who
	 */
	public void setInvokerId(long who) {
		if (InvokerIdentity.isInvalid(who)) {
			throw new IllegalValueException("illegal invoker identity %d", who);
		}
		invokerId = who;
	}

	/**
	 * 返回异步调用器编号
	 * @return
	 */
	public long getInvokerId() {
		return invokerId;
	}

	/**
	 * 设置SiftStack任务编号
	 * @param i
	 */
	public void setTaskId(long i) {
		taskId = i;
	}

	/**
	 * 返回SiftStack任务编号
	 * @return
	 */
	public long getTaskId() {
		return taskId;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return - StackFlag实例
	 */
	public SiftStackFlag duplicate() {
		return new SiftStackFlag(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SiftStackFlag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SiftStackFlag) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (invokerId ^ taskId);
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
	 * 返回SIFT存取堆栈标识的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d#%d", invokerId, taskId);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SiftStackFlag that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(invokerId, that.invokerId);
		if (ret == 0) {
			ret = Laxkit.compareTo(taskId, that.taskId);
		}
		return ret;
	}
}