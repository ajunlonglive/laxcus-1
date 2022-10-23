/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.tip.*;

/**
 * 诊断服务器节点调用器
 * 
 * 所有需要诊断服务器节点的子类都从它派生
 * 
 * @author scott.liang
 * @version 1.0 4/26/2022
 * @since laxcus 1.0
 */
public abstract class MeetHubServiceInvoker extends MeetInvoker {

	/**
	 * 构造默认的诊断服务器节点调用器
	 */
	public MeetHubServiceInvoker() {
		super();
	}

	/**
	 * 构造诊断服务器节点调用器，指定命令
	 * @param cmd
	 */
	public MeetHubServiceInvoker(Command cmd) {
		super(cmd);
	}

	//	/**
	//	 * 判断CALL节点已经存在
	//	 * @param hub CALL节点地址
	//	 * @return 返回真或者假
	//	 */
	//	protected boolean checkCallHub(Node hub) {
	//		// 判断网关CALL节点存在
	//		boolean success = CallOnFrontPool.getInstance().hasSite(hub);
	//		// 判断网关
	//		if (!success) {
	//			NodeSet set = getStaffPool().getCallSites();
	//			if (set != null) {
	//				success = set.contains(hub);
	//			}
	//		}
	//		// 判断有云存储节点
	//		if (!success) {
	//			success = getStaffPool().hasCloudSite(hub);
	//		}
	//
	//		if (!success) {
	//			ProductListener listener = getProductListener();
	//			if (listener != null) {
	//				listener.push(null);
	//			} else {
	//				faultX(FaultTip.NOTFOUND_SITE_X, hub);
	//			}
	//		}
	//
	//		return success;
	//	}

	/**
	 * 判断云存储节点存在
	 * @param hub 云存储节点
	 * @return 存在返回真，否则假
	 */
	protected boolean checkCloudHub(Node hub) {
		boolean success = getStaffPool().hasCloudSite(hub);

		if (!success) {
			ProductListener listener = getProductListener();
			if (listener != null) {
				listener.push(null);
			} else {
				faultX(FaultTip.NOTFOUND_SITE_X, hub);
			}
		}
		return success;
	}

}
