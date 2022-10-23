/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 撤销ACCOUNT站点到HASH站点
 * 
 * @author scott.liang
 * @version 1.0 23/9/2018
 * @since laxcus 1.0
 */
public class DropAccountSite extends Command {
	
	private static final long serialVersionUID = 7372856749583510641L;

	/** ACCOUNT站点地址 **/
	private Node node;

	/**
	 * 构造默认和私有的撤销ACCOUNT站点到HASH站点命令
	 */
	private DropAccountSite() {
		super();
	}

	/**
	 * 生成撤销ACCOUNT站点到HASH站点命令的数据副本
	 * @param that 撤销ACCOUNT站点到HASH站点命令
	 */
	private DropAccountSite(DropAccountSite that) {
		super(that);
		node = that.node;
	}
	
	/**
	 * 构造撤销ACCOUNT站点到HASH站点命令，指定ACCOUNT站点地址
	 * @param node ACCOUNT站点地址
	 */
	public DropAccountSite(Node node) {
		this();
		setNode(node);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 */
	public DropAccountSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置节点地址
	 * @param e
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);
		node = e;
	}

	/**
	 * 返回节点地址
	 * @return
	 */
	public Node getNode() {
		return node;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropAccountSite duplicate() {
		return new DropAccountSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(node);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		node = new Node(reader);
	}

}