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
 * 向BANK站点申请主机序列号。
 * 请求端是HASH/GATE站点。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeSiteSerial extends Command {

	private static final long serialVersionUID = -5181007192017780125L;

	/** 站点类型 **/
	private byte siteFamily;

	/**
	 * 构造默认的申请主机序列号命令
	 */
	private TakeSiteSerial() {
		super();
	}

	/**
	 * 构造申请主机序列号，指定站点类型
	 * @param family 站点类型
	 */
	public TakeSiteSerial(byte family) {
		this();
		setSiteFamily(family);
	}

	/**
	 * 生成申请主机序列号的数据副本
	 * @param that 申请主机序列号
	 */
	private TakeSiteSerial(TakeSiteSerial that) {
		super(that);
		siteFamily = that.siteFamily;
	}

	/**
	 * 从可类化数据读取器中解析申请主机序列号
	 * @param reader 可类化数据读取器
	 */
	public TakeSiteSerial(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置来源站点类型
	 * @param who 来源站点类型
	 */
	public void setSiteFamily(byte who) {
		if (!SiteTag.isSite(who)) {
			throw new IllegalValueException("illegal site:%d", who);
		}
		siteFamily = who;
	}

	/**
	 * 返回来源站点类型
	 * @return 来源站点类型
	 */
	public byte getSiteFamily() {
		return siteFamily;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeSiteSerial duplicate() {
		return new TakeSiteSerial(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.write(siteFamily);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		siteFamily = reader.read();
	}

}