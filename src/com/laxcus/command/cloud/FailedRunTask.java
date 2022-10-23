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
 * 中止运行分布式应用 <br><br>
 * 
 * 对象被放入ProductListener.push，通知接收端
 * 
 * @author scott.liang
 * @version 1.0 6/17/2022
 * @since laxcus 1.0
 */
public class FailedRunTask implements Classable, Cloneable, Serializable , Comparable<FailedRunTask>{
	
	private static final long serialVersionUID = 3852993996080146400L;

	/** 参数故障 **/
	public static final int PARAM_FAILED = 1;

	/** 系统故障 **/
	public static final int SYSTEM_FAILED = 2;
	
	/** 没有找到组件 **/
	public static final int NOTFOUND = 3;
	
	public static final int RUNTIME_ERROR = 4;

	/** 中止类型 **/
	private int type;
	
	/**
	 * 构造默认的中止运行分布式应用
	 */
	public FailedRunTask() {
		super();
	}

	/**
	 * 构造中止运行分布式应用，指定分布任务组件类型 
	 * @param taskFamily 分布任务组件类型
	 */
	public FailedRunTask(int who) {
		this();
		setType(who);
	}

	/**
	 * 生成中止运行分布式应用副本
	 * @param that 中止运行分布式应用
	 */
	private FailedRunTask(FailedRunTask that) {
		this();
		type = that.type;
	}

	/**
	 * 从可类化读取器中解析中止运行分布式应用
	 * @param reader 可类化数据读取器
	 */
	public FailedRunTask(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置中止类型
	 * @param e
	 */
	public void setType(int who) {
		type = who;
	}
	
	/**
	 * 返回中止类型
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

		// 中止类型
		writer.writeInt(type);
	
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		type = reader.readInt();
		// 统计读取字节数
		return reader.getSeek() - seek;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return FailedRunTask实例
	 */
	public FailedRunTask duplicate() {
		return new FailedRunTask(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != FailedRunTask.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((FailedRunTask) that) == 0;
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
	public int compareTo(FailedRunTask that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(type, that.type);
	}

}