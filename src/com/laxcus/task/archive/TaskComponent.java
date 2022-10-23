/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.archive;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 分布任务组件 <br>
 * 
 * 包含分布任务组件标记和整个文件的内容，由ARCHIVE节点传递给其它工作节点。
 * 
 * @author scott.liang
 * @version 1.1 7/23/2015
 * @since laxcus 1.0
 */
public final class TaskComponent implements Classable, Cloneable, Serializable, Comparable<TaskComponent> { 

	private static final long serialVersionUID = 76470341119733907L;

	/** 分布任务组件标记 **/
	private TaskTag tag;

//	/** 自有组件 **/
//	private boolean selfly;
	
	/** 是组件集 **/
	private boolean group;

	/** 字节内容 **/
	private byte[] content;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		tag = null;
		content = null;
	}

	/**
	 * 根据传入的分布任务组件，生成它的副本
	 * @param that 分布任务组件
	 */
	private TaskComponent(TaskComponent that) {
		this();
		tag = that.tag;
//		selfly = that.selfly;
		group = that.group;
		content = that.content;
	}

	/**
	 * 构造一个默认的分布任务组件
	 */
	public TaskComponent() {
		super();
//		selfly = false;
		group = false;
	}

	/**
	 * 构造分布任务组件，指定参数
	 * @param tag 分布任务组件标记
	 * @param content 数据内容
	 */
	public TaskComponent(TaskTag tag, byte[] content) {
		this();
		setTag(tag);
		setContent(content);
	}

//	/**
//	 * 构造分布任务组件，指定参数
//	 * @param tag 分布任务组件标记
//	 * @param content 数据内容
//	 * @param selfly 自有
//	 */
//	public TaskComponent(TaskTag tag, byte[] content, boolean selfly) {
//		this(tag, content);
//		setSelfly(selfly);
//	}

	/**
	 * 构造分布任务组件，指定参数
	 * @param part 任务组件部件
	 * @param sign 内容MD5签名
	 * @param b 数据内容
	 */
	public TaskComponent(TaskPart part, MD5Hash sign, byte[] b) {
		this(new TaskTag(part, sign), b);
	}

	/**
	 * 从可类化数据读取器中解析分布任务组件参数
	 * @param reader 可类化数据读取器
	 */
	public TaskComponent(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置分布任务组件标记
	 * @param e TaskTag实例
	 */
	public void setTag(TaskTag e) {
		tag = e;
	}

	/**
	 * 返回分布任务组件标记
	 * @return TaskTag实例
	 */
	public TaskTag getTag() {
		return tag;
	}

	/**
	 * 返回分布任务组件的工作部件
	 * @return TaskPart实例
	 */
	public TaskPart getPart() {
		return tag.getPart();
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return tag.getIssuer();
	}

	/**
	 * 返回分布任务组件的阶段类型
	 * @return 整数值表示的阶段类型
	 */
	public int getFamily() {
		return tag.getFamily();
	}

	/**
	 * 返回数据内容的MD5签名
	 * @return MD5Hash实例
	 */
	public MD5Hash getSign() {
		return tag.getSign();
	}

//	/**
//	 * 设置当前组件为自有组件
//	 * @param b 是或者否
//	 */
//	public void setSelfly(boolean b) {
//		selfly = b;
//	}
//
//	/**
//	 * 判断为自有组件
//	 * @return 返回真或者假
//	 */
//	public boolean isSelfly() {
//		return selfly;
//	}

	/**
	 * 设置当前组件为组件集，即是以dtg为后缀的文件，包含多个dtc文件
	 * @param b 是或者否
	 */
	public void setGroup(boolean b) {
		group = b;
	}

	/**
	 * 判断为组件集
	 * @return 返回真或者假
	 */
	public boolean isGroup() {
		return group;
	}
	
	/**
	 * 设置DTC文件内容
	 * @param b 字节数组
	 */
	public void setContent(byte[] b) {
		content = b;
	}

	/**
	 * 返回DTC文件内容
	 * @return 字节数组
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * 生成当前实例的一个数据副本
	 * @return TaskComponent实例
	 */
	public TaskComponent duplicate() {
		return new TaskComponent(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return tag.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return tag.hashCode();
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
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != TaskComponent.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((TaskComponent) that) == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TaskComponent that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(tag, that.tag);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 写入参数
		writer.writeObject(tag);
		writer.writeBoolean(group);
		writer.writeByteArray(content);
		// 返回写入的数据长度
		return writer.size() - scale;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 解析参数
		tag = new TaskTag(reader);
		group = reader.readBoolean();
		content = reader.readByteArray();
		// 返回解析的字节长度
		return reader.getSeek() - scale;
	}

}