/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ui.display;

import com.laxcus.task.guide.parameter.*;

/**
 * 引导参数生成器
 * 
 * @author scott.liang
 * @version 1.0 3/28/2022
 * @since laxcus 1.0
 */
public interface GuideParamCreator {

	/**
	 * 分布式任务的初始化参数输入，返回命令句柄
	 * 
	 * @param caption 标题
	 * @param list 参数集合
	 * @return 成功返回真，否则假
	 */
	boolean create(String caption, InputParameterList list);

}