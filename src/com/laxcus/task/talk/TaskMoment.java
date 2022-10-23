/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.talk;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 分布任务组件瞬时状态。<br><br>
 * 
 * 分布任务组件瞬时状态由两个参数组成：当前状态，分布任务组件在队列中的编号。瞬时状态只对应某次检查，下次检查即可能发生变化。 <br>
 * 只有分布任务组件当前状态大于等于0，成员数目大于0时，分布任务组件瞬时状态才有效。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/23/2018
 * @since laxcus 1.0
 */
public final class TaskMoment implements Classable, Cloneable, Serializable, Comparable<TaskMoment> {

	private static final long serialVersionUID = -6598736458140554699L;

	/** 分布任务组件当前状态**/
	private int status;

	/** 分布任务组件在队列中的编号**/
	private int number;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 分布任务组件当前状态
		writer.writeInt(status);
		// 分布任务组件在队列中的编号
		writer.writeInt(number);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 分布任务组件当前状态
		status = reader.readInt();
		// 分布任务组件在队列中的编号
		number = reader.readInt();
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入参数，生成它的数据副本
	 * @param that TaskModel实例
	 */
	private TaskMoment(TaskMoment that) {
		super();
		status = that.status;
		number = that.number;
	}

	/**
	 * 构造默认和私有的分布任务组件瞬时状态
	 */
	public TaskMoment() {
		super();
		status = -1; // 无定义
		number = -1; // 无定义
	}

	/**
	 * 构造分布任务组件瞬时状态，指定分布任务组件当前状态和分布任务组件在队列中的编号
	 * @param status 分布任务组件当前状态
	 * @param number 分布任务组件在队列中的编号
	 */
	public TaskMoment(int status, int number) {
		this();
		setStatus(status);
		setNumber(number);
	}
	
	/**
	 * 从可类化数据读取器中解析分布任务组件瞬时状态
	 * @param reader 可类化数据读取器
	 */
	public TaskMoment(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置分布任务组件当前状态
	 * @param who 分布任务组件当前状态
	 */
	public void setStatus(int who) {
		if (!TaskStatus.isStatus(who)) {
			throw new IllegalValueException("illegal status:%d", who);
		}
		status = who;
	}

	/**
	 * 返回分布任务组件当前状态
	 * @return 分布任务组件当前状态
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * 判断是没有找到
	 * @return 返回真或者假
	 */
	public boolean isNotFound() {
		return TaskStatus.isNotFound(status);
	}

	/**
	 * 判断是命令的等待状态
	 * @return 返回真或者假
	 */
	public boolean isCommand() {
		return TaskStatus.isCommand(status);
	}

	/**
	 * 判断是调用器的运行状态
	 * @return 返回真或者假
	 */
	public boolean isInvoker() {
		return TaskStatus.isInvoker(status);
	}

	/**
	 * 设置分布任务组件在队列中的编号
	 * @param i 分布任务组件在队列中的编号
	 */
	public void setNumber(int i) {
		number = i;
	}

	/**
	 * 返回分布任务组件在队列中的编号
	 * @return 分布任务组件在队列中的编号
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * 判断有效
	 * @return 返回真或者假
	 */
	public boolean isValid() {
		return status >= 0 && number > 0;
	}

	/**
	 * 判断无效
	 * @return 返回真或者假
	 */
	public boolean isInvalid() {
		return !isValid();
	}
	
	/**
	 * 根据签名，定位它的下标位置
	 * @param siger 用户签名
	 * @return 返回下标位置，无效返回-1。
	 */
	public int locate(Siger siger) {
		if (isValid()) {
			return siger.mod(number);
		}
		return -1;
	}
	
	/**
	 * 根据签名，判断它属于这个站点坐标范围
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean allow(Siger siger) {
		boolean success = isValid();
		if (success) {
			success = (status == siger.mod(number));
		}
		return success;
	}
	
	/**
	 * 生成当前实例的数据副本
	 * @return TaskModel实例
	 */
	public TaskMoment duplicate() {
		return new TaskMoment(this);
	}

	/**
	 * 检查两个分布任务组件瞬时状态一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != TaskMoment.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((TaskMoment) that) == 0;
	}

	/**
	 * 返回分布任务组件瞬时状态的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (number == 0) {
			return status;
		} else {
			return status ^ number;
		}
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
	 * 返回分布任务组件瞬时状态的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d#%d", status, number);
	}

	/**
	 * 比较两个分布任务组件瞬时状态相同
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TaskMoment that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(status, that.status);
		if (ret == 0) {
			ret = Laxkit.compareTo(number, that.number);
		}
		return ret;
	}

}