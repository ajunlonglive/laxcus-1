/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 在两个DATA节点之间复制数据块。
 * 可以是主节点到从节点，或者从节点到主节点。其它情况忽略！
 * 
 * @author scott.liang
 * @version 1.0 11/10/2020
 * @since laxcus 1.0
 */
public class CopyEntity extends ProcessTable {

	private static final long serialVersionUID = -8266894719276397778L;

	/** 数据块编号 **/
	private ArrayList<Long> stubs = new ArrayList<Long>();
	
	/** 源头节点 **/
	private Node from;
	
	/** 目标节点 **/
	private Node to;
	
	/**
	 * 构造默认的复制数据块命令
	 */
	public CopyEntity() {
		super();
	}

	/**
	 * 生成复制数据块命令副本
	 * @param that 复制数据块命令
	 */
	private CopyEntity(CopyEntity that) {
		super(that);
		stubs.addAll(that.stubs);
		from = that.from;
		to = that.to;
	}

	/**
	 * 构造复制数据块命令，指定表名
	 * @param space 表名
	 */
	public CopyEntity(Space space) {
		super(space);
	}

	/**
	 * 从可类化读取器中解析复制数据块命令
	 * @param reader 可类化读取器
	 */
	public CopyEntity(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 保存数据块编号
	 * @param value 数据块编号
	 * @return 返回真或者假
	 */
	public boolean add(long value) {
		if (stubs.contains(value)) {
			return false;
		}
		return stubs.add(value);
	}

	/**
	 * 保存数据块编号
	 * @param a 编号集合
	 * @return 返回新增编号数目
	 */
	public int addAll(Collection<Long> a) {
		int size = stubs.size();
		for (long value : a) {
			stubs.add(value);
		}
		return stubs.size() - size;
	}
	
	/**
	 * 输出数据块编号
	 * @return
	 */
	public List<Long> list() {
		return new ArrayList<Long>(stubs);
	}

	public int size() {
		return stubs.size();
	}

	public boolean isEmpty() {
		return stubs.isEmpty();
	}

	/**
	 * 设置源头节点
	 * @param e
	 */
	public void setFrom(Node e) {
		Laxkit.nullabled(e);
		from = e;
	}

	/**
	 * 返回源头节点
	 * @return
	 */
	public Node getFrom() {
		return from;
	}

	/**
	 * 设置目标节点
	 * @param e Node实例
	 */
	public void setTo(Node e) {
		Laxkit.nullabled(e);
		to = e;
	}

	/**
	 * 返回目标节点
	 * @return 节点实例
	 */
	public Node getTo() {
		return to;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CopyEntity duplicate() {
		return new CopyEntity(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(stubs.size());
		for (long e : stubs) {
			writer.writeLong(e);
		}
		writer.writeObject(from);
		writer.writeObject(to);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			long value = reader.readLong();
			add(value);
		}
		from = new Node(reader);
		to = new Node(reader);
	}
}
