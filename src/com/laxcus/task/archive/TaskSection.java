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
 * 分布组件任务区。<br><br>
 * 
 * 工作区由三个参数组成：用户签名、阶段类型、软件名称。<br>
 * 通过工作区，可以区分一个账号下不同阶段类型、不同软件包下的分布任务组件。
 * “阶段类型”和“软件名称”是必选参数，“用户签名”是可选参数。如果用户名没有设置，即是系统级别（被集群公用），否则属于用户级别。
 * 
 * @author scott.liang
 * @version 1.0 6/17/2020
 * @since laxcus 1.0
 */
public final class TaskSection implements Classable, Serializable, Cloneable, Comparable<TaskSection> {

	private static final long serialVersionUID = -201181881204695853L;

	/** 用户签名 **/
	private Siger issuer;

	/** 阶段类型 **/
	private int family;

	/** 软件名称 **/
	private Naming ware;

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeInstance(issuer);
		writer.writeInt(family);
		writer.writeObject(ware);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		issuer = reader.readInstance(Siger.class);
		family = reader.readInt();
		ware = new Naming(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的工作区，生成它的数据副本
	 * @param that TaskSection实例
	 */
	private TaskSection(TaskSection that) {
		super();
		issuer = that.issuer;
		family = that.family;
		ware = that.ware;
	}

	/**
	 * 构造默认和私有的工作区。
	 */
	private TaskSection() {
		super();
	}

	/**
	 * 构造工作区，指定全部参数
	 * @param issuer 发布者的用户签名
	 * @param family 阶段类型
	 * @param ware 软件名称
	 */
	public TaskSection(Siger issuer, int family, Naming ware) {
		this();
		setIssuer(issuer);
		setFamily(family);
		setWare(ware);
	}

	/**
	 * 构造工作区，指定全部参数
	 * @param issuer 发布者的SHA256签名
	 * @param family 阶段类型
	 * @param ware 软件名称
	 */
	public TaskSection(SHA256Hash issuer, int family, Naming ware) {
		this(new Siger(issuer), family, ware);
	}

	/**
	 * 构造工作区，指定全部参数
	 * @param part 部件
	 * @param ware 软件名称
	 */
	public TaskSection(TaskPart part, Naming ware) {
		this(part.getIssuer(), part.getFamily(), ware);
	}

	/**
	 * 从可类化数据读取器中解析工作区
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TaskSection(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回工作部件
	 * @return TaskPart实例
	 */
	public TaskPart getTaskPart() {
		return new TaskPart(issuer, family);
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return issuer ;
	}

	/**
	 * 判断是系统级阶段命名。没有签名是系统组件。
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public boolean isSystemLevel() {
		return issuer == null;
	}

	/**
	 * 判断是用户级阶段命名。有签名是用户组件！
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public boolean isUserLevel() {
		return issuer != null;
	}

	/**
	 * 返回阶段类型，见PhaseTag中定义
	 * @return 阶段类型
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 设置用户签名，允许空值
	 * @param e Siger实例
	 */
	public void setIssuer(Siger e) {
		issuer = e;
	}

	/**
	 * 设置阶段类型，见PhaseTag中定义
	 * @param who 阶段类型
	 */
	public void setFamily(int who) {
		if (!PhaseTag.isPhase(who)) {
			throw new IllegalPhaseException("illegal phase: %d", who);
		}
		family = who;
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
	 * 生成当前实例副本
	 * @return TaskSection实例
	 */
	public TaskSection duplicate() {
		return new TaskSection(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != TaskSection.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((TaskSection) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (issuer == null) {
			return family ^ ware.hashCode();
		}
		return issuer.hashCode() ^ family ^ ware.hashCode();
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
	public int compareTo(TaskSection that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(issuer, that.issuer);
		if (ret == 0) {
			ret = Laxkit.compareTo(family, that.family);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(ware, that.ware);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s:<%s#%s>", PhaseTag.translate(family),
				(issuer != null ? issuer : "SYSTEM"), ware);
	}

}