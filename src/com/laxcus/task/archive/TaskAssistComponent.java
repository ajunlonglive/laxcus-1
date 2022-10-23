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
import com.laxcus.util.naming.*;

/**
 * 分布任务组件应用附件 <br><br>
 * 
 * 包含分布任务组件工作部件、文件名、文件字节数组。<br>
 * FRONT节点根据用户要求，传递给CALL/DATA/WORK/BUILD节点。
 * 
 * @author scott.liang
 * @version 1.1 10/7/2019
 * @since laxcus 1.0
 */
public final class TaskAssistComponent implements Classable, Cloneable, Serializable, Comparable<TaskAssistComponent> { 

	private static final long serialVersionUID = 1246955560753002352L;

	/** 分布任务组件标记 **/
	private TaskTag tag;
	
	/** 软件名称 **/
	private Naming ware;
	
	/** 文件名，只是文件名称，不包含路径 **/
	private String name;

	/** 字节内容 **/
	private byte[] content;
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		tag = null;
		ware = null;
		name = null;
		content = null;
	}

	/**
	 * 根据传入的分布任务组件应用附件，生成它的副本
	 * @param that 分布任务组件应用附件
	 */
	private TaskAssistComponent(TaskAssistComponent that) {
		this();
		tag = that.tag;
		ware = that.ware;
		name = that.name;
		content = that.content;
	}

	/**
	 * 构造一个默认的分布任务组件应用附件
	 */
	public TaskAssistComponent() {
		super();
	}

	/**
	 * 构造分布任务组件应用附件，指定参数
	 * @param tag 分布任务组件工作部件
	 * @param ware 软件名称
	 * @param name 文件名称（不包含路径)
	 * @param b 数据内容
	 */
	public TaskAssistComponent(TaskTag tag, Naming ware, String name, byte[] b) {
		this();
		setTag(tag);
		setWare(ware);
		setName(name);
		setContent(b);
	}

	/**
	 * 构造分布任务组件应用附件，指定参数
	 * @param part 分布任务组件工作部件
	 * @param sign 内容签名
	 * @param ware 软件名称
	 * @param b 数据内容
	 */
	public TaskAssistComponent(TaskPart part, MD5Hash sign, Naming ware, String name, byte[] b) {
		this(new TaskTag(part, sign), ware, name, b);
	}

	/**
	 * 从可类化数据读取器中解析分布任务组件应用附件参数
	 * @param reader 可类化数据读取器
	 */
	public TaskAssistComponent(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从解析分布任务组件应用附件参数
	 * @param b 字节数组
	 */
	public TaskAssistComponent(byte[] b) {
		this(new ClassReader(b));
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
	 * 返回分布任务组件工作部件
	 * @return TaskPart实例
	 */
	public TaskPart getPart() {
		return tag.getPart();
	}
	
	/**
	 * 返回分布任务组件的工作区
	 * @return TaskSection实例
	 */
	public TaskSection getSection() {
		return new TaskSection(tag.getIssuer(), tag.getFamily(), ware);
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return tag.getIssuer();
	}
	
	/**
	 * 返回阶段类型
	 * @return 阶段类型，见PhaseTag定义。
	 */
	public int getFamily() {
		return tag.getFamily();
	}
	
	/**
	 * 返回内容签名
	 * @return MD5散列码
	 */
	public MD5Hash getSign() {
		return tag.getSign();
	}

	/**
	 * 设置软件名称
	 * @param e
	 */
	public void setWare(Naming e) {
		Laxkit.nullabled(e);
		ware = e;
	}

	/**
	 * 返回软件名称
	 * @return 软件名称
	 */
	public Naming getWare() {
		return ware;
	}

	/**
	 * 设置文件名，只是文件名本身
	 * @param e String实例
	 */
	public void setName(String e) {
		Laxkit.nullabled(e);
		name = e;
	}

	/**
	 * 返回文件名
	 * @return String实例
	 */
	public String getName() {
		return name;
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
	 * @return TaskAssistComponent实例
	 */
	public TaskAssistComponent duplicate() {
		return new TaskAssistComponent(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s.%s", (tag != null ? tag.toString()
				: "system"), ware, name);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = (tag != null ? tag.hashCode() : 0);
		
		return hash ^ ware.hashCode() ^ name.hashCode();
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
		if (that == null || that.getClass() != TaskAssistComponent.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((TaskAssistComponent) that) == 0;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TaskAssistComponent that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(tag, that.tag);
		if (ret == 0) {
			ret = Laxkit.compareTo(ware, that.ware);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(name, that.name);
		}
		return ret;
	}

	/**
	 * 输出字节数组
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 写入参数
		writer.writeObject(tag);
		writer.writeObject(ware);
		writer.writeString(name);
		writer.writeByteArray(content);
		// 返回写入的数据长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 解析参数
		tag = new TaskTag(reader);
		ware = new Naming(reader);
		name = reader.readString();
		content = reader.readByteArray();
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

}