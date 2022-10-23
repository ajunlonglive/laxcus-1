/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top;

import com.laxcus.launch.hub.*;
import com.laxcus.site.top.*;
import com.laxcus.site.*;

/**
 * TOP站点监视器。<br><br>
 * 
 * 当TOP备份站点启动进入，进入监视器模式。TOP站点监视器注册到TOP管理站点上，监视管理站点的状态。
 * 
 * @author scott.liang
 * @version 1.3 6/10/2012
 * @since laxcus 1.0
 */
public class TopMonitor extends HubMonitor {

	/** TOP备份站点静态句柄 **/
	private static TopMonitor selfHandle = new TopMonitor();

	/**
	 * 返回TOP站点监视器静态句柄
	 * @return
	 */
	public static TopMonitor getInstance() {
		return TopMonitor.selfHandle;
	}

	/**
	 * 构造默认和私有TOP站点监视器
	 */
	private TopMonitor() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		super.defaultProcess();
	}
	
//	public void process() {
//		super.defaultProcess();
//		
//		Logger.info(this, "process", "into...");
//		
//		boolean logined = login();
//		if(logined) {
//			hello();
//		} else {
//			launcher.stop();
//		}
//		
//		long end = this.refreshEndTime();
//		
//		while (!this.isInterrupted()) {
//			if (super.hasLogin()) { // 来自管理站点的通知
//				end = nextTouchTime();
//				login();
//			} else if (super.hasRelogin()) { // 来自管理站点的通知
//				end = nextTouchTime();
//				relogin();
//			} else if (isTouchTimeout(end)) {
//				end = nextTouchTime(); // 下次触发时间
//				if (isDisableTimeout()) { // 达到失效超时
//					// 启动协商机制。
//					boolean exit = voting();
//					if (exit) {
//						logined = false;
//						break;
//					}
//				} else if (this.isMinTimeout() || this.isMaxSiteTimeout()) {
//					super.hello(3);
//				} else {
//					super.hello();
//				}
//			} else {
//				super.resting(end);
//			}
//		}
//
//		// 关闭
//		if(logined) {
//			super.logout();
//		}
//		
//		Logger.info(this, "process", "exit");
//	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.hub.HubMonitor#createSite()
	 */
	@Override
	protected Site createSite() {
		Node local = getHubLauncher().getListener();
		TopSite site = new TopSite();
		site.setNode(local);
		site.setManager(false); // 是备份站点
		return site;
	}

}