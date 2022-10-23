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
 * ESTABLISH.SIFT阶段映像域 <br><br>
 * 
 * 这是ESTABLISH.SIFT产生的中间数据，返回给ESTABLISH.ASSIGN阶段处理。
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public class SiftField extends EstablishStubZone {

	private static final long serialVersionUID = -3985140716341451128L;

	/**
	 * 根据传入实例，生成一个SIFT阶段映像域浅层数据副本
	 * @param that SiftField实例
	 */
	private SiftField(SiftField that) {
		super(that);
	}

	/**
	 * 构造默认的SIFT阶段映像域
	 */
	public SiftField() {
		super();
	}

	/**
	 * 构造SIFT阶段映像域，指定数据构建标识
	 * @param flag 数据构建标识
	 */
	public SiftField(EstablishFlag flag) {
		this();
		setFlag(flag);
	}

	/**
	 * 从可类化数据读取器中解析和构造SIFT阶段映像域
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SiftField(ClassReader reader) {
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
	public SiftField duplicate() {
		return new SiftField(this);
	}

}