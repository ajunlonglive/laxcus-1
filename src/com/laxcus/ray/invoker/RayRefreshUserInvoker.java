/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 刷新注册用户命令调用器 <br>
 * 
 * WATCH站点发起命令调用，经过HOME/TOP发送到GATE站点，GATE站点更新本地的注册用户记录，将执行结果通知给WATCH站点。
 * 
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public class RayRefreshUserInvoker extends RayRefreshResourceInvoker {

	/**
	 * 构造刷新注册用户命令调用器，指定命令
	 * @param cmd 刷新注册用户命令
	 */
	public RayRefreshUserInvoker(RefreshUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshUser getCommand() {
		return (RefreshUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 必须注册到BANK站点，否则权限不足
		if (!isBankHub()) {
			faultX(FaultTip.BANK_RETRY);
			return false;
		}
		// 提交到BANK站点
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 处理返回结果
		int index = findEchoKey(0);
		RefreshUserProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(RefreshUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 打印结果
		print(product, false);

		return useful();
	}

}
