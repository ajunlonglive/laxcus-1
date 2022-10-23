/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.scan;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 扫描用户数据资源
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public abstract class ScanReference extends Command {

	private static final long serialVersionUID = 6727543189637142581L;

	/** 指定站点地址 **/
	private TreeSet<Node> sites = new TreeSet<Node>();

	/**
	 * 构造默认的扫描用户数据资源实例
	 */
	protected ScanReference() {
		super();
	}

	/**
	 * 生成扫描用户数据资源数据副本
	 * @param that ScanReference实例
	 */
	protected ScanReference(ScanReference that) {
		super(that);
		sites.addAll(that.sites);
	}

	/**
	 * 保存站点地址
	 * @param e Node实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addSite(Node e) {
		Laxkit.nullabled(e);

		return sites.add(e);
	}

	/**
	 * 保存一批站点地址
	 * @param a 站点列表
	 * @return 返回新增站点数目
	 */
	public int addSites(List<Node> a) {
		int size = sites.size();
		for (Node e : a) {
			addSite(e);
		}
		return sites.size() - size;
	}

	/**
	 * 输出站点列表
	 * @return Node列表
	 */
	public List<Node> getSites() {
		return new ArrayList<Node>(sites);
	}

	/**
	 * 判断定义了站点
	 * @return 返回真或者假
	 */
	public boolean hasSites() {
		return sites.size() > 0;
	}
	
	/**
	 * 判断包含指定的站点
	 * @param e 节点地址
	 * @return 返回真或者假
	 */
	public boolean contains(Node e) {
		return sites.contains(e);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
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
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			sites.add(e);
		}
	}

}