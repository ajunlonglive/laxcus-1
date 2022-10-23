/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

import com.laxcus.access.schema.*;
import com.laxcus.law.cross.*;

/**
 * 数据库客户端
 * 
 * @author scott.liang
 * @version 1.0 3/25/2022
 * @since laxcus 1.0
 */
public interface DatabaseClient extends PlatformListener {

	/**
	 * 显示数据库
	 * @param schema
	 */
	void exhibit(Schema schema);

	/**
	 * 清除数据库
	 * @param fame
	 */
	void erase(Fame fame);

	/**
	 * 显示数据表
	 * @param table
	 */
	void exhibit(Table table);

	/**
	 * 清除数据表
	 * @param space
	 */
	void erase(Space space);

	/**
	 * 显示被授权单元
	 * @param item
	 */
	void exhibit(PassiveItem item);

	/**
	 * 清除被授权单元
	 * @param item
	 */
	void erase(PassiveItem item);

	/**
	 * 设置数据库客户的账号级别
	 * @param grade 级别，见GradeTag中的定义
	 */
	void setGrade(int grade);

	/**
	 * 重置全部
	 * 当节点重新登录时或者网络断开时使用
	 */
	void reset();
}
