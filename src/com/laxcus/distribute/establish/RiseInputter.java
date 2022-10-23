/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * ESTABLISH.RISE阶段参数输入接口。<br>
 * 
 * 参数来自FRONT站点的用户输入。<br>
 * 
 * @author scott.liang
 * @version 1.1 7/26/2015
 * @since laxcus 1.0
 */
public final class RiseInputter extends EstablishInputter {

	private static final long serialVersionUID = 8141607024888937288L;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	protected void buildSuffix(ClassWriter writer) {
		// 输出前缀信息
		super.buildSuffix(writer);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	protected void resolveSuffix(ClassReader reader) {
		// 解析前缀信息
		super.resolveSuffix(reader);
	}

	/**
	 * 使用传入RISE输入接口，生成它的副本
	 * @param that RiseInputter实例
	 */
	private RiseInputter(RiseInputter that) {
		super(that);
	}

	/**
	 * 构造RISE阶段输入接口
	 */
	public RiseInputter() {
		super();
	}

	/**
	 * 构造RISE阶段输入接口，并且指定它的命名(必须是RISE阶段)
	 * @param phase 阶段命名
	 */
	public RiseInputter(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RiseInputter(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeObject#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger username) {
		super.setIssuer(username);
	}

	/**
	 * 生成RISE输入命名阶段对象的副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public RiseInputter duplicate() {
		return new RiseInputter(this);
	}
}