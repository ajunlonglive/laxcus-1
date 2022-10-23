/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal;

import java.awt.*;
import java.net.*;

import com.laxcus.log.client.*;

/**
 * LAXCUS主站线程
 * 
 * @author scott.liang
 * @version 1.0 11/8/2019
 * @since laxcus 1.0
 */
class DesktopBrowerThread implements Runnable {
	
	/** LAXCUS主站 **/
	private String site;
	
	/** 线程 **/
	private Thread thread;
	
	/**
	 * 构造LAXCUS主站线程，指定LAXCUS主站
	 * @param e LAXCUS主站
	 */
	public DesktopBrowerThread(String e) {
		site = e;
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
		show();
		// 撤销线程
		thread = null;
	}
	
	/**
	 * 打开桌面默认的浏览器，显示网页
	 */
	private void show() {
		// 桌面
		Desktop desk = Desktop.getDesktop();
		boolean success = desk.isSupported(Desktop.Action.BROWSE);
		if (success) {
			try {
				URI uri = URI.create(site);
				desk.browse(uri);
			} catch (Throwable e) {
				Logger.fatal(e);
			}
		}
	}

}