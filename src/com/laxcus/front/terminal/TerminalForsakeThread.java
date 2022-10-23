/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal;

import com.laxcus.util.sound.*;

/**
 * 失效提示线程
 * 
 * @author scott.liang
 * @version 1.0 11/6/2019
 * @since laxcus 1.0
 */
class TerminalForsakeThread implements Runnable {
	
	/** TERMINAL启动器 **/
	private TerminalLauncher launcher;
	
	/** 线程 **/
	private Thread thread;
	
	/**
	 * 构造失效提示线程，指定启动器
	 * @param e 启动器
	 */
	public TerminalForsakeThread(TerminalLauncher e) {
		launcher = e;
	}
	
	/**
	 * 启动线程
	 */
	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// 注销
		launcher.logout();
		// 播放错误声音
		launcher.playSound(SoundTag.ERROR);
		// 窗口处理
		launcher.getWindow().doForsakeThread();
		// 撤销线程
		thread = null;
	}

}