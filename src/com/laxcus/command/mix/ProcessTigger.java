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
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 处理TIGGER操作
 * 
 * @author scott.liang
 * @version 1.0 1/24/2020
 * @since laxcus 1.0
 */
public abstract class ProcessTigger extends Command {

	private static final long serialVersionUID = -7081255906377823945L;

	/** TIGGER操作类型，见TigType定义 **/
	private int type;

	/** 设置为本地操作 **/
	private boolean local;

	/** 目标站点地址 **/
	private ArrayList<Node> sites = new ArrayList<Node>();

	/**
	 * 构造处理TIGGER操作
	 */
	protected ProcessTigger() {
		super();
		setType(0);
		setLocal(false);
	}

	/**
	 * 生成命令副本
	 * @param that ProcessTigger实例
	 */
	protected ProcessTigger(ProcessTigger that) {
		super(that);
		type = that.type;
		local = that.local;
		sites.addAll(that.sites);
	}

	/**
	 * 设置Tigger操作类型
	 * @param who Tigger操作类型
	 */
	public void setType(int who) {
		type = who;
	}

	/**
	 * 返回最大Tigger操作类型
	 * @return Tigger操作类型
	 */
	public int getType() {
		return type;
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
		// 存在，忽略它
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
		return TigType.translateString(type);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(type);
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
		type = reader.readInt();
		local = reader.readBoolean();
		// 解析地址
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			sites.add(e);
		}
	}

}