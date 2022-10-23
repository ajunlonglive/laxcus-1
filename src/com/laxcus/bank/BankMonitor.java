/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank;

import com.laxcus.launch.hub.*;
import com.laxcus.site.*;
import com.laxcus.site.bank.*;

/**
 * BANK站点监视器。<br><br>
 * 
 * BANK监视站点启动监视器，监视BANK管理站点的状态。
 * 在集群架构部署上，要求管理站点和备份站点放一个网络环境中，监视站点可以直接连上管理站点，中间不应该有交换机隔开。
 * 
 * @author scott.liang
 * @version 1.0 6/25/2018
 * @since laxcus 1.0
 */
public class BankMonitor extends HubMonitor {

	/** BANK备份站点静态句柄 **/
	private static BankMonitor selfHandle = new BankMonitor();

	/**
	 * 返回BANK站点监视器静态句柄
	 * @return BankMonitor实例
	 */
	public static BankMonitor getInstance() {
		return BankMonitor.selfHandle;
	}

	/**
	 * 构造默认和私有BANK站点监视器
	 */
	private BankMonitor() {
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
		defaultProcess();
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
		BankSite site = new BankSite();
		site.setNode(local);
		site.setManager(false); // 是备份站点
		return site;
	}
	
}