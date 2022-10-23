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
 * ESTABLISH.SCAN阶段映像数据域<br>
 * 
 * 这是ESTABLISH.SCAN分布任务组件实例产生的数据，返回给CALL/ESTABLISH.ASSIGN阶段处理。
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public class ScanField extends EstablishStubZone {

	private static final long serialVersionUID = 889918499997088351L;

	/**
	 * 根据传入实例，生成一个SCAN阶段映像数据域浅层数据副本
	 * @param that ScanField实例
	 */
	private ScanField(ScanField that) {
		super(that);
	}

	/**
	 * 构造默认的SCAN阶段映像数据域
	 */
	public ScanField() {
		super();
	}

	/**
	 * 构造SCAN阶段映像数据域，指定数据构建标识
	 * @param flag 数据构建标识
	 */
	public ScanField(EstablishFlag flag) {
		this();
		setFlag(flag);
	}

	/**
	 * 从可类化数据读取器中解析和构造SCAN阶段映像数据域
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ScanField(ClassReader reader) {
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
	public ScanField duplicate() {
		return new ScanField(this);
	}
	
}