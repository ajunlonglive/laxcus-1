/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.window;

/**
 * 命令核准接口。<br>
 * WATCH向其它站点发送命令前，为保证在确实是管理员要求发送，而不是误操作。通过这个接口进行最后判断。
 * 
 * @author scott.liang
 * @version 1.0 8/25/2012
 * @since laxcus 1.0
 */
public interface WatchCommandAuditor {

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
	 * @param content 提示内容
	 * @return 确定返回真，否则假
	 */
	boolean confirm(String title, String content);
}
