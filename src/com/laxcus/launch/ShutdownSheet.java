/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 远程关闭地址表。<br>
 * 在得到来自网络的“关闭站点指令”时，如果命令源头地址在上，远程关闭将被接受。
 * 
 * @author scott.liang 
 * @version 1.0 5/16/2009
 * @since laxcus 1.0
 */
final class ShutdownSheet {

	/** 保存被授权，允许远程关闭的网络地址 **/
	private Set<Address> array = new TreeSet<Address>();

	/**
	 * 构造远程关闭地址表
	 */
	public ShutdownSheet() {
		super();
	}

	/**
	 * 保存一个网络地址
	 * @param e
	 * @return
	 */
	public boolean add(Address e) {
		Laxkit.nullabled(e);

		return array.add(e.duplicate());
	}

	/**
	 * 保存一组网络地址
	 * @param a
	 * @return 返回保存的数目
	 */
	public int addAll(Address[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			this.add(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 返回地址表集合
	 * @return
	 */
	public List<Address> list() {
		return new ArrayList<Address>(this.array);
	}

	/**
	 * 判断传入的地址存在
	 * @param e
	 * @return
	 */
	public boolean contains(Address e) {
		return this.array.contains(e);
	}

	/**
	 * 清空地址表
	 */
	public void clear() {
		this.array.clear();
	}

	/**
	 * 返回地址成员数目
	 * @return
	 */
	public int size() {
		return this.array.size();
	}

	/**
	 * 判断地址集合是空
	 * @return
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

}