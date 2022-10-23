/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import com.laxcus.command.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获得分布任务组件包命令。<br>
 * 
 * 这个命令由CALL/DATA/BUILD/WORK站点发出，目标是ACCOUNT站点，要求ACCOUNT返回指定的分布任务组件。
 * 集群的全部分布组件以“dtc”后缀保存ACCOUNT站点，ACCOUNT返回的内容是dtc的格式化数据
 * 
 * @author scott.liang
 * @version 1.1 05/06/2015
 * @since laxcus 1.0
 */
public final class TakeTaskComponent extends Command {

	private static final long serialVersionUID = 702571758805925221L;

	/** 分布任务组件标记 **/
	private TaskTag tag;

	/**
	 * 根据传入的获得分布任务组件包命令，生成它的数据副本
	 * @param that TakeTaskComponent实例
	 */
	private TakeTaskComponent(TakeTaskComponent that) {
		super(that);
		setTag(that.tag);
	}

	/**
	 * 构造默认和私有的获得分布任务组件包命令。
	 */
	public TakeTaskComponent() {
		super();
	}

	/**
	 * 构造获得分布任务组件包命令，指定分布任务组件标记。
	 * @param tag 分布任务组件标记
	 */
	public TakeTaskComponent(TaskTag tag) {
		this();
		setTag(tag);
	}

	/**
	 * 从可类化数据读取器中解析获得分布任务组件包命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeTaskComponent(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置分布任务组件标记，不允许空指针。
	 * @param e TaskTag实例
	 * @throws NullPointerException
	 */
	public void setTag(TaskTag e) {
		Laxkit.nullabled(e);

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
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeTaskComponent duplicate() {
		return new TakeTaskComponent(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(tag);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		tag = reader.readInstance(TaskTag.class);
	}

}