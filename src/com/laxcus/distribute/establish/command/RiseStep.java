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
 * ESTABLISH.RISE阶段处理步骤。<br><br>
 * 
 * 在CALL站点产生，在DATA站点执行。从BUILD站点下载数据块，然后替换本地旧数据，并且发布。
 * 
 * @author scott.liang
 * @version 1.1 05/02/2015
 * @since laxcus 1.0
 */
public class RiseStep extends DistributedStep {

	private static final long serialVersionUID = 7728555538463330766L;

	/**
	 * 构造默认和私有的ESTABLISH.RISE阶段处理步骤。
	 */
	private RiseStep() {
		super();
	}

	/**
	 * 根据传入的ESTABLISH.RISE阶段处理步骤，生成它的数据副本
	 * @param that RiseStep实例
	 */
	private RiseStep(RiseStep that) {
		super(that);
	}

	/**
	 * 构造ESTABLISH.RISE阶段处理步骤，指定它的用户签名和RISE阶段会话
	 * @param issuer 操作者签名
	 * @param session RISE阶段会话
	 */
	public RiseStep(Siger issuer, RiseSession session) {
		this();
		setIssuer(issuer);
		setSession(session);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RiseStep(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeStep#getSession()
	 */
	@Override
	public RiseSession getSession() {
		return (RiseSession) super.getSession();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RiseStep duplicate() {
		return new RiseStep(this);
	}

}