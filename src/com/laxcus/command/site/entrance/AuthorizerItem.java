/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.entrance;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 授权人注册站点
 * 
 * @author scott.liang
 * @version 1.0 8/3/2017
 * @since laxcus 1.0
 */
public class AuthorizerItem implements Cloneable, Serializable, Classable, Comparable<AuthorizerItem> {

	private static final long serialVersionUID = -5198594185479982246L;

	/** 授权人账号签名 **/
	private Siger authorizer;

	/** GATE站点地址 **/
	private Node site;

	/**
	 * 构造默认的授权人注册站点
	 */
	private AuthorizerItem() {
		super();
	}

	/**
	 * 构造授权人注册站点，指定参数
	 * @param authorizer 授权人签名
	 * @param site 授权人注册站点（GATE站点）
	 */
	public AuthorizerItem(Siger authorizer, Node site) {
		this();
		setAuthorizer(authorizer);
		setSite(site);
	}

	/**
	 * 从可类化数据读取器解析授权人注册站点
	 * @param reader 可类化数据读取器
	 */
	public AuthorizerItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成授权人注册站点的数据副本
	 * @param that AuthorizerItem实例
	 */
	private AuthorizerItem(AuthorizerItem that) {
		this();
		authorizer = that.authorizer;
		site = that.site;
	}

	/**
	 * 设置授权人账号签名
	 * @param e Siger实例
	 */
	public void setAuthorizer(Siger e) {
		Laxkit.nullabled(e);

		authorizer = e;
	}

	/**
	 * 返回授权人账号签名
	 * @return Siger实例
	 */
	public Siger getAuthorizer() {
		return authorizer;
	}

	/**
	 * 设置授权人注册站点（GATE站点）
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回授权人注册站点（GATE站点）
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}


	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AuthorizerItem that) {
		if(that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(authorizer, that.authorizer);
		if(ret == 0) {
			ret = Laxkit.compareTo(site, that.site);
		}
		return ret;
	}

	/**
	 * 生成授权人注册站点的数据副本
	 * @return AuthorizerItem实例
	 */
	public AuthorizerItem duplicate() {
		return new AuthorizerItem(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (that != this) {
			return true;
		}
		// 比较一致
		return compareTo((AuthorizerItem) that) == 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return authorizer.hashCode() ^ site.hashCode();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(authorizer);
		writer.writeObject(site);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		authorizer = new Siger(reader);
		site = new Node(reader);
		return reader.getSeek() - seek;
	}

}