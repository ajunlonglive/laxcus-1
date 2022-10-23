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
import com.laxcus.util.classable.*;
import com.laxcus.util.*;
import com.laxcus.site.*;

/**
 * 设置节点的最大异步缓存尺寸。<br><br>
 * 
 * 在WATCH节点由管理员操作。FRONT节点只能操作本机环境。<br>
 * 
 * 语法格式：SET MAX ECHO BUFFER 数字 [TO ALL|LOCAL|site, ...]<br>
 * 
 * @author scott.liang
 * @version 1.0 5/22/2019
 * @since laxcus 1.0
 */
public final class MaxEchoBuffer extends Command {

	private static final long serialVersionUID = -1956105075063690456L;

	/** 空间尺寸 **/
	private long capacity;
	
	/** 设置为本地环境 **/
	private boolean local;

	/** 指定的站点 */
	private TreeSet<Node> array = new TreeSet<Node>();

	/**
	 * 根据传入的设置节点的最大异步缓存尺寸，生成它的数据副本
	 * @param that MaxEchoBuffer实例
	 */
	private MaxEchoBuffer(MaxEchoBuffer that) {
		super(that);	
		capacity = that.capacity;
		local = that.local;
		array.addAll(that.array);
	}

	/**
	 * 构造默认的设置节点的最大异步缓存尺寸。
	 */
	private MaxEchoBuffer() {
		super();
	}

	/**
	 * 构造设置节点的最大异步缓存尺寸，指定内存尺寸
	 * @param much 内存尺寸
	 */
	public MaxEchoBuffer(long much) {
		this();
		setCapacity(much);
	}

	/**
	 * 从可类化数据读取器中解析数据优化命令
	 * @param reader 可类化数据读取器
	 */
	public MaxEchoBuffer(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置异步缓存空间尺寸，小于或者等于0是不限制
	 * @param much 缓存空间
	 */
	public void setCapacity(long much) {
		capacity = much;
	}

	/**
	 * 返回内存尺寸
	 * @return 内存尺寸
	 */
	public long getCapacity() {
		return capacity;
	}

	/**
	 * 设置为本地操作。WATCH/FRONT节点。
	 * @param b 真或者假
	 */
	public void setLocal(boolean b) {
		local = b;
	}

	/**
	 * 判断是本地操作。WATCH/FRONT节点
	 * @return 真或者假。
	 */
	public boolean isLocal() {
		return local;
	}
	
	/**
	 * 判断是操作全部站点
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return !local && array.isEmpty();
	}
	
	/**
	 * 保存一个站点，不允许空指针
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);
		// 存在，忽略它
		if (array.contains(e)) {
			return false;
		}
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
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public MaxEchoBuffer duplicate() {
		return new MaxEchoBuffer(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 内存尺寸
		writer.writeLong(capacity);
		// 本地
		writer.writeBoolean(local);
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
		// 内存尺寸
		capacity = reader.readLong();
		// 本地
		local = reader.readBoolean();
		// 地址成员数目
		int size = reader.readInt();
		// 解析地址
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			array.add(e);
		}
	}

}