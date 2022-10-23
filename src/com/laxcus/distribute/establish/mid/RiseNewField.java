/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.mid;

import com.laxcus.util.classable.*;

/**
 * ESTABLISH.RISE阶段数据块更新域<br>
 * 
 * 数据在DATA节点的ESTABLISH.RISE阶段产生，返回给CALL.ESTABLISH调用器，再转发给FRONT.END阶段显示。
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public class RiseNewField extends EstablishStubZone {

	private static final long serialVersionUID = 5658385444891804748L;

	/**
	 * 根据传入的ESTABLISH.RISE阶段数据块更新域实例，生成它的浅层数据副本
	 * @param that ESTABLISH.RISE阶段数据块更新域实例
	 */
	private RiseNewField(RiseNewField that) {
		super();
	}

	/**
	 * 构造默认的ESTABLISH.RISE阶段数据块更新域
	 */
	public RiseNewField() {
		super();
	}

	/**
	 * 构造RISE阶段数据块更新域，指定数据构建标识
	 * @param flag 数据构建标识
	 */
	public RiseNewField(EstablishFlag flag) {
		this();
		setFlag(flag);
	}

	/**
	 * 从可类化数据读取器中解析和构造RISE阶段数据块更新域
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RiseNewField(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.establish.mid.EstablishStepField#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.establish.mid.EstablishStepField#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MiddleZone#duplicate()
	 */
	@Override
	public RiseNewField duplicate() {
		return new RiseNewField(this);
	}

}