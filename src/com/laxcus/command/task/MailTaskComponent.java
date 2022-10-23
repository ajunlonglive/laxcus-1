/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 投递分布任务组件包 <br><br>
 * 
 * 这个命令由FRONT节点投递给GATE站点，GATE投递给ACCOUNT站点。<br><br>
 * 
 * 流程：<br>
 * 1. FRONT -> GATE <br>
 * 2. GATE -> ACCOUNT <br>
 * 3. ACCOUNT -> DATA/WORK/BUILD/CALL<br>
 * 
 * @author scott.liang
 * @version 1.0 10/10/2019
 * @since laxcus 1.0
 */
public class MailTaskComponent extends Command {

	private static final long serialVersionUID = -343366300261783211L;
	
	/**
	 * 构造默认投递分布任务组件包
	 */
	public MailTaskComponent() {
		super();
	}

	/**
	 * 构造投递分布任务组件包的数据副本
	 * @param that 投递分布任务组件包
	 */
	private MailTaskComponent(MailTaskComponent that) {
		super(that);
	}

	/**
	 * 从可类化读取器中解析投递分布任务组件包
	 * @param reader 可类化数据读取器
	 */
	public MailTaskComponent(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public MailTaskComponent duplicate() {
		return new MailTaskComponent(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {

	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {

	}

}