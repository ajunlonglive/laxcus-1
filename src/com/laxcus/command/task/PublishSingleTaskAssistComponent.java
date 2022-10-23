/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import java.io.*;

import com.laxcus.command.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 发布分布任务组件应用附件
 * 
 * @author scott.liang
 * @version 1.0 10/7/2019
 * @since laxcus 1.0
 */
public class PublishSingleTaskAssistComponent extends Command {

	private static final long serialVersionUID = -7578932171288311310L;

	/** 本地磁盘文件，允许空指针 **/
	private File file;

	/** 应用附件 **/
	private TaskAssistComponent component;

	/**
	 * 构造默认的发布分布任务组件应用附件
	 */
	private PublishSingleTaskAssistComponent() {
		super();
	}
	
	/**
	 * 构造发布分布任务组件应用附件
	 * @param component 文件名
	 * @param family 阶段类型
	 */
	public PublishSingleTaskAssistComponent(TaskAssistComponent component) {
		this();
		setComponent(component);
	}

	/**
	 * 构造发布分布任务组件应用附件
	 * @param component 文件名
	 * @param family 阶段类型
	 */
	public PublishSingleTaskAssistComponent(TaskAssistComponent component, File file) {
		this(component);
		setFile(file);
	}

	/**
	 * 生成发布分布任务组件应用附件数据副本
	 * @param that 发布分布任务组件应用附件实例
	 */
	private PublishSingleTaskAssistComponent(PublishSingleTaskAssistComponent that) {
		super(that);
		component = that.component;
		file = that.file;
	}

	/**
	 * 从可类化读取器中解析发布分布任务组件应用附件
	 * @param reader 可类化数据读取器
	 */
	public PublishSingleTaskAssistComponent(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置文件名，允许空指针
	 * @param e File实例
	 */
	public void setFile(File e) {
		file = e;
	}

	/**
	 * 返回文件名
	 * @return File实例
	 */
	public File getFile() {
		return file;
	}

	/**
	 * 返回投递阶段类型
	 * @return 投递阶段类型
	 */
	public int getFamily() {
		return component.getFamily();
	}

	/**
	 * 设置应用附件，不允许空指针
	 * @param e 应用附件
	 */
	public void setComponent(TaskAssistComponent e) {
		Laxkit.nullabled(e);
		component = e;
	}

	/**
	 * 返回应用附件
	 * @return 应用附件
	 */
	public TaskAssistComponent getComponent() {
		return component;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PublishSingleTaskAssistComponent duplicate() {
		return new PublishSingleTaskAssistComponent(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(component);
		// 磁盘文件
		writer.writeFile(file);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		component = new TaskAssistComponent(reader);
		// 磁盘文件
		file = reader.readFile();
	}

}