/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rebuild;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 设置DSM表压缩倍数。<br><br>
 * 
 * 相对“SET MAX DSM REDUCE”，这是一个临时性操作，它不改变账号中的表压缩倍数定义，只做到运行过程中的DATA节点，并且是通过WATCH节点由管理员操作！<br>
 * 
 * 语法格式：SET DSM REDUCE schema.table 倍数 [TO data site, ...]<br>
 * 
 * @author scott.liang
 * @version 1.1 5/21/2019
 * @since laxcus 1.0
 */
public final class SetDSMReduce extends Command {

	private static final long serialVersionUID = 4405339693527724628L;

	/** 数据表名 **/
	private Space space;
	
	/** 压缩倍数 **/
	private int multiple;

	/** DATA主站点地址集合 */
	private TreeSet<Node> array = new TreeSet<Node>();

	/**
	 * 根据传入的设置DSM表压缩倍数，生成它的数据副本
	 * @param that SetDSMReduce实例
	 */
	private SetDSMReduce(SetDSMReduce that) {
		super(that);	
		space = that.space;
		multiple = that.multiple;
		array.addAll(that.array);
	}

	/**
	 * 构造默认的设置DSM表压缩倍数。
	 */
	private SetDSMReduce() {
		super();
	}

	/**
	 * 构造设置DSM表压缩倍数，指定数据表名
	 * @param space 数据表名
	 * @param multiple 压缩倍烽
	 */
	public SetDSMReduce(Space space, int multiple) {
		this();
		setSpace(space);
		setMultiple(multiple);
	}

	/**
	 * 从可类化数据读取器中解析数据优化命令
	 * @param reader 可类化数据读取器
	 */
	public SetDSMReduce(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据表名
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置压缩倍数，不能小于1！！！
	 * @param much 压缩倍数
	 */
	public void setMultiple(int much) {
		if (much < 1) {
			throw new IllegalValueException("illegal value: %d", much);
		}
		multiple = much;
	}

	/**
	 * 返回压缩倍数
	 * @return 压缩倍数
	 */
	public int getMultiple() {
		return multiple;
	}
	
	/**
	 * 保存一个DATA站点，不允许空指针
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 删除一个DATA站点
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean remove(Node e) {
		return array.remove(e);
	}

	/**
	 * 保存一批DATA站点
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
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetDSMReduce duplicate() {
		return new SetDSMReduce(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
		// 压缩倍数
		writer.writeInt(multiple);
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
		// 数据表名
		space = new Space(reader);
		// 压缩倍数
		multiple = reader.readInt();
		// 地址成员数目
		int size = reader.readInt();
		// 解析地址
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			array.add(e);
		}
	}

}