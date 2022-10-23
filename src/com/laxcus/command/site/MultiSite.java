/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 多站点命令。<br>
 * 
 * 保存多个节点地址。
 * 
 * @author scott.liang
 * @version 1.1 05/09/2015
 * @since laxcus 1.0
 */
public abstract class MultiSite extends Command {

	private static final long serialVersionUID = 2312609116971775707L;

	/** 站点集合 **/
	private TreeSet<Node> sites = new TreeSet<Node>();

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 站点
		writer.writeInt(sites.size());
		for (Node e : sites) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 站点
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			sites.add(e);
		}
	}

	/**
	 * 构造多站点命令
	 */
	protected MultiSite() {
		super();
	}

	/**
	 * 根据传入的多站点命令，生成它的数据副本
	 * @param that MultiSite实例
	 */
	protected MultiSite(MultiSite that) {
		super(that);
		sites.addAll(that.sites);
	}

	/**
	 * 增加一个站点
	 * @param e Node实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addSite(Node e) {
		Laxkit.nullabled(e);
		// 保存站点
		boolean success = (!sites.contains(e));
		if (success) {
			success = sites.add(e);
		}
		return success;
	}
	
	/**
	 * 保存一批站点
	 * @param a Node数组
	 * @return 返回新增成员数目
	 */
	public int addSites(Collection<Node> a) {
		int size = sites.size();
		for (Node e : a) {
			addSite(e);
		}
		return sites.size() - size;
	}
	

	/**
	 * 保存一组站点
	 * @param nodes 站点地址
	 * @return 返回增加成员数目
	 */
	public int addSites(Node[] nodes) {
		int size = sites.size();
		for (int i = 0; nodes != null && i < nodes.length; i++) {
			addSite(nodes[i]);
		}
		return sites.size() - size;
	}

	/**
	 * 输出全部站点地址
	 * 
	 * @return 返回Node列表
	 */
	public List<Node> getSites() {
		return new ArrayList<Node>(sites);
	}
	
	/**
	 * 清除地址
	 */
	public void clearSites() {
		sites.clear();
	}

	/**
	 * 返回站点数目
	 * @return 站点数目
	 */
	public int getSiteSize() {
		return sites.size();
	}
	
	/**
	 * 判断站点存在
	 * @param e 站点
	 * @return 返回真或者假
	 */
	public boolean contains(Node e) {
		return e != null && sites.contains(e);
	}

}