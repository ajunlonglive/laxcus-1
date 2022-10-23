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
 * 设置站点日志等级 <br>
 * 
 * 这个命令由WATCH站点发起，分到不同的站点去执行。
 * 
 * @author scott.liang
 * @version 1.0 08/16/2017
 * @since laxcus 1.0
 */
public class SetLogLevel extends Command {

	private static final long serialVersionUID = 5294804865626837822L;

	/** 日志级别，见LogLevel类定义 **/
	private int level;

	/** 站点地址 **/
	private ArrayList<Node> sites = new ArrayList<Node>();

	/**
	 * 构造默认的设置站点日志命令
	 */
	public SetLogLevel() {
		super();
		// 默认是DEBUG级别
		setLevel(LogLevel.DEBUG);
	}

	/**
	 * 构造设置站点日志命令，指定日志级别
	 * @param level 日志级别
	 */
	public SetLogLevel(int level) {
		this();
		setLevel(level);
	}

	/**
	 * 从可类化数据读取器中解析设置站点日志命令
	 * @param reader 可类化数据读取器
	 */
	public SetLogLevel(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成设置站点日志命令的数据副本
	 * @param that 原本实例
	 */
	private SetLogLevel(SetLogLevel that) {
		super(that);
		level = that.level;
		sites.addAll(that.sites);
	}

	/**
	 * 返回日志级别
	 * @return 日志级别
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * 设置日志级别
	 * @param who 日志级别
	 */
	public void setLevel(int who) {
		if (!LogLevel.isLevel(who)) {
			throw new IllegalValueException("illegal log level:%d", who);
		}
		level = who;
	}

	/**
	 * 保存一个站点地址
	 * @param e Node实例
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
	 * 输出全部站点地址
	 * @return Node数组
	 */
	public List<Node> list() {
		return new ArrayList<Node>(sites);
	}
	
	/**
	 * 清除地址
	 */
	public void clear() {
		sites.clear();
	}

	/**
	 * 统计地址成员数目
	 * @return 返回成员数目
	 */
	public int size() {
		return sites.size();
	}

	/**
	 * 判断是全部
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetLogLevel duplicate() {
		return new SetLogLevel(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(level);
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
		level = reader.readInt();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			sites.add(node);
		}
	}

}