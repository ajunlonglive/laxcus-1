/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.distant;


import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.echo.*;
import com.laxcus.site.*;
import com.laxcus.task.*;

/**
 * DISTANT阶段资源代理。<br>
 * 
 * 这个接口在WORK站点实现，被CONTACT.DISTANT阶段任务实例调用，为DISTANT分布任务组件提供访问本地资源的能力。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/03/2020
 * @since laxcus 1.0
 */
public interface DistantTrustor extends SiteTrustor {

	/**
	 * 根据调用器编号和数据表名，查找表实例
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * 
	 * @return 返回Table实例或者空指针
	 * @throws TaskException - 如果签名错误，弹出异常
	 */
	Table findDistantTable(long invokerId, Space space) throws TaskException;

	/**
	 * 根据数据表名，去CALL站点查找全部关联的DATA主节点
	 * @param invokerId 调用器编号
	 * @param hub CALL站点地址
	 * @param space 数据表名
	 * @return Node列表，没有是空指针
	 * @throws TaskException
	 */
	List<Node> findPrimeSites(long invokerId, Node hub, Space space) throws TaskException;

	/**
	 * 使用SELECT命令，去DATA站点筛选出与SELECT命令关联的数据块编号。
	 * 这个操作发生在从CALL站点获取数据块编号之后，通过这个方法，将进一步判断和压缩有效的数据资源。将最终的数据处理量降到最低。
	 * 
	 * @param invokerId 调用器编号，用于安全检查
	 * @param hub DATA站点地址（不分主从，但是必须是DATA站点）
	 * @param cmd SELECT命令（必须是包含SELECT-FROM-WHERE的语句块，其它格式，如嵌套、ORDER BY、GROUP BY子句不支持）
	 * @return 数据块编号列表，没有是空指针
	 * @throws TaskException
	 */
	List<Long> filteStubs(long invokerId, Node hub, Select cmd) throws TaskException;

	/**
	 * 去CALL站点，查询数据块所在的DATA站点地址（不分主从）
	 * 
	 * @param invokerId 调用器编号（判断操作来源，检查它的合法性）
	 * @param hub CALL站点地址
	 * @param space 数据表名
	 * @param stubs 数据块编号集合
	 * @return 返回数据块关联站点
	 * @throws TaskException
	 */
	List<StubEntry> findStubSites(long invokerId, Node hub, Space space, List<Long> stubs) throws TaskException;
	
	/**
	 * 去CALL站点，查询数据块所在的DATA主站点地址
	 * 
	 * @param invokerId 调用器编号（判断操作来源，检查它的合法性）
	 * @param hub CALL站点地址
	 * @param space 数据表名
	 * @param stubs 数据块编号集合
	 * @return 数据块关联站点
	 * @throws TaskException
	 */
	List<StubEntry> findStubPrimeSites(long invokerId, Node hub, Space space, List<Long> stubs) throws TaskException;

	/**
	 * 向指定的DATA站点发起检索。<br><br>
	 * 
	 * 这是分布网络环境三层数据定位的最后一节，可以基本确定目标站点有需要的数据。同时由于SELECT产生的数据量较大，为了避免检索量过大造成资源独点现象，和减少DATA站点压力，使每个并行计算任务都能获得调用机会，所以本处定义每次只能操作一个数据块。<br>
	 * 这种设计观念源于“时间换空间”。用可能较多的时间，来减少空间消耗，保证处理过程的稳定和可靠性。<br>
	 * 
	 * @param invokerId 调用器编号（判断操作来源，检查它的合法性）
	 * @param hub DATA站点地址（不分主从，但是必须是DATA站点）
	 * @param cmd 检索命令
	 * @param stub 数据块编号
	 * @return 返回检索结果
	 * @throws TaskException
	 */
	byte[] select(long invokerId, Node hub, Select cmd, long stub) throws TaskException;

	/**
	 * 将数据写入指定的DATA站点
	 * 
	 * @param invokerId 调用器编号（判断调用者来源和它本身及所被操作数据资源的合法性）
	 * @param hub DATA主站点（必须是主站点）
	 * @param cmd INSERT命令
	 * @return INSERT第一段执行结果
	 * @throws TaskException
	 */
	AssumeInsert insert(long invokerId, Node hub, Insert cmd) throws TaskException;

