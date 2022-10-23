/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.account;

import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 主动推送ACCOUNT站点命令。<br>
 * 
 * 命令中包含ACCOUNT站点地址和关联的用户签名。<br>
 * 命令由上级站点向下级站点推送。<br>
 * 命令由TOP、HOME站点发出，目标是下级的HOME、CALL、WORK、DATA、BUILD站点。<br>
 * 
 * @author scott.liang
 * @version 1.1 6/23/2015
 * @since laxcus 1.0
 */
public final class PushAccountSite extends CastAccountSite {
	
	private static final long serialVersionUID = -7658549406397191562L;
	
	/** 用户签名 **/
	private TreeSet<Siger> array = new TreeSet<Siger>();

	/**
	 * 构造默认和私有的主动推送ACCOUNT站点命令
	 */
	private PushAccountSite() {
		super();
	}

	/**
	 * 建立传入实例的数据副本
	 * @param that 主动推送ACCOUNT站点命令实例
	 */
	private PushAccountSite(PushAccountSite that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造主动推送ACCOUNT站点命令，指定ACCOUNT站点地址
	 * @param site ACCOUNT站点地址
	 */
	public PushAccountSite(Node site) {
		this();
		setNode(site);
	}

	/**
	 * 从可类化数据读取器中解析主动推送ACCOUNT站点命令。
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public PushAccountSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个用户签名
	 * @param e Siger实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Siger e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}
	
	/**
	 * 保存一批用户签名
	 * @param a Siger数组
	 * @return 返回新增成员数目
	 */
	public int addAll(List<Siger> a) {
		int size = array.size();
		for (Siger e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部用户签名
	 * @return 返回Siger列表
	 */
	public List<Siger> list() {
		return new ArrayList<Siger>(array);
	}

	/**
	 * 统计成员数目
	 * @return 返回成员数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PushAccountSite duplicate() {
		return new PushAccountSite(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.site.CastSite#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 写成员数目
		writer.writeInt(array.size());
		// 写成员对象
		for (Siger siger : array) {
			writer.writeObject(siger);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.site.CastSite#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 读成员数目
		int size = reader.readInt();
		// 读表成员
		for (int i = 0; i < size; i++) {
			Siger siger = new Siger(reader);
			array.add(siger);
		}
	}

}