/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.command;

import com.laxcus.distribute.*;
import com.laxcus.distribute.establish.session.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * ESTABLISH.SIFT阶段处理步骤。<br>
 * 在CALL站点产生，在BUILD站点执行。
 * 
 * @author scott.liang
 * @version 1.1 05/02/2015
 * @since laxcus 1.0
 */
public class SiftStep extends DistributedStep {

	private static final long serialVersionUID = 5330481073437273335L;

	/**
	 * 构造ESTABLISH.SIFT阶段处理步骤。
	 */
	private SiftStep() {
		super();
	}

	/**
	 * 根据传入的ESTABLISH.SIFT阶段处理步骤，生成它的数据副本
	 * @param that SiftStep实例
	 */
	private SiftStep(SiftStep that) {
		super(that);
	}

	/**
	 * 构造ESTABLISH.SIFT阶段处理步骤，指定它的用户签名和会话实例
	 * @param issuer 发布者签名
	 * @param session SIFT阶段会话
	 */
	public SiftStep(Siger issuer,SiftSession session) {
		this();
		setIssuer(issuer);
		setSession(session);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SiftStep(ClassReader reader) {
		this();
		super.resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeStep#getSession()
	 */
	@Override
	public SiftSession getSession() {
		return (SiftSession) super.getSession();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SiftStep duplicate() {
		return new SiftStep(this);
	}

}