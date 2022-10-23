/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.task;

import java.util.regex.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 计算任务令牌
 * 
 * @author scott.liang
 * @version 1.0 8/8/2020
 * @since laxcus 1.0
 */
public class TaskToken extends CloudToken implements Comparable<TaskToken> {

	/** 根命名，包括两种：1. 根命名和子命名的组件. 2. 只有根命名 **/
	private static final String SUFFIX_FULL = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\s*[\\.]\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\s*$";
	private static final String SUFFIX_SIMPLE = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]+?)\\s*$";

	/** 软件命名 **/
	private String naming;

	/** 任务启动类  **/
	private String bootClass;

	/** 资源数据 **/
	private String resource;

	/** 项目类**/
	private String projectClass;

	/**
	 * 判断数据有效
	 * @return 真或者假
	 */
	public boolean isEnabled() {
		boolean success = isEnabled(naming);
		if (success) {
			success = isNaming();
		}
		if (success) {
			success = isEnabled(bootClass);
		}
		if (success) {
			success = isEnabled(resource);
		}
		if (success) {
			success = isEnabled(projectClass);
		}
		return success;
	}

	/**
	 * 判断一个参数有效
	 * @param e 参数
	 * @return 真或者假
	 */
	private boolean isEnabled(String e) {
		return e != null && e.trim().length() > 0;
	}
	
	/**
	 * 判断命名有效
	 * @return
	 */
	private boolean isNaming() {
		return checkNaming(this.naming.toString());
	}

	/**
	 * 检查命名
	 * @param input 输入
	 * @return 判断正确
	 */
	private boolean checkNaming(String input) {
		// 全命名格式(根命名和子命名)
		Pattern pattern = Pattern.compile(SUFFIX_FULL);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			return true;
		}

		// 根命名格式
		pattern = Pattern.compile(SUFFIX_SIMPLE);
		matcher = pattern.matcher(input);
		if (matcher.matches()) { 
			return true;
		}

		return false;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.cloud.task.CloudToken#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeString(naming);
		writer.writeString(bootClass);
		writer.writeString(resource);
		writer.writeString(projectClass);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.cloud.task.CloudToken#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		naming = reader.readString();
		bootClass = reader.readString();
		resource = reader.readString();
		projectClass = reader.readString();
	}

	/**
	 * 构造默认的计算任务令牌
	 */
	public TaskToken() {
		super();
	}

	/**
	 * 构造默认的计算任务令牌
	 * @param naming 命名
	 */
	public TaskToken(String naming) {
		this();
		setNaming(naming);
	}

	/**
	 * 构造默认的计算任务令牌
	 */
	public TaskToken(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成计算任务令牌
	 * @param that 传入类
	 */
	private TaskToken(TaskToken that) {
		this();
		naming = that.naming;
		bootClass = that.bootClass;
		resource = that.resource;
		projectClass = that.projectClass;
	}

	/**
	 * 设置命名
	 * @param e
	 */
	public void setNaming(String e) {
		Laxkit.nullabled(e);
		naming = e.trim();
	}

	/**
	 * 返回命名
	 * @return
	 */
	public String getNaming() {
		return naming;
	}

	/**
	 * 设置引导类
	 * @param e
	 */
	public void setBootClass(String e) {
		Laxkit.nullabled(e);
		bootClass = e;
	}

	/**
	 * 返回引导类
	 * @return
	 */
	public String getBootClass() {
		return bootClass;
	}

	/**
	 * 设置资源数据
	 * @param e
	 */
	public void setResource(String e) {
		Laxkit.nullabled(e);
		resource = e;
	}

	/**
	 * 返回资源数据
	 * @return
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * 设置项目引导类
	 * @param e
	 */
	public void setProjectClass(String e) {
		Laxkit.nullabled(e);
		projectClass = e;
	}

	/**
	 * 返回项目引导类
	 * @return
	 */
	public String getProjectClass() {
		return projectClass;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.cloud.task.CloudToken#buildXML()
	 */
	@Override
	public String buildXML() {
		StringBuilder bf = new StringBuilder();
//		bf.append(formatXML_CDATA("naming", getNaming()));
//		bf.append(formatXML_CDATA("boot-class", getBootClass()));
//		bf.append(formatXML_CDATA("project-class", getProjectClass()));
//		bf.append(formatXML_CDATA("resource", getResource()));
		
		bf.append(formatXML_CDATA(TaskMark.TASK_NAMING, getNaming())); //  "naming"
		bf.append(formatXML_CDATA(TaskMark.TASK_CLASS, getBootClass())); //  "boot-class"
		bf.append(formatXML_CDATA(TaskMark.TASK_PROJECT_CLASS, getProjectClass())); //  "project-class"
		bf.append(formatXML_CDATA(TaskMark.TASK_RESOURCE, getResource())); //  "resource"

		// 保存一段
		return formatXML(TaskMark.TASK, bf.toString()); // "task"
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.cloud.task.CloudToken#duplicate()
	 */
	@Override
	public TaskToken duplicate() {
		return new TaskToken(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return naming;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		}
		return compareTo((TaskToken) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return naming.toLowerCase().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TaskToken that) {
		if( that == null ) {
			return 1;
		}

		return naming.compareToIgnoreCase(that.naming);
	}

}