/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.forbid;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 显示禁止操作单元
 * 
 * @author scott.liang
 * @version 1.0 4/1/2017
 * @since laxcus 1.0
 */
public class ShowForbid extends Command {

	private static final long serialVersionUID = -2161810942556614288L;

	/**
	 * 构造默认的显示禁止操作单元
	 */
	public ShowForbid() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析显示禁止操作单元
	 * @param reader 可类化数据读取器
	 */
	public ShowForbid(ClassReader reader) {
		this();
		this.resolve(reader);
	}
	
	/**
	 * 生成显示禁止操作单元数据副本
	 * @param that ShowForbid实例
	 */
	private ShowForbid(ShowForbid that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShowForbid duplicate() {
		return new ShowForbid(this);
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