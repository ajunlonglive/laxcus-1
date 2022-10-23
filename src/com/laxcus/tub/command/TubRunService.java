/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.command;

import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 启动边缘计算服务
 * 
 * @author scott.liang
 * @version 1.0 10/18/2020
 * @since laxcus 1.0
 */
public class TubRunService extends TubCommand {

	/** 命名  **/
	private Naming naming;
	
	/** 参数集合  **/
	private String arguments;

	/**
	 * 构造默认的启动边缘计算服务
	 */
	public TubRunService() {
		super();
	}

	/**
	 * 生成启动边缘计算服务的数据副本
	 * @param that 启动边缘计算服务实例
	 */
	private TubRunService(TubRunService that) {
		super(that);
		naming = that.naming;
		arguments = that.arguments;
	}

	/**
	 * 构造启动边缘计算服务，指定容器命名和参数集
	 * @param naming 容器命名
	 */
	public TubRunService(Naming naming) {
		this();
		setNaming(naming);
	}

	/**
	 * 构造启动边缘计算服务，指定容器命名和参数集
	 * @param naming 容器命名
	 * @param args 参数集
	 */
	public TubRunService(Naming naming, String args) {
		this(naming);
		setArguments(args);
	}

	/**
	 * 从可类化数据读取器中解析启动边缘计算服务命令
	 * @param reader 可类化数据读取器
	 */
	public TubRunService(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置容器命名
	 * @param e Naming实例
	 */
	public void setNaming(Naming e) {
		naming = e;
	}

	/**
	 * 返回容器命名
	 * @return Naming实例
	 */
	public Naming getNaming() {
		return naming;
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
	public TubRunService duplicate() {
		return new TubRunService(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.TubCommand#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s", naming);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.TubCommand#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(naming);
		writer.writeString(arguments);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.TubCommand#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		naming = reader.readInstance(Naming.class);
		arguments = reader.readString();
	}

}
