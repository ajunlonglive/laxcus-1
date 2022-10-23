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

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 一个LAXCUS集群的全部数据块集合
 * 
 * @author scott.liang
 * @version 1.0 9/12/2016
 * @since laxcus 1.0
 */
public final class SignSheet implements Classable, Cloneable, Serializable, Comparable<SignSheet> {

	private static final long serialVersionUID = 9112819457959169439L;

	/** 表名 **/
	private Space space;

	/** 数据块编号 -> 同数据块单元集合 **/
	private TreeMap<Long, LikeSheet> array = new TreeMap<Long, LikeSheet>();

	/**
	 * 构造数据块签名表
	 */
	public SignSheet() {
		super();
	}

	/**
	 * 构造数据块签名表，指定表名
	 * @param space 表名
	 */
	public SignSheet(Space space) {
		this();
		setSpace(space);
	}
	
	/**
	 * 根据传入实例，生成它的数据副本
	 * @param that SignSheet
	 */
	private SignSheet(SignSheet that) {
		this();
		space = that.space;
		array.putAll(that.array);
	}

	/**
	 * 从可类化读取器中解析SignSheet参数
	 * @param reader 可类化读取器
	 */
	public SignSheet(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置数据表名，不允许空值
	 * @param e  数据表名
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
		return this.space;
	}
	
	/**
	 * 返回全部数据块编号
	 * @return 数据块编号列表
	 */
	public List<Long> keys() {
		return new ArrayList<Long>(array.keySet());
	}

	/**
	 * 返回全部数据块数目
	 * @return 数据块数目的整型值
	 */
	public long countStubs() {
		long count = 0;
		for (LikeSheet e : array.values()) {
			count += e.size();
		}
		return count;
	}
	
	/**
	 * 统计匹配的数据块数目
	 * @return 匹配的数据块数目
	 */
	public long countIdenticals() {
		long count = 0;
		Iterator<Map.Entry<Long, LikeSheet>> iterator = array.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Long, LikeSheet> entry = iterator.next();
			count += entry.getValue().countIdenticals();
		}
		// 返回全部数据块数目
		return count;
	}

	/**
	 * 根据数据块编号，查找它的表集合
	 * @param stub  数据块编号
	 * @return  LikeSheet
	 */
	public LikeSheet find(long stub) {
		return array.get(stub);
	}

	/**
	 * 保存一个数据块单元
	 * @param node 站点地址
	 * @param sign 签名
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node node, StubSign sign) {
		SignItem item = new SignItem(node, sign);
		LikeSheet sheet = array.get(sign.getStub());
		if (sheet == null) {
			sheet = new LikeSheet();
			array.put(sign.getStub(), sheet);
		}
		return sheet.add(item);
	}
	
	/**
	 * 生成数据副本
	 * @return SignSheet实例
	 */
	public SignSheet duplicate() {
		return new SignSheet(this);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SignSheet that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(space, that.space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeObject(space);
		writer.writeInt(array.size());

		Iterator<Map.Entry<Long, LikeSheet>> iterator = array.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Long, LikeSheet> entry = iterator.next();
			writer.writeLong(entry.getKey());
			writer.writeObject(entry.getValue());
		}

		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.readInt();
		space = new Space(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			long stub = reader.readLong();
			LikeSheet sheet = new LikeSheet(reader);
			array.put(stub, sheet);
		}
		return reader.getSeek() - seek;
	}

}
