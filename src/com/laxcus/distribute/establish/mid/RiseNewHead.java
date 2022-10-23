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
 * RISE阶段更新数据域 <br><br>
 * 
 * RISE阶段更新数据域由CALL.ASSIGN阶段分配，被DATA.RISE执行。<br>
 * RISE更新数据域的源头是BUILD站点。随RiseSession发送到DATA站点执行。
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public class RiseNewHead extends EstablishStubZone {

	private static final long serialVersionUID = 3034230734336275205L;

	/**
	 * 根据传入实例，生成一个RISE阶段更新数据域浅层数据副本
	 * @param that RiseNewHead实例
	 */
	private RiseNewHead(RiseNewHead that) {
		super(that);
	}

	/**
	 * 构造默认的RISE阶段更新数据域
	 */
	public RiseNewHead() {
		super();
	}

	/**
	 * 构造RISE阶段更新数据域，指定数据构建标识
	 * @param flag 数据构建标识
	 */
	public RiseNewHead(EstablishFlag flag) {
		this();
		setFlag(flag);
	}

	/**
	 * 从可类化数据读取器中解析和构造RISE阶段更新数据域
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RiseNewHead(ClassReader reader) {
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
	public RiseNewHead duplicate() {
		return new RiseNewHead(this);
	}

}