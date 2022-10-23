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
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * FRONT节点检测注册在本地的CALL节点。<br><br>
 * 
 * 命令格式1：CHECK REMOTE TABLE 数据库.表, 数据库.表, ... <br>
 * 命令格式2: CHECK REMOTE TABLE  <br>
 * 
 * @author scott.liang
 * @version 1.1 11/07/2018
 * @since laxcus 1.0
 */
public class CheckRemoteTable extends Command {
	
	private static final long serialVersionUID = -4758924710681243217L;
	
	/** 数据表名集合  **/
	private ArrayList<Space> array = new ArrayList<Space>();

	/**
	 * 构造默认的FRONT节点检测注册在本地的CALL节点。
	 */
	public CheckRemoteTable() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析显示数据表
	 * @param reader 可类化数据读取器
	 */
	public CheckRemoteTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的FRONT节点检测注册在本地的CALL节点，生成它的数据副本
	 * @param that CheckRemoteTable实例
	 */
	private CheckRemoteTable(CheckRemoteTable that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 判断显示全部
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return array.size() == 0;
	}

	/**
	 * 保存一个数据表名
	 * @param e Space实例
	 * @return 返回真或者假
	 */
	public boolean add(Space e) {
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
	public int addAll(Collection<Space> a) {
		int size = array.size();
		for (Space e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 返回数据表名列表
	 * @return Space列表
	 */
	public List<Space> list() {
		return new ArrayList<Space>(array);
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 返回表名数目
	 * @return 表名数
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CheckRemoteTable duplicate() {
		return new CheckRemoteTable(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Space e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space e = new Space(reader);
			add(e);
		}
	}

}
