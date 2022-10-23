/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.custom;

/**
 * 自定义命令解码器。<br>
 * 这个接口由用户去实现。
 * 
 * @author scott.liang
 * @version 1.0 6/21/2017
 * @since laxcus 1.0
 */
public interface CustomCommandCracker {

	/**
	 * 判断是有效的自定义命令
	 * 
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	boolean isCommand(String input);

	/**
	 * 把字符串语句生成命令
	 * 
	 * @param input 输入语句
	 * @return 返回CustomCommand命令实例
	 */
	CustomCommand split(String input);

}