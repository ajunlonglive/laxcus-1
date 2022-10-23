/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 节点最低内存限制 <br>
 * 
 * 低于规定阀值，节点将报警给WATCH节点。
 * 
 * 通常一个节点内存稳定状态，要求空闲内存占据20%以上，低于这个阀值将可能造成内存溢出，尤其在计算/存储巨大规模大的节点上，如DATA/WORK/BUILD节点。
 * 
 * @author scott.liang
 * @version 1.0 8/8/2019
 * @since laxcus 1.0
 */
public final class LeastMemory extends Command {

	private static final long serialVersionUID = 4240999379498765694L;

	/** 节点最少内存空间 **/
	private long capacity;
	
	/** 内存占用比率 **/
	private double rate;

	/** 设置为本地 **/
	private boolean local;
	
	/** 目标站点地址 **/
	private ArrayList<Node> sites = new ArrayList<Node>();

	/**
	 * 构造节点最低内存限制
	 */
	public LeastMemory() {
		super();
		setUnlimit();
		setLocal(false);
	}

	/**
	 * 从可类化数据读取器中解析节点最低内存限制
	 * @param reader 可类化数据读取器
	 */
	public LeastMemory(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造节点最低内存限制，指定节点最低内存限制
	 * @param len 节点最低内存限制
	 */
	public LeastMemory(long len) {
		this();
		setCapacity(len);
	}
	
	/**
	 * 生成命令副本
	 * @param that LeastMemory实例
	 */
	private LeastMemory(LeastMemory that) {
		super(that);
		capacity = that.capacity;
		rate = that.rate;
		local = that.local;
		sites.addAll(that.sites);
	}
	
	/**
	 * 设置为不限制
	 */
	public void setUnlimit() {
		setCapacity(-1);
		setRate(0.0f);
	}

	/**
	 * 判断是不限制
	 * @return 返回真或者假
	 */
	public boolean isUnlimit() {
		return capacity <= 0 && rate == 0.0f;
	}

	/**
	 * 设置节点最低内存限制，以字节为单位。
	 * @param len 节点最低内存限制
	 */
	public void setCapacity(long len) {
		capacity = len;
	}

	/**
	 * 返回节点最低内存限制空间，以字节为单位。
	 * @return 节点最低内存限制空间
	 */
	public long getCapacity() {
		return capacity;
	}

	/**
	 * 设置内存占比
	 * @param b 双浮点数
	 */
	public void setRate(double b) {
		rate = b;
	}

	/**
	 * 返回最小内存占比
	 * @return 双浮点数
	 */
	public double getRate() {
		return rate;
	}

	/**
	 * 设置为本地节点
	 * @param b 真或者假
	 */
	public void setLocal(boolean b) {
		local = b;
	}

	/**
	 * 判断是本地节点
	 * @return 返回真或者假
	 */
	public boolean isLocal() {
		return local;
	}

	/**
	 * 保存一个站点地址，不允许空指针
	 * @param e Node实例
	 * @return 返回新增成员数目
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);
		// 存在，忽略！
		if (sites.contains(e)) {
			return false;
		}
		
		return sites.add(e);
	}

	/**
	 * 返回站点地址
	 * @return 节点列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(sites);
	}

	/**
	 * 设置一批站点地址
	 * @param a Node集合
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Node> a) {
		int size = sites.size();
		for (Node e : a) {
			add(e);
		}
		return sites.size() - size;
	}

	/**
	 * 返回站点数目
	 * @return 站点数目
	 */
	public int size() {
		return sites.size();
	}
	
	/**
	 * 清除
	 */
	public void clear() {
		sites.clear();
	}

	/**
	 * 判断站点是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 判断处理全部站点
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return size() == 0 && !local;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d - %.3f", capacity, rate);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public LeastMemory duplicate() {
		return new LeastMemory(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(capacity);
		writer.writeDouble(rate);
		writer.writeBoolean(local);
		// 保存地址
		writer.writeInt(sites.size());
		for (Node e : sites) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		capacity = reader.readLong();
		rate = reader.readDouble();
		local = reader.readBoolean();
		// 解析地址
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			sites.add(e);
		}
	}

}