/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;

import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 资源检索接口。<br><br>
 * 
 * 提供对用户账号、数据库、数据表的在线检查方法。<br>
 * 管理员和拥有管理员权限的注册用户，默认拥有对全部数据资源的检查能力。普通注册用户，只能检查属于自己的数据资源。
 * 
 * @author scott.liang
 * @version 1.0 12/3/2009
 * @since laxcus 1.0
 */
public interface ResourceChooser {
	
	/**
	 * 判断是管理员
	 * @return 返回真或者假
	 */
	boolean isAdministrator();
	
	/**
	 * 判断是注册用户，但是拥有管理员身份
	 * @return 返回真或者假
	 */
	boolean isSameAdministrator();
	
	/**
	 * 返回当前用户的签名
	 * @return Siger实例
	 */
	Siger getOwner();

	/**
	 * 找到某个用户的账号
	 * @param siger 用户签名，只有管理员或者等同管理员账号才能操作
	 * @return Account实例
	 */
	Account findAccount(Siger siger) throws ResourceException;

	/**
	 * 查找一个本地的数据表
	 * @param space 数据表名
	 * @return 返回数据表实例，或者空指针。
	 */
	Table findTable(Space space);

	/**
	 * 判断数据表存在。先检查本地的配置 ，如果没有，发起网络查询。
	 * @param space 数据表名
	 * @return 存在返回“真”，否则“假”。
	 * @throws ResourceException - 如果操作者不是管理员或者不拥有建表权限，将弹出资源操作异常。
	 */
	boolean hasTable(Space space) throws ResourceException;

	/**
	 * 判断数据库存在。先检查本地配置，如果没有，发起网络查询。<br>
	 * 特别声明：在LAXCUS的设计中，数据库名称是全局统一的。<br>
	 * @param fame 数据库名称
	 * @return 存在返回“真”，否则“假”。
	 * @throws ResourceException - 如果操作者不是管理员或者不拥有建立数据库权限，将弹出资源操作异常
	 */
	boolean hasSchema(Fame fame) throws ResourceException;

	/**
	 * 判断分布应用存在于本地
	 * @param phase 应用根命名
	 * @return 存在返回“真”，否则“假”。
	 * @throws ResourceException
	 */
	boolean hasLocalTask(Phase phase) throws ResourceException;

	/**
	 * 判断用户账号存在。账号已经在本地或者LAXCUS集群中定义。
	 * @param siger 用户名称的明文
	 * @return 存在返回“真”，否则“假”。
	 * @throws ResourceException - 如果用户不是管理员或者不拥有建立账号权限，将弹出资源操作异常。管理员默认拥有全部管理权限，不包括数据操作权限。
	 */
	boolean hasUser(String siger) throws ResourceException;
	
	/**
	 * 判断用户账号存在。账号已经在本地或者LAXCUS集群中定义。
	 * @param siger 用户签名，是SHA256编码
	 * @return 存在返回“真”，否则“假”。
	 * @throws ResourceException - 如果用户不是管理员或者不拥有建立账号权限，将弹出资源操作异常。管理员默认拥有全部管理权限，不包括数据操作权限。
	 */
	boolean hasUser(Siger siger) throws ResourceException;
	
	/**
	 * 判断有匹配的边缘容器组件
	 * @param naming 命名
	 * @return 返回真或者假
	 * @throws ResourceException
	 */
	boolean hasTubTag(Naming naming) throws ResourceException;
	
	/**
	 * 判断是用户账号持有人自己
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	boolean isPrivate(Siger siger);

	/**
	 * 判断数据库是当前用户私有
	 * @param fame 数据库名
	 * @return 返回真或者假
	 */
	boolean isPrivate(Fame fame);

	/**
	 * 判断数据表是当前用户私有
	 * @param space 表名
	 * @return 返回真或者假
	 */
	boolean isPrivate(Space space);
	

	/**
	 * 判断是被分享表
	 * @param space 表名
	 * @return 返回真或者假
	 */
	boolean isPassiveTable(Space space);
	
	/**
	 * 判断支持某个用户级操作。此判断只是ControlTag的操作符定义，不包括 CrossOperator操作符定义。
	 * @param operator ControlTag操作符
	 * @return 返回真或者假
	 */
	boolean canUser(short operator);
	
	/**
	 * 判断支持某个数据库操作。此判断只是ControlTag的操作符定义，不包括 CrossOperator操作符定义。
	 * @param fame 数据库名
	 * @param operator ControlTag操作符
	 * @return 返回真或者假
	 */
	boolean canSchema(Fame fame, short operator);
	
	/**
	 * 判断支持某个数据表操作。此判断只是ControlTag的操作符定义，不包括 CrossOperator操作符定义。
	 * @param space 数据表名
	 * @param operator ControlTag操作符
	 * @return 返回真或者假
	 */
	boolean canTable(Space space, short operator);
	
	/**
	 * 判断权限允许SELECT操作。包括 ControlTag.SELECT 和  CrossOperator.SELECT的判断
	 * @param space 表名
	 * @return 返回真或者假
	 */
	boolean canSelect(Space space);

	/**
	 * 判断权限允许INSERT操作。包括 ControlTag.INSERT 和  CrossOperator.INSERT的判断
	 * @param space 表名
	 * @return 返回真或者假
	 */
	boolean canInsert(Space space);

	/**
	 * 判断权限允许DELETE操作。包括 ControlTag.DELETE 和  CrossOperator.DELETE的判断
	 * @param space 表名
	 * @return 返回真或者假
	 */
	boolean canDelete(Space space);

	/**
	 * 判断权限允许UPDATE操作。包括 ControlTag.UPDATE 和  CrossOperator.UPDATE的判断
	 * @param space 表名
	 * @return 返回真或者假
	 */
	boolean canUpdate(Space space);
	
	/**
	 * 判断能够执行分布计算
	 * @return
	 */
	boolean canConduct();

	/**
	 * 判断能够执行快速计算
	 * @return
	 */
	boolean canContact();
	
	/**
	 * 判断能够执行分布数据构建
	 * @return
	 */
	boolean canEstablish();

	/**
	 * 判断能够执行分布数据构建
	 * @return
	 */
	boolean canEstablish(Space space);

}