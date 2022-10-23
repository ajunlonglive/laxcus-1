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
 * 获取系统分布任务组件的ACCOUNT站点。<br><br>
 * 流程：<br>
 * 1. 获取保存系统组件的ACCOUNT站点。<br>
 * 2. 在本地启动TakeTaskTag命令，检查和加载组件。<br><br>
 * 
 * 系统组件存在于所有ACCOUNT站点，任意一个ACCOUNT节点都可以加载。<br>
 * 此操作是DATA/WORK/BUILD/CALL节点，在启动时检测没有系统组件时触发，如果有则忽略。<br><br>
 * 
 * 执行顺序：DATA/WORK/BUILD/CALL -> HOME -> TOP -> BANK。
 * 
 * @author scott.liang
 * @version 1.0 10/11/2019
 * @since laxcus 1.0
 */
public class TakeSystemTaskSite extends Command {

	private static final long serialVersionUID = 7551172383967426951L;

	/**
	 * 构造默认获取保存系统组件的ACCOUNT站点
	 */
	public TakeSystemTaskSite() {
		super();
	}

	/**
	 * 构造获取保存系统组件的ACCOUNT站点的数据副本
	 * @param that 获取保存系统组件的ACCOUNT站点
	 */
	private TakeSystemTaskSite(TakeSystemTaskSite that) {
		super(that);
	}

	/**
	 * 从可类化读取器中解析获取保存系统组件的ACCOUNT站点
	 * @param reader 可类化数据读取器
	 */
	public TakeSystemTaskSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeSystemTaskSite duplicate() {
		return new TakeSystemTaskSite(this);
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