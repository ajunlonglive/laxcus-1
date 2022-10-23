/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.calculate.command;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 释放由一个CONDUCT.FROM/TO, CONTACT.DISTANT阶段任务产生的，基于一个任务编号的全部中间数据 <br>
 * 
 * 此操作由CallConductInvoker/CallContactInvoker在全部工作结束后发出，目的是清除DATA/WORK站点上的已经使用，可能没有完全释放的中间数据。
 * 
 * 特别说明："com.laxcus.distribute.calculate.*"目录下的类，适用于CONDUCT/CONTACT两个命令
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public class ReleaseFluxArea extends Command {

	private static final long serialVersionUID = 107656144555412023L;

	/** 任务编号 **/
	private long taskId;

	/**
	 * 构造默认和私有的ReleaseFluxArea命令
	 */
	private ReleaseFluxArea() {
		super();
	}

	/**
	 * 根据ReleaseFluxArea命令，生成它的数据副本
	 * @param that ReleaseFluxArea实例
	 */
	private ReleaseFluxArea(ReleaseFluxArea that) {
		super(that);
		taskId = that.taskId;
	}

	/**
	 * 构造ReleaseFluxArea命令，指定任务编号和中间数据映像域
	 * @param taskId 任务编号
	 */
	public ReleaseFluxArea(long taskId) {
		this();
		setTaskId(taskId);
	}

	/**
	 * 从可类化读取器中解析抓取ReleaseFluxArea命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ReleaseFluxArea(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置任务编号
	 * @param who 任务编号
	 */
	public void setTaskId(long who) {
		taskId = who;
	}

	/**
	 * 返回任务编号
	 * @return 任务编号
	 */
	public long getTaskId() {
		return taskId;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ReleaseFluxArea duplicate() {
		return new ReleaseFluxArea(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(taskId);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		taskId = reader.readLong();
	}

}