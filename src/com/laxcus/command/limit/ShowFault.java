/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.limit;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 显示锁定单元
 * 
 * @author scott.liang
 * @version 1.0 3/28/2017
 * @since laxcus 1.0
 */
public class ShowFault extends Command {

	private static final long serialVersionUID = -5656542238554994979L;

	/**
	 * 构造默认的显示锁定单元
	 */
	public ShowFault() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析显示锁定单元
	 * @param reader 可类化数据读取器
	 */
	public ShowFault(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成显示锁定单元数据副本
	 * @param that ShowFault实例
	 */
	private ShowFault(ShowFault that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShowFault duplicate() {
		return new ShowFault(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// TODO Auto-generated method stub

	}

}