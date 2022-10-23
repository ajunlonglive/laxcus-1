/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.util.*;

import com.laxcus.access.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.sign.*;
import com.laxcus.command.stub.sign.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;

/**
 * 获取数据块签名调用器。<br>
 * 
 * 命令由CALL站点发出，目标是DATA站点。DATA站点收集本地的数据块签名数据，发送给CALL站点
 * 
 * 
 * @author scott.liang
 * @version 1.0 9/21/2015
 * @since laxcus 1.0
 */
public class DataTakeSignInvoker extends DataInvoker {

	/**
	 * 构造获取数据块签名，指定命令
	 * @param cmd 获取数据块签名命令
	 */
	public DataTakeSignInvoker(TakeSign cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeSign getCommand() {
		return (TakeSign) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeSign cmd = getCommand();
		Space space = cmd.getSpace();

		List<StubSign> list = null;
		try {
			list = AccessTrustor.sign(space);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		// 判断错误
		if (list == null || list.isEmpty()) {
			replyFault(Major.FAULTED, Minor.NOTFOUND);
			return useful(false);
		}

		// 数据块签名表
		SignTable table = new SignTable(space);
		table.addAll(list);
		
		// 签名站点
		SignSite site = new SignSite(getLocal(), table);
		// 发送签名数据处理结果
		TakeSignProduct product = new TakeSignProduct(site);
		boolean success = replyProduct(product);
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
