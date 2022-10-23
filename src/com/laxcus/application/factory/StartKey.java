/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.factory;

import java.util.*;

/**
 * 启动关联键
 * 
 * @author scott.liang
 * @version 1.0 1/27/2022
 * @since laxcus 1.0
 */
public class StartKey implements Comparable<StartKey> {

	/** 类型 **/
	private String type;

	/** 关联 **/
	private ArrayList<StartToken> array = new ArrayList<StartToken>();
	
	/**
	 * 构造默认的启动关联键
	 * @param type
	 */
	public StartKey(String type) {
		super();
		setType(type);
	}
	
	/**
	 * 设置类型
	 * @param s
	 */
	public void setType(String s) {
		type = s;
	}

	/**
	 * 返回类型
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * 增加启动令牌
	 * @param key
	 * @return
	 */
	public boolean add(StartToken key) {
		if (key == null) {
			return false;
		}
		// 如果不存在，保存
		if (!array.contains(key)) {
			return array.add(key);
		}
		return false;
	}
	
	/**
	 * 加载一批
	 * @param tokens
	 */
	public int addAll(Collection<StartToken> tokens) {
		int size = array.size();
		for (StartToken e : tokens) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 删除实例
	 * @param key
	 */
	public void remove(StartToken key) {
		array.remove(key);
	}
	
	/**
	 * 返回指定下标时的启动令牌
	 * @param index 下标
	 * @return 返回启动令牌
	 */
	public StartToken get(int index) {
		if (index < 0 || index >= array.size()) {
			return null;
		}
		return array.get(index);
	}

	/**
	 * 统计值
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空值
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 返回全部启动令牌
	 * @return
	 */
	public List<StartToken> list() {
		return new ArrayList<StartToken>(array);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		return compareTo((StartKey) that) == 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (type == null) {
			return 0;
		}
		return type.toLowerCase().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StartKey that) {
		if (that == null) {
			return -1;
		}
		return type.compareToIgnoreCase(that.type);
	}

}