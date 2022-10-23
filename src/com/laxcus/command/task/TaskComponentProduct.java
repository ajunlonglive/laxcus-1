/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import com.laxcus.echo.product.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.classable.*;

/**
 * 分布任务组件报告。
 * 
 * @author scott.liang
 * @version 1.0 3/11/2013
 * @since laxcus 1.0
 */
public final class TaskComponentProduct extends EchoProduct {

	private static final long serialVersionUID = 7069203371492701427L;

	/** 分布任务组件 **/
	private TaskComponent component;
		
	/**
	 * 根据传入的分布任务组件报告，生成它的副本
	 * @param that TaskComponentProduct实例
	 */
	private TaskComponentProduct(TaskComponentProduct that) {
		super(that);
		component = that.component;
	}
	
	/**
	 * 构造默认和私有的分布任务组件报告
	 */
	private TaskComponentProduct() {
		super();
	}

	/**
	 * 构造分布任务组件报告，指定分布任务组件
	 * @param e TaskComponent实例
	 */
	public TaskComponentProduct(TaskComponent e) {
		this();
		setComponent(e);
	}

	/**
	 * 从可类化数据读取器中解析分布任务组件报告
	 * @param reader - 可类化数据读取器
	 * @since 1.1
	 */
	public TaskComponentProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置分布任务组件
	 * @param e TaskComponent实例
	 */
	public void setComponent(TaskComponent  e) {
		component = e;
	}
	
	/**
	 * 返回分布任务组件
	 * @return TaskComponent实例
	 */
	public TaskComponent getComponent() {
		return component;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TaskComponentProduct duplicate() {
		return new TaskComponentProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(component);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		component = reader.readInstance(TaskComponent.class);
	}

}