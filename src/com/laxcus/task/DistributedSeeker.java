/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

import com.laxcus.util.naming.*;

/**
 * 远程资源向导接口<br><br>
 * 
 * 所有以xxxSeeker命名的接口，都使用在CALL站点上。
 * 它被运行在CALL站点上的分布任务组件使用，提供它们查找、分析、判断远程节点的资源。<br>
 * 
 * @author scott.liang
 * @version 1.0 03/06/2009
 * @since laxcus 1.0
 */
public interface DistributedSeeker {

	/**
	 * 判断阶段命名存在且是系统级
	 * 
	 * @param invokerId 调用器编号
	 * @param phase 阶段命名
	 * @return 判断条件成立返回“真”，否则“假”。
	 */
	boolean isSystemLevel(long invokerId, Phase phase) throws TaskException;

	/**
	 * 判断阶段命名存在且是用户级
	 * 
	 * @param invokerId 调用器编号
	 * @param phase 阶段命名
	 * @return 判断条件成立返回“真”，否则“假”。
	 */
	boolean isUserLevel(long invokerId, Phase phase) throws TaskException;

}