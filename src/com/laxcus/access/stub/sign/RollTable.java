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
 * 被要求做回滚数据表。<br>
 * 
 * 回滚操作是DATA从站点从DATA主站点下载正确的数据块，从而保持数据块签名一致。
 * 
 * @author scott.liang
 * @version 1.0 4/12/2017
 * @since laxcus 1.0
 */
public class RollTable implements Classable, Cloneable, Serializable, Comparable<RollTable> {

	private static final long serialVersionUID = -2149253125439362864L;

	/** 主站点地址 **/
	private Node master;

	/** 表名 **/
	private Space space;

	/** 签名表 **/
	private TreeSet<StubSign> signs = new TreeSet<StubSign>();
	
	/**
	 * 构造默认的被要求做回滚数据表
	 */
	private RollTable() {
		super();
	}

	/**
	 * 生成回滚数据表的数据副本
	 * @param that
	 */
	private RollTable(RollTable that) {
		this();
		master = that.master;
		space = that.space;
		signs.addAll(that.signs);
	}

	/**
	 * 从可类化数据读取器中生成回滚数据表
	 * @param reader
	 */
	public RollTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成回滚数据表，指定站点
	 * @param prime
	 * @param space
	 */
	public RollTable(Node prime, Space space) {
		this();
		setMaster(prime);
		setSpace(space);
	}
	
	/**
	 * 设置主站点地址
	 * @param e Node实例
	 */
	public void setMaster(Node e) {
		Laxkit.nullabled(e);

		master = e;
	}

	/**
	 * 返回主站点地址
	 * @return Node实例
	 */
	public Node getMaster() {
		return master;
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
	 * @return 数据表名
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 保存一个数据块签名 
	 * @param e  数据块签名
	 * @return  保存成功返回“真”，否则“假”。
	 */
	public boolean add(StubSign e) {
		Laxkit.nullabled(e);
		
		return signs.add(e);
	}

	/**
	 * 保存一批数据块签名
	 * @param a  数据块签名集合
	 * @return  返回新增加的数据块签名数目
	 */
	public int addAll(Collection<StubSign> a) {
		int size = signs.size();
		for (StubSign e : a) {
			add(e);
		}
		return signs.size() - size;
	}

	/**
	 * 删除数据块签名
	 * @param e  数据块签名
	 * @return  删除成功返回“真”，否则“假”。
	 */
	public boolean remove(StubSign e) {
		return signs.remove(e);
	}

	/**
	 * 判断数据块签名存在
	 * @param e
	 * @return  存在返回“真”，否则“假”。
	 */
	public boolean contains(StubSign e) {
		return signs.contains(e);
	}

	/**
	 * 返回数据块签名列表
	 * @return
	 */
	public List<StubSign> list() {
		return new ArrayList<StubSign>(signs);
	}

	/**
	 * 返回数据块签名数目
	 * @return 整型值
	 */
	public int size() {
		return signs.size();
	}

	/**
	 * 判断数据块签名集合是空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return signs.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 生成数据副本
	 * @return  RollSite实例
	 */
	public RollTable duplicate() {
		return new RollTable(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RollTable that) {
		if (that == null) {
			return 1;
		}
		// 判断匹配
		int ret = Laxkit.compareTo(master, that.master);
		if (ret == 0) {
			ret = Laxkit.compareTo(space, that.space);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(master);
		writer.writeObject(space);
		writer.writeInt(signs.size());
		for (StubSign e : signs) {
			writer.writeObject(e);
		}
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		master = new Node(reader);
		space = new Space(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			StubSign sign = new StubSign(reader);
			signs.add(sign);
		}
		return reader.getSeek() - seek;
	}

}