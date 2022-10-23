/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import java.util.*;

import com.laxcus.application.manage.*;
import com.laxcus.util.*;

/**
 * 启动器
 * 
 * @author scott.liang
 * @version 1.0 2/8/2022
 * @since laxcus 1.0
 */
final class Starter implements Comparable<Starter> {
	
	/** 启动键 **/
	private WKey key;

	/** 启动链数组 **/
	private ArrayList<String> array = new ArrayList<String>();

	/**
	 * 构造启动器
	 * @param key
	 */
	public Starter(WKey key) {
		super();
		setKey(key);
	}

	/**
	 * 设置启动键
	 * @param e
	 */
	public void setKey(WKey e) {
		key = e;
	}

	/**
	 * 返回启动键
	 * @return
	 */
	public WKey getKey() {
		return key;
	}

	/**
	 * 保存启动链
	 * @param link
	 */
	public void add(String link) {
		array.add(link);
	}

	/**
	 * 输出全部启动链
	 * @return
	 */
	public String[] toArray() {
		String[] a = new String[array.size()];
		return array.toArray(a);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Starter) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Starter that) {
		if (that == null) {
			return 1;
		}
		// 比较
		return Laxkit.compareTo(key, that.key);
	}

}