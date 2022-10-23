/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.entrance.pool;

import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * FRONT推送单元
 * 
 * @author scott.liang
 * @version 1.0 7/20/2019
 * @since laxcus 1.0
 */
final class FrontPushItem {

	/** 启动时间。在构造时生成，不能修改 **/
	private long launchTime;
	
	/** 用户签名 **/
	private Siger siger;

	/** 触发或者否 **/
	private volatile boolean touched;
	
	/** GATE站点地址 **/
	private Node site;

	/**
	 * 构造默认的FRONT推送单元
	 */
	private FrontPushItem() {
		super();
		// 启动时间
		launchTime = System.currentTimeMillis();
		touched = false;
	}

	/**
	 * 构造FRONT推送单元
	 * @param siger 用户单元
	 */
	public FrontPushItem(Siger siger) {
		this();
		setSiger(siger);
	}

	/**
	 * 判断工作超时
	 * @param timeout 超时时间，单位：毫秒
	 * @return 返回真或者假
	 */
	public boolean isTimeout(long timeout) {
		return System.currentTimeMillis() - launchTime >= timeout;
	}

	/**
	 * 设置检查的账号用户名
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);
		
		siger = e;
	}

	/**
	 * 返回检查的账号用户名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 判断触发
	 * @return 返回真或者假
	 */
	public boolean isTouched() {
		return touched;
	}

	/**
	 * 设置Node站点实例
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		site = e;
		touched = true;
	}
	
	/**
	 * 返回GATE站点
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

}