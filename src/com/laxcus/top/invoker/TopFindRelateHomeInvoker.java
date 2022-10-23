/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import com.laxcus.command.relate.*;
import com.laxcus.log.client.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 查询与指定账号关联的HOME站点。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2013
 * @since laxcus 1.0
 */
public class TopFindRelateHomeInvoker extends TopInvoker {

	/**
	 * 构造HOME站点调用器实例，指定命令
	 * @param cmd
	 */
	public TopFindRelateHomeInvoker(FindRelateHome cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindRelateHome cmd = (FindRelateHome) super.getCommand();
		Siger username = cmd.getUsername();

		FindRelateHomeProduct product = new FindRelateHomeProduct();

		// 查询
		NodeSet set = HomeOnTopPool.getInstance().findSites(username);
		if (set != null) {
			product.addAll(set.show());
		}

		// 返回处理结果
		boolean success = super.replyObject(product);
		
		Logger.debug(this, "launch", success, "send size:%d", product.size());

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}