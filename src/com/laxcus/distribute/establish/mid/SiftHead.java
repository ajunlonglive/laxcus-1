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
 * SIFT阶段请求单元。<br><br>
 * 
 * SIFT阶段请求单元由CALL.ASSIGN阶段分配，被BUILD.SIFT执行。
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public class SiftHead extends EstablishStubZone {

	private static final long serialVersionUID = -3985140716341451128L;

	/**
	 * 根据传入实例，生成一个SIFT阶段请求单元浅层数据副本
	 * @param that SiftHead实例
	 */
	private SiftHead(SiftHead that) {
		super(that);
	}

	/**
	 * 构造默认的SIFT阶段请求单元
	 */
	public SiftHead() {
		super();
	}

	/**
	 * 构造SIFT阶段请求单元，指定数据构建标识
	 * @param flag 数据构建标识
	 */
	public SiftHead(EstablishFlag flag) {
		this();
		setFlag(flag);
	}

	/**
	 * 从可类化数据读取器中解析和构造SIFT阶段请求单元
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SiftHead(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.establish.mid.EstablishStubField#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.establish.mid.EstablishStubField#resolveSuffix(com.laxcus.util.ClassReader)
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
	public SiftHead duplicate() {
		return new SiftHead(this);
	}

}