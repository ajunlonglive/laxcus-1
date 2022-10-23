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
 * CONDUCT.FROM阶段处理步骤。<br>
 * 在CALL站点产生，在DATA站点执行。
 * 
 * @author scott.liang
 * @version 1.1 05/02/2015
 * @since laxcus 1.0
 */
public class FromStep extends DistributedStep {

	private static final long serialVersionUID = -9214846456288113341L;

	/**
	 * 构造CONDUCT.FROM阶段处理步骤。
	 */
	private FromStep() {
		super();
	}

	/**
	 * 根据传入的CONDUCT.FROM阶段处理步骤实例，生成它的数据副本
	 * @param that CONDUCT.FROM阶段处理步骤实例
	 */
	private FromStep(FromStep that) {
		super(that);
	}

	/**
	 * 构造CONDUCT.FROM阶段处理步骤，指定操作人和FROM阶段会话
	 * @param issuer 操作人签名
	 * @param session FROM阶段会话
	 */
	public FromStep(Siger issuer, FromSession session) {
		this();
		setIssuer(issuer);
		setSession(session);
	}

	/**
	 * 构造CONDUCT.FROM阶段处理步骤，指定操作人、FROM阶段会话、内存处理模式
	 * @param issuer 操作人签名
	 * @param session FROM阶段会话
	 * @param memory 内存处理模式
	 */
	public FromStep(Siger issuer, FromSession session, boolean memory) {
		this(issuer, session);
		setMemory(memory);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FromStep(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回CONDUCT.FROM阶段会话
	 * @see com.laxcus.distribute.DistributedStep#getSession()
	 */
	@Override
	public FromSession getSession() {
		return (FromSession) super.getSession();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FromStep duplicate() {
		return new FromStep(this);
	}

}