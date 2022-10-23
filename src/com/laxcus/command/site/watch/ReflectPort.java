/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 设置映射端口。<br>
 * 此操作由管理员通知WATCH节点在运行时设置。
 * 映射端口通常用于网关的GATE、ENTRANCE、CALL节点。
 * 
 * @author scott.liang
 * @version 1.0 10/22/2020
 * @since laxcus 1.0
 */
public class ReflectPort extends Command {

	private static final long serialVersionUID = -5057654073312078171L;
	
	/** 设置节点 **/
	private Node site;

	/** 用户基点集合 **/
	private TreeSet<ReflectPortItem> array = new TreeSet<ReflectPortItem>();

	/**
	 * 构造默认的设置映射端口
	 */
	public ReflectPort() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析设置映射端口
	 * @param reader 可类化数据读取器
	 */
	public ReflectPort(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成设置映射端口的数据副本
	 * @param that 设置映射端口处理结果
	 */
	private ReflectPort(ReflectPort that) {
		super(that);
		site = that.site.duplicate();
		array.addAll(that.array);
	}

	/**
	 * 设置节点
	 * @param e
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);
		site = e;
	}

	/**
	 * 返回节点
	 * @return
	 */
	public Node getSite() {
		return site;
	}
	
	/**
	 * 增加一个用户基点，不允许空指针
	 * @param e 用户基点
	 * @return 成功返回真，否则假
	 */
	public boolean add(ReflectPortItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批用户基点
	 * @param a 用户基点数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<ReflectPortItem> a) {
		int size = array.size();
		for (ReflectPortItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批用户基点
	 * @param e 实例
	 * @return 返回新增成员数目
	 */
	public int addAll(ReflectPort e) {
		return addAll(e.array);
	}
	
	/**
	 * 输出全部用户基点
	 * @return 用户基点列表
	 */
	public List<ReflectPortItem> list() {
		return new ArrayList<ReflectPortItem>(array);
	}

	/**
	 * 输出用户基点成员数目
	 * @return 成员数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ReflectPort duplicate() {
		return new ReflectPort(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(site);
		// 参数
		writer.writeInt(array.size());
		for (ReflectPortItem e : array) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = new Node(reader);
		// 参数
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			ReflectPortItem e = new ReflectPortItem(reader);
			array.add(e);
		}
	}

}