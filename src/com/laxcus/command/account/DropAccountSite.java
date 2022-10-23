/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.account;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 删除一个注册站点。 <br>
 * 
 * 这个命令是上级站点向下级站点发出，要求下级站点清除指定ACCOUNT站点和站点签名记录。
 * 
 * @author scott.liang
 * @version 1.1 6/23/2015
 * @since laxcus 1.0
 */
public final class DropAccountSite extends CastAccountSite {

	private static final long serialVersionUID = 7174603199320042241L;

	/**
	 * 构造默认和私有的DropArchiveSite实例
	 */
	private DropAccountSite() {
		super();
	}

	/**
	 * 生成传入实例的数据副本
	 * @param that DropArchiveSite实例
	 */
	private DropAccountSite(DropAccountSite that) {
		super(that);
	}

	/**
	 * 构造DropArchiveSite，指定ACCOUNT站点命令
	 * @param site ACCOUNT站点
	 */
	public DropAccountSite(Node site) {
		super(site);
	}

	/**
	 * 从可类化数据读取器中解析DropArchiveSite命令。
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public DropAccountSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropAccountSite duplicate() {
		return new DropAccountSite(this);
	}

}