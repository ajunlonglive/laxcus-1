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
 * 卸载数据块调用器<br>
 * 数据块从内存中清除，数据存取恢复磁盘模式。
 * 
 * @author scott.liang
 * @version 1.1 09/03/2012
 * @since laxcus 1.0
 */
public class DataStopEntityInvoker extends DataInvoker {

	/**
	 * 构造卸载数据块调用器，指定命令
	 * @param cmd 卸载数据块命令
	 */
	public DataStopEntityInvoker(StopEntity cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public StopEntity getCommand() {
		return (StopEntity) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		StopEntity cmd = getCommand();
		Space space = cmd.getSpace();
		
		int ret = AccessTrustor.stopEntity(space);
		boolean success = (ret >= 0);
		Logger.note(this, "launch", success, "stop '%s' result code %d", space, ret);

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
