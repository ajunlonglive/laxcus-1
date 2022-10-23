/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home;

import com.laxcus.launch.hub.*;
import com.laxcus.site.*;
import com.laxcus.site.home.*;

/**
 * HOME站点监视器。<br><br>
 * 
 * HOME监视站点启动监视器，监视HOME管理站点的状态。
 * 在集群架构部署上，要求管理站点和备份站点放一个网络环境中，监视站点可以直接连上管理站点，中间不应该有交换机隔开。
 * 
 * @author scott.liang
 * @version 1.3 6/10/2012
 * @since laxcus 1.0
 */
public class HomeMonitor extends HubMonitor {

	/** HOME备份站点静态句柄 **/
	private static HomeMonitor selfHandle = new HomeMonitor();

	/**
	 * 返回HOME站点监视器静态句柄
	 * @return HomeMonitor实例
	 */
	public static HomeMonitor getInstance() {
		return HomeMonitor.selfHandle;
	}

	/**
	 * 构造默认和私有HOME站点监视器
	 */
	private HomeMonitor() {
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
		HomeSite site = new HomeSite();
		site.setNode(local);
		site.setManager(false); // 是备份站点
		return site;
	}

//	private boolean login() {
//		HomeSite site = createSite();
//		return super.login(site);
//	}
//
//	private boolean relogin() {
//		HomeSite site = createSite();
//		return super.relogin(site);
//	}

//	public void process() {
//	// 1. 注册到HOME管理站点
//	boolean success = this.login();
//	// 注册成功，发送HELO激活消息；否则关闭进程
//	if (success) {
//		hello();
//	} else {
//		HomeLauncher.getInstance().stop();
//	}
//
//	long end = this.refreshEndTime();
//
//	// 与管理站点保持激活，同时起到监视管理站点的作用。
//	while (!isInterrupted()) {
//		//			this.delay(1000);
//
//		//			super.hello();
//
//		//			// 通知下属站点，判断有效
//		//			if(System.currentTimeMillis() - endtime >= maxtime *3) {
//		//				endtime += maxtime;
//		//			}
//		//			// 确定主站失效，协商选择新站点
//		//			// 通知下属站点，重新注册到指定站点
//		//			// 退出，进入主站状态
//		//			
//		//			// 与HOME站点保持激活状态
//		//			if(System.currentTimeMillis() >= endtime) {
//		//				endtime += maxtime;
//		//				// helo();
//		//			}
//
//		if (super.hasLogin()) {
//			end = nextTouchTime();
//			login();
//		} else if (super.hasRelogin() || isMaxSiteTimeout()) {
//			end = nextTouchTime();
//			relogin();
//		} else if (isBing() || isTouchTimeout(end)) {
//			end = nextTouchTime(); // 确定下次触发时间
//			super.hello();
//		} else {
//			super.resting(end);
//		}
//	}
//
//	// 注销
//	if(success) {
//		logout();
//	}
//}
	
}
