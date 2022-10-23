/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

import com.laxcus.util.classable.*;

/**
 * 默认的任务管理项目。
 * 
 * @author scott.liang
 * @version 1.1 6/12/2015
 * @since laxcus 1.0
 */
public class DefaultProject extends TaskProject {
	
	private static final long serialVersionUID = 234204047322881322L;

	/**
	 * 根据传入项目实例，生成它的数据副本
	 * @param that DefaultProject实例
	 */
	private DefaultProject(DefaultProject that) {
		super(that);
	}

	/**
	 * 构造默认的分布处理项目
	 */
	public DefaultProject() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析项目参数
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.0
	 */
	public DefaultProject(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.Project#duplicate()
	 */
	@Override
	public DefaultProject duplicate() {
		return new DefaultProject(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.Project#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 空参数
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.Project#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 空参数
		
	}

}
