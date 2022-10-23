/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.seeker;

import com.laxcus.task.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * SIFT阶段资源向导接口，指示集群中的SIFT阶段组件的分布状况。<br>
 * SIFT阶段资源向导接口运行在CALL站点上。
 * 
 * @author scott.liang
 * @version 1.0 11/11/2009
 * @since laxcus 1.0
 */
public interface SiftSeeker extends DistributedSeeker {

	/**
	 * 根据SIFT阶段命名，查找对应的BUILD站点地址集合
	 * 
	 * @param invokerId 调用器编号
	 * @param sift SIFT阶段命名
	 * @return 如果有效，返回节点地址集合。如果没有找到，返回空指针
	 * @throws TaskException - 在查找站点时发生的异常
	 */
	NodeSet findSiftSites(long invokerId, Phase sift) throws TaskException;

}