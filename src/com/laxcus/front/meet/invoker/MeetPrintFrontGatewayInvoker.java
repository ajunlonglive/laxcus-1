/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.site.*;
import com.laxcus.util.display.show.*;

/**
 * 打印FRONT网关调用器
 * 
 * @author scott.liang
 * @version 1.0 02/15/2018
 * @since laxcus 1.0
 */
public class MeetPrintFrontGatewayInvoker extends MeetInvoker {

	/**
	 * 构造打印FRONT网关调用器，指定命令
	 * @param cmd 打印FRONT网关命令
	 */
	public MeetPrintFrontGatewayInvoker(PrintFrontGateway cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// CALL网关站点地址
		List<Node> sites = getStaffPool().getGateways();
		// 显示
		createShowTitle(new String[] { "FRONT-GATEWAY/SITE" });
		for (Node node : sites) {
			ShowItem item = new ShowItem();
			item.add(new ShowStringCell(0, node));
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
		
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}