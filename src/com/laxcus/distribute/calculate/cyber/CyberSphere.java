/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.calculate.cyber;

import java.util.*;
import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 网络数据空间<br>
 * 
 * CyberSphere包含一个或者多个CyberArea <br><br>
 * 
 * 在“CyberXxx”前缀的网络数据层次定义顺序中，从上往下依次是：<br>
 * 1. CyberSphere <br>
 * 2. CyberArea <br>
 * 3. CyberField <br><br>
 * 
 * @author scott.liang
 * @version 1.1 03/18/2015
 * @since laxcus 1.0
 */
public final class CyberSphere implements Classable, Cloneable, Serializable {
	
	private static final long serialVersionUID = 6876113926806288140L;
	
	/** 数据区数组 **/
	private Map<Long, CyberArea> array = new TreeMap<Long, CyberArea>();

	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that
	 */
	private CyberSphere(CyberSphere that) {
		this();
		for (CyberArea area : that.array.values()) {
			add(area.duplicate());
		}
	}
	
	/**
	 * 构造默认的网络数据空间
	 */
	public CyberSphere() {
		super();
	}
	
	/**
	 * 从可类化数据读取器中解析网络数据区
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CyberSphere(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 保存一个网络数据区
	 * @param e CyberArea实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(CyberArea e) {
		Laxkit.nullabled(e);

		// 如果存在，不保存
		if(array.containsKey(e.getMod())) {
			return false;
		}
		return array.put(e.getMod(), e) == null;
	}
	
	/**
	 * 根据模值，删除网络数据区
	 * @param mod 模值
	 * @return 成功返回真，否则假
	 */
	public boolean remove(long mod) {
		return array.remove(mod) != null;
	}

	/**
	 * 根据模值，查找一个网络数据区
	 * @param mod 模值
	 * @return 返回CyberArea实例，或者空值
	 */
	public CyberArea find(long mod) {
		return array.get(mod);
	}
	
	/**
	 * 删除一个网络数据区
	 * @param e CyberArea实例
	 * @return 成功返回真，否则假
	 */
	public boolean remove(CyberArea e) {
		Laxkit.nullabled(e);

		return remove(e.getMod());
	}
	
	/**
	 * 替换网络数据区
	 * @param e CyberArea实例
	 */
	public void update(CyberArea e) {
		remove(e);
		add(e);
	}

	/**
	 * 输出全部模值
	 * @return 长整型集合
	 */
	public Set<Long> getMods() {
		return new TreeSet<Long>(array.keySet());
	}
	
	/**
	 * 输出全部网络数据区
	 * @return CyberArea列表
	 */
	public List<CyberArea> list() {
		return new ArrayList<CyberArea>(array.values());
	}
	
	/**
	 * 返回成员数目
	 * @return 成员数目的整型
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return CyberSphere实例
	 */
	public CyberSphere duplicate() {
		return new CyberSphere(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		writer.writeInt(array.size());
		for (CyberArea area : array.values()) {
			writer.writeObject(area);
		}
		return writer.size() - scale;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			CyberArea area = new CyberArea(reader);
			add(area);
		}
		return reader.getSeek() - scale;
	}

}