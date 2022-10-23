/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

import com.laxcus.util.*;

/**
 * 分布任务组件监听接口<br>
 * 
 * 处理由各种分布任务组件管理池（TaskPool子类）发出的事件，在CALL、DATA、WORK、BUILD节点上实现。<br>
 * 
 * @author scott.liang 
 * @version 1.0 7/2/2009
 * @since laxcus 1.0
 */
public interface TaskListener {

	/**
	 * 通知接口实现类，组件管理池的阶段命名配置已经更新。
	 * @param family 组件管理池阶段类型
	 */
	void refreshTask(int family);

	/**
	 * 根据用户签名，判断用户在所在站点存在
	 * @param issuer 用户签名（SHA256散列码）
	 * @return 返回“真”或者“假”。
	 */
	boolean hasTaskUser(Siger issuer);
}