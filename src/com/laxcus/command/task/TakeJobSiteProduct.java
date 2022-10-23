/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获取工作节点地址结果
 * 
 * @author scott.liang
 * @version 1.0 3/17/2020
 * @since laxcus 1.0
 */
public class TakeJobSiteProduct extends EchoProduct {
	
	private static final long serialVersionUID = 5339941625411840869L;

	/** 用户签名 **/
	private Siger username;

	/** 工作节点地址集合 **/
	private TreeSet<Node> sites = new TreeSet<Node>();

	/**
	 * 构造获取工作节点地址结果
	 */
	public TakeJobSiteProduct() {
		super();
	}

	/**
	 * 构造获取工作节点地址结果，指定用户签名
	 * @param username 用户签名
	 */
	public TakeJobSiteProduct(Siger username) {
		super();
		setUsername(username);
	}

	/**
	 * 从可类化数据读取器中解析获取工作节点地址结果
	 * @param reader 可类化数据读取器
	 */
	public TakeJobSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成获取工作节点地址结果的数据副本
	 * @param that 获取工作节点地址结果
	 */
	private TakeJobSiteProduct(TakeJobSiteProduct that) {
		super(that);
		username = that.username;
		sites.addAll(that.sites);
	}

	/**
	 * 设置用户签名，允许空指针
	 * @param e Siger实例
	 */
	public void setUsername(Siger e) {
		username = e;
	}
	
	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return username;
	}
	
	/**
	 * 保存节点地址
	 * @param e
	 * @return
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);
		return sites.add(e);
	}

	/**
	 * 保存一批节点地址
	 * @param a
	 * @return
	 */
	public int addAll(Collection<Node> a) {
		Laxkit.nullabled(a);
		int size = sites.size();
		sites.addAll(a);
		return sites.size() - size;
	}

	/**
	 * 输出全部节点
	 * @return Node集合
	 */
	public List<Node> list() {
		return new ArrayList<Node>(sites);
	}

	/**
	 * 统计节点数目
	 * @return 整数
	 */
	public int size() {
		return sites.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeJobSiteProduct duplicate() {
		return new TakeJobSiteProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(username);
		writer.writeInt(sites.size());
		for (Node e : sites) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		username = reader.readInstance(Siger.class);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			sites.add(e);
		}
	}

}
