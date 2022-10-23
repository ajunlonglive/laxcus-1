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
 * 释放一段由CONDUCT.FROM/TO, CONTACT.DISTANT任务产生的实体数据。<br>
 * 
 * 这个命令由WORK站点发出，是WORK站点通过“TakeFluxData”获得实体数据之后，通知DATA/WORK站点删除实体数据。
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public class ReleaseFluxField extends Command {

	private static final long serialVersionUID = -4921036624263459719L;

	/** 任务编号  **/
	private long taskId;

	/** 模值 **/
	private long mod;

	/**
	 * 构造默认和私有的释放ReleaseFluxField命令
	 */
	private ReleaseFluxField() {
		super();
	}

	/**
	 * 根据释放ReleaseFluxField命令，生成它的数据副本
	 * @param that ReleaseFluxField实例
	 */
	private ReleaseFluxField(ReleaseFluxField that) {
		super(that);
		taskId = that.taskId;
		mod = that.mod;
	}

	/**
	 * 构造释放ReleaseFluxField命令，指定任务编号和中间数据映像域
	 * @param taskId 任务编号
	 * @param mod 模值
	 */
	public ReleaseFluxField(long taskId, long mod) {
		this();
		setTaskId(taskId);
		setMod(mod);
	}

	/**
	 * 从可类化读取器中解析抓取释放ReleaseFluxField命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ReleaseFluxField(ClassReader reader) {
		this();
		super.resolve(reader);
	}

	/**
	 * 设置任务编号
	 * @param i 任务编号
	 */
	public void setTaskId(long i) {
		taskId = i;
	}

	/**
	 * 返回任务编号
	 * @return 任务编号
	 */
	public long getTaskId() {
		return taskId;
	}

	/**
	 * 设置模值
	 * @param i 模值
	 */
	public void setMod(long i) {
		mod = i;
	}

	/**
	 * 返回模值
	 * @return 模值
	 */
	public long getMod() {
		return mod;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ReleaseFluxField duplicate() {
		return new ReleaseFluxField(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(taskId);
		writer.writeLong(mod);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		taskId = reader.readLong();
		mod = reader.readLong();
	}

}