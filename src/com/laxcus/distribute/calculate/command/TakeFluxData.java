/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.calculate.command;

import com.laxcus.command.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.util.classable.*;

/**
 * 获取CONDUCT分布计算/CONTACT快速计算的中间实体数据 <br>
 * 
 * 对应CONDUCT操作，此操作由WORK站点发出，目标是DATA站点，或者上一级的WORK站点。
 * 对应CONTACT操作，此操作由WORK站点发出，目标是上一级的WORK节点。
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public class TakeFluxData extends Command {

	private static final long serialVersionUID = 4073019888687821286L;

	/** 任务编号  **/
	private long taskId;

	/** 中间数据映像域 **/
	private FluxField field;

	/**
	 * 构造默认的获取分布计算的中间数据命令
	 */
	public TakeFluxData() {
		super();
	}

	/**
	 * 根据获取分布计算的中间数据命令，生成它的数据副本
	 * @param that TakeFluxData实例
	 */
	private TakeFluxData(TakeFluxData that) {
		super(that);
		taskId = that.taskId;
		field = that.field;
	}

	/**
	 * 构造获取分布计算的中间数据命令，指定任务编号和中间数据映像域
	 * @param taskId 任务编号
	 * @param field 中间数据映像域
	 */
	public TakeFluxData(long taskId, FluxField field) {
		this();
		setTaskId(taskId);
		setField(field);
	}

	/**
	 * 从可类化读取器中解析抓取获取分布计算的中间数据命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeFluxData(ClassReader reader) {
		this();
		resolve(reader);
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
	 * 设置中间数据映像域
	 * @param e FluxField实例
	 */
	public void setField(FluxField e) {
		field = e;
	}

	/**
	 * 返回中间数据映像域
	 * @return FluxField实例
	 */
	public FluxField getField() {
		return field;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeFluxData duplicate() {
		return new TakeFluxData(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(taskId);
		writer.writeObject(field);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		taskId = reader.readLong();
		field = new FluxField(reader);
	}

}