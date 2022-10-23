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
 * 虚拟机内存最大使用限制，超过将自动启动垃圾回收机制。<br>
 * 
 * 这个限制不向WATCH报告。
 * 
 * 通常限制在60%-70%以内。
 * 
 * @author scott.liang
 * @version 1.0 9/8/2019
 * @since laxcus 1.0
 */
public final class MostVMMemory extends Command {

	private static final long serialVersionUID = -4169126938736458655L;

	/** 内存占用比率 **/
	private double rate;
	
	/** 设置为本地 **/
	private boolean local;
	
	/** 目标站点地址 **/
	private ArrayList<Node> sites = new ArrayList<Node>();

	/**
	 * 构造虚拟机内存最大使用限制
	 */
	private MostVMMemory() {
		super();
		setRate(80.0f);
		setLocal(false);
	}

	/**
	 * 从可类化数据读取器中解析虚拟机内存最大使用限制
	 * @param reader 可类化数据读取器
	 */
	public MostVMMemory(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造虚拟机内存最大使用限制，指定虚拟机内存最大使用限制值
	 * @param rate 虚拟机内存最大使用限制
	 */
	public MostVMMemory(double rate) {
		this();
		setRate(rate);
	}
	
	/**
	 * 生成命令副本
	 * @param that MostVMMemory实例
	 */
	private MostVMMemory(MostVMMemory that) {
		super(that);
		rate = that.rate;
		local = that.local;
		sites.addAll(that.sites);
	}

	/**
	 * 设置虚拟机内存使用占比
	 * @param b 双浮点数
	 */
	public void setRate(double b) {
		if(b > 0.0f) rate = b;
	}

	/**
	 * 返回最大虚拟机内存使用占比
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
	 * 清除全部
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
		return String.format("%.2f", rate);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public MostVMMemory duplicate() {
		return new MostVMMemory(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
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