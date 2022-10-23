/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish;

import com.laxcus.distribute.*;
import com.laxcus.util.classable.*;

/**
 * ESTABLISH阶段对象的参数输入器。<br>
 * 
 * ESTABLISH输入器的数据来源于FRONT节点的用户输入，在“ISSUE”阶段使用、分析、判断。
 * 
 * @author scott.liang
 * @version 1.1 7/26/2015
 * @since laxcus 1.0
 */
public abstract class EstablishInputter extends AccessObject {

	private static final long serialVersionUID = 6728782041847374233L;

	/**
	 * 构造一个默认的ESTABLISH参数输入器
	 */
	protected EstablishInputter() {
		super();
	}

	/**
	 * 根据传入的ESTABLISH参数输入器，生成它的对象副本
	 * @param that EstablishInputter实例
	 */
	protected EstablishInputter(EstablishInputter that) {
		super(that);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.AccessObject#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 生成前缀
		super.buildSuffix(writer);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.AccessObject#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析前缀
		super.resolveSuffix(reader);
	}

}