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
 * 分布任务组件标记报告 <br><br>
 * 
 * 数据包中包含一个组件标识，由ACCOUNT站点发给CALL/DATA/WORK/BUILD站点。它们在本地检查是否存在。
 * 
 * @author scott.liang
 * @version 1.0 3/11/2013
 * @since laxcus 1.0
 */
public final class TakeTaskTagProduct extends EchoProduct {

	private static final long serialVersionUID = 2843688720222059506L;

	/** 分布任务组件标记 **/
	private TaskTag tag;
	
	/**
	 * 根据传入的分布任务组件标记报告，生成它的副本
	 * @param that TakeTaskTagProduct实例
	 */
	private TakeTaskTagProduct(TakeTaskTagProduct that) {
		super(that);
		tag = that.tag;
	}
	
	/**
	 * 构造默认和私有的分布组件数据包
	 */
	private TakeTaskTagProduct() {
		super();
	}

	/**
	 * 构造分布组件数据包，指定分布任务组件标记
	 * @param e  TaskTag实例
	 */
	public TakeTaskTagProduct(TaskTag e) {
		this();
		setTag(e);
	}

	/**
	 * 从可类化数据读取器中解析分布任务组件标记报告
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeTaskTagProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置分布任务组件标记
	 * @param e TaskTag实例
	 */
	public void setTag(TaskTag  e) {
		tag = e;
	}
	
	/**
	 * 返回分布任务组件标记
	 * @return TaskTag实例
	 */
	public TaskTag getTag() {
		return tag;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeTaskTagProduct duplicate() {
		return new TakeTaskTagProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(tag);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		tag = reader.readInstance(TaskTag.class);
	}

}