	/**
	 * 决定INSERT操作的最后处理结果。发生在“insert”方法之后。
	 * 
	 * @param invokerId 调用器编号（判断调用者来源和它本身及所操作数据资源的合法性）
	 * @param hub DATA站点上的调用器监听地址
	 * @param cmd INSERT诊断命令（INSERT协商第二段）
	 * @return INSERT第三段执行结果
	 * @throws TaskException
	 */
	AssumeInsert decide(long invokerId, Cabin hub, AssertInsert cmd) throws TaskException;

	/**
	 * 将数据从指定的DATA站点删除
	 * 
	 * @param invokerId 调用器编号（判断调用者来源和它本身及所操作数据资源的合法性）
	 * @param hub DATA站点上的调用器监听地址
	 * @param cmd DELETE命令
	 * @param stubs 数据块编号集合
	 * @return DELETE第一段执行结果
	 * @throws TaskException
	 */
	AssumeDelete delete(long invokerId, Node hub, Delete cmd, List<Long> stubs) throws TaskException;

	/**
	 * 决定DELETE操作的最后处理结果。发生在“delete”方法之后
	 * 
	 * @param invokerId 调用器编号（判断调用者来源和它本身及所操作数据资源的合法性）
	 * @param hub DATA站点上的调用器监听地址
	 * @param cmd DELETE诊断命令（DELETE协商第二段）
	 * @return DELETE第三段执行结果
	 * @throws TaskException
	 */
	AssumeDelete decide(long invokerId, Cabin hub, AssertDelete cmd) throws TaskException;

	/**
	 * 更新指定DATA站点上的数据
	 * 
	 * @param invokerId 调用器编号（判断调用者来源和它本身及所操作数据资源的合法性）
	 * @param hub DATA站点上的调用器监听地址
	 * @param cmd UPATE命令
	 * @param stubs 数据块编号集合
	 * @return UPATE第一段执行结果
	 * @throws TaskException
	 */
	AssumeUpdate update(long invokerId, Node hub, Update cmd, List<Long> stubs) throws TaskException;

	/**
	 * 决定UPATE操作的最后处理结果。发生在“update”方法之后
	 * 
	 * @param invokerId 调用器编号（判断调用者来源和它本身及所操作数据资源的合法性）
	 * @param hub DATA站点上的调用器监听地址
	 * @param cmd UPATE诊断命令（UPATE协商第二段）
	 * @return UPATE第三段执行结果
	 * @throws TaskException
	 */
	AssumeUpdate decide(long invokerId, Cabin hub, AssertUpdate cmd) throws TaskException;

//	/**
//	 * 根据调用器编号和数据表名，查找数据表
//	 * @param invokerId  异步调用器编号
//	 * @param space  数据表名
//	 * 
//	 * @return  返回Table实例或者空指针
//	 * @throws TaskException 如果签名错误，弹出空指针
//	 */
//	Table findDistantTable(long invokerId, Space space) throws TaskException;
//
//	/**
//	 * 数据检索。允许在SELECT命令中带函数。<br>
//	 * 从磁盘中取得数据，为减少计算机压力，每次只调用一个数据块（以时间换空间）。
//	 * 检索结果有两种：1. 没有返回空指针；2. 有数据返回字节流，使用RowCracker解析
//	 * 
//	 * @param invokerId  异步调用器编号
//	 * @param cmd  SELECT命令
//	 * @param stub  数据块编号
//	 * @return  返回检索结果，检索结果是AccessStack内容段数据。没有返回空指针。
//	 */
//	byte[] select(long invokerId, Select cmd, long stub) throws TaskException;
//
//	/**
//	 * 向磁盘写入数据。写入结果有三种：<br>
//	 * 1. 大于0是成功写入的行数；2. 等于0是没有写入；3. 小于0是写入错误码，错误码见FaultCode定义。<br><br>
//	 * 
//	 * @param invokerId  调用器编号
//	 * @param cmd 插入命令
//	 * @return 返回写入行数或者错误提示
//	 */
//	int insert(long invokerId, Insert cmd) throws TaskException;
//
//	/**
//	 * 从磁盘删除数据。删除结果有三种：<br>
//	 * 1. 大于0是删除的行数，成功。 2. 等于0是没有删除。 3.小于0是删除错误码，错误码见FaultCode定义。<br><br>
//	 * 
//	 * 产生错误后，系统资源会根据用户的选择，对资源进行锁定。此时需要用户在前端执行一致性恢复，将全网数据恢复到一致状态。
//	 * 
//	 * @param invokerId  调用器编号
//	 * @param cmd  删除命令
//	 * @param stub  数据块编号
//	 * @return  返回删除行数或者错误提示
//	 */
//	int delete(long invokerId, Delete cmd, long stub) throws TaskException;

}