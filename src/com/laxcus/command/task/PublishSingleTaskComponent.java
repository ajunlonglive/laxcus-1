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
 * 发布分布任务组件
 * 
 * @author scott.liang
 * @version 1.0 10/10/2019
 * @since laxcus 1.0
 */
public class PublishSingleTaskComponent extends Command {

	private static final long serialVersionUID = -6231880789674319484L;
	
	/** 本地磁盘文件路径，允许空指针 **/
	private String path;

	/** 分布任务组件实例  **/
	private TaskComponent component;

	/**
	 * 构造默认的发布分布任务组件
	 */
	private PublishSingleTaskComponent() {
		super();
	}
	
	/**
	 * 构造发布分布任务组件
	 * @param component 分布任务组件实例
	 */
	public PublishSingleTaskComponent(TaskComponent component) {
		this();
		setComponent(component);
	}
	
	/**
	 * 构造发布分布任务组件
	 * @param path 文件路径
	 * @param component 分布任务组件实例
	 */
	public PublishSingleTaskComponent(String path, TaskComponent component) {
		this(component);
		setPath(path);
	}
	
	/**
	 * 构造发布分布任务组件
	 * @param file 文件
	 * @param component 分布任务组件实例
	 */
	public PublishSingleTaskComponent(File file, TaskComponent component) {
		this(Laxkit.canonical(file), component);
	}
	
	/**
	 * 生成发布分布任务组件数据副本
	 * @param that 发布分布任务组件实例
	 */
	private PublishSingleTaskComponent(PublishSingleTaskComponent that) {
		super(that);
		path = that.path;
		component = that.component;
	}

	/**
	 * 从可类化读取器中解析发布分布任务组件
	 * @param reader 可类化数据读取器
	 */
	public PublishSingleTaskComponent(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置文件路径，允许空指针
	 * @param e 文件路径
	 */
	public void setPath(String e) {
		path = e;
	}

	/**
	 * 返回文件路径
	 * @return 文件路径
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 返回投递阶段类型
	 * @return 投递阶段类型
	 */
	public int getFamily() {
		return component.getPart().getFamily();
	}

	/**
	 * 设置分布组件，不允许空指针
	 * @param e TaskComponent实例
	 */
	public void setComponent(TaskComponent e) {
		Laxkit.nullabled(e);
		component = e;
	}

	/**
	 * 返回组件
	 * @return TaskComponent实例
	 */
	public TaskComponent getComponent() {
		return component;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PublishSingleTaskComponent duplicate() {
		return new PublishSingleTaskComponent(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeString(path);
		writer.writeObject(component);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		path = reader.readString();
		component = new TaskComponent(reader);
	}

}