/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据表成员站点。<br>
 * 
 * HOME集群下面有四个数据表成员站点：CALL/DATA/WORK/BUILD。
 * 
 * @author scott.liang
 * @version 1.0 7/17/2012
 * @since laxcus 1.0
 */
public abstract class TableMember extends PhaseMember {

	private static final long serialVersionUID = -2703364673507908645L;

	/** 数据表名集合 **/
	private TreeSet<Space> spaces = new TreeSet<Space>();

	/**
	 * 根据传入数据表站点成员参数，生成它的数据副本
	 * @param that TableMember实例
	 */
	protected TableMember(TableMember that) {
		super(that);
		spaces.addAll(that.spaces);
	}

	/**
	 * 构造默认的数据表站点成员
	 */
	protected TableMember() {
		super();
	}

	/**
	 * 指定数据表名
	 * @param e Space实例
	 * @return 返回真或者假
	 */
	public boolean addTable(Space e) {
		Laxkit.nullabled(e);

		return spaces.add(e);
	}

	/**
	 * 删除数据表名
	 * @param e Space实例
	 * @return 返回真或者假
	 */
	public boolean removeTable(Space e) {
		Laxkit.nullabled(e);
		return spaces.remove(e);
	}

	/**
	 * 返回数据表名集合
	 * @return Space列表
	 */
	public List<Space> getTables() {
		return new ArrayList<Space>(spaces);
	}

	/**
	 * 判断包含数据表名
	 * @param e Space实例
	 * @return 返回真或者假
	 */
	public boolean contains(Space e) {
		return spaces.contains(e);
	}
	
	/**
	 * 判断数据表是空
	 * @return 返回真或者假
	 */
	public boolean isTableEmpty() {
		return spaces.size() ==0;
	}

	/**
	 * 统计数据表数目
	 * @return 数据表数目
	 */
	public int getTableSize() {
		return spaces.size();
	}
	
	/**
	 * 判断集合是空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return isPhaseEmpty() && isTableEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.PhaseMember#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		spaces.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.PhaseMember#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 数据表名集合
		writer.writeInt(spaces.size());
		for (Space e : spaces) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.PhaseMember#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 数据表名集合
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space e = new Space(reader);
			spaces.add(e);
		}
	}

}