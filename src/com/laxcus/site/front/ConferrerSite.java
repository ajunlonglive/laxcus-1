/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.front;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * FRONT被授权人站点。<br>
 * 保存被授权人和授权人的关联记录
 * 
 * @author scott.liang
 * @version 1.0 7/23/2018
 * @since laxcus 1.0
 */
public class ConferrerSite extends Site {

	private static final long serialVersionUID = 4644415973540425697L;
	
	/** FRONT标记码 **/
	private ClassCode hash;
	
	/** 被授权人账号 **/
	private User conferrer;

	/** 授权人签名 **/
	private ArrayList<Siger> authorizers = new ArrayList<Siger>();

	/**
	 * 构造一个默认的FRONT被授权人站点
	 */
	public ConferrerSite() {
		super(SiteTag.FRONT_SITE);
	}

	/**
	 * 构造一个默认的FRONT被授权人站点
	 */
	public ConferrerSite(User conferrer) {
		this();
		setConferrer(conferrer);
	}

	/**
	 * 从可类化读取器中解析FRONT被授权人站点信息
	 * @param reader 可类化数据读取器
	 */
	public ConferrerSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的FRONT被授权人站点实例，生成它的数据副本
	 * @param that ConferrerSite站点
	 */
	private ConferrerSite(ConferrerSite that) {
		super(that);
		hash = that.hash.duplicate();
		// 被授权人
		conferrer = that.conferrer.duplicate();
		// 授权人
		authorizers.addAll(that.authorizers);
	}


	/**
	 * 设置散列码
	 * @param e
	 */
	public void setHash(ClassCode e) {
		hash = e;
	}

	/**
	 * 返回散列码
	 * @return
	 */
	public ClassCode getHash() {
		return hash;
	}

	
	/**
	 * 设置被授权人账号
	 * @param e 被授权人账号
	 */
	public void setConferrer(User e) {
		Laxkit.nullabled(e);
		conferrer = e;
	}

	/**
	 * 返回被授权人账号
	 * @return 被授权人账号
	 */
	public User getConferrer() {
		return conferrer;
	}

	/**
	 * 返回被授权人签名
	 * @return 被授权人签名
	 */
	public Siger getConferrerUsername() {
		return conferrer.getUsername();
	}

	/**
	 * 添加一个授权人签名
	 * @param e 授权人签名
	 * @return 返回真或者假
	 */
	public boolean addAuthorizer(Siger e) {
		Laxkit.nullabled(e);
		if(!authorizers.contains(e)) {
			return authorizers.add(e);
		}
		return false;
	}

	/**
	 * 删除一个授权人签名
	 * @param e 授权人签名
	 * @return 返回真或者假
	 */
	public boolean removeAuthorizer(Siger e) {
		Laxkit.nullabled(e);
		return authorizers.remove(e);
	}

	/**
	 * 返回全部授权人签名
	 * @return 授权人签名列表
	 */
	public List<Siger> getAuthorizers() {
		return new ArrayList<Siger>(authorizers);
	}

	/**
	 * 返回指定下标位置的授权人签名
	 * @param index 下标索引
	 * @return 授权人签名
	 */
	public Siger getAuthorizer(int index) {
		// 在范围内...
		if (index >= 0 && index < authorizers.size()) {
			return authorizers.get(index);
		}
		return null;
	}

	/**
	 * 判断指定的授权人签名存在
	 * @param e 授权人签名
	 * @return 返回真或者假
	 */
	public boolean hasAuthorizer(Siger e) {
		Laxkit.nullabled(e);
		return authorizers.contains(e);
	}

	/**
	 * 判断还有授权人
	 * @return 返回真或者假
	 */
	public boolean hasAuthorizers() {
		return authorizers.size() > 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#duplicate()
	 */
	@Override
	public ConferrerSite duplicate() {
		return new ConferrerSite(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 哈希码
		writer.writeObject(hash);
		// 用户账号
		writer.writeObject(conferrer);
		// 表的数目和写入每一个表
		writer.writeInt(authorizers.size());
		for (Siger authorizer : authorizers) {
			writer.writeObject(authorizer);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 哈希码
		hash = new ClassCode(reader);
		// 用户账号
		conferrer = new User(reader);
		// 授权人签名
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Siger authorizer = new Siger(reader);
			authorizers.add(authorizer);
		}
	}

}
