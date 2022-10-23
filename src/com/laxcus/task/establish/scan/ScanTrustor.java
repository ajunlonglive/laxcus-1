/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.scan;

import com.laxcus.access.schema.*;
import com.laxcus.distribute.establish.mid.*;
import com.laxcus.task.*;

/**
 * ESTABLISH.SCAN工作代理 <br>
 * 
 * 在DATA节点上实现。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/13/2009
 * @since laxcus 1.0
 */
public interface ScanTrustor extends SiteTrustor {

	/**
	 * 判断表存在
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	boolean hasScanTable(long invokerId, Space space) throws TaskException;

	/**
	 * 在判断调用器编号和表空间获得许可的情况下，根据数据表名，查找表配置
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @return Table实例
	 */
	Table findScanTable(long invokerId, Space space) throws TaskException;

	/**
	 * 扫描指定数据表名下的全部封闭状态数据块，返回数据块元信息集合。
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @return 如果成功，返回一个ScanField实例；如果失败，返回空指针。
	 */
	ScanField detect(long invokerId, Space space) throws TaskException;

}