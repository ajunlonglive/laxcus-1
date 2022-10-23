/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.home;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * HOME站点成员<br><br>
 * 
 * @author scott.liang
 * @version 1.1 6/12/2015
 * @since laxcus 1.0
 */
public final class HomeMember extends SiteMember {

	private static final long serialVersionUID = -2974719415729019808L;

	/** 数据表名集合 **/
	private TreeSet<Space> array = new TreeSet<Space>();

	/**
	 * 根据传入HOME站点成员参数，生成它的数据副本
	 * @param that HomeMember实例
	 */
	private HomeMember(HomeMember that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造默认的HOME站点成员
	 */
	private HomeMember() {
		super();
	}

	/**
	 * 构造HOME站点成员，指定数据持有人
	 * @param siger 持有人
	 */
	public HomeMember(Siger siger) {
		this();
		setSiger(siger);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public HomeMember(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 指定数据表名
	 * @param e Space实例
	 * @return 返回真或者假
	 */
	public boolean addSpace(Space e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 删除数据表名
	 * @param e Space实例
	 * @return 返回真或者假
	 */
	public boolean removeSpace(Space e) {
		return array.remove(e);
	}

	/**
	 * 返回全部数据表名
	 * @return Space列表
	 */
	public List<Space> getSpaces() {
		return new ArrayList<Space>(array);
	}

	/**
	 * 判断包含数据表名
	 * @param e Space实例
	 * @return 返回真或者假
	 */
	public boolean contains(Space e) {
		return array.contains(e);
	}

	/**
	 * 判断集合是空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty(); 
	}

	/**
	 * 清除全部
	 */
	public void reset() {
		array.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.SiteMember#duplicate()
	 */
	@Override
	public HomeMember duplicate() {
		return new HomeMember(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.SiteMember#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 数据表名集合
		writer.writeInt(array.size());
		for (Space e : array) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.SiteMember#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 数据表名集合
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space e = new Space(reader);
			array.add(e);
		}
	}

}