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
 * 推送HASH站点到GATE站点
 * 
 * @author scott.liang
 * @version 1.0 23/9/2018
 * @since laxcus 1.0
 */
public class PushHashSite extends Command {
	
	private static final long serialVersionUID = 4631606093711276661L;

	/** HASH站点编号 **/
	private int no;
	
	/** HASH站点地址 **/
	private Node node;

	/**
	 * 构造默认和私有的推送HASH站点到GATE站点命令
	 */
	private PushHashSite() {
		super();
	}

	/**
	 * 构造实例副本
	 * @param that
	 */
	private PushHashSite(PushHashSite that) {
		super(that);
		no = that.no;
		node = that.node;
	}
	
	/**
	 * 构造实例，指定参数
	 * @param no
	 * @param node
	 */
	public PushHashSite(int no, Node node) {
		this();
		setNo(no);
		setNode(node);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 */
	public PushHashSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置编号
	 * @param who
	 */
	public void setNo(int who) {
		no = who;
	}

	/**
	 * 返回编号
	 * @return
	 */
	public int getNo() {
		return no;
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
	public PushHashSite duplicate() {
		return new PushHashSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(no);
		writer.writeObject(node);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		no = reader.readInt();
		node = new Node(reader);
	}

}
