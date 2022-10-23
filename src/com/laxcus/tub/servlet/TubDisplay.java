/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.servlet;

/**
 * 边缘容器显示接口
 * @author scott.liang
 * @version 1.0 6/24/2019
 * @since laxcus 1.0
 */
public interface TubDisplay {

	/**
	 * 向窗口投递消息
	 * @param text 消息文本
	 * @param sound 播放声音
	 */
	void message(String text, boolean sound);

	/**
	 * 向窗口投递警告
	 * @param text 警告文本
	 * @param sound 播放声音
	 */
	void warning(String text, boolean sound);

	/**
	 * 向窗口投递错误
	 * @param text 错误文本
	 * @param sound 播放声音
	 */
	void fault(String text, boolean sound);

	/**
	 * 向窗口投递消息
	 * @param text 消息文本
	 */
	void message(String text);

	/**
	 * 向窗口投递警告
	 * @param text 警告文本
	 */
	void warning(String text);

	/**
	 * 向窗口投递错误
	 * @param text 错误文本
	 */
	void fault(String text);

}