/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.pool;

import java.util.*;

import com.laxcus.site.*;

/**
 * 网关节点分配表
 * 
 * @author scott.liang
 * @version 1.0 6/11/2018
 * @since laxcus 1.0
 */
public class GatewayScaleTable {

	class GatewayStatus implements Comparable<GatewayStatus> {

		/** 网关节点 **/
		Node remote;

		/** 统计数目 **/
		int count;

		/**
		 * 构造实例
		 * @param node
		 * @param size
		 */
		GatewayStatus(Node node, int size) {
			remote = node;
			count = size;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(GatewayStatus that) {
			// 升序排序，低值在前
			return (count < that.count ? -1 : (count > that.count ? 1 : 0));
		}
	}

	/** 记录集合 **/
	private ArrayList<GatewayStatus> array = new ArrayList<GatewayStatus>();

	/**
	 * 构造默认的网关节点分配表
	 */
	public GatewayScaleTable() {
		super();
	}

	/**
	 * 保存一个参数，排序
	 * 
	 * @param node
	 * @param size
	 */
	public void add(Node node, int size) {
		GatewayStatus e = new GatewayStatus(node, size);
		array.add(e);
		Collections.sort(array);
	}

	/**
	 * 返回下一个
	 * 
	 * @return 节点地址
	 */
	public Node next() {
		if (array.isEmpty()) {
			return null;
		}
		GatewayStatus e = array.get(0);
		e.count++;
		Collections.sort(array);
		return e.remote;
	}

}