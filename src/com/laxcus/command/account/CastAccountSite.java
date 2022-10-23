/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.account;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 广播ACCOUNT站点
 * 
 * @author scott.liang
 * @version 1.1 6/23/2015
 * @since laxcus 1.0
 */
public abstract class CastAccountSite extends Command {

	private static final long serialVersionUID = -3155020988970785760L;

	/** ACCOUNT站点地址 **/
	private Node node;

	/**
	 * 构造默认和私有的广播ACCOUNT站点命令。
	 */
	protected CastAccountSite() {
		super();
	}

	/**
	 * 根据传入的广播ACCOUNT站点命令实例，生成它的数据副本
	 * 
	 * @param that CastArchiveSite实例
	 */
	protected CastAccountSite(CastAccountSite that) {
		super(that);
		setNode(that.node);
	}

	/**
	 * 构造广播ACCOUNT站点命令实例，指定ACCOUNT站点地址
	 * 
	 * @param node ACCOUNT站点地址
	 */
	protected CastAccountSite(Node node) {
		this();
		setNode(node);
	}

	/**
	 * 设置ACCOUNT站点地址
	 * @param e Node实例
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);

		node = e;
	}

	/**
	 * 返回ACCOUNT站点地址
	 * @return Node实例
	 */
	public Node getNode() {
		return node;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(node);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		node = new Node(reader);
	}

}