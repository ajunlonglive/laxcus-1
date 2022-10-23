/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 删除分布应用处理结果
 * 
 * @author scott.liang
 * @version 1.0 6/21/2020
 * @since laxcus 1.0
 */
public class DropTaskApplicationProduct extends MultiProcessProduct {

	private static final long serialVersionUID = 36704572476484237L;

	/**
	 * 构造默认的发布分布任务组件应用附件处理结果
	 */
	public DropTaskApplicationProduct() {
		super();
	}

	/**
	 * 删除分布应用处理结果
	 * @param rights 正确
	 * @param faluts 错误
	 */
	public DropTaskApplicationProduct(int rights, int faluts) {
		this();
		addRights(rights);
		addFaults(faluts);
	}

	/**
	 * 生成发布分布任务组件应用附件处理结果数据副本
	 * @param that 发布分布任务组件应用附件处理结果实例
	 */
	private DropTaskApplicationProduct(DropTaskApplicationProduct that) {
		super(that);
	}

	/**
	 * 从可类化读取器中解析发布分布任务组件应用附件处理结果
	 * @param reader 可类化数据读取器
	 */
	public DropTaskApplicationProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropTaskApplicationProduct duplicate() {
		return new DropTaskApplicationProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
	}

}