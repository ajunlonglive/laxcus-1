/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

/**
 * 命令核准器。<br>
 * FRONT/WATCH站点在向服务器提交命令之前，需要用户确定这个命令可以发送。用于字符终端和图形终端上。
 * 
 * @author scott.liang
 * @version 1.0 04/09/2013
 * @since laxcus 1.0
 */
public interface CommandAuditor {
	
	/**
	 * 核准命令是执行
	 * @return 确定返回真，否则假
	 */
	boolean confirm();
	
	/**
	 * 核准命令是执行
	 * @param content 显示文本
	 * @return 确定返回真，否则假
	 */
	boolean confirm(String content);
	
	/**
	 * 核准命令是执行
	 * @param title 提示文本
	 * @param content 显示文本
	 * @return 确定返回真，否则假
	 */
	boolean confirm(String title, String content);
	
//	/**
//	 * 通过文本键，获得文本值
//	 * KEY值定义在本地配置文件里，双方遵循。
//	 * @param key 键值
//	 * @return 返回文本
//	 */
//	String getString(String key);
//	
//	/**
//	 * 从控制界面读取一行
//	 * @param input 输入的文本
//	 * @return 返回的结果
//	 */
//	String readLine(String input);
//
//	/**
//	 * 打印文本，不要回车
//	 * @param text
//	 */
//	void print(String text);
//
//	/**
//	 * 打印文本，结尾带回车
//	 * @param text
//	 */
//	void println(String text);

}