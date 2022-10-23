/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

import com.laxcus.util.naming.*;

/**
 * 分布式组件客户端
 * 显示或者删除客户端上的组件
 * 
 * @author scott.liang
 * @version 1.0 3/25/2022
 * @since laxcus 1.0
 */
public interface WareClient extends PlatformListener {

	/**
	 * 显示分布式组件
	 * @param phase
	 */
	void exhibit(Phase phase);

	/**
	 * 删除显示的分布式组件
	 * @param phase
	 */
	void erase(Phase phase);

	/**
	 * 重置全部
	 * 当节点重新登录时或者网络断开时使用
	 */
	void reset();
}