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
 * 分布任务组件标记。<br><br>
 * 
 * 用于区分每一个分布任务组件的唯一性。由两部分组成：<br>
 * 1. 分布任务组件的工作部件。 <br>
 * 2. 分布任务组件内容的MD5签名。 <br><br>
 * 
 * @author scott.liang
 * @version 1.1 7/23/2015
 * @since laxcus 1.0
 */
public final class TaskTag implements Classable, Cloneable, Serializable, Comparable<TaskTag> {

	private static final long serialVersionUID = -5530416825000746507L;

	/** 工作部件 **/
	private TaskPart part;

	/** 内容签名 **/
	private MD5Hash sign;

	/**
	 * 构造默认和私有分布任务组件标记
	 */
	private TaskTag() {
		super();
	}

	/**
	 * 根据传入的分布任务组件标记，生成它的数据副本
	 * @param that 分布任务组件标记
	 */
	private TaskTag(TaskTag that) {
		this();
		part = that.part.duplicate();
		sign = that.sign.duplicate();
	}

	/**
	 * 构造分布任务组件标记，指定参数
	 * @param part 工作部件
	 * @param sign 内容签名
	 */
	public TaskTag(TaskPart part, MD5Hash sign) {
		this();
		setPart(part);
		setSign(sign);
	}

	/**
	 * 从可类化数据读取器中解析分布任务组件标记
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TaskTag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置分布任务组件工作部件，不允许空指针
	 * @param e TaskPart实例
	 */
	public void setPart(TaskPart e) {
		Laxkit.nullabled(e);

		part = e;
	}

	/**
	 * 返回分布任务组件工作部件
	 * @return TaskPart实例
	 */
	public TaskPart getPart() {
		return part;
	}

	/**
	 * 返回阶段类型，见PhaseTag中定义
	 * @return 阶段类型的整型值描述
	 */
	public int getFamily() {
		return part.getFamily();
	}
	
	/**
	 * 返回用户签名
	 * @return 注册用户返回Siger实例，系统组件返回空指针
	 */
	public Siger getIssuer() {
		return part.getIssuer();
	}

	/**
	 * 判断是系统级阶段命名。没有签名是系统组件。
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public boolean isSystemLevel() {
		return part.isSystemLevel();
	}

	/**
	 * 判断是用户级阶段命名。有签名是用户组件！
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public boolean isUserLevel() {
		return part.isUserLevel();
	}

	/**
	 * 设置内容签名
	 * @param e MD5散列码
	 */
	public void setSign(MD5Hash e) {
		Laxkit.nullabled(e);

		sign = e;
	}

	/**
	 * 返回内容签名
	 * @return MD5散列码
	 */
	public MD5Hash getSign() {
		return sign;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return TaskTag实例
	 */
	public TaskTag duplicate() {
		return new TaskTag(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != TaskTag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((TaskTag) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return part.hashCode() ^ sign.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s{%s}", part, sign);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TaskTag that) {
		if(that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(part, that.part);
		if (ret == 0) {
			ret = Laxkit.compareTo(sign, that.sign);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(part);
		writer.writeObject(sign);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		part = new TaskPart(reader);
		sign = new MD5Hash(reader);
		return reader.getSeek() - seek;
	}

}