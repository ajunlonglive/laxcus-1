/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task.talk;

import com.laxcus.echo.product.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.classable.*;

/**
 * 分布任务组件状态查询处理结果
 * 
 * @author scott.liang
 * @version 1.0 6/14/2018
 * @since laxcus 1.0
 */
public class TalkCheckProduct extends EchoProduct {

	private static final long serialVersionUID = -8594507855811715886L;

	/** 状态结果 **/
	private TaskMoment status;

	/**
	 * 构造默认和私有的分布任务组件状态查询处理结果
	 */
	private TalkCheckProduct() {
		super();
	}

	/**
	 * 构造分布任务组件状态查询处理结果，指定状态
	 * @param status
	 */
	public TalkCheckProduct(TaskMoment status) {
		this();
		setStatus(status);
	}

	/**
	 * 从可类化读取器中解析分布任务组件状态查询处理结果
	 * @param reader 可类化读取器
	 */
	public TalkCheckProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成分布任务组件状态查询处理结果的数据副本
	 * @param that 分布任务组件状态查询处理结果
	 */
	private TalkCheckProduct(TalkCheckProduct that) {
		super(that);
		status = that.status;
	}

	/**
	 * 设置状态
	 * @param who 状态码
	 */
	public void setStatus(TaskMoment who) {
		status = who;
	}

	/**
	 * 返回状态
	 * @return 状态码
	 */
	public TaskMoment getStatus(){
		return status;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TalkCheckProduct duplicate() {
		return new TalkCheckProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(status);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		status = new TaskMoment(reader);
	}

}