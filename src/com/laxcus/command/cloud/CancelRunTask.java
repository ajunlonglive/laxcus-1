/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 撤销运行分布式应用 <br><br>
 * 
 * 这个对象被放入ProductListener.push，通知接收端
 * 
 * @author scott.liang
 * @version 1.0 6/17/2022
 * @since laxcus 1.0
 */
public class CancelRunTask implements Classable, Cloneable, Serializable , Comparable<CancelRunTask>{
	
	private static final long serialVersionUID = 3852993996080146400L;

	/** 用户撤销 **/
	public static final int USER_CANCEL = 1;

	/** 系统撤销 **/
	public static final int SYSTEM_CANCEL = 2;

	/** 撤销类型 **/
	private int type;
	
	/**
	 * 构造默认的撤销运行分布式应用
	 */
	public CancelRunTask() {
		super();
	}

	/**
	 * 构造撤销运行分布式应用，指定分布任务组件类型 
	 * @param taskFamily 分布任务组件类型
	 */
	public CancelRunTask(int who) {
		this();
		setType(who);
	}

	/**
	 * 生成撤销运行分布式应用副本
	 * @param that 撤销运行分布式应用
	 */
	private CancelRunTask(CancelRunTask that) {
		this();
		type = that.type;
	}

	/**
	 * 从可类化读取器中解析撤销运行分布式应用
	 * @param reader 可类化数据读取器
	 */
	public CancelRunTask(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置撤销类型
	 * @param e
	 */
	public void setType(int who) {
		type = who;
	}
	
	/**
	 * 返回撤销类型
	 * @return
	 */
	public int getType() {
		return type;
	}
	

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();

		// 撤销类型
		writer.writeInt(type);
	

		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();

		
		// CALL节点地址
		type = reader.readInt();
		

		// 统计读取字节数
		return reader.getSeek() - seek;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return CancelRunTask实例
	 */
	public CancelRunTask duplicate() {
		return new CancelRunTask(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CancelRunTask.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((CancelRunTask) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CancelRunTask that) {
		if (that == null) {
			return 1;
		}

		return Laxkit.compareTo(type, that.type);
	}

}