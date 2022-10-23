/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.command.refer.*;
import com.laxcus.home.pool.*;
import com.laxcus.util.*;

/**
 * 获得账号资源引用调用器。<br>
 * 命令来自HOME站点子站点，HOME在本地取出发给它。
 * 
 * @author scott.liang
 * @version 1.0 6/6/2019
 * @since laxcus 1.0
 */
public class HomeTakeReferInvoker extends HomeInvoker {

	/**
	 * 构造获得账号资源引用调用器，指定命令
	 * @param cmd 获得资源引用
	 */
	public HomeTakeReferInvoker(TakeRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeRefer getCommand() {
		return (TakeRefer) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeRefer cmd = getCommand();
		Siger siger = cmd.getSiger();

		// 找到资源数据
		Refer refer = StaffOnHomePool.getInstance().find(siger);
		boolean success = (refer != null);
		if (success) {
			TakeReferProduct product = new TakeReferProduct(refer);
			success = replyProduct(product);
		} else {
			replyFault();
		}

		// 退出
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