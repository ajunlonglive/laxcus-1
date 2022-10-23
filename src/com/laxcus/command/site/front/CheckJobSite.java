/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.front;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 显示相关的工作任务节点，包括CALL、WORK、DATA、BUILD类节点。<br><br>
 * 
 * 这个命令只在FRONT节点使用，发向CALL节点。
 * 
 * @author scott.liang
 * @version 1.0 9/21/2021
 * @since laxcus 1.0
 */
public class CheckJobSite extends Command {
	
	private static final long serialVersionUID = 2228765618576598101L;

	/**
	 * 构造默认的显示相关的工作任务节点
	 */
	public CheckJobSite() {
		super();
	}

	/**
	 * 生成显示相关的工作任务节点的数据副本
	 * @param that 显示相关的工作任务节点
	 */
	private CheckJobSite(CheckJobSite that) {
		super(that);
	}

	/**
	 * 从可类化数据读取器中解析显示相关的工作任务节点
	 * @param reader 可类化数据读取器
	 */
	public CheckJobSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CheckJobSite duplicate() {
		return new CheckJobSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		
	}

}