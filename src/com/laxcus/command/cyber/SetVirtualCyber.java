/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cyber;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.*;
import com.laxcus.site.*;

/**
 * 设置虚拟空间参数 <br><br>
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public abstract class SetVirtualCyber extends Command {

	private static final long serialVersionUID = 8584653671001996984L;

	/** 可以承载的人数 **/
	private int persons;

	/** 阀值 **/
	private double threshold;

	/** 指定的站点 */
	private TreeSet<Node> array = new TreeSet<Node>();

	/**
	 * 根据传入的设置虚拟空间参数，生成它的数据副本
	 * @param that SetVirtualCyber实例
	 */
	protected SetVirtualCyber(SetVirtualCyber that) {
		super(that);	
		persons = that.persons;
		threshold = that.threshold;
		array.addAll(that.array);
	}

	/**
	 * 构造默认的设置虚拟空间参数。
	 */
	protected SetVirtualCyber() {
		super();
	}

	/**
	 * 设置可以承载的人数，小于或者等于0是不限制
	 * @param what 可以承载的人数
	 */
	public void setPersons(int what) {
		persons = what;
	}

	/**
	 * 返回可以承载的人数
	 * @return 成员数
	 */
	public int getPersons() {
		return persons;
	}

	/**
	 * 设置为阀值比例
	 * @param b 真或者假
	 */
	public void setThreshold(double b) {
		threshold = b;
	}

	/**
	 * 判断是阀值比例
	 * @return 真或者假。
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * 判断是操作全部站点
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return array.isEmpty();
	}

	/**
	 * 保存一个站点，不允许空指针
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 删除一个节点地址
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean remove(Node e) {
		return array.remove(e);
	}

	/**
	 * 保存一批节点地址
	 * @param a Node数组
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
	 * 返回站点列表
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(array);
	}

	/**
	 * 清除全部站点
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 统计站点数目
	 * @return 站点数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集。如果这里为空，表示设置全部DATA主站点下的某个DSM表
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 成员数
		writer.writeInt(persons);
		// 阀值
		writer.writeDouble(threshold);
		// 地址成员数目
		writer.writeInt(array.size());
		// 保存地址
		for (Node e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 成员数
		persons = reader.readInt();
		// 阀值
		threshold = reader.readDouble();
		// 地址成员数目
		int size = reader.readInt();
		// 解析地址
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			array.add(e);
		}
	}

}