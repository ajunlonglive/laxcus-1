/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

/**
 * 声音监听器
 * 
 * @author scott.liang
 * @version 1.0 3/8/2022
 * @since laxcus 1.0
 */
public interface SoundListener extends PlatformListener {

	/**
	 * 播放消息声音
	 */
	void playMessage();
	
	/**
	 * 播放警告声音
	 */
	void playWarning();
	
	/**
	 * 播放错误声音
	 */
	void playError();
}
