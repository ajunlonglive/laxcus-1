/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.scan;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 扫描数据表。<br><br>
 * 
 * FRONT节点：FRONT -> CALL -> DATA。<br>
 * WATCH节点：WATCH -> TOP/HOME -> DATA。<br><br>
 * 
 * 弱一致性检查，允许扫描过程中，部分节点存在错误。
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public final class ScanTable extends ScanReference {

	private static final long serialVersionUID = -5422281893187463816L;

	/** 数据表名 **/
	private TreeSet<Space> array = new TreeSet<Space>();

	/**
	 * 构造默认的扫描数据表实例
	 */
	public ScanTable() {
		super();
	}
	
	/**
	 * 从可类化读取器中解析扫描数据表实例
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ScanTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成传入实例的数据副本
	 * @param that ScanTable实例
	 */
	private ScanTable(ScanTable that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存数据表名
	 * @param e 数据表名
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Space e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批数据表名
	 * @param a 数据表名列表
	 * @return 返回新增成员数目
	 */
	public int addAll(List<Space> a) {
		int size = array.size();
		for (Space e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 判断包含
	 * @param e 数据表名
	 * @return 返回真或者假
	 */
	public boolean contains(Space e) {
		return array.contains(e);
	}

	/**
	 * 输出数据表名列表
	 * @return 返回数据表名列表
	 */
	public List<Space> list() {
		return new ArrayList<Space>(array);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ScanTable duplicate() {
		return new ScanTable(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.scan.ScanReference#buildSuffix(com.laxcus.util.classable.ClassWriter)
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
	 * @see com.laxcus.command.scan.ScanReference#resolveSuffix(com.laxcus.util.classable.ClassReader)
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
