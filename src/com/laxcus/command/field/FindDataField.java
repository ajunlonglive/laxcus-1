/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.field;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 查找DATA站点元数据命令。<br>
 * 
 * 这个命令由CALL站点发向HOME站点，HOME站点根据数据表名判断后，转发给DATA/WORK/BUILD站点，再发回给CALL站点。
 * 
 * @author scott.liang
 * @version 1.1 10/12/2015
 * @since laxcus 1.0
 */
public final class FindDataField extends FindField {

	private static final long serialVersionUID = 7712245586310859300L;

	/** 数据表名集合 **/
	private TreeSet<Space> spaces = new TreeSet<Space>();

	/**
	 * 根据传入的查找DATA站点元数据命令，生成它的数据副本
	 * @param that FindDataField实例
	 */
	private FindDataField(FindDataField that) {
		super(that);
		spaces.addAll(that.spaces);
	}

	/**
	 * 构造默认的查找DATA站点元数据命令
	 */
	public FindDataField() {
		super();
	}

	/**
	 * 构造查找DATA站点元数据命令，指定命令发起方地址
	 * @param from 发起方站点地址
	 */
	public FindDataField(Node from) {
		this();
		setNode(from);
	}

	/**
	 * 从可类化数据读取器中解析查找DATA站点元数据命令
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public FindDataField(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据表名，不允许空值
	 * @param e 数据表名
	 * @return 保存成功返回真，否则假
	 */
	public boolean addSpace(Space e) {
		if (e != null) {
			return spaces.add(e);
		}
		return false;
	}

	/**
	 * 保存全部数据表名
	 * @param a 全部数据表名
	 * @return 返回新增成员数目
	 */
	public int addSpaces(Collection<Space> a) {
		int size = spaces.size();
		for (Space e : a) {
			addSpace(e);
		}
		return spaces.size() - size;
	}

	/**
	 * 输出全部数据表名
	 * @return Space列表
	 */
	public List<Space> getSpaces() {
		return new ArrayList<Space>(spaces);
	}

	/**
	 * 统计数据表名的数目
	 * @return 表名数目
	 */
	public int getSpaceCount() {
		return spaces.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindDataField duplicate() {
		return new FindDataField(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 数据表名
		writer.writeInt(spaces.size());
		for (Space e : spaces) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 数据表名
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space e = new Space(reader);
			spaces.add(e);
		}
	}

}