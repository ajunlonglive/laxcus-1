/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.seeker;

import com.laxcus.access.schema.*;
import com.laxcus.task.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * DISTANT阶段资源向导接口<BR><BR>
 * 
 * DISTANT阶段资源向导接口指示集群DISTANT阶段组件的分布状况，提供DISTANT阶段分布资源的管理、产生、检索等工作。<BR>
 * DISTANT阶段资源向导接口在CALL站点实现。
 * 
 * @author scott.liang
 * @version 1.0 5/8/2020
 * @since laxcus 1.0
 */
public interface DistantSeeker extends DistributedSeeker {

	/**
	 * 根据调用器编号和数据表名，查找对应的数据表配置
	 * 
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @return Table实例
	 * @throws TaskException - 账号用户签名不一致或者表不存在，弹出组件异常
	 */
	Table findDistantTable(long invokerId, Space space) throws TaskException;

	/**
	 * 根据DISTANT阶段命名，找到全部WORK站点地址
	 * @param invokerId 调用器编号
	 * @param distant DISTANT阶段命名
	 * @return 站点集合
	 */
	NodeSet findDistantSites(long invokerId, Phase distant) throws TaskException;
}