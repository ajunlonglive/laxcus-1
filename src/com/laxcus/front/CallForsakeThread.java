/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front;

import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.rabbet.*;
import com.laxcus.util.net.*;
import com.laxcus.site.*;

/**
 * 重启CALL节点注册
 * 
 * @author scott.liang
 * @version 1.0 8/28/2019
 * @since laxcus 1.0
 */
class CallForsakeThread implements Runnable {

	/** 前端启动器 **/
	private FrontLauncher launcher;
	
	/** CALL节点来源SOCKET地址 **/
	private SocketHost remote;
	
	/** 线程句柄 **/
	private Thread thread;
	
	/**
	 * 构造重启CALL节点注册线程
	 * @param e1 FrontLauncher实例
	 * @param e2 CALL节点UDP SOCKET地址
	 */
	public CallForsakeThread(FrontLauncher e1, SocketHost e2) {
		super();
		launcher = e1;
		remote = e2;
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
		// 找到CALL节点地址，必须存在才能注册!
		CallRabbet rabbet = CallOnFrontPool.getInstance().find(remote);
		boolean success = (rabbet != null);
		if (success) {
			Node hub = rabbet.getHub();
			success = CallOnFrontPool.getInstance().relogin(hub);
			// 不成功，删除资源管理池的节点和关联数据表/阶段命名
			if (!success) {
				launcher.getStaffPool().remove(hub);
			}
		}

		Logger.debug(this, "run", success, "relogin to %s", remote);
		
		thread = null;
	}

}
