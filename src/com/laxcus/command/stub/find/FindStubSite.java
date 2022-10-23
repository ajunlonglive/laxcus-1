/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.find;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 通过数据表名和数据块编号，查询关联的DATA站点 <br>
 * 
 * @author scott.liang
 * @version 1.1 12/7/2015
 * @since laxcus 1.0
 */
public abstract class FindStubSite extends Command {
	
	private static final long serialVersionUID = -3076304080387478444L;

	/** 数据表名 **/
	private Space space;
	
	/** 数据块编号集合 **/
	private TreeSet<Long> stubs = new TreeSet<Long>();

	/**
	 * 生成FindStubSite命令的数据副本
	 * @param that FindStubSite命令
	 */
	protected FindStubSite(FindStubSite that) {
		super(that);
		space = that.space;
		stubs.addAll(that.stubs);
	}

	/**
	 * 构造默认的FindStubSite命令
	 */
	protected FindStubSite() {
		super();
	}
	
	/**
	 * 设置数据表名（允许空值）
	 * @param e 数据表名
	 */
	public void setSpace(Space e) {
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
	 * 保存数据块编号
	 * @param e 数据块编号
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Long e) {
		if (e != null) {
			return stubs.add(e);
		}
		return false;
	}

	/**
	 * 保存一批数据块编号
	 * @param a 数据块编号数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Long> a) {
		int size = stubs.size();
		for (Long e : a) {
			add(e);
		}
		return stubs.size() - size;
	}

	/**
	 * 以列表的形式返回全部数据块编号的副本
	 * @return 数据块编号列表
	 */
	public List<Long> list() {
		return new ArrayList<Long>(stubs);
	}

	/**
	 * 以集合的形式返回全部数据块编号的副本
	 * @return 数据块编号集合
	 */
	public Set<Long> set() {
		return new TreeSet<Long>(stubs);
	}

	/**
	 * 统计数据块编号数目
	 * @return 数据块编号数目
	 */
	public int size() {
		return stubs.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.post.PostMethod#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		// 数据表名
		writer.writeInstance(space);
		// 数据块编号数组
		writer.writeInt(stubs.size());
		for (Long e : stubs) {
			writer.writeLong(e.longValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.post.PostMethod#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		// 数据表名
		space = reader.readInstance(Space.class);
		// 数据块编号数组
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			long stub = reader.readLong();
			stubs.add(stub);
		}
	}


}