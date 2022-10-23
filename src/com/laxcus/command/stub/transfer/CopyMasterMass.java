/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.transfer;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.*;

/**
 * 从DATA主节点复制数据块到从节点。<br><br>
 * 
 * 数据从DATA主节点传递到DATA从节点
 * 
 * @author scott.liang
 * @version 1.0 6/15/2019
 * @since laxcus 1.0
 */
public class CopyMasterMass extends Command {

	private static final long serialVersionUID = 9019993680789036994L;

	/** DATA MASTER主节点 **/
	private Node master;

	/** DATA SLAVE从节点，多个 **/
	private TreeSet<Node> slaves = new TreeSet<Node>();

	/** 数据表名，在设置时定义 **/
	private Space space;

	/** 数据块编号，如果不定义是DATA MASTER主节点下的全部数据块  **/
	private TreeSet<Long> stubs = new TreeSet<Long>();
	
	/**
	 * 构造默认的从DATA主节点复制数据块到从节点
	 */
	public CopyMasterMass() {
		super();
	}
	
	/**
	 * 构造默认的从DATA主节点复制数据块到从节点
	 * @param space 表名
	 */
	public CopyMasterMass(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public CopyMasterMass(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成从DATA主节点复制数据块到从节点的数据副本
	 * @param that 从DATA主节点复制数据块到从节点
	 */
	private CopyMasterMass(CopyMasterMass that) {
		super(that);
		master = that.master;
		slaves.addAll(that.slaves);
		space = that.space;
		stubs.addAll(that.stubs);
	}
	
	/**
	 * 设置用户资源引用
	 * @param e Node实例
	 */
	public void setMaster(Node e) {
		Laxkit.nullabled(e);
		master = e;
	}

	/**
	 * 返回用户资源引用
	 * @return Node实例
	 */
	public Node getMaster() {
		return master;
	}

	/**
	 * 设置节点
	 * @param e Node实例
	 */
	public boolean addSlave(Node e) {
		Laxkit.nullabled(e);
		return slaves.add(e);
	}

	/**
	 * 保存一组从节点
	 * @param a 节点列表
	 * @return 返回新增成员数目
	 */
	public int addSlaves(Collection<Node> a) {
		int size = slaves.size();
		for (Node e : a) {
			addSlave(e);
		}
		return slaves.size() - size;
	}
	
	/**
	 * 返回节点列表
	 * @return Node列表
	 */
	public List<Node> getSlaves() {
		return new ArrayList<Node>(slaves);
	}
	
	/**
	 * 设置表名
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);
		space = e;
	}

	/**
	 * 返回表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}
	
	/**
	 * 设置数据块编号
	 * @param id 数据块编号
	 */
	public boolean addStub(long id) {
		return stubs.add(id);
	}

	/**
	 * 返回数据块编号
	 * @return 数据块编号
	 */
	public List<Long> getStubs() {
		return new ArrayList<Long>(stubs);
	}
	
	/**
	 * 判断取全部数据块编码
	 * @return 返回真或者假
	 */
	public boolean isAllStubs() {
		return stubs.size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CopyMasterMass duplicate() {
		return new CopyMasterMass(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.MultiNode#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 主节点
		writer.writeInstance(master);
		// 从节点
		writer.writeInt(slaves.size());
		for (Node e : slaves) {
			writer.writeObject(e);
		}
		// 表名
		writer.writeObject(space);
		// 目标节点
		writer.writeInt(stubs.size());
		for (long e : stubs) {
			writer.writeLong(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.MultiNode#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 主节点
		master = reader.readInstance(Node.class);
		// 从节点
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			slaves.add(e);
		}
		// 表名
		space = new Space(reader);
		// 数据块编号
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			long e = reader.readLong();
			stubs.add(e);
		}
	}
	
}

