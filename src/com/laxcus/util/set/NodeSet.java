/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.set;

import java.io.*;
import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.lock.*;

/**
 * 站点地址集合。<br><br>
 * 
 * 类中的方法，增加/删除/输出成员分为同步/非同步两种。
 * add、remove、list为非同步，没有加锁操作。push、drop、lend为同步，加锁操作。
 * 操作时，这两种方法不能同时共用，必须选择其一。
 * 
 * <br>
 * 其它方法，next,exists为加锁操作。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class NodeSet extends MutexHandler implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = 6786115457742675723L;

	/** 循环调用下标，从0开始(平均循环调用)，到集合的最大值时复0 */
	private int iterateIndex;

	/** 节点地址集合 */
	private TreeSet<Node> array = new TreeSet<Node>();

	/**
	 * 将节点地址集合写入可类化数据存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		writer.writeInt(iterateIndex);
		writer.writeInt(array.size());
		for (Node node : array) {
			writer.writeObject(node);
		}
		return writer.size() - scale;
	}

	/**
	 * 从可类化数据读取器中解析节点地址集合
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.1
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		iterateIndex = reader.readInt();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			array.add(node);
		}
		return reader.getSeek() - scale;
	}

	/**
	 * 根据传入的节点地址集合实例，生成它的数据副本
	 * @param that NodeSet实例
	 */
	public NodeSet(NodeSet that) {
		super();
		array.addAll(that.array);
		iterateIndex = that.iterateIndex;
	}

	/**
	 * 构造默认的节点地址集合
	 */
	public NodeSet() {
		super();
		iterateIndex = 0;
	}

	/**
	 * 构造节点地址集合，同时保存一批节点地址
	 * @param nodes 节点数组
	 */
	public NodeSet(Node[] nodes) {
		this();
		addAll(nodes);
	}

	/**
	 * 构造节点地址集合，同时保存一批节点地址
	 * @param nodes 节点列表
	 * @since 1.1
	 */
	public NodeSet(Collection<Node> nodes) {
		this();
		addAll(nodes);
	}

	/**
	 * 从可类化数据读取中解析节点地址集合
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public NodeSet(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置循环下标，不允许是负数
	 * @param i 循环下标
	 */
	public void setIterateIndex(int i) {
		if (i < 0) {
			throw new IllegalValueException("cannot be < 0, this is %d", i);
		}
		iterateIndex = i;
	}

	/**
	 * 返回循环下标
	 * @return 循环下标
	 */
	public int getIterateIndex() {
		return iterateIndex;
	}

	/**
	 * 逻辑"与"操作：保留共有的站点地址，其它删除。
	 * @param set 被比较集合
	 */
	public void AND(Set<Node> set) {
		array.retainAll(set);
	}

	/**
	 * 逻辑"与"操作：保留共有的站点地址，其它删除。
	 * @param that 被比较集合
	 */
	public void AND(NodeSet that) {
		AND(that.array);
	}

	/**
	 * 逻辑"或"操作：重叠的保留一个，不重叠的也保留
	 * @param set 被比较集合
	 */
	public void OR(Set<Node> set) {
		array.addAll(set);
	}

	/**
	 * 逻辑"或"操作
	 * @param set 被比较集合
	 */
	public void OR(NodeSet set) {
		OR(set.array);
	}

	/**
	 * 增加一个节点地址。不允许空指针或者节点重叠的现象存在。
	 * @param e 节点地址
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean add(Node e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 删除一个节点地址。
	 * @param e 节点地址
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean remove(Node e) {
		if (e != null) {
			return array.remove(e);
		}
		return false;
	}

	/**
	 * 增加一组节点地址。每个节点地址都是唯一的，不允许空指针或者重叠现象存在。
	 * @param nodes 节点地址集合
	 * @return 返回增加的成员数
	 */
	public int addAll(Node[] nodes) {
		int size = array.size();
		for (int i = 0; nodes != null && i < nodes.length; i++) {
			add(nodes[i]);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批节点地址
	 * @param nodes 节点集合
	 * @return 增加的成员数目
	 */
	public int addAll(Collection<Node> nodes) {
		int size = array.size();
		if (nodes != null) {
			array.addAll(nodes);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存一批节点地址
	 * @param that NodeSet实例
	 * @return 返回新增加的节点数目
	 */
	public int addAll(NodeSet that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that.array);
		}
		return array.size() - size;
	}

	/**
	 * 删除一组节点地址.返回删除的数量
	 * @param nodes Node集合
	 * @return 返回删除的成员数目
	 */
	public int removeAll(Collection<Node> nodes) {
		int size = array.size();
		for (Node node : nodes) {
			remove(node);
		}
		return size - array.size();
	}

	/**
	 * 删除一组站点地址，返回删除的数量 
	 * @param that NodeSet实例
	 * @return 返回删除的成员数目
	 */
	public int removeAll(NodeSet that) {
		int size = array.size();
		array.removeAll(that.array);
		return size - array.size();
	}
	
	/**
	 * 以非锁定方式，判断节点地址存在
	 * @param node 节点实例
	 * @return 返回真或者假
	 */
	public boolean contains(Node node) {
		boolean success = (node != null);
		if (success) {
			success = array.contains(node);
		}
		return success;
	}
	
	/**
	 * 返回节点迭代器
	 * @return Node迭代
	 */
	public Iterator<Node> iterator() {
		return array.iterator();
	}

	/**
	 * 以非锁定和复制数据副本方式，输出全部节点地址
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(array);
	}

	/**
	 * 以非锁定方式和复制数据副本方式，输出全部节点地址
	 * @return Node集合
	 */
	public Set<Node> set() {
		return new TreeSet<Node>(array);
	}


	/**
	 * 以非锁定方式清除全部节点地址
	 */
	public void clear() {
		array.clear();
	}
	
	/**
	 * 统计节点地址数量
	 * @return 节点数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断节点地址是否为空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 以非锁定方式输出全部节点数组
	 * @return 节点数组
	 */
	public Node[] toArray() {
		Node[] a = new Node[array.size()];
		return array.toArray(a);
	}
	
	/**
	 * 以非锁定方式，返回前面一个节点
	 * @return Node实例
	 */
	public Node precede() {
		int size = array.size();
		// 超出，重置到最后的下标
		if (size > 0) {
			if (iterateIndex < 0) {  // 小于0时，恢复到最后下标
				iterateIndex = size - 1;
			}
			ArrayList<Node> a = new ArrayList<Node>(array);
			// 返回结果
			if (iterateIndex < a.size()) {
				Node sub = a.get(iterateIndex);
				iterateIndex--; // 自减1
				return sub;
			}
		}
		// 否则返回空指针
		return null;
	}
	
	/**
	 * 按照迭代编号，以非锁定的方式返回下一个站点地址。
	 * @return Node句柄；如果是空集合返回空指针
	 */
	public Node follow() {
		int size = array.size();
		if (size > 0) {
			// 超出，复位0下标
			if (iterateIndex >= size) {
				iterateIndex = 0;
			}
			// 枚举节点地址
			ArrayList<Node> a = new ArrayList<Node>(array);
			if (iterateIndex < a.size()) {
				Node sub = a.get(iterateIndex);
				iterateIndex++; // 自增1
				return sub;
			}
		}
		return null;
	}

	
//	/**
//	 * 按照迭代编号，以非锁定的方式返回下一个站点地址。
//	 * @return Node句柄；如果是空集合返回空指针
//	 */
//	public Node follow() {
//		int size = array.size();
//		if (size > 0) {
//			// 超出，复位0下标
//			if (iterateIndex >= size) {
//				iterateIndex = 0;
//			}
//			// 枚举节点地址
//			Iterator<Node> iterator = array.iterator();
//			for (int i = 0; iterator.hasNext(); i++) {
//				if (i < iterateIndex) {
//					iterator.next();
//				} else if (i == iterateIndex) {
//					iterateIndex++;// 自增1
//					return iterator.next();
//				}
//			}
//		}
//		return null;
//	}

	/**
	 * 根据传入的站点地址，以非锁定的方式返回它后面的站点地址。
	 * @param that 站点地址
	 * @return 返回下一个有效的站点地址；如果指定站点不存在，返回开始位置的站点地址；如果是空集合，返回空指针。
	 */
	public Node follow(Node that) {
		int size = array.size();
		if (size < 1) {
			return null;
		}

		Node scale = null;
		Iterator<Node> iterator = array.iterator();
		while (iterator.hasNext()) {
			Node node = iterator.next();
			// 找到这个地址
			if (node.compareTo(that) == 0) {
				if (iterator.hasNext()) {
					scale = iterator.next();
				} else {
					scale = array.iterator().next();
				}
				break;
			}
		}
		if(scale == null) {
			scale = array.iterator().next();
		}
		return scale;
	}

	/**
	 * 以锁定方式返回前面一个
	 * @return Node实例
	 */
	public Node previous() {
		// 锁定，返回前面一个
		super.lockSingle();
		try {
			return precede();
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 以锁定方式循环依次调用每一个节点地址
	 * @return 下一个节点
	 */
	public Node next() {
		super.lockSingle();
		try {
			return follow();
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 以锁定方式找到指定的主机位置开始，返回它的下一个节点地址。如果没有，返回NULL
	 * 根据传入的节点地址，返回它的后面的节点地址。
	 * @param that 当前节点
	 * @return 有效返回下一个地址，否则返回开始位置的地址。
	 */
	public Node next(Node that) {
		super.lockSingle();
		try {
			return follow(that);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 根据当前地址集合，克隆它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new NodeSet(this);
	}

	/**
	 * 以锁定方式增加一个节点地址。不允许空指针或者节点地址重叠的现象存在。
	 * @param node 节点地址
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean push(Node node) {
		boolean success = (node != null);
		super.lockSingle();
		try {
			if (success) {
				success = array.add(node);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 *以锁定方式 删除一个节点地址。
	 * @param node 节点地址
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean drop(Node node) {
		boolean success = (node != null);
		super.lockSingle();
		try {
			if (success) {
				success = array.remove(node);
			}
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 以锁定方式增加一组节点地址。每个节点地址都是唯一的，不允许重叠现象存在。
	 * @param nodes 节点数组
	 * @return 返回增加的成员数
	 */
	public int push(Node[] nodes) {
		int size = array.size();
		for (int i = 0; nodes != null && i < nodes.length; i++) {
			push(nodes[i]);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定方式增加一组节点地址
	 * @param nodes 运行节点地址集合
	 * @return 返回增加的成员数
	 */
	public int pushAll(Collection<Node> nodes) {
		int size = array.size();
		for (Node node : nodes) {
			push(node);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定方式增加一组节点地址
	 * @param that 节点集合
	 * @return 返回增加的成员数
	 */
	public int pushAll(NodeSet that) {
		int size = array.size();
		for (Node node : that.show()) {
			push(node);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定的方式，删除一组节点地址，返回删除的数量。
	 * @param nodes 节点集合
	 * @return 返回删除的成员数目
	 */
	public int dropAll(Collection<Node> nodes) {
		int size = array.size();
		for (Node node : nodes) {
			drop(node);
		}
		return size - array.size();
	}

	/**
	 * 以锁定的方式，删除一组节点地址
	 * @param that 节点集合
	 * @return 返回删除的成员数目
	 */
	public int dropAll(NodeSet that) {
		int size = array.size();
		for (Node node : that.show()) {
			drop(node);
		}
		return size - array.size();
	}

	/**
	 * 以锁定方式输出全部节点地址
	 * @return Node列表
	 */
	public List<Node> show() {
		super.lockSingle();
		try {
			return new ArrayList<Node>(array);
		} finally {
			super.unlockSingle();
		}
	}
	
	/**
	 * 以锁定方式，输出节点数组
	 * @return Node数组
	 */
	public Node[] array() {
		super.lockSingle();
		try {
			Node[] a = new Node[array.size()];
			return array.toArray(a);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 以锁定方式，判断节点地址存在
	 * @param node 节点地址
	 * @return 存在返回“真”，否则“假”。
	 */
	public boolean exists(Node node) {
		boolean success = (node != null);
		super.lockMulti();
		try {
			if (success) {
				success = array.contains(node);
			}
		} finally {
			super.unlockMulti();
		}
		return success;
	}
}