/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.access.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.command.stub.reflex.*;
import com.laxcus.data.pool.*;
import com.laxcus.log.client.*;

/**
 * 删除缓存映像数据块命令
 * 
 * @author scott.liang
 * @version 1.0 4/23/2013
 * @since laxcus 1.0
 */
public class DataDeleteCacheReflexInvoker extends DataInvoker {

	/**
	 * 构造删除缓存映像数据块命令，指定命令
	 * @param cmd 删除缓存映像数据块命令
	 */
	public DataDeleteCacheReflexInvoker(DeleteCacheReflex cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeleteCacheReflex getCommand() {
		return (DeleteCacheReflex) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DeleteCacheReflex cmd = getCommand();
		StubFlag flag = cmd.getFlag();
		Space space = flag.getSpace();
		long stub = flag.getStub();
		
		// 判断缓存映像块存在！
		boolean exists = AccessTrustor.hasCacheReflex(space, stub);
		// 如果没有找到，返回成功，这属于冗余操作
		if (!exists) {
			Logger.warning(this, "launch", "not found %s", flag);
			
			DeleteCacheReflexProduct product = new DeleteCacheReflexProduct(flag, true);
			// 返回给调用端
			replyProduct(product);
			return useful();
		}

		// 删除缓存映像数据块
		int ret = AccessTrustor.deleteCacheReflex(space, stub);
		boolean success = (ret >= 0);
		// 成功，更新缓存映像块
		if (success) {
			StaffOnDataPool.getInstance().reloadCacheReflexStub();
		}

		// 数据块删除结果
		DeleteCacheReflexProduct product = new DeleteCacheReflexProduct(flag, success);
		// 返回给调用端
		replyProduct(product);

		Logger.debug(this, "launch", success, "delete %s, return code %d", flag, ret);

		// 退出
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
