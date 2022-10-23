/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.command;

import com.laxcus.util.classable.*;

/**
 * 停止边缘计算服务
 * 
 * @author scott.liang
 * @version 1.0 10/18/2020
 * @since laxcus 1.0
 */
public class TubStopService extends TubCommand {

	/** 进程ID，大于或者等于0  **/
	private long processId;

	/** 参数集合  **/
	private String arguments;

	/**
	 * 构造默认的停止边缘计算服务
	 */
	public TubStopService() {
		super();
	}

	/**
	 * 生成停止边缘计算服务的数据副本
	 * @param that 停止边缘计算服务实例
	 */
	private TubStopService(TubStopService that) {
		super(that);
		processId = that.processId;
		arguments = that.arguments;
	}

	/**
	 * 构造停止边缘计算服务，指定容器进程ID和参数集
	 * @param naming 容器进程ID，大于或者等于0
	 * @param args 参数集
	 */
	public TubStopService(long naming, String args) {
		this();
		setProcessId(naming);
		setArguments(args);
	}

	/**
	 * 从可类化数据读取器中解析停止边缘计算服务命令
	 * @param reader 可类化数据读取器
	 */
	public TubStopService(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置容器进程ID，大于或者等于0
	 * @param e Naming实例
	 */
	public void setProcessId(long e) {
		processId = e;
	}

	/**
	 * 返回容器进程ID，大于或者等于0
	 * @return Naming实例
	 */
	public long getProcessId() {
		return processId;
	}

	/**
	 * 设置参数集
	 * @param e String实例
	 */
	public void setArguments(String e) {
		arguments = e;
	}

	/**
	 * 返回参数集
	 * @return String实例
	 */
	public String getArguments() {
		return arguments;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.TubCommand#duplicate()
	 */
	@Override
	public TubStopService duplicate() {
		return new TubStopService(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.TubCommand#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d", processId);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.TubCommand#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(processId);
		writer.writeString(arguments);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.TubCommand#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		processId = reader.readLong();
		arguments = reader.readString();
	}

}