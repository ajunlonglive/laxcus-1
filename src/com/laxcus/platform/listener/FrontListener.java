/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

import com.laxcus.site.*;

/**
 * FRONT节点监听器
 * 
 * @author scott.liang
 * @version 1.0 3/26/2022
 * @since laxcus 1.0
 */
public interface FrontListener extends PlatformListener {
	
	/**
	 * 返回云存储节点地址（同CALL节点）
	 * @return Node数组
	 */
	Node[] getCloudSites();

	/**
	 * 返回注册的CALL节点地址
	 * 
	 * @return Node数组
	 */
	Node[] getCallSites();

	/**
	 * 返回注册的ENTRANCE节点地址
	 * 
	 * @return Node节点
	 */
	Node getEntranceSite();

	/**
	 * 返回注册的GATE节点地址
	 * 
	 * @return
	 */
	Node getGateSite();

}
