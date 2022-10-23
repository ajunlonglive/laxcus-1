/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.conduct.session;

import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;
import com.laxcus.site.Node;

/**
 * CONDUCT.FROM阶段会话。<br>
 * 
 * FROM阶段会话产生后续计算需要的数据。<br>
 * 
 * @author scott.liang
 * @version 1.2 7/17/2015
 * @since laxcus 1.0
 */
public final class FromSession extends ConductSession {

	private static final long serialVersionUID = 4152632912862490457L;

	/*
	 * 将FROM阶段会话参数写入可类化存储器
	 * @see com.laxcus.distribute.conduct.session.ConductSession#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 前缀信息写入
		super.buildSuffix(writer);
	}

	/*
	 * 从可类化读取器中读取FROM阶段会话参数
	 * @see com.laxcus.distribute.conduct.session.ConductSession#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析前缀信息
		super.resolveSuffix(reader);
	}

	/**
	 * 根据传入的FROM阶段会话，生成它的副本
	 * @param that FromSession实例
	 */
	private FromSession(FromSession that) {
		super(that);
	}

	/**
	 * 构造FROM阶段会话
	 */
	public FromSession() {
		super(PhaseTag.FROM);
	}

	/**
	 * 构造FROM阶段会话，指定阶段命名
	 * @param phase 阶段命名
	 */
	public FromSession(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 构造FROM阶段会话，指定阶段命名和目标地址
	 * 
	 * @param phase 阶段命名
	 * @param remote 目标地址
	 */
	public FromSession(Phase phase, Node remote) {
		this(phase);
		setRemote(remote);
	}

	/**
	 * 从可类化读取器中解析FROM阶段会话参数
	 * @param reader 可类化数据读取器
	 * @since 1.2
	 */
	public FromSession(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成FROM阶段会话对象的副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public FromSession duplicate() {
		return new FromSession(this);
	}


}