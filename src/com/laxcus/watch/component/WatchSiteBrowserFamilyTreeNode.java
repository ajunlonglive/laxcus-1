/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import javax.swing.tree.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.watch.*;

/**
 * 站点类型的树节点
 * 
 * @author scott.liang
 * @version 1.0 12/8/2012
 * @since laxcus 1.0
 */
public class WatchSiteBrowserFamilyTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 5730832459787425837L;

	/** 站点类型 **/
	private byte siteFamily;

	/**
	 * 构造站点类型的树节点，指定站点类型
	 * @param family
	 */
	public WatchSiteBrowserFamilyTreeNode(byte family) {
		super();
		setFamily(family);
	}

	/**
	 * 设置站点类型
	 * @param who
	 */
	public void setFamily(byte who) {
		if (!SiteTag.isSite(who)) {
			throw new IllegalValueException("illegal %d", who);
		}
		siteFamily = who;
	}

	/**
	 * 返回站点类型
	 * @return
	 */
	public byte getFamily() {
		return siteFamily;
	}
	

	/**
	 * 返回节点类型描述
	 * @return
	 */
	public String getDescription() {
		return SiteTag.translate(siteFamily);
	}

	/**
	 * 判断是TOP站点
	 * @return
	 */
	public boolean isTop() {
		return SiteTag.isTop(siteFamily);
	}

	/**
	 * 判断是HOME站点
	 * @return
	 */
	public boolean isHome() {
		return SiteTag.isHome(siteFamily);
	}

	/**
	 * 判断是WATCH站点
	 * @return
	 */
	public boolean isWatch() {
		return SiteTag.isWatch(siteFamily);
	}

	/**
	 * 判断是LOG站点
	 * @return
	 */
	public boolean isLog() {
		return SiteTag.isLog(siteFamily);
	}

	/**
	 * 判断是是FRONT站点
	 * @return
	 */
	public boolean isFront() {
		return SiteTag.isFront(siteFamily);
	}

	/**
	 * 判断是DATA站点
	 * @return
	 */
	public boolean isData() {
		return SiteTag.isData(siteFamily);
	}

	/**
	 * 判断是WORK站点
	 * @return
	 */
	public boolean isWork() {
		return SiteTag.isWork(siteFamily);
	}

	/**
	 * 判断是BUILD站点
	 * @return
	 */
	public boolean isBuild() {
		return SiteTag.isBuild(siteFamily);
	}

	/**
	 * 判断是CALL站点
	 * @return
	 */
	public boolean isCall() {
		return SiteTag.isCall(siteFamily);
	}

	/**
	 * 判断是BANK站点
	 * @return
	 */
	public boolean isBank() {
		return SiteTag.isBank(siteFamily);
	}

	/**
	 * 判断是ACCOUNT站点
	 * @return
	 */
	public boolean isAccount() {
		return SiteTag.isAccount(siteFamily);
	}
	
	/**
	 * 判断是HASH站点
	 * @return
	 */
	public boolean isHash() {
		return SiteTag.isHash(siteFamily);
	}

	/**
	 * 判断是GATE站点
	 * @return
	 */
	public boolean isGate() {
		return SiteTag.isGate(siteFamily);
	}

	/**
	 * 判断是ENTRANCE站点
	 * @return
	 */
	public boolean isEntrance() {
		return SiteTag.isEntrance(siteFamily);
	}
	
	/**
	 * 生成路径
	 * @param family 站点类型描述
	 * @return 返回XML路径
	 */
	private String getTitle(String family) {
		return WatchLauncher.getInstance().findCaption(
				"Window/SiteBrowser/" + family + "/title");
	}

	/**
	 * 返回文本描述
	 * 
	 * @return
	 */
	public String getText() {
		switch (siteFamily) {
		case SiteTag.TOP_SITE:
			return getTitle("Top");
			// HOME集群
		case SiteTag.HOME_SITE:
			return getTitle("Home");
		case SiteTag.CALL_SITE:
			return getTitle("Call");
		case SiteTag.DATA_SITE:
			return getTitle("Data");
		case SiteTag.WORK_SITE:
			return getTitle("Work");
		case SiteTag.BUILD_SITE:
			return getTitle("Build");
			// BANK集群
		case SiteTag.BANK_SITE:
			return getTitle("Bank");
		case SiteTag.ACCOUNT_SITE:
			return getTitle("Account");
		case SiteTag.HASH_SITE:
			return getTitle("Hash");
		case SiteTag.GATE_SITE:
			return getTitle("Gate");
		case SiteTag.ENTRANCE_SITE:
			return getTitle("Entrance");
			// 其它节点
		case SiteTag.LOG_SITE:
			return getTitle("Log");
		case SiteTag.WATCH_SITE:
			return getTitle("Watch");
		case SiteTag.FRONT_SITE:
			return getTitle("Front");
		}
		return "None";
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#toString()
	 */
	@Override
	public String toString() {
		return getText();
	}

}