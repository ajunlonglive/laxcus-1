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
 * 检测服务器系统信息。<br>
 * 系统信息包括：CPU、内存、磁盘的信息。<br>
 * 这个命令只针对LINUX平台，WINDOWS平台暂时不支持。
 * 
 * 命令由WATCH站点发起，分到不同的站点去执行。
 * FRONT节点只能设置本地自己的调用器数目。
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public class CheckSystemInfo extends Command {

	private static final long serialVersionUID = 562772289871481960L;

	/** 服务器地址 **/
	private ArrayList<Node> sites = new ArrayList<Node>();
	
	/** 判断在本地执行 **/
	private boolean local;

	/**
	 * 构造默认的检测服务器系统信息命令
	 */
	public CheckSystemInfo() {
		super();
		local = false;
	}

	/**
	 * 从可类化数据读取器中解析检测服务器系统信息命令
	 * @param reader 可类化数据读取器
	 */
	public CheckSystemInfo(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成检测服务器系统信息命令的数据副本
	 * @param that CheckSystemInfo实例
	 */
	private CheckSystemInfo(CheckSystemInfo that) {
		super(that);
		sites.addAll(that.sites);
		local = that.local;
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
	 * 保存一个服务器地址，不允许空指针
	 * @param e Node实例
	 * @return 保存成功返回真，否则假
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
	 * 输出全部服务器地址
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
	public CheckSystemInfo duplicate() {
		return new CheckSystemInfo(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
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
		local = reader.readBoolean();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			sites.add(node);
		}
	}

}