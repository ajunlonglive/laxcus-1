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
 * 显示限制操作单元
 * 
 * @author scott.liang
 * @version 1.0 3/28/2017
 * @since laxcus 1.0
 */
public class ShowLimit extends Command {

	private static final long serialVersionUID = -3272214468545500404L;

	/**
	 * 构造默认的显示限制操作单元
	 */
	public ShowLimit() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析显示限制操作单元
	 * @param reader 可类化数据读取器
	 */
	public ShowLimit(ClassReader reader) {
		this();
		this.resolve(reader);
	}
	
	/**
	 * 生成显示限制操作单元数据副本
	 * @param that ShowLimit实例
	 */
	private ShowLimit(ShowLimit that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShowLimit duplicate() {
		return new ShowLimit(this);
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