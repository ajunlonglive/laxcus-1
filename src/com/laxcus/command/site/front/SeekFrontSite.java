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
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 检索基于节点地址的用户登录信息。<br>
 * 依据GATE/CALL站点，获取某个GATE站点或者全部GATE站点上的FRONT登录用户。
 * 这个方法只限管理员登录到BANK子域集群后才能使用。
 * 
 * @author scott.liang
 * @version 1.0 09/09/2015
 * @since laxcus 1.0
 */
public class SeekFrontSite extends Command {

	private static final long serialVersionUID = -2168730683362915196L;

	/** 指定GATE/CALL站点地址 **/ 
	private TreeSet<Node> array = new TreeSet<Node>();

	/**
	 * 根据传入的检索基于节点地址的用户登录信息，生成数据副本
	 * @param that 检索基于节点地址的用户登录信息
	 */
	private SeekFrontSite(SeekFrontSite that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造默认的检索基于节点地址的用户登录信息
	 */
	public SeekFrontSite() {
		super();
	}
	
	/**
	 * 
	 * @param reader 可类化数据读取器
	 */
	public SeekFrontSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造检索基于节点地址的用户登录信息，保存一组记录
	 * @param a Node数组
	 */
	public SeekFrontSite(List<Node> a) {
		this();
		addAll(a);
	}

	/**
	 * 判断显示全部FRONT详细信息
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return array.size() == 0;
	}

	/**
	 * 保存一个GATE地址
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存全部GATE地址
	 * @param a Node列表
	 * @return 返回新增成员数目
	 */
	public int addAll(List<Node> a) {
		int size = array.size();
		for (Node e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出GATE/CALL地址
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(array);
	}

	/**
	 * 判断地址包含
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean contains(Node e) {
		return array.contains(e);
	}

	/**
	 * 统计GATE地址数目
	 * @return 地址数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断GATE地址数目空值
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SeekFrontSite duplicate() {
		return new SeekFrontSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Node e : array) {
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
			Node e = new Node(reader);
			array.add(e);
		}
	}

}