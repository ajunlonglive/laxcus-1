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
 * 推送被销毁站点命令。<br>
 * 在注册站点失联的状态下，TOP/HOME/BANK管理站点删除注册站点，属于故障状态。与“DropSite”的正常退出有根本区别。
 * 
 * @author scott.liang
 * @version 1.1 11/15/2015
 * @since laxcus 1.0
 */
public final class DestroySite extends DisableSite {

	private static final long serialVersionUID = -4073433753909716889L;

	/**
	 * 构造推送被销毁站点命令
	 */
	private DestroySite() {
		super();
	}

	/**
	 * 根据传入的推送被销毁站点命令，生成它的数据副本
	 * @param that DestroySite实例
	 */
	private DestroySite(DestroySite that) {
		super(that);
	}

	/**
	 * 构造推送被销毁站点命令，指定站点地址
	 * @param site Node实例
	 */
	public DestroySite(Node site) {
		super(site);
	}

	/**
	 * 从可类化数据读取器中解析推送被销毁站点命令
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public DestroySite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DestroySite duplicate() {
		return new DestroySite(this);
	}

}