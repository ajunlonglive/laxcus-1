/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.to;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.echo.*;
import com.laxcus.site.*;
import com.laxcus.task.*;

/**
 * TO阶段资源代理<br>
 * 
 * ToTrustor在WORK站点实现，被CONDUCT.TO阶段任务实例调用，为TO阶段任务处理提供访问本地资源的能力。<br><br>
 * 
 * ToTrustor的数据检索（SELECT）处理流程是：<br>
 * 1. 根据数据表名，去CALL站点查找关联的DATA主站点（findPrimeSites）<br>
 * 2. 根据SELECT语句，去DATA主站点过滤出关联的数据块编号（filteStubs，DATA站点根据SELECT语句，匹配关联数据块编号和返回）<br>
 * 3. 根据数据块编号，去CALL站点查询关联的DATA站点（不分主从），CALL站点将按照平衡调用原则进行分配（findStubSites）<br>
 * 4. 根据数据块编号和SELECT语句，去指定的DATA站点查询实体数据（select）<br><br>
 * 
 * 上述SELECT检索，是三级定位方案，通过逐层缩小数据范围，最终找到所需要的数据内容。即：<1> 全部DATA节点，<2>每个DATA节点的全部关联数据块，<3>每个数据块里的实体数据。仍然以“以时间换空间”为宗旨，且兼顾到稳定性、效率、数据规模、避免数据冗余等几个指标。<br>
 * 
 * <br><br>
 * 
 * ToTrustor的删除、更新（DELETE、UPDATE）处理流程：<br><br>
 * 1. 根据数据表名，去CALL站点拿到全部关联的DATA主站点（findPrimeSites）<br>
 * 2. 生成SELECT语句，去DATA主站点拿到全部关联的数据块编号（filteStubs）。<br>
 * 3. 根据数据块编号，去DATA站点执行DELETE、UPDATE操作。<br><br>
 * 
 * ToTrustor的插入（INSERT）处理流程：<br><br>
 * 1. 根据数据表名，去CALL站点拿到全部关联的DATA主站点（findPrimeSites）。<br>
 * 2. 选择一个DATA主站点，发送INSERT命令。<br><br><br>
 * 
 * 
 * 注意：
 * 1. 在此使用SELECT、DELETE、UPDATE命令，必须是最小化的语句，且只能包含有WHERE子句。
 * 
 * @author scott.liang
 * @version 1.3 11/19/2013
 * @since laxcus 1.0
 */
public interface ToTrustor extends SiteTrustor {

	/**
	 * 根据调用器编号和数据表名，查找表实例
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * 
	 * @return 返回Table实例或者空指针
	 * @throws TaskException - 如果签名错误，弹出异常
	 */
	Table findToTable(long invokerId, Space space) throws TaskException;

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
	 * @param cmd SELECT命令（只能是包含SELECT-FROM-WHERE的语句块，其它格式，如嵌套、ORDER BY、GROUP BY子句不支持）
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

}