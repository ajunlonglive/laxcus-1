/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.contact.command;

import com.laxcus.distribute.*;
import com.laxcus.distribute.contact.session.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * CONTACT.DISTANT阶段处理步骤。<br>
 * 在CALL站点产生，分发到WORK站点执行。
 * 
 * @author scott.liang
 * @version 1.0 05/09/2020
 * @since laxcus 1.0
 */
public class DistantStep extends DistributedStep {

	private static final long serialVersionUID = 8145698775722153069L;

	/**
	 * 构造CONTACT.DISTANT阶段处理步骤。
	 */
	private DistantStep() {
		super();
	}

	/**
	 * 根据传入的CONTACT.DISTANT阶段处理步骤，生成它的数据副本
	 * @param that CONTACT.DISTANT阶段处理步骤实例
	 */
	private DistantStep(DistantStep that) {
		super(that);
	}

	/**
	 * 构造CONTACT.DISTANT阶段处理步骤，指定操作人和DISTANT阶段会话
	 * @param issuer 命令操作人签名
	 * @param session DISTANT阶段会话
	 */
	public DistantStep(Siger issuer, DistantSession session) {
		this();
		setIssuer(issuer);
		setSession(session);
	}

	/**
	 * 构造CONTACT.DISTANT阶段处理步骤，指定操作人、DISTANT阶段会话、内存处理模式
	 * @param issuer 命令操作人签名
	 * @param session DISTANT阶段会话
	 * @param memory 内存处理模式
	 */
	public DistantStep(Siger issuer, DistantSession session, boolean memory) {
		this(issuer, session);
		setMemory(memory);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 */
	public DistantStep(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回CONTACT.DISTANT阶段会话
	 * @see com.laxcus.distribute.DistributedStep#getSession()
	 */
	@Override
	public DistantSession getSession() {
		return (DistantSession) super.getSession();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DistantStep duplicate() {
		return new DistantStep(this);
	}

}