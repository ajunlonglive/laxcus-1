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
 * ESTABLISH.SIFT阶段参数输入接口。<br>
 * 
 * 参数来自FRONT站点的用户输入。<br>
 * 
 * @author scott.liang
 * @version 1.1 7/26/2015
 * @since laxcus 1.0
 */
public final class SiftInputter extends EstablishInputter {

	private static final long serialVersionUID = 6622456105437909842L;

	/** “SIFT”阶段分析的列空间集合 **/
	private Set<Dock> docks = new TreeSet<Dock>();

	/**
	 * 使用传入SIFT输入接口，生成它的副本
	 * @param that SiftInputter实例
	 */
	private SiftInputter(SiftInputter that) {
		super(that);
		// 保存列空间
		docks.addAll(that.docks);
	}

	/**
	 * 构造SIFT阶段输入接口
	 */
	public SiftInputter() {
		super();
	}

	/**
	 * 构造SIFT阶段输入接口，并且指定它的命名(必须是SIFT阶段)
	 * @param phase 阶段命名
	 */
	public SiftInputter(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SiftInputter(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个列空间
	 * @param e Dock实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addDock(Dock e) {
		Laxkit.nullabled(e);

		return docks.add(e);
	}

	/**
	 * 保存一组列空间
	 * @param a
	 * @return 返回新增成员数目
	 */
	public int addDocks(List<Dock> a) {
		int size = docks.size();
		for (Dock e : a) {
			addDock(e);
		}
		return docks.size() - size;
	}

	/**
	 * 返回列空间集合
	 * @return List<Dock>
	 */
	public List<Dock> getDocks() {
		return new ArrayList<Dock>(docks);
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
	 * 生成SIFT输入命名阶段对象的副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public SiftInputter duplicate() {
		return new SiftInputter(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	protected void buildSuffix(ClassWriter writer) {
		// 输出前缀信息
		super.buildSuffix(writer);
		// 列空间集合
		writer.writeInt(docks.size());
		for (Dock that : docks) {
			writer.writeObject(that);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	protected void resolveSuffix(ClassReader reader) {
		// 解析前缀信息
		super.resolveSuffix(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Dock dock = new Dock(reader);
			addDock(dock);
		}
	}
}