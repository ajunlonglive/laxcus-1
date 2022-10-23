/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 用户消费单元
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public final class UserCostItem implements Classable, Cloneable, Serializable, Comparable<UserCostItem> {

	private static final long serialVersionUID = -6293384810197643693L;

	/** 节点类型 **/
	private byte family;
	
	/** 调用器编号 **/
	private long invokerId;
	
	/** 迭代数 **/
	private int iterateIndex;
	
	/** 命令 **/
	private String command;

	/** 时间 **/
	private long initTime, endTime;
	
	/** 未使用容量 **/
	private long processTime;
	
	/**
	 * 构造默认的被刷新处理单元
	 */
	public UserCostItem() {
		super();
		family = 0;
		invokerId = 0;
		iterateIndex = 0;
		command = null;
		initTime = 0;
		endTime = 0;
		processTime = 0;
	}

	/**
	 * 根据传入实例，生成用户消费单元的数据副本
	 * @param that UserCostItem实例
	 */
	private UserCostItem(UserCostItem that) {
		super();
		family = that.family;
		invokerId = that.invokerId;
		iterateIndex = that.iterateIndex;
		command = that.command;
		initTime = that.initTime;
		endTime = that.endTime;
		processTime = that.processTime;
	}

	/**
	 * 从可类化数据读取器中用户消费单元
	 * @param reader 可类化数据读取器
	 */
	public UserCostItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置节点类型
	 * @param who
	 */
	public void setFamily(byte who) {
		family = who;
	}

	/**
	 * 返回节点类型
	 * @return
	 */
	public byte getFamily() {
		return family;
	}

	/**
	 * 设置调用器编号
	 * @param id 编号
	 */
	public void setInvokerId(long id) {
		invokerId = id;
	}

	/**
	 * 返回调用器编号
	 * @return 毫秒
	 */
	public long getInvokerId() {
		return invokerId;
	}
	
	/**
	 * 设置迭代次数
	 * @param i
	 */
	public void setIterateIndex(int i) {
		iterateIndex = i;
	}

	/**
	 * 返回迭代次数
	 * @return
	 */
	public int getIterateIndex() {
		return iterateIndex;
	}

	/**
	 * 设置命令
	 * @param s
	 */
	public void setCommand(String s) {
		command = s;
	}

	/**
	 * 返回命令
	 * @return
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * 设置初始时间
	 * @param ms
	 */
	public void setInitTime(long ms) {
		initTime = ms;
	}

	/**
	 * 返回初始时间
	 * @return
	 */
	public long getInitTime() {
		return initTime;
	}

	/**
	 * 设置结束时间
	 * @param ms
	 */
	public void setEndTime(long ms) {
		endTime = ms;
	}

	/**
	 * 返回结束时间
	 * @return
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * 设置有效工作时间
	 * @param ms 毫秒
	 */
	public void setProcessTime(long ms) {
		processTime = ms;
	}

	/**
	 * 返回有效工作时间
	 * @return 毫秒
	 */
	public long getProcessTime() {
		return processTime;
	}
	
	/**
	 * 生成当前实例的数据副本
	 * @return UserCostItem实例
	 */
	public UserCostItem duplicate() {
		return new UserCostItem(this);
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
		return (int) ((invokerId >>> 32) ^ invokerId);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || getClass() != that.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((UserCostItem ) that) == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(UserCostItem that) {
		int ret = Laxkit.compareTo(family, that.family);
		if (ret == 0) {
			ret = Laxkit.compareTo(initTime, that.initTime);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(command, that.command, false);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(invokerId, that.invokerId);
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
	 * 保存参数
	 * @param writer 
	 */
	protected void buildSuffix(ClassWriter writer) {
		writer.write(family);
		writer.writeLong(invokerId);
		writer.writeInt(iterateIndex);
		writer.writeString(command);
		
		writer.writeLong(initTime);
		writer.writeLong(endTime);
		writer.writeLong(processTime);
	}

	/**
	 * 解析参数
	 * @param reader
	 */
	protected void resolveSuffix(ClassReader reader) {
		family = reader.read();
		invokerId = reader.readLong();
		iterateIndex = reader.readInt();
		command = reader.readString();

		initTime = reader.readLong();
		endTime = reader.readLong();
		processTime = reader.readLong();
	}

}