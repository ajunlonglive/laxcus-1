/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 推送注销站点命令。<br>
 * 是在注册站点要求情况下删除，属于正常的退出。
 * 
 * @author scott.liang
 * @version 1.1 11/15/2015
 * @since laxcus 1.0
 */
public final class DropSite extends DisableSite {

	private static final long serialVersionUID = -235087825241582437L;

	/**
	 * 构造默认和私有的推送注销站点命令
	 */
	private DropSite() {
		super();
	}

	/**
	 * 根据传入的推送注销站点命令实例，生成它的数据副本
	 * @param that 推送注销站点实例
	 */
	private DropSite(DropSite that) {
		super(that);
	}

	/**
	 * 构造推送注销站点命令命令，指定站点地址
	 * @param site Node实例
	 */
	public DropSite(Node site) {
		super(site);
	}

	/**
	 * 从可类化数据读取器中解析推送注销站点命令
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public DropSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropSite duplicate() {
		return new DropSite(this);
	}

}