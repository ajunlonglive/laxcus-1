/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.relate.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.call.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * CALL站点扫描调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/27/2013
 * @since laxcus 1.0
 */
public class HomeFindRelateCallInvoker extends HomeInvoker {

	/**
	 * 构造CALL站点扫描调用器，指定命令
	 * @param cmd - SCAN CALL命令
	 */
	public HomeFindRelateCallInvoker(TakeOwnerCall cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeOwnerCall cmd = (TakeOwnerCall) getCommand();
		Siger username = cmd.getSiger();

		NodeSet set = CallOnHomePool.getInstance().findSites(username);
		boolean success = (set != null && set.size() > 0);

		TakeOwnerCallProduct product = new TakeOwnerCallProduct();
		if (success) {
			List<Node> list = set.show();
			for (Node node : list) {
				CallSite site = (CallSite) CallOnHomePool.getInstance().find(node);
				if (site == null) {
					continue;
				}
				CallMember box = site.find(username);
				if (box == null) {
					continue;
				}
				product.addSpaces(node, box.getTables());
				product.addPhases(node, box.getPhases());
			}
		}

		// 发送给请求端
		super.replyObject(product);

		// 显示
		Logger.debug(this, "launch", success, "scan %s", username);

		return useful(success);
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
