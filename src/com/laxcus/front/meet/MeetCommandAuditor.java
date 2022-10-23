/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet;

import com.laxcus.command.*;

/**
 * FRONT站点的命令核准器。<br>
 * FRONT站点在向服务器提交命令之前，需要用户确定这个命令可以发送。用于字符终端和图形终端上。
 * 
 * @author scott.liang
 * @version 1.0 04/09/2013
 * @since laxcus 1.0
 */
public interface MeetCommandAuditor {

	/**
	 * 核准命令是执行
	 * @return 确定返回真，否则假
	 */
	boolean confirm();
	
	/**
	 * 核准命令是执行
	 * @param title 提示文本
	 * @return 确定返回真，否则假
	 */
	boolean confirm(String title);
	
	/**
	 * 核准命令是执行
	 * @param title 提示文本
	 * @param content 显示文本
	 * @return 确定返回真，否则假
	 */
	boolean confirm(String title, String content);
	
	/**
	 * 处理转交命令
	 * @param command
	 */
	void shift(Command command);
}