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
 * 推送新注册站点命令。
 * 
 * @author scott.liang
 * @version 1.1 11/15/2015
 * @since laxcus 1.0
 */
public final class PushSite extends CastSite {

	private static final long serialVersionUID = -5813885389673028862L;

	/**
	 * 构造默认的推送新注册站点命令。
	 */
	private PushSite() {
		super();
	}

	/**
	 * 根据传入的推送新注册站点命令，生成它的数据副本
	 * @param that PushSite实例
	 */
	private PushSite(PushSite that) {
		super(that);
	}

	/**
	 * 构造推送新注册站点命令，指定注册站点地址
	 * @param site Node实例
	 */
	public PushSite(Node site) {
		super(site);
	}

	/**
	 * 从可类化数据读取器中解析推送新注册站点命令。
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public PushSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PushSite duplicate() {
		return new PushSite(this);
	}

}