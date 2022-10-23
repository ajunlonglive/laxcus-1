/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ui.display;

/**
 * 分布式应用组件显示接口。<br><br>
 * 
 * 处理“RUN DAPP”命令的接口实例，继承“MeetDisplay”的功能，同时实现“RUN DAPP”的参数生成工作。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/28/2022
 * @since laxcus 1.0
 */
public interface TaskDisplay extends MeetDisplay {

	/**
	 * 返回执行句柄
	 * @return GuideParamCreator实例
	 */
	GuideParamCreator getGuideParamCreator();

}