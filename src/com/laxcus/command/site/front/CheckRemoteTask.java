/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.front;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * FRONT节点检测注册在本地的CALL节点。<br><br>
 * 
 * 命令格式1：CHECK REMOTE TASK 组件, 组件, ... <br>
 * 命令格式2: CHECK REMOTE TASK  <br>
 * 
 * 组件：SOFT1.TASK1, SOFT2.TASK2 ...
 * 
 * @author scott.liang
 * @version 1.1 11/07/2018
 * @since laxcus 1.0
 */
public class CheckRemoteTask extends Command {

	private static final long serialVersionUID = 4843296468391439551L;

	/** 组件集合  **/
	private ArrayList<Sock> array = new ArrayList<Sock>();

	/** 完整的格式，默认是真 **/
	private boolean full;

	/**
	 * 构造默认的FRONT节点检测注册在本地的CALL节点。
	 */
	public CheckRemoteTask() {
		super();
		setFull(true);
	}

	/**
	 * 构造实例
	 * @param sock 任务组件基础字
	 */
	public CheckRemoteTask(Sock sock) {
		this();
		add(sock);
	}
	
	/**
	 * 从可类化数据读取器中解析显示组件
	 * @param reader 可类化数据读取器
	 */
	public CheckRemoteTask(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的FRONT节点检测注册在本地的CALL节点，生成它的数据副本
	 * @param that CheckRemoteTask实例
	 */
	private CheckRemoteTask(CheckRemoteTask that) {
		super(that);
		array.addAll(that.array);
		full = that.full;
	}

	/**
	 * 设置完整格式
	 * @param b 真或者假
	 */
	public void setFull(boolean b) {
		full = b;
	}

	/**
	 * 判断是完整格式
	 * @return 返回真或者假
	 */
	public boolean isFull() {
		return full;
	}

	/**
	 * 判断是简单格式
	 * @return 返回真或者假
	 */
	public boolean isSimple() {
		return !isFull();
	}

	/**
	 * 判断显示全部
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return array.size() == 0;
	}

	/**
	 * 保存一个组件
	 * @param e Sock实例
	 * @return 返回真或者假
	 */
	public boolean add(Sock e) {
		Laxkit.nullabled(e);
		// 存在，忽略它！
		if (array.contains(e)) {
			return false;
		}

		return array.add(e);
	}

	/**
	 * 保存一批数据表
	 * @param a 数据表集合
	 * @return 返回新增数目
	 */
	public int addAll(Collection<Sock> a) {
		int size = array.size();
		for (Sock e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 返回组件列表
	 * @return Sock列表
	 */
	public List<Sock> list() {
		return new ArrayList<Sock>(array);
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 返回组件数目
	 * @return 组件数
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CheckRemoteTask duplicate() {
		return new CheckRemoteTask(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Sock e : array) {
			writer.writeObject(e);
		}
		writer.writeBoolean(full);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Sock e = new Sock(reader);
			add(e);
		}
		full = reader.readBoolean();
	}

}