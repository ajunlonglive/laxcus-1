/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.control;

/**
 * 桌面运行环境重置为初始状态
 * 只在初始登录时显示
 * 
 * @author scott.liang
 * @version 1.0 3/6/2022
 * @since laxcus 1.0
 */
public interface ResetHandler {

	/**
	 * 执行重置操作
	 * 
	 * @return 成功返回真，否则假
	 */
	boolean reset();

}
