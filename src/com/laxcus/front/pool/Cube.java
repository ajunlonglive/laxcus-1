/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.pool;

import java.util.*;
import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * CALL站点单元  <BR>
 * 记录一个CALL站点的关联信息
 * 
 * @author scott.liang
 * @version 1.1 11/2/2015
 * @since laxcus 1.0
 */
public final class Cube implements Cloneable, Serializable, Comparable<Cube> {

	private static final long serialVersionUID = 4307511651046420760L;

	/** CALL站点地址 **/
	private Node node;

	/** 数据表名集合 **/
	private SpaceSet spaces = new SpaceSet();

	/** 任务命名命令 **/
	private PhaseSet phases = new PhaseSet();

	/**
	 * 构造默认和私有的Cube实例
	 */
	private Cube() {
		super();
	}

	/**
	 * 从传入实例，构造浅层数据副本
	 * @param that Cube实例
	 */
	private Cube(Cube that) {
		this();
		node = that.node;
		spaces.addAll(that.spaces);
		phases.addAll(that.phases);
	}

	/**
	 * 构造Cube实例，指定CALL站点地址
	 * @param node Node实例
	 */
	public Cube(Node node) {
		this();
		setNode(node);
	}

	/**
	 * 设置CALL站点地址
	 * @param e Node实例
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);

		node = e;
	}

	/**
	 * 返回CALL站点地址
	 * @return Node实例
	 */
	public Node getNode() {
		return node;
	}
	
	/**
	 * 保存数据表名
	 * @param e 表名
	 * @return 成功返回真，否则假
	 */
	public boolean add(Space e) {
		return spaces.add(e);
	}
	
	/**
	 * 保存阶段命名
	 * @param e 阶段命名
	 * @return 成功返回真，否则假
	 */
	public boolean add(Phase e) {
		return phases.add(e);
	}
	
	/**
	 * 删除数据表名
	 * @param e 表名
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Space e) {
		return spaces.remove(e);
	}
	
	/**
	 * 删除阶段命名
	 * @param e 阶段命名
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Phase e) {
		return phases.remove(e);
	}
	
	/**
	 * 返回全部数据表名
	 * @return Space列表
	 */
	public List<Space> getSpaces() {
		return spaces.list();
	}
	
	/**
	 * 返回全部阶段命名
	 * @return Phase列表
	 */
	public List<Phase> getPhases() {
		return phases.list();
	}

	/**
	 * 判断是空集合，包括表和阶段命名都是空状态
	 * @return 返回真或者假
	 */
	public boolean isEmpty(){
		return spaces.isEmpty() && phases.isEmpty();
	}

	/**
	 * 生成浅层实例副本
	 * @return Cube实例
	 */
	public Cube duplicate() {
		return new Cube(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Cube.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Cube) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (node == null) {
			return 0;
		}
		return node.hashCode();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (node == null) {
			return "none node";
		}
		return node.toString();
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
	public int compareTo(Cube that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(node, that.node);
	}

}