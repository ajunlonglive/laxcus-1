/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rule;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 显示锁事务规则
 * 
 * @author scott.liang
 * @version 1.0 4/2/2017
 * @since laxcus 1.0
 */
public class ShowLockRule extends Command {

	private static final long serialVersionUID = -7056817910621260293L;

	/**
	 * 构造默认的显示事务规则
	 */
	public ShowLockRule() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析显示事务规则
	 * @param reader 可类化数据读取器
	 */
	public ShowLockRule(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成显示事务规则数据副本
	 * @param that ShowRule实例
	 */
	private ShowLockRule(ShowLockRule that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShowLockRule duplicate() {
		return new ShowLockRule(this);
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