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
 * 发布分布任务组件动态链接库
 * 
 * @author scott.liang
 * @version 1.0 10/18/2019
 * @since laxcus 1.0
 */
public class PublishSingleTaskLibraryComponent extends Command {

	private static final long serialVersionUID = 2272889419237372063L;

	/** 本地磁盘文件，允许空指针 **/
	private File file;

	/** 动态链接库 **/
	private TaskLibraryComponent component;

	/**
	 * 构造默认的发布分布任务组件动态链接库
	 */
	private PublishSingleTaskLibraryComponent() {
		super();
	}
	
	/**
	 * 构造发布分布任务组件动态链接库
	 * @param component 文件名
	 * @param family 阶段类型
	 */
	public PublishSingleTaskLibraryComponent(TaskLibraryComponent component) {
		this();
		setComponent(component);
	}

	/**
	 * 构造发布分布任务组件动态链接库
	 * @param component 文件名
	 * @param family 阶段类型
	 */
	public PublishSingleTaskLibraryComponent(TaskLibraryComponent component, File file) {
		this(component);
		setFile(file);
	}

	/**
	 * 生成发布分布任务组件动态链接库数据副本
	 * @param that 发布分布任务组件动态链接库实例
	 */
	private PublishSingleTaskLibraryComponent(PublishSingleTaskLibraryComponent that) {
		super(that);
		component = that.component;
		file = that.file;
	}

	/**
	 * 从可类化读取器中解析发布分布任务组件动态链接库
	 * @param reader 可类化数据读取器
	 */
	public PublishSingleTaskLibraryComponent(ClassReader reader) {
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
	 * 设置动态链接库，不允许空指针
	 * @param e 动态链接库
	 */
	public void setComponent(TaskLibraryComponent e) {
		Laxkit.nullabled(e);
		component = e;
	}

	/**
	 * 返回动态链接库
	 * @return 动态链接库
	 */
	public TaskLibraryComponent getComponent() {
		return component;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PublishSingleTaskLibraryComponent duplicate() {
		return new PublishSingleTaskLibraryComponent(this);
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
		component = new TaskLibraryComponent(reader);
		// 磁盘文件
		file = reader.readFile();
	}

}