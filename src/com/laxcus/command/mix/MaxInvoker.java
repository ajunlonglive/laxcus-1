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
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 设置节点调用器数目。<br>
 * 
 * 命令由WATCH站点发起，分到不同的站点去执行。
 * FRONT节点只能设置本地自己的调用器数目。
 * 
 * @author scott.liang
 * @version 1.0 9/11/2020
 * @since laxcus 1.0
 */
public class MaxInvoker extends Command {

	private static final long serialVersionUID = -3561155213533814428L;

	/** 调用器数目 **/
	private int invokers;
	
	/** 调用器限制时间 **/
	private long confineTime;

	/** 站点地址 **/
	private ArrayList<Node> sites = new ArrayList<Node>();
	
	/** 判断在本地执行 **/
	private boolean local;

	/**
	 * 构造默认的设置节点调用器数目命令
	 */
	public MaxInvoker() {
		super();
		local = false;
		invokers = 0;
		confineTime = 0;
	}

	/**
	 * 从可类化数据读取器中解析设置节点调用器数目命令
	 * @param reader 可类化数据读取器
	 */
	public MaxInvoker(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成设置节点调用器数目命令的数据副本
	 * @param that SetMaxInvoker实例
	 */
	private MaxInvoker(MaxInvoker that) {
		super(that);
		sites.addAll(that.sites);
		local = that.local;
		invokers = that.invokers;
		confineTime = that.confineTime;
	}

	/**
	 * 设置调用器数目
	 * @param len
	 */
	public void setInvokers(int len) {
		if (len > 0) invokers = len;
	}

	/**
	 * 返回调用器数目
	 * @return
	 */
	public int getInvokers() {
		return invokers;
	}
	
	/**
	 * 设置调用器限制时间
	 * @param ms 毫秒
	 */
	public void setConfineTime(long ms) {
		if (ms > 0) confineTime = ms;
	}

	/**
	 * 返回调用器限制时间
	 * @return 毫秒
	 */
	public long getConfineTime() {
		return confineTime;
	}
	
	/**
	 * 设置为本地执行
	 * @param b 真或者假
	 */
	public void setLocal(boolean b) {
		local = b;
	}

	/**
	 * 判断是本地执行
	 * @return 返回真或者假
	 */
	public boolean isLocal() {
		return local;
	}
	
	/**
	 * 保存一个站点地址，不允许空指针
	 * @param e Node实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);
		// 判断
		if (sites.contains(e)) {
			return false;
		}
		return sites.add(e);
	}

	/**
	 * 保存一批站点
	 * @param a Node数组
	 * @return 返回新增成员数目
	 */
	public int addAll(List<Node> a) {
		int size = sites.size();
		for (Node e : a) {
			add(e);
		}
		return sites.size() - size;
	}
	
	/**
	 * 输出全部站点地址
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(sites);
	}

	/**
	 * 地址成员数目
	 * @return 成员数目
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
	 * 判断是全部
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return size() == 0 && !local;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public MaxInvoker duplicate() {
		return new MaxInvoker(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(invokers);
		writer.writeLong(confineTime);
		writer.writeBoolean(local);
		writer.writeInt(sites.size());
		for (Node node : sites) {
			writer.writeObject(node);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		invokers = reader.readInt();
		confineTime = reader.readLong();
		local = reader.readBoolean();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			sites.add(node);
		}
	}

}