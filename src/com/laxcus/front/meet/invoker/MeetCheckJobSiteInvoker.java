/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.command.site.front.*;
import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 显示作业节点调用器。<br>
 * 
 * 只在FRONT节点有效。
 * 
 * @author scott.liang
 * @version 1.0 9/21/2021
 * @since laxcus 1.0
 */
public class MeetCheckJobSiteInvoker extends MeetInvoker {

	/**
	 * 构造显示作业节点调用器，指定命令
	 * @param cmd 显示作业节点
	 */
	public MeetCheckJobSiteInvoker(CheckJobSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckJobSite getCommand() {
		return (CheckJobSite) super.getCommand();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ProductListener listener = getProductListener();

		// 已经注销
		if (isLogout()) {
			faultX(FaultTip.SITE_NOT_LOGING);
			// 如果有结果监听器，通知它
			if (listener != null) {
				listener.push(null);
			}
			return false;
		}
		// 如果是管理，忽略
		if (isAdministrator()) {
			if (listener != null) {
				listener.push(null);
			} else {
				faultX(FaultTip.PERMISSION_MISSING);
			}
			return false;
		}

		// CALL SITE
		List<Node> hubs = CallOnFrontPool.getInstance().getHubs();
		if (hubs.isEmpty()) {
			if (listener != null) {
				listener.push(null);
			} else {
				faultX(FaultTip.SITE_MISSING);
			}
			return false;
		}

		CheckJobSite cmd = getCommand();
		int count = super.incompleteTo(hubs, cmd);
		boolean success = (count > 0);
		// 成功或者失败，显示信息
		if (success) {
			if (listener == null) {
				messageX(MessageTip.COMMAND_ACCEPTED);
			}
		} else {
			if (listener != null) {
				listener.push(null);
			} else {
				faultX(FaultTip.CANNOT_SUBMIT);
			}
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CheckJobSiteProduct product = new CheckJobSiteProduct();
		
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					CheckJobSiteProduct e = getObject(CheckJobSiteProduct.class, index);
					if (e != null) {
						product.addAll(e);
					}
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 显示结果
		ProductListener listener = getProductListener();
		if (listener != null) {
			listener.push(product);
		} else {
			String dataText = getXMLContent("CHECK-JOB-SITE/TYPE/DATA");
			String buildText = getXMLContent("CHECK-JOB-SITE/TYPE/BUILD");
			String workText = getXMLContent("CHECK-JOB-SITE/TYPE/WORK");
			String callText = getXMLContent("CHECK-JOB-SITE/TYPE/CALL");

			createShowTitle(new String[] { "CHECK-JOB-SITE/TYPE", "CHECK-JOB-SITE/SITE" });
			
			for (Node node : product.list()) {
				if (node.isData()) {
					printRow(new Object[] { dataText, node });
				} else if (node.isWork()) {
					printRow(new Object[] { workText, node });
				} else if (node.isBuild()) {
					printRow(new Object[] { buildText, node });
				} else if (node.isCall()) {
					printRow(new Object[] { callText, node });
				}
			}
			flushTable();
		}
		
		return useful();
	}
}