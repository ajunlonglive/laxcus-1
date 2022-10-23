/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.schema;

import java.io.*;

import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 测点：由任务阶段和数据库表名组成。<br>
 * 标记一台主机上的表和任务阶段的配置对象。<br>
 * 
 * @author scott.liang 
 * @version 1.0 4/28/2009
 * @since laxcus 1.0
 */
public final class Anchor implements Serializable, Cloneable, Classable, Comparable<Anchor> {

	private static final long serialVersionUID = -3870330301965676097L;

	/** 工作组件的任务阶段 */
	private Phase phase;

	/** 数据库表名 */
	private Space space;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		writer.writeObject(this.phase);
		writer.writeObject(this.space);
		return writer.size() - scale;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		phase = new Phase(reader);
		space = new Space(reader);
		return reader.getSeek() - scale;
	}

	/**
	 * 根据传入对象，生成它的数据副本
	 * @param that
	 */
	private Anchor(Anchor that) {
		this();
		phase = (Phase) that.phase.clone();
		space = (Space) that.space.clone();
	}

	/**
	 * 构造一个无定义的测点
	 */
	private Anchor() {
		super();
	}

	/**
	 * 根据任务命名和数据库表构造实例
	 * 
	 * @param phase
	 * @param space
	 */
	public Anchor(Phase phase, Space space) {
		this();
		setPhase(phase);
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析参数
	 * @param reader - 可类化读取器
	 */
	public Anchor(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置任务命名
	 * @param that
	 */
	public void setPhase(Phase that) {
		phase = (Phase) that.clone();
	}

	/**
	 * 返回任务命名
	 * 
	 * @return Phase实例
	 */
	public Phase getPhase() {
		return phase;
	}

	/**
	 * 设置表名
	 * @param that
	 */
	public void setSpace(Space that) {
		space = (Space) that.clone();
	}

	/**
	 * 返回表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Anchor.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Anchor) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return phase.hashCode() ^ space.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new Anchor(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s - %s", phase, space);
	}

	/*
	 * 比较两个对象排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Anchor that) {
		// 空对象排在前面，有效对象在后面
		if (that == null) {
			return 1;
		}
		int ret = phase.compareTo(that.phase);
		if (ret == 0) {
			ret = space.compareTo(that.space);
		}
		return ret;
	}

}