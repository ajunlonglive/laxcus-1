/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 从指定的站点撤销用户。<br>
 * 
 * 这个命令由WATCH站点发出，提交到TOP/HOME站点，最终受理的是CALL/WORK/BUILD/DATA站点。
 * 如果这个命令作用到DATA节点，将导致磁盘上的数据被删除！在执行命令时，窗口有提示，所以请谨用！
 * 
 * @author scott.liang
 * @version 1.0 6/1/2019
 * @since laxcus 1.0
 */
public class EraseUser extends MultiUser {

	private static final long serialVersionUID = 2478597345111748798L;

	/** 目标节点数组 **/
	private TreeSet<Node> array = new TreeSet<Node>();
	
	/**
	 * 构造默认的从指定的站点撤销用户
	 */
	public EraseUser() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public EraseUser(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成从指定的站点撤销用户的数据副本
	 * @param that 从指定的站点撤销用户
	 */
	private EraseUser(EraseUser that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 设置查目标节点
	 * @param e Node实例
	 */
	public boolean addSite(Node e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 返回查目标节点
	 * @return Node实例
	 */
	public List<Node> getSites() {
		return new ArrayList<Node>(array);
	}

	/**
	 * 判断有相关节点
	 * @param siteFamily 节点类型
	 * @return 返回真或者假
	 */
	public boolean hasSite(byte siteFamily) {
		int count = 0;
		for (Node e : array) {
			if (e.getFamily() == siteFamily) {
				count++;
			}
		}
		return count > 0;
	}

	/**
	 * 判断有DATA类型节点
	 * @return 返回真或者假
	 */
	public boolean hasDataSite() {
		return hasSite(SiteTag.DATA_SITE);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public EraseUser duplicate() {
		return new EraseUser(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.MultiUser#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 目标节点
		writer.writeInt(array.size());
		for (Node e : array) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.MultiUser#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 目标节点
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			array.add(e);
		}
	}
	
}
