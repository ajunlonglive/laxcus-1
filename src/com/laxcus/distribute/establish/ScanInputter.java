/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * ESTABLISH.SCAN阶段参数输入器。<br>
 * 
 * 参数来自FRONT站点的用户输入。<br>
 * 
 * @author scott.liang
 * @version 1.1 7/26/2015
 * @since laxcus 1.0
 */
public final class ScanInputter extends EstablishInputter {

	private static final long serialVersionUID = -3822255443127044743L;

	/** 被扫描的表名 **/
	private Set<Space> array = new TreeSet<Space>();

	/**
	 * 使用传入SCAN参数输入器，生成它的副本
	 * @param that ScanInputter实例
	 */
	private ScanInputter(ScanInputter that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造SCAN阶段参数输入器
	 */
	public ScanInputter() {
		super();
	}

	/**
	 * 构造SCAN阶段参数输入器，并且指定它的命名(必须是SCAN阶段)
	 * @param phase 阶段命名
	 */
	public ScanInputter(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ScanInputter(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个数据表名
	 * @param e Space实例
	 * @return 返回真或者假
	 */
	public boolean addSpace(Space e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批数据表名
	 * @param a Space数组
	 * @return 返回新增成员数目
	 */
	public int addSpaces(List<Space> a) {
		int size = array.size();
		for (Space e : a) {
			addSpace(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出数据表名集合
	 * @return Space列表
	 */
	public List<Space> getSpaces() {
		return new ArrayList<Space>(array);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeObject#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger username) {
		super.setIssuer(username);
	}

	/**
	 * 生成SCAN输入命名阶段对象的副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public ScanInputter duplicate() {
		return new ScanInputter(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	protected void buildSuffix(ClassWriter writer) {
		// 输出前缀信息
		super.buildSuffix(writer);
		// 数据表名集合
		writer.writeInt(array.size());
		for (Space space : array) {
			writer.writeObject(space);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	protected void resolveSuffix(ClassReader reader) {
		// 解析前缀信息
		super.resolveSuffix(reader);
		// 数据表名集合
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space space = new Space(reader);
			array.add(space);
		}
	}
}