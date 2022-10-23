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
import com.laxcus.util.disk.*;

/**
 * 节点最低磁盘空间限制 <br>
 * 
 * 低于规定阀值，节点将报警给WATCH节点。
 * 
 * @author scott.liang
 * @version 1.0 8/18/2019
 * @since laxcus 1.0
 */
public final class LeastDisk extends Command {

	private static final long serialVersionUID = -5354637700260232267L;

	/** 节点最少磁盘空间 **/
	private long capacity;
	
	/** 磁盘空间占用比率 **/
	private double rate;
	
	/** 设备路径 **/
	private TreeSet<LeastPath> paths = new TreeSet<LeastPath>();
	
	/** 设置为本地 **/
	private boolean local;
	
	/** 目标站点地址 **/
	private ArrayList<Node> sites = new ArrayList<Node>();

	/**
	 * 构造节点最低磁盘空间限制
	 */
	public LeastDisk() {
		super();
		setUnlimit();
		setLocal(false);
	}

	/**
	 * 从可类化数据读取器中解析节点最低磁盘空间限制
	 * @param reader 可类化数据读取器
	 */
	public LeastDisk(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成命令副本
	 * @param that LeastDisk实例
	 */
	private LeastDisk(LeastDisk that) {
		super(that);
		capacity = that.capacity;
		rate = that.rate;
		paths.addAll(that.paths);
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
		return capacity <= 0 && rate == 0.0f && paths.isEmpty();
	}

	/**
	 * 设置节点最低磁盘空间限制，以字节为单位。
	 * @param len 节点最低磁盘空间限制
	 */
	public void setCapacity(long len) {
		capacity = len;
	}

	/**
	 * 返回节点最低磁盘空间限制空间，以字节为单位。
	 * @return 节点最低磁盘空间限制空间
	 */
	public long getCapacity() {
		return capacity;
	}

	/**
	 * 设置磁盘空间占比
	 * @param b 双浮点数
	 */
	public void setRate(double b) {
		rate = b;
	}

	/**
	 * 返回最小磁盘空间占比
	 * @return 双浮点数
	 */
	public double getRate() {
		return rate;
	}

	/**
	 * 保存一个设备路径，不允许空指针
	 * @param e LeastPath实例
	 * @return 返回新增成员数目
	 */
	public boolean addPath(LeastPath e) {
		Laxkit.nullabled(e);

		return paths.add(e);
	}
	
	/**
	 * 判断有路径定义
	 * 
	 * @return 返回真或者假
	 */
	public boolean hasPaths() {
		return paths.size() > 0;
	}

	/**
	 * 返回设备路径
	 * @return 设备路径列表
	 */
	public List<LeastPath> getPaths() {
		return new ArrayList<LeastPath>(paths);
	}

	/**
	 * 设置一批设备路径
	 * @param a LeastPath集合
	 * @return 返回新增成员数目
	 */
	public int addPaths(Collection<LeastPath> a) {
		int size = paths.size();
		for (LeastPath e : a) {
			addPath(e);
		}
		return paths.size() - size;
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
	public boolean addSite(Node e) {
		Laxkit.nullabled(e);
		// 存在，忽略！
		if (sites.contains(e)) {
			return false;
		}
		
		return sites.add(e);
	}
	
	/**
	 * 清除节点地址
	 */
	public void clearSites() {
		sites.clear();
	}

	/**
	 * 返回站点地址
	 * @return 节点列表
	 */
	public List<Node> getSites() {
		return new ArrayList<Node>(sites);
	}
	
	/**
	 * 设置一批站点地址
	 * @param a Node集合
	 * @return 返回新增成员数目
	 */
	public int addSites(Collection<Node> a) {
		int size = sites.size();
		for (Node e : a) {
			addSite(e);
		}
		return sites.size() - size;
	}

	/**
	 * 判断处理全部站点
	 * @return 返回真或者假
	 */
	public boolean isAllSites() {
		return sites.isEmpty() && !local;
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
	public LeastDisk duplicate() {
		return new LeastDisk(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(capacity);
		writer.writeDouble(rate);
		// 保存路径
		writer.writeInt(paths.size());
		for (LeastPath e : paths) {
			writer.writeObject(e);
		}
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
		// 解析路径
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			LeastPath e = new LeastPath(reader);
			paths.add(e);
		}
		local = reader.readBoolean();
		// 解析地址
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			sites.add(e);
		}
	}

}