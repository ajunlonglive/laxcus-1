/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.secure;

import java.io.*;
import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 删除密钥令牌检测单元
 * 
 * @author scott.liang
 * @version 1.0 2/12/2021
 * @since laxcus 1.0
 */
public final class DropSecureTokenItem implements Classable, Cloneable, Serializable, Comparable<DropSecureTokenItem> {

	private static final long serialVersionUID = -3216126542920859744L;

	/** 站点地址 **/
	private Node node;
	
	/** 成功或者否 **/
	private boolean successful;
	
	/** 显示结果 **/
	private ArrayList<DropSecureTokenSlice> array = new ArrayList<DropSecureTokenSlice>();

	/**
	 * 构造默认的被刷新处理单元
	 */
	public DropSecureTokenItem() {
		super();
	}

	/**
	 * 根据传入实例，生成删除密钥令牌检测单元的数据副本
	 * @param that DropSecureTokenItem实例
	 */
	private DropSecureTokenItem(DropSecureTokenItem that) {
		super();
		node = that.node;
		successful = that.successful;
		array.addAll(that.array);
	}
	
	/**
	 * 构造删除密钥令牌检测单元，指定站点地址和处理结果
	 * @param node 站点地址
	 * @param successful 成功
	 */
	public DropSecureTokenItem(Node node, boolean successful) {
		this();
		setSite(node);
		setSuccessful(successful);
	}
	
	/**
	 * 从可类化数据读取器中删除密钥令牌检测单元
	 * @param reader 可类化数据读取器
	 */
	public DropSecureTokenItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置站点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		node = e;
	}

	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return node;
	}

	/**
	 * 设置成功标识
	 * @param b 成功标识
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断是成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * 保存一个密钥令牌，不允许空指针
	 * @param e DropSecureTokenSlice实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(DropSecureTokenSlice e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批密钥令牌
	 * @param a DropSecureTokenSlice数组
	 * @return 返回新增成员数目
	 */
	public int addAll(List<DropSecureTokenSlice> a) {
		int size = array.size();
		for (DropSecureTokenSlice e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 输出全部密钥令牌
	 * @return DropSecureTokenSlice列表
	 */
	public List<DropSecureTokenSlice> list() {
		return new ArrayList<DropSecureTokenSlice>(array);
	}

	/**
	 * 清除
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 地址成员数目
	 * @return 成员数目
	 */
	public int size() {
		return array.size();
	}
	
	/**
	 * 生成当前实例的数据副本
	 * @return DropSecureTokenItem实例
	 */
	public DropSecureTokenItem duplicate() {
		return new DropSecureTokenItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || getClass() != that.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((DropSecureTokenItem ) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return node.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return node.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DropSecureTokenItem that) {
		if (that == null) {
			return 1;
		}
		// 比较参数
		return Laxkit.compareTo(node, that.node);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		buildSuffix(writer);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		resolveSuffix(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 保存参数
	 * @param writer 
	 */
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(node);
		writer.writeBoolean(successful);
		writer.writeInt(array.size());
		for (DropSecureTokenSlice e : array) {
			writer.writeObject(e);
		}
	}

	/**
	 * 解析参数
	 * @param reader
	 */
	protected void resolveSuffix(ClassReader reader) {
		node = new Node(reader);
		successful = reader.readBoolean();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			DropSecureTokenSlice e = new DropSecureTokenSlice(reader);
			array.add(e);
		}
	}

}