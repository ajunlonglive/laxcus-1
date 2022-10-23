/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.command.site.front.*;
import com.laxcus.front.pool.*;
import com.laxcus.site.*;
import com.laxcus.ui.display.*;

/**
 * 显示连接节点调用器。<br>
 * 
 * 只在FRONT节点有效。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopCheckRemoteSiteInvoker extends DesktopInvoker {

	/**
	 * 构造显示连接节点调用器，指定命令
	 * @param cmd 显示连接节点
	 */
	public DesktopCheckRemoteSiteInvoker(CheckRemoteSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckRemoteSite getCommand() {
		return (CheckRemoteSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		String entranceText = getXMLContent("CHECK-REMOTE-SITE/TYPE/ENTRANCE");
		String gateText = getXMLContent("CHECK-REMOTE-SITE/TYPE/GATE");
		String authorizer_gateText = getXMLContent("CHECK-REMOTE-SITE/TYPE/AUTHROIZER-GATE");
		String cloudText = getXMLContent("CHECK-REMOTE-SITE/TYPE/CLOUD-SITE");
		String callText = getXMLContent("CHECK-REMOTE-SITE/TYPE/CALL");
		
		createShowTitle(new String[] { "CHECK-REMOTE-SITE/TYPE", "CHECK-REMOTE-SITE/SITE" });
		
		ProductListener listener = getProductListener();
		boolean pass = (listener != null);
		CheckRemoteSiteProduct product = new CheckRemoteSiteProduct();

		// ENTRANCE
		Node entrance = getLauncher().getInitHub();
		if (pass) {
			product.add(new CheckRemoteSiteItem(CheckRemoteSiteItem.ENTRANCE, entrance));
		} else {
			printRow(new Object[] { entranceText, entrance });
		}
		
		// GATE
		Node gate = getLauncher().getHub();
		if (pass) {
			product.add(new CheckRemoteSiteItem(CheckRemoteSiteItem.GATE, gate));
		} else {
			printRow(new Object[] { gateText, gate });
		}

		// AUTHROIZER GATE SITE
		List<Node> gates = AuthroizerGateOnFrontPool.getInstance().getHubs();
		for (Node node : gates) {
			if (pass) {
				product.add(new CheckRemoteSiteItem(CheckRemoteSiteItem.AUTHORIZER_GATE, node));
			} else {
				printRow(new Object[] { authorizer_gateText, node });
			}
		}
		
		// CLOUD STORE SITE
		List<Node> clouds = this.getStaffPool().getCloudSites();
		for (Node node : clouds) {
			if (pass) {
				product.add(new CheckRemoteSiteItem(CheckRemoteSiteItem.CLOUD_STORE, node));
			} else {
				printRow(new Object[] { cloudText, node });
			}
		}
		
		// CALL SITE
		List<Node> calls = CallOnFrontPool.getInstance().getHubs();
		for (Node node : calls) {
			if (pass) {
				product.add(new CheckRemoteSiteItem(CheckRemoteSiteItem.CALL, node));
			} else {
				printRow(new Object[] { callText, node });
			}
		}
		
		if (pass) {
			listener.push(product);
		} else {
			// 输出全部记录
			flushTable();
		}

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
}