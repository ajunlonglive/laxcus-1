/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 运行分布式应用软件
 * 
 * @author scott.liang
 * @version 1.0 8/29/2020
 * @since laxcus 1.0
 */
public final class RunTask extends Command {

	private static final long serialVersionUID = -8886784865649108787L;

	/** 基础字 **/
	private Sock sock;
	
	/**
	 * 构造默认和私有的运行分布式应用软件
	 */
	private RunTask() {
		super();
	}

	/**
	 * 生成运行分布式应用软件副本
	 * @param that 运行分布式应用软件
	 */
	private RunTask(RunTask that) {
		super(that);
		sock = that.sock;
	}

	/**
	 * 构造运行分布式应用软件，指定实例
	 * @param sock 基础字
	 */
	public RunTask(Sock sock) {
		this();
		setSock(sock);
	}
	
	/**
	 * 从可类化数据读取器中解析运行分布式应用软件
	 * @param reader 可类化读取器
	 */
	public RunTask(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置基础字，不允许空指针
	 * @param e 基础字
	 */
	public void setSock(Sock e) {
		Laxkit.nullabled(e);
		sock = e;
	}

	/**
	 * 返回基础字
	 * @return 基础字对象
	 */
	public Sock getSock() {
		return sock;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RunTask duplicate() {
		return new RunTask(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(sock);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		sock = reader.readInstance(Sock.class);
	}

}
