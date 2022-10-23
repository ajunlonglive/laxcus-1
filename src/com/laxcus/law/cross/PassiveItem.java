/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.cross;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 被授权单元 <br><br>
 * 
 * 被授权单元由授权人签名和共享标识组成，保存在被授权人账号里。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/29/2017
 * @since laxcus 1.0
 */
public final class PassiveItem extends CrossItem {

	private static final long serialVersionUID = 4374807895072318843L;

	/** 授权人（数据表持有人） **/
	private Siger authorizer;

	/**
	 * 构造默认的被授权单元
	 */
	private PassiveItem() {
		super();
	}

	/**
	 * 生成当前被授权单元的数据副本
	 * @param that PassiveItem实例
	 */
	private PassiveItem(PassiveItem that) {
		super(that);
		authorizer = that.authorizer;
	}

	/**
	 * 生成共享单元的数据副本
	 * @param that 共享单元
	 */
	protected PassiveItem(CrossItem that) {
		super(that);
	}

	/**
	 * 构造被授权单元，指定参数
	 * @param authorizer 授权人（数据表持有人）
	 * @param flag 共享资源标识
	 */
	public PassiveItem(Siger authorizer, CrossFlag flag) {
		this();
		setAuthorizer(authorizer);
		setFlag(flag);
	}

	/**
	 * 从可类化数据读取器中解析被授权单元
	 * @param reader 可类化数据读取器
	 */
	public PassiveItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出被授权单元
	 * @param reader 标记化读取器
	 */
	public PassiveItem(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 根据被授权人签名，生成授权单元
	 * @param conferrer 被授权人
	 * @return 授权单元
	 */
	public ActiveItem createActiveItem(Siger conferrer) {
		ActiveItem item = new ActiveItem(this);
		item.setConferrer(conferrer);
		return item;
	}

	/**
	 * 设置授权人（数据表持有人）
	 * @param e Siger实例
	 */
	public void setAuthorizer(Siger e) {
		Laxkit.nullabled(e);

		authorizer = e;
	}

	/**
	 * 返回授权人（数据表持有人）
	 * @return Siger实例
	 */
	public Siger getAuthorizer() {
		return authorizer;
	}

	/**
	 * 除操作符以外的参数，其它一致
	 * @param that
	 * @return 返回真或者假
	 */
	public boolean alike(PassiveItem that) {
		return (Laxkit.compareTo(that.getAuthorizer(), authorizer) == 0 && 
				Laxkit.compareTo(that.getSpace(), getSpace()) == 0);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.law.cross.CrossItem#duplicate()
	 */
	@Override
	public PassiveItem duplicate() {
		return new PassiveItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		// 比较两个锁定规则参数完成一致
		return compareTo((PassiveItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ authorizer.hashCode() ;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", authorizer, getFlag());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.law.cross.CrossItem#compareTo(com.laxcus.law.cross.CrossItem)
	 */
	@Override
	public int compareTo(CrossItem that) {
		// 交给上级比较
		int ret = super.compareTo(that);
		// 当前一致性比较
		if (ret == 0 && that.getClass() == PassiveItem.class) {
			PassiveItem e = (PassiveItem) that;
			ret = Laxkit.compareTo(authorizer, e.authorizer);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.law.cross.CrossItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(authorizer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.law.cross.CrossItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		authorizer = new Siger(reader);
	}

}