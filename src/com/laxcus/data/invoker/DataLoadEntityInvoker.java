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
import com.laxcus.command.access.fast.*;
import com.laxcus.log.client.*;

/**
 * 加载数据块调用器。<br><br>
 * 
 * 数据处理从磁盘模式转为内存模式，相当于内存数据库。这种模式适合高时效的应用。
 * 
 * @author scott.liang
 * @version 1.1 09/03/2012
 * @since laxcus 1.0
 */
public class DataLoadEntityInvoker extends DataInvoker {

	/**
	 * 构造加载数据块调用器，指定命令
	 * @param cmd 加载数据命令
	 */
	public DataLoadEntityInvoker(LoadEntity cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public LoadEntity getCommand() {
		return (LoadEntity) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		LoadEntity cmd = getCommand();
		Space space = cmd.getSpace();
		int ret = AccessTrustor.loadEntity(space);
		boolean success = (ret >= 0);

		Logger.note(this, "launch", success, "load '%s' result code %d", space, ret);

		// 发送受理结果
		FastMassItem item = new FastMassItem(getLocal(), success);
		FastMassProduct product = new FastMassProduct(item);
		replyProduct(product);

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
