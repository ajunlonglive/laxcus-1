/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch;

import com.laxcus.util.net.*;

/**
 * WATCH登录追踪器
 * @author scott.liang
 * @version 1.0 9/16/2018
 * @since laxcus 1.0
 */
public interface WatchLoginTracker {

	/**
	 * 启动追踪
	 */
	void start();
	
	/**
	 * 停止追踪
	 */
	void stop();
	
	/**
	 * 设置PITCH码
	 * @param who
	 */
	void setPitchId(int who);
	
	/**
	 * 返回PITCH码
	 * @return
	 */
	int getPitchId();
	
	/**
	 * 设置接受“pitch”的服务端地址
	 * @param e SiteHost实例
	 */
	void setPitchHub(SiteHost e);

	/**
	 * 接受“pitch”的服务端地址
	 * @return SiteHost实例
	 */
	SiteHost getPitchHub();
}