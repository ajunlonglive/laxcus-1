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
 * 获得BANK子站点数目。<br>
 * ACCOUNT/HASH/GATE/ENTRANCE站点发出请求，BANK站点接收。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeBankSubSiteCount extends Command {

	private static final long serialVersionUID = 8664262557797442882L;

	/** 申请获得的站点类型 **/
	private byte applyFamily;

	/**
	 * 构造默认的获得BANK子站点数目命令
	 */
	private TakeBankSubSiteCount() {
		super();
	}

	/**
	 * 构造获得BANK子站点数目，指定申请获得的站点类型
	 * @param family 申请获得的站点类型
	 */
	public TakeBankSubSiteCount(byte family) {
		this();
		setApplyFamily(family);
	}

	/**
	 * 生成获得BANK子站点数目的数据副本
	 * @param that 获得BANK子站点数目
	 */
	private TakeBankSubSiteCount(TakeBankSubSiteCount that) {
		super(that);
		applyFamily = that.applyFamily;
	}

	/**
	 * 从可类化数据读取器中解析获得BANK子站点数目
	 * @param reader 可类化数据读取器
	 */
	public TakeBankSubSiteCount(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置来源申请获得的站点类型
	 * @param who 来源申请获得的站点类型
	 */
	public void setApplyFamily(byte who) {
		if (!SiteTag.isSite(who)) {
			throw new IllegalValueException("illegal site:%d", who);
		}
		applyFamily = who;
	}

	/**
	 * 返回来源申请获得的站点类型
	 * @return 来源申请获得的站点类型
	 */
	public byte getApplyFamily() {
		return applyFamily;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeBankSubSiteCount duplicate() {
		return new TakeBankSubSiteCount(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.write(applyFamily);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		applyFamily = reader.read();
	}

}
