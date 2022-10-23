/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

import com.laxcus.command.*;

/**
 * 分布式命令解析器 <br><br>
 * 
 * 将字符串命令转义为命令实例。
 * 
 * @author scott.liang
 * @version 1.0 4/10/2022
 * @since laxcus 1.0
 */
public interface CommandParser extends PlatformListener {

	/**
	 * 解析命令
	 * @param input 输入语法
	 * @return 成功返回匹配的命令，失败返回空指针
	 */
	Command split(String input);

}