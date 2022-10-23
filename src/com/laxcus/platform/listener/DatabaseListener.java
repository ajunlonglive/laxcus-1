/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

import com.laxcus.access.schema.*;
import com.laxcus.util.set.*;

/**
 * 数据库监听器
 * 这是服务器模式的接口
 * 
 * @author scott.liang
 * @version 1.0 3/26/2022
 * @since laxcus 1.0
 */
public interface DatabaseListener extends PlatformListener {
	
	/**
	 * 取出数据库名称
	 * @return
	 */
	Fame[] getFames();
	
	/**
	 * 取关联的表名称
	 * @param fame
	 * @return
	 */
	Space[] getSpaces(Fame fame);

	/**
	 * 输出当前全部表名
	 * @return
	 */
	Space[] getSpaces();

//	/**
//	 * 查找分布组件的CALL站点
//	 * @param phase
//	 * @return
//	 */
//	NodeSet findTaskSites(Phase phase);
//
//	/**
//	 * 返回当前的阶段命名
//	 * @param family 类型
//	 * 
//	 * @return Phase列表
//	 */
//	Phase[] findPhases(int family);

	/**
	 * 查找相关表的节点
	 * @param space
	 * @return
	 */
	NodeSet findTableSites(Space space);

	/**
	 * 查找关联表
	 * @param space 表名
	 * @return Table实例，或者空指针
	 */
	Table findTable(Space space);

	/**
	 * 查找被授权表
	 * @param space 表名
	 * @return Table实例，或者空指针
	 */
	Table findPassiveTable(Space space);

	/**
	 * 判断是被授权表
	 * @param space
	 * @return
	 */
	boolean isPassiveTable(Space space);

	/**
	 * 判断某个表执行某种操作
	 * @param space
	 * @param operator
	 * @return
	 */
	boolean canTable(Space space, short operator);

	// ControlTag.IMPORT_ENTITY);
	//
	// for (Space space : staff.getSpaces()) {
	// if (staff.isPassiveTable(space)) {
	// boolean allow = staff.canTable(space, ControlTag.IMPORT_ENTITY);
	// if (allow) {
	// tables.add(space);
	// }

}
