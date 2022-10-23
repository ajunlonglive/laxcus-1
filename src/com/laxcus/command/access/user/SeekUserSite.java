/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.util.classable.*;

/**
 * 检索用户站点分布
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class SeekUserSite extends SeekUserResource {

	private static final long serialVersionUID = 5046980564956984736L;

	/**
	 * 构造默认的检索用户站点分布
	 */
	public SeekUserSite() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析检索用户站点分布
	 * @param reader 可类化数据读取器
	 */
	public SeekUserSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成检索用户站点分布的数据副本
	 * @param that 检索用户站点分布
	 */
	private SeekUserSite(SeekUserSite that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SeekUserSite duplicate() {
		return new SeekUserSite(this);
	}

}