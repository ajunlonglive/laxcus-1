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
 * 推送ACCOUNT站点到HASH站点
 * 
 * @author scott.liang
 * @version 1.0 23/9/2018
 * @since laxcus 1.0
 */
public class PushAccountSite extends Command {
	
	private static final long serialVersionUID = -6865696337783922682L;

	/** ACCOUNT站点地址 **/
	private Node node;

	/**
	 * 构造默认和私有的推送ACCOUNT站点到HASH站点命令
	 */
	private PushAccountSite() {
		super();
	}

	/**
	 * 生成推送ACCOUNT站点到HASH站点命令的数据副本
	 * @param that 推送ACCOUNT站点到HASH站点命令
	 */
	private PushAccountSite(PushAccountSite that) {
		super(that);
		node = that.node;
	}
	
	/**
	 * 构造推送ACCOUNT站点到HASH站点命令，指定ACCOUNT站点地址
	 * @param node ACCOUNT站点地址
	 */
	public PushAccountSite(Node node) {
		this();
		setNode(node);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 */
	public PushAccountSite(ClassReader reader) {
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
	public PushAccountSite duplicate() {
		return new PushAccountSite(this);
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