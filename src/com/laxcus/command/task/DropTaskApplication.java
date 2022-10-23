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
 * 删除位于工作节点上的分布应用。<br>
 * 
 * 这个命令由ACCOUNT节点发出，目标是DATA/WORK/BUILD/CALL节点。
 * 
 * @author scott.liang
 * @version 1.0 6/21/2020
 * @since laxcus 1.0
 */
public class DropTaskApplication extends Command {

	private static final long serialVersionUID = -7578932171288311310L;

	/** 任务分区 **/
	private TaskSection section;

	/**
	 * 构造默认的删除位于工作节点上的分布应用
	 */
	private DropTaskApplication() {
		super();
	}
	
	/**
	 * 构造删除位于工作节点上的分布应用
	 * @param component 文件名
	 * @param family 阶段类型
	 */
	public DropTaskApplication(TaskSection component) {
		this();
		setSection(component);
	}

	/**
	 * 生成删除位于工作节点上的分布应用数据副本
	 * @param that 删除位于工作节点上的分布应用实例
	 */
	private DropTaskApplication(DropTaskApplication that) {
		super(that);
		section = that.section;
	}

	/**
	 * 从可类化读取器中解析删除位于工作节点上的分布应用
	 * @param reader 可类化数据读取器
	 */
	public DropTaskApplication(ClassReader reader) {
		this();
		resolve(reader);
	}

//	/**
//	 * 返回投递阶段类型
//	 * @return 投递阶段类型
//	 */
//	public int getFamily() {
//		return section.getFamily();
//	}

	/**
	 * 设置任务分区，不允许空指针
	 * @param e 任务分区
	 */
	public void setSection(TaskSection e) {
		Laxkit.nullabled(e);
		section = e;
	}

	/**
	 * 返回任务分区
	 * @return 任务分区
	 */
	public TaskSection getSection() {
		return section;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropTaskApplication duplicate() {
		return new DropTaskApplication(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(section);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		section = new TaskSection(reader);
	}

}