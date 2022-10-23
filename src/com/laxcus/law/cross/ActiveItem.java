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
 * 授权单元 <br><br>
 * 
 * 授权单元由被授权人签名和共享标识组成，保存在授权人账号里。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2017
 * @since laxcus 1.0
 */
public final class ActiveItem extends CrossItem {

	private static final long serialVersionUID = 5225166152109131752L;

	/** 被授权人 **/
	private Siger conferrer;

	/**
	 * 构造默认的授权单元
	 */
	private ActiveItem() {
		super();
	}

	/**
	 * 生成授权单元的数据副本
	 * @param that
	 */
	private ActiveItem(ActiveItem that) {
		super(that);
		conferrer = that.conferrer;
	}

	/**
	 * 生成共享单元的数据副本
	 * @param that 共享单元
	 */
	protected ActiveItem(CrossItem that) {
		super(that);
	}
	
	/**
	 * 构造授权单元，指定参数
	 * @param conferrer 被授权人签名
	 * @param flag 共享资源标识
	 */
	public ActiveItem(Siger conferrer, CrossFlag flag) {
		this();
		setConferrer(conferrer);
		setFlag(flag);
	}

	/**
	 * 从可类化数据读取器中解析授权单元
	 * @param reader 可类化数据读取器
	 */
	public ActiveItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出授权单元
	 * @param reader 标记化读取器
	 */
	public ActiveItem(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 根据当前授权单元，生成被授权单元
	 * @param authorizer 授权人
	 * @return 返回被授权单元
	 */
	public PassiveItem createPassiveItem(Siger authorizer) {
		PassiveItem item = new PassiveItem(this);
		item.setAuthorizer(authorizer);
		return item;
	}

	/**
	 * 设置被授权人
	 * @param e Siger实例
	 */
	public void setConferrer(Siger e) {
		Laxkit.nullabled(e);

		conferrer = e;
	}

	/**
	 * 返回被授权人
	 * @return Siger实例
	 */
	public Siger getConferrer() {
		return conferrer;
	}
	
	/**
	 * 除操作符以外的参数，其它一致
	 * @param that
	 * @return 返回真或者假
	 */
	public boolean alike(ActiveItem that) {
		return (Laxkit.compareTo(that.getConferrer(), conferrer) == 0 && 
				Laxkit.compareTo(that.getSpace(), getSpace()) == 0);
	}

	/**
	 * 生成当前实例的数据副本
	 * @return ActiveItem实例
	 */
	@Override
	public ActiveItem duplicate() {
		return new ActiveItem(this);
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
		return compareTo((ActiveItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ conferrer.hashCode() ;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", conferrer, getFlag());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.law.cross.CrossItem#compareTo(com.laxcus.law.cross.CrossItem)
	 */
	@Override
	public int compareTo(CrossItem that) {
		// 比较
		int ret = super.compareTo(that);
		// 当前比较
		if (ret == 0 && that.getClass() == ActiveItem.class) {
			ActiveItem e = (ActiveItem) that;
			ret = Laxkit.compareTo(conferrer, e.conferrer);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.law.cross.CrossItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(conferrer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.law.cross.CrossItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		conferrer = new Siger(reader);
	}

}