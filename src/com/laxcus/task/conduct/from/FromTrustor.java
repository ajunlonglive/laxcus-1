/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.from;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.task.*;

/**
 * FROM阶段资源代理。<br>
 * 
 * 这个接口在DATA站点实现，被CONDUCT.FROM阶段任务实例调用，为FROM分布任务组件提供访问本地资源的能力。<br>
 * 
 * 在所有方法中，都要求用户提供调用器编号（invokerId）。设计这个参数的原因是安全检查，系统必须保证每个用户的请求都在在线且正常的。
 * 
 * 
 * @author scott.liang
 * @version 1.0 04/03/2009
 * @since laxcus 1.0
 */
public interface FromTrustor extends SiteTrustor {

	/**
	 * 根据调用器编号和数据表名，查找数据表
	 * @param invokerId  异步调用器编号
	 * @param space  数据表名
	 * 
	 * @return  返回Table实例或者空指针
	 * @throws TaskException 如果签名错误，弹出空指针
	 */
	Table findFromTable(long invokerId, Space space) throws TaskException;

	/**
	 * 数据检索。允许在SELECT命令中带函数。<br>
	 * 从磁盘中取得数据，为减少计算机压力，每次只调用一个数据块（以时间换空间）。
	 * 检索结果有两种：1. 没有返回空指针；2. 有数据返回字节流，使用RowCracker解析
	 * 
	 * @param invokerId  异步调用器编号
	 * @param cmd  SELECT命令
	 * @param stub  数据块编号
	 * @return  返回检索结果，检索结果是AccessStack内容段数据。没有返回空指针。
	 */
	byte[] select(long invokerId, Select cmd, long stub) throws TaskException;

	/**
	 * 向磁盘写入数据。写入结果有三种：<br>
	 * 1. 大于0是成功写入的行数；2. 等于0是没有写入；3. 小于0是写入错误码，错误码见FaultCode定义。<br><br>
	 * 
	 * @param invokerId  调用器编号
	 * @param cmd 插入命令
	 * @return 返回写入行数或者错误提示
	 */
	int insert(long invokerId, Insert cmd) throws TaskException;

	/**
	 * 从磁盘删除数据。删除结果有三种：<br>
	 * 1. 大于0是删除的行数，成功。 2. 等于0是没有删除。 3.小于0是删除错误码，错误码见FaultCode定义。<br><br>
	 * 
	 * 产生错误后，系统资源会根据用户的选择，对资源进行锁定。此时需要用户在前端执行一致性恢复，将全网数据恢复到一致状态。
	 * 
	 * @param invokerId  调用器编号
	 * @param cmd  删除命令
	 * @param stub  数据块编号
	 * @return  返回删除行数或者错误提示
	 */
	int delete(long invokerId, Delete cmd, long stub) throws TaskException;

}