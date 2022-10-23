/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 许可账号表。用于CALL、WORK、BUILD节点的运行中的账号检测上。
 * 
 * @author scott.liang
 * @version 1.0 9/15/2012
 * @since laxcus 1.0
 */
public final class Authority implements Classable, Cloneable, Serializable, Comparable<Authority> {

	private static final long serialVersionUID = -7551338250831178125L;
	
	/** 最大成员数 **/
	private int members;

	/** 许可用户名称集合 **/
	private TreeSet<Siger> array = new TreeSet<Siger>();

	/**
	 * 将许可账号表参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 最大成员数
		writer.writeInt(members);
		// 注册成员数量
		writer.writeInt(array.size());
		// 注册成员
		for(Siger e : array) {
			writer.writeObject(e);
		}
		// 返回写入的字节长度
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析许可账号表参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 最大成员数
		members = reader.readInt();
		// 注册成员数量
		int size = reader.readInt();
		// 解析注册成员和保存
		for (int i = 0; i < size; i++) {
			Siger e = new Siger(reader);
			array.add(e);
		}
		// 返回读出的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的许可账号表，生成它的数据副本
	 * @param that
	 */
	private Authority(Authority that) {
		super();
		members = that.members;
		array.addAll(that.array);
	}

	/**
	 * 构造许可账号表
	 */
	public Authority() {
		super();
		members = 1;
	}

	/**
	 * 构造许可账号表，从可类化读取器中解析参数
	 * @param reader
	 */
	public Authority(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置最多账号数目
	 * @param i
	 */
	public void setMembers(int i) {
		if (i < 1) {
			throw new IllegalArgumentException("illegal " + i);
		}
		members = i;
	}

	/**
	 * 返回允许的最大连接数
	 * @return
	 */
	public int getMembers() {
		return members;
	}

	/**
	 * 保存一个许可用户名
	 * @param e - 许可用户名
	 * @return - 如果不存在，添加和返回true；否则返回false。
	 */
	public boolean add(Siger e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 删除一个许可用户名
	 * @param e - 许可用户名
	 * @return
	 */
	public boolean remove(Siger e) {
		return array.remove(e);
	}

	/**
	 * 检查一个账号用户名是否在允许范围内。
	 * @param user - 账号用户名。
	 * @return - 允许返回true，否则返回false。
	 */
	public boolean inside(Siger user) {		
		return array.contains(user);
	}

	/**
	 * 返回许可用户名列表
	 * @return
	 */
	public List<Siger> list() {
		return new ArrayList<Siger>(array);
	}
	
	/**
	 * 返回许可用户名集合
	 * @return
	 */
	public Set<Siger> set() {
		return new TreeSet<Siger>(array);
	}
	
	/**
	 * 返回许可用户名的数组。如果是空集，返回0长度的数组。
	 * @return
	 */
	public Siger[] toArray() {
		Siger[] s = new Siger[array.size()];
		return array.toArray(s);
	}

	/**
	 * 返回许可用户名的数量
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 如果没有许可用户名，返回true；否则返回false。
	 * @return
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 比较两个许可账号表是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Authority.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Authority) that) == 0;
	}

	/**
	 * 返回此许可账号表的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return array.size();
	}

	/**
	 * 返回此许可账号表的浅表副本，没有复制内部的成员本身。
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new Authority(this);
	}

	/**
	 * 以升序排列两个许可账号表的先后顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Authority that) {
		int ret = Laxkit.compareTo(array.size(), that.array.size());
		if (ret == 0) {
			Iterator<Siger> e1 = array.iterator();
			Iterator<Siger> e2 = that.array.iterator();
			while (e1.hasNext()) {
				ret = (e1.next().compareTo(e2.next()));
				if (ret == 0) break;
			}
		}
		return ret;
	}

}