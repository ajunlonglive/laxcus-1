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
 * 获得BANK子站点。<br>
 * HASH/GATE/ENTRANCE站点发出请求，BANK站点接收。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeBankSubSites extends Command {

	private static final long serialVersionUID = -5181007192017780125L;

	/** 申请获得的站点类型 **/
	private byte applyFamily;

	/**
	 * 构造默认的申请主机序列号命令
	 */
	private TakeBankSubSites() {
		super();
	}

	/**
	 * 构造申请主机序列号，指定申请获得的站点类型
	 * @param family 申请获得的站点类型
	 */
	public TakeBankSubSites(byte family) {
		this();
		setApplyFamily(family);
	}

	/**
	 * 生成申请主机序列号的数据副本
	 * @param that 申请主机序列号
	 */
	private TakeBankSubSites(TakeBankSubSites that) {
		super(that);
		applyFamily = that.applyFamily;
	}

	/**
	 * 从可类化数据读取器中解析申请主机序列号
	 * @param reader 可类化数据读取器
	 */
	public TakeBankSubSites(ClassReader reader) {
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
	public TakeBankSubSites duplicate() {
		return new TakeBankSubSites(this);
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
