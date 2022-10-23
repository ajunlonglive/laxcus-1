/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.rabbet;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * GATE辅助连接器<br><br>
 * 
 * 在FRONT站点上使用。
 * 
 * @author scott.liang
 * @version 1.0 8/3/2017
 * @since laxcus 1.0
 */
public final class GateRabbet extends Rabbet {

	private static final long serialVersionUID = 5681479313063146352L;

	/** 授权人签名 **/
	private Siger authorizer;

	/** 主机地址 **/
	private SocketHost remote;

	/**
	 * 根据传入的GATE辅助连接器，生成它的数据副本
	 * @param that GateRabbet实例
	 */
	private GateRabbet(GateRabbet that) {
		super(that);
		authorizer = that.authorizer.duplicate();
		remote = that.remote.duplicate();
	}

	/**
	 * 构造默认的GATE辅助连接器
	 */
	public GateRabbet() {
		super(SiteTag.GATE_SITE);
	}

	/**
	 * 构造GATE辅助连接器，指定服务器地址
	 * @param hub 服务器地址
	 */
	public GateRabbet(Node hub) {
		this();
		setHub(hub);
	}

	/**
	 * 构造GATE辅助连接器，指定全部参数
	 * @param hub 服务器地址
	 * @param authorizer 授权人签名
	 */
	public GateRabbet(Node hub, Siger authorizer) {
		this(hub);
		setAuthorizer(authorizer);
	}

	/**
	 * 从可类化读取器中解析GATE辅助连接器
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public GateRabbet(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置授权人签名
	 * @param e Siger实例
	 */
	public void setAuthorizer(Siger e) {
		Laxkit.nullabled(e);

		authorizer = e;
	}

	/**
	 * 返回授权人签名
	 * @return Siger实例
	 */
	public Siger getAuthorizer() {
		return authorizer;
	}

	/**
	 * 目标地址
	 * @param e
	 */
	public void setRemote(SocketHost e) {
		Laxkit.nullabled(e);
		remote = e;
	}

	/**
	 * 目标地址
	 * @return
	 */
	public SocketHost getRemote() {
		return remote;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.rabbet.Rabbet#compareTo(com.laxcus.site.rabbet.Rabbet)
	 */
	@Override
	public int compareTo(Rabbet that) {
		int ret = super.compareTo(that);

		// 比较是同类
		if (ret == 0 &&Laxkit.isClassFrom(that, GateRabbet.class)) {
			GateRabbet next = (GateRabbet) that;
			ret = Laxkit.compareTo(authorizer, next.authorizer);
			if (ret == 0) {
				ret = Laxkit.compareTo(remote, next.remote);
			}
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.Rabbet#duplicate()
	 */
	@Override
	public GateRabbet duplicate() {
		return new GateRabbet(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.ClientSite#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(authorizer);
		writer.writeObject(remote);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.ClientSite#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		authorizer = new Siger(reader);
		remote = new SocketHost(reader);
	}

}