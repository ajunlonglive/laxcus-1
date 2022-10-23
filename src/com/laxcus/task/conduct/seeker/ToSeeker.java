/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.seeker;

import com.laxcus.task.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * TO阶段资源向导接口<BR><BR>
 * 
 * TO阶段资源向导接口指示集群TO阶段组件的分布状况，提供TO阶段分布资源的管理、产生、检索等工作。<BR>
 * TO阶段资源向导接口在CALL站点实现。
 * 
 * @author scott.liang
 * @version 1.0 4/23/2009
 * @since laxcus 1.0
 */
public interface ToSeeker extends DistributedSeeker {

	/**
	 * 根据TO阶段命名，找到全部WORK站点地址
	 * @param invokerId 调用器编号
	 * @param to TO阶段命名
	 * @return 站点集合
	 */
	NodeSet findToSites(long invokerId, Phase to) throws TaskException;
	
	/**
	 * 统计与这个调用器相关的WORK节点
	 * @param invokerId 调用器编号
	 * @return 返回统计值
	 * @throws TaskException
	 */
	int getToSites(long invokerId) throws TaskException;
}