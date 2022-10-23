/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub.sign;

import java.io.*;
import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 基于同一个数据编号的分布数据块集合
 * 
 * @author scott.liang
 * @version 1.0 9/12/2016
 * @since laxcus 1.0
 */
public final class LikeSheet implements Classable ,Cloneable, Serializable {

	private static final long serialVersionUID = 3934457253627478522L;

	/** 成员数组 **/
	private TreeSet<SignItem> array = new TreeSet<SignItem>();

	/**
	 * 构造分布数据块集合
	 */
	public LikeSheet() {
		super();
	}

	/**
	 * 生成分布数据块集合副本
	 * @param that LikeSheet实例
	 */
	private LikeSheet(LikeSheet that) {
		this();
		array.addAll(that.array);
	}

	/**
	 * 从可类化数据读取器中解析分布数据块集合
	 * @param reader  可类化数据读取器
	 */
	public LikeSheet(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存数据块签名单元
	 * @param e 数据块签名单元
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(SignItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存数据块签名单元
	 * @param node 站点地址
	 * @param sign 签名
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node node, StubSign sign) {
		SignItem item = new SignItem(node, sign);
		return add(item);
	}

	/**
	 * 返回首选签名
	 * @return  返回SignItem，没有返回空指针
	 */
	public SignItem getPreferredSign() {
		for (SignItem item : array) {
			if (item.isMaster()) {
				return item;
			}
		}
		return null;
	}

	/**
	 * 以主块为基准，判断一致性的数据块数目
	 * @return 返回一致性的数据块数目
	 */
	public int countIdenticals() {
		SignItem item = getPreferredSign();
		if (item == null) {
			return 0;
		}

		int size = 0;
		// 判断签名一致
		for (SignItem e : array) {
			int ret = Laxkit.compareTo(item.getHash(), e.getHash());
			if (ret == 0) {
				size++;
			}
		}
		// 返回一致的数目块
		return size;
	}

	/**
	 * 判断签名一致性
	 * @return 返回真或者假
	 */
	public boolean isIdenticalSign() {
		int size = countIdenticals();
		return size == array.size();
	}

	/**
	 * 返回成员数目
	 * @return 整型值的成员数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 返回数据副本
	 * @return LikeSheet实例
	 */
	public LikeSheet duplicate() {
		return new LikeSheet(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int scale = writer.size();
		writer.writeInt(array.size());
		for (SignItem e : array) {
			writer.writeObject(e);
		}
		return writer.size() - scale;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.readInt();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SignItem e = new SignItem(reader);
			array.add(e);
		}
		return array.size() - seek;
	}

}