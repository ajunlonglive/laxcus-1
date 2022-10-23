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
 * 扫描数据库。<br><br>
 * 
 * FRONT节点：FRONT -> CALL -> DATA <br>
 * WATCH节点：WATCH -> TOP/HOME -> DATA <br><br>
 * 
 * 如果是WATCH节点发出，HOME节点发送给DATA节点前，要分解成 “SCAN TABLE”命令。<br>
 * 弱一致性检查，允许扫描过程中存在错误。<br>
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public final class ScanSchema extends ScanReference {

	private static final long serialVersionUID = 4499609330690391004L;

	/** 数据库集合 **/
	private TreeSet<Fame> array = new TreeSet<Fame>();

	/**
	 * 构造扫描数据库实例
	 */
	public ScanSchema() {
		super();
	}

	/**
	 * 从可类化读取器中解析扫描数据库实例
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ScanSchema(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成扫描数据库副本
	 * @param that ScanSchema实例
	 */
	private ScanSchema(ScanSchema that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个数据库名，不允许空指针
	 * @param e  数据库名
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Fame e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批数据库名
	 * @param a 数据库名列表
	 * @return 返回新增数据库名数目
	 */
	public int addAll(List<Fame> a) {
		int size = array.size();
		for (Fame e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 判断包含
	 * @param e 数据库名
	 * @return 返回真或者假
	 */
	public boolean contains(Fame e) {
		return array.contains(e);
	}
	
	/**
	 * 输出数据库名列表
	 * @return Fame列表
	 */
	public List<Fame> list() {
		return new ArrayList<Fame>(array);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ScanSchema duplicate() {
		return new ScanSchema(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.scan.ScanReference#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(array.size());
		for (Fame e : array) {
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
			Fame e = new Fame(reader);
			array.add(e);
		}
	}
}