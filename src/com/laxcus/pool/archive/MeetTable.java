/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.pool.archive;

import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 用户签名表 <br>
 * 
 * 保存一个ARCHIVE站点地址，和它关联的用户签名。
 * 
 * @author scott.liang
 * @version 1.0 8/16/2014
 * @since laxcus 1.0
 */
final class MeetTable {

	/** ARCHIVE站点地址 **/
	private Node node;

	/** 用户签名集合 **/
	private TreeSet<Siger> array = new TreeSet<Siger>();

	/**
	 * 构造默认和私有的用户签名表
	 */
	private MeetTable() {
		super();
	}

	/**
	 * 构造用户签名表，指定ARCHIVE站点地址
	 * @param node - ARCHIVE站点地址
	 */
	public MeetTable(Node node) {
		this();
		setNode(node);
	}
	
	/**
	 * 设置节点地址。如果是子类是网关站点，这个地址属于内网地址。
	 * @param e - 节点地址
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);

		node = e.duplicate();
	}

	/**
	 * 返回节点地址。
	 * @return - Node实例
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * 保存用户签名
	 * @param e
	 * @return
	 */
	public boolean add(Siger e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 删除用户签名
	 * @param e
	 * @return
	 */
	public boolean remove(Siger e) {
		return array.remove(e);
	}

	/**
	 * 判断签名存在
	 * @param e
	 * @return
	 */
	public boolean contains(Siger e) {
		return array.contains(e);
	}

	/**
	 * 输出全部签名
	 * @return
	 */
	public List<Siger> list() {
		return new ArrayList<Siger>(array);
	}
	
	/**
	 * 清除全部
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 返回签名数目
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集
	 * @return
	 */
	public boolean isEmpty() {
		return size() == 0;
	}



}
