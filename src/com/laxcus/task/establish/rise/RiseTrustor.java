/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.rise;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.index.*;
import com.laxcus.site.*;
import com.laxcus.task.*;

/**
 * ESTABLISH.RISE工作代理 <br>
 * 
 * 在DATA节点上实现。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/13/2009
 * @since laxcus 1.0
 */
public interface RiseTrustor extends SiteTrustor {

	/**
	 * 判断有数据表存在
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	boolean hasRiseTable(long invokerId, Space space) throws TaskException;

	/**
	 * 根据数据表名，查找数据表
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @return Table实例
	 */
	Table findRiseTable(long invokerId, Space space) throws TaskException;

	/**
	 * 判断数据块存在
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回真或者假
	 */
	boolean hasChunk(long invokerId, Space space, long stub) throws TaskException;

	/**
	 * 查找数据块
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回数据块单元
	 * @throws TaskException
	 */
	StubItem findChunk(long invokerId, Space space, long stub) throws TaskException;

	/**
	 * 删除数据块
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回数据块单元
	 */
	StubItem deleteChunk(long invokerId, Space space, long stub) throws TaskException;

	/**
	 * 更新数据块 <br>
	 * 
	 * 更新存在两种可能：<br>
	 * 1. 硬盘上有旧的数据块，先删除，再执行下载和本地更新。<br>
	 * 2. 硬盘上没有这个数据，直接下载，然后本地更新。<br>
	 * 
	 * @param invokerId 调用器编号
	 * @param hub 数据块源头地址（BUILD站点地址）
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回数据块单元
	 */
	StubItem updateChunk(long invokerId, Node hub, Space space, long stub) throws TaskException;
}