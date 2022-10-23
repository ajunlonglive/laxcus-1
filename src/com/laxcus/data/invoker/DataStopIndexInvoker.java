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
 * 卸载索引调用器。<br><br>
 * 
 * 将索引从内存清除，恢复为磁盘处理模式。
 * 
 * @author scott.liang
 * @version 1.1 09/03/2012
 * @since laxcus 1.0
 */
public class DataStopIndexInvoker extends DataInvoker {

	/**
	 * 构造卸载索引调用器，指定命令
	 * @param cmd 卸载索引命令
	 */
	public DataStopIndexInvoker(StopIndex cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		StopIndex cmd = (StopIndex)getCommand();
		Space space = cmd.getSpace();
		
		int ret = AccessTrustor.stopIndex(space);
		boolean success = (ret >= 0);
		Logger.note(this, "launch", success, "stop '%s' result code %d", space, ret);

		// 发送受理结果
		FastMassItem item = new FastMassItem(getLocal(), success);
		FastMassProduct product = new FastMassProduct(item);
		replyProduct(product);

		// 返回结果
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
