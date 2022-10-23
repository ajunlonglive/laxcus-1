/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.conduct.command;

import com.laxcus.distribute.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * CONDUCT.TO阶段处理步骤。<br>
 * 在CALL站点产生，在WORK站点执行。
 * 
 * @author scott.liang
 * @version 1.1 05/02/2015
 * @since laxcus 1.0
 */
public class ToStep extends DistributedStep {

	private static final long serialVersionUID = 8145698775722153069L;

	/**
	 * 构造CONDUCT.TO阶段处理步骤。
	 */
	private ToStep() {
		super();
	}

	/**
	 * 根据传入的CONDUCT.TO阶段处理步骤，生成它的数据副本
	 * @param that CONDUCT.TO阶段处理步骤实例
	 */
	private ToStep(ToStep that) {
		super(that);
	}

	/**
	 * 构造CONDUCT.TO阶段处理步骤，指定操作人和TO阶段会话
	 * @param issuer 命令操作人签名
	 * @param session TO阶段会话
	 */
	public ToStep(Siger issuer, ToSession session) {
		this();
		setIssuer(issuer);
		setSession(session);
	}

	/**
	 * 构造CONDUCT.TO阶段处理步骤，指定操作人、TO阶段会话、内存处理模式
	 * @param issuer 命令操作人签名
	 * @param session TO阶段会话
	 * @param memory 内存处理模式
	 */
	public ToStep(Siger issuer, ToSession session, boolean memory) {
		this(issuer, session);
		setMemory(memory);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ToStep(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回CONDUCT.TO阶段会话
	 * @see com.laxcus.distribute.DistributedStep#getSession()
	 */
	@Override
	public ToSession getSession() {
		return (ToSession) super.getSession();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ToStep duplicate() {
		return new ToStep(this);
	}

}