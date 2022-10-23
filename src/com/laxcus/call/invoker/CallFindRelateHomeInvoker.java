/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.call.pool.*;
import com.laxcus.command.relate.*;
import com.laxcus.site.Node;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 查询与指定账号关联的HOME站点。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2013
 * @since laxcus 1.0
 */
public class CallFindRelateHomeInvoker extends CallInvoker {

	/**
	 * 构造调用器，指定命令
	 * @param cmd
	 */
	public CallFindRelateHomeInvoker(FindRelateHome cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 命令发送到注册HOME站点
		return launchToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 取数据
		FindRelateHomeProduct product = null;
		try {
			if (isSuccessObjectable(0)) {
				product = getObject(FindRelateHomeProduct.class, 0);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			Logger.debug(this, "ending", "size is %d", product.size());
			
			// 删除可能关联的地址
			product.remove(getHub());
			
			// 逐一注册
			for (Node hub : product.list()) {
				Logger.debug(this, "ending", "hub is %s", hub);

				// 判断账号存在
				boolean correct = HomeOnCallPool.getInstance().contains(hub);
				// 不存在启动注册
				if (!correct) {
					correct = HomeOnCallPool.getInstance().login(hub);
				}
				Logger.debug(this, "ending", correct, "login to %s", hub);
			}
		}

		setQuit(true);
		return success;
	}

}
