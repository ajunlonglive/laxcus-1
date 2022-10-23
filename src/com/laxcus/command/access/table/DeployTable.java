/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;


import java.util.*;

import com.laxcus.util.*;
import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 部署数据表到指定的站点。<br><br>
 * 
 * 这个命令由WATCH站点发出，提交到TOP/HOME站点，最终受理的是CALL/WORK/BUILD/DATA站点。<br>
 * 被部署的表必须是已经存在，且在HOME节点上注册。<br><br>
 * 
 * 部署表在不同类型节点有所区别：<br>
 * 1. CALL/WORK/BUILD节点，部署一个表，意味着这个资源引用的所有表都可以使用，不只是一个表。<br>
 * 2. DATA节点，部署一个表，只能这个表，其他表不在其列。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/14/2019
 * @since laxcus 1.0
 */
public class DeployTable extends Command {

	private static final long serialVersionUID = -6057669964597695991L;

	/** 资源引用 ，HOME节点分配 **/
	private Refer refer;

	/** 表实例 ，HOME节点分配 **/
	private Table table;

	/** 数据表名，在设置时定义 **/
	private Space space;

	/** 目标节点数组 **/
	private TreeSet<Node> array = new TreeSet<Node>();
	
	/**
	 * 构造默认的部署数据表到指定的站点
	 */
	public DeployTable() {
		super();
	}
	
	/**
	 * 构造默认的部署数据表到指定的站点
	 * @param space 表名
	 */
	public DeployTable(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public DeployTable(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成部署数据表到指定的站点的数据副本
	 * @param that 部署数据表到指定的站点
	 */
	private DeployTable(DeployTable that) {
		super(that);
		refer = that.refer;
		table = that.table;
		space = that.space;
		array.addAll(that.array);
	}
	
	/**
	 * 设置用户资源引用
	 * @param e Refer实例
	 */
	public void setRefer(Refer e) {
		// 赋值
		refer = e;
	}

	/**
	 * 返回用户资源引用
	 * @return Refer实例
	 */
	public Refer getRefer() {
		return refer;
	}

	/**
	 * 设置表实例
	 * @param e Table实例
	 */
	public void setTable(Table e) {
		// 赋值
		table = e;
	}

	/**
	 * 返回表实例
	 * @return Table实例
	 */
	public Table getTable() {
		return table;
	}
	
	/**
	 * 设置表名
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);
		space = e;
	}

	/**
	 * 返回表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
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


	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DeployTable duplicate() {
		return new DeployTable(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.MultiTable#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(refer);
		writer.writeInstance(table);
		writer.writeObject(space);
		// 目标节点
		writer.writeInt(array.size());
		for (Node e : array) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.MultiTable#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		refer = reader.readInstance(Refer.class);
		table = reader.readInstance(Table.class);
		space = new Space(reader);
		// 目标节点
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			array.add(e);
		}
	}
	
}
