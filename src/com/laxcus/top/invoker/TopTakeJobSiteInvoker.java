/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.task.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 获取工作节点调用器。
 * 
 * @author scott.liang
 * @version 1.0 3/17/2020
 * @since laxcus 1.0
 */
public class TopTakeJobSiteInvoker extends TopInvoker {

	/**
	 * 构造默认的获取工作节点调用器，指定命令
	 * @param cmd 获取工作节点
	 */
	public TopTakeJobSiteInvoker(TakeJobSite cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeJobSite getCommand() {
		return (TakeJobSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeJobSite cmd = getCommand();
		List<Node> slaves = HomeOnTopPool.getInstance().detail();
		int count = incompleteTo(slaves, cmd);
		boolean success = (count > 0);
		if (!success) {
			replyFault();
		}
		// 返回结果
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		TakeJobSite cmd = getCommand();
		Siger username = cmd.getUsername();
		TakeJobSiteProduct product = new TakeJobSiteProduct(username);

		// 保存结果
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			// 不成功忽略它
			if (!isSuccessObjectable(index)) {
				continue;
			}
			try {
				TakeJobSiteProduct that = getObject(TakeJobSiteProduct.class, index);
				product.addAll( that.list());
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		// 反馈结果!
		boolean success = replyProduct(product);
		
		return useful(success);
	}

}
