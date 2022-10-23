/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.set.*;

/**
 * 判断分布任务组件的ACCOUNT站点结果
 * 
 * @author scott.liang
 * @version 1.0 03/12/2017
 * @since laxcus 1.0
 */
public class AssertTaskHubProduct extends EchoProduct {

	private static final long serialVersionUID = -1712680851477612760L;

	/** 分布任务组件的工作部件 -> ACCOUNT站点地址 **/
	private Map<TaskPart, NodeSet> sites = new TreeMap<TaskPart, NodeSet>();

	/**
	 * 构造默认的判断分布任务组件的ACCOUNT站点结果
	 */
	public AssertTaskHubProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析判断分布任务组件的ACCOUNT站点结果
	 * @param reader 可类化数据读取器
	 */
	public AssertTaskHubProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成判断分布任务组件的ACCOUNT站点结果的数据副本
	 * @param that AssertTaskHubProduct实例
	 */
	private AssertTaskHubProduct(AssertTaskHubProduct that) {
		super(that);
		sites.putAll(that.sites);
	}

	/**
	 * 保存分布任务组件和它的ACCOUNT站点地址
	 * @param part 分布任务组件的工作部件
	 * @param node ACCOUNT站点地址
	 */
	public boolean add(TaskPart part, Node node) {
		NodeSet set = sites.get(part);
		if (set == null) {
			set = new NodeSet();
			sites.put(part, set);
		}
		return set.add(node);
	}

	/**
	 * 保存分布任务组件和一批它的ACCOUNT站点地址
	 * @param part 分布任务组件的工作部件
	 * @param nodes ACCOUNT站点地址集合
	 * @return 返回新增成员数目
	 */
	public int addAll(TaskPart part, Collection<Node> nodes) {
		int count = 0;
		for (Node node : nodes) {
			boolean success = add(part, node);
			if (success) count++;
		}
		return count;
	}
	
	/**
	 * 删除分布任务组件
	 * @param key 分布任务组件的工作部件
	 * @return 成功返回真，否则假
	 */
	public boolean remove(TaskPart key) {
		return sites.remove(key) != null;
	}

	/**
	 * 输出全部TaskPart集合
	 * @return TaskPart列表
	 */
	public List<TaskPart> getKeys() {
		return new ArrayList<TaskPart>(sites.keySet());
	}
	
	/**
	 * 输出全部站点地址
	 * @return 站点列表
	 */
	public List<Node> getValues() {
		TreeSet<Node> a = new TreeSet<Node>();
		for (NodeSet e : sites.values()) {
			a.addAll(e.list());
		}
		return new ArrayList<Node>(a);
	}

	/**
	 * 根据分布任务组件的工作部件，查找ACCOUNT站点
	 * @param part TaskPart实例
	 * @return Node列表
	 */
	public List<Node> find(TaskPart part) {
		NodeSet set = sites.get(part);
		if (set != null) {
			return set.list();
		}
		return null;
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 返回成员数目
	 * @return 成员数目
	 */
	public int size() {
		return sites.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public AssertTaskHubProduct duplicate() {
		return new AssertTaskHubProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(sites.size());
		Iterator<Map.Entry<TaskPart, NodeSet>> iterator = sites.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<TaskPart, NodeSet> entry = iterator.next();
			writer.writeObject(entry.getKey());
			writer.writeObject(entry.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			TaskPart part = new TaskPart(reader);
			NodeSet set = new NodeSet(reader);
			sites.put(part, set);
		}
	}
}
