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
 * ESTABLISH.SCAN阶段处理步骤。<br>
 * 在CALL站点产生，在DATA站点执行。
 * 
 * @author scott.liang
 * @version 1.1 05/02/2015
 * @since laxcus 1.0
 */
public class ScanStep extends DistributedStep {

	private static final long serialVersionUID = 3240766266726315284L;

	/**
	 * 构造ESTABLISH.SCAN阶段处理步骤。
	 */
	private ScanStep() {
		super();
	}

	/**
	 * 根据传入的ESTABLISH.SCAN阶段处理步骤，生成它的数据副本
	 * @param that ESTABLISH.SCAN阶段处理步骤实例
	 */
	private ScanStep(ScanStep that) {
		super(that);
	}

	/**
	 * 构造ESTABLISH.SCAN阶段命令，指定它的用户签名和会话实例
	 * 
	 * @param issuer 发布者签名
	 * @param session SCAN阶段会话
	 */
	public ScanStep(Siger issuer, ScanSession session) {
		this();
		setIssuer(issuer);
		setSession(session);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ScanStep(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回SCAN阶段会话
	 * @see com.laxcus.distribute.DistributedStep#getSession()
	 */
	@Override
	public ScanSession getSession() {
		return (ScanSession) super.getSession();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ScanStep duplicate() {
		return new ScanStep(this);
	}

}