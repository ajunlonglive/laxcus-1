/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 分布式组件监听器
 * 它属于服务器端
 * 
 * @author scott.liang
 * @version 1.0 3/26/2022
 * @since laxcus 1.0
 */
public interface WareListener extends PlatformListener {

	/**
	 * 查找分布组件的CALL站点
	 * @param phase
	 * @return
	 */
	NodeSet findTaskSites(Phase phase);

	/**
	 * 返回当前的阶段命名
	 * @param family 类型
	 * 
	 * @return Phase列表
	 */
	Phase[] findPhases(int family);

}
