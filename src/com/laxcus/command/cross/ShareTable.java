/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 共享数据表资源。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/30/2017
 * @since laxcus 1.0
 */
public abstract class ShareTable extends ShareCross {

	private static final long serialVersionUID = 6594016122363840616L;
	
	/** 数据表名 **/
	private Set<Space> array = new TreeSet<Space>();

	/**
	 * 构造默认的共享数据表资源
	 */
	protected ShareTable() {
		super();
	}

	/**
	 * 生成共享数据表资源的数据副本
	 * @param that ShareTable实例
	 */
	protected ShareTable(ShareTable that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个数据表名
	 * @param e Space实例
	 */
	public void addSpace(Space e) {
		Laxkit.nullabled(e);

		array.add(e);
	}
	
	/**
	 * 保存一批数据表名
	 * @param a 表名数组
	 * @return 返回新增成员数目
	 */
	public int addSpaces(Collection<Space> a) {
		int size = array.size();
		for (Space e : a) {
			addSpace(e);
		}
		return array.size() - size;
	}

	/**
	 * 返回全部数据表名
	 * @return Space列表
	 */
	public List<Space> getSpaces() {
		return new ArrayList<Space>(array);
	}

	/**
	 * 判断处理全部
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return array.size() == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.endow.ShareCross#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(array.size());
		for (Space e : array) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.endow.ShareCross#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space e = new Space(reader);
			array.add(e);
		}
	}
}