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
 * 加载索引调用器。<br><br>
 * 
 * 索引处理从磁盘转为内存模式，有助于提高数据检索业务。
 * 
 * @author scott.liang
 * @version 1.1 09/03/2012
 * @since laxcus 1.0
 */
public class DataLoadIndexInvoker extends DataInvoker {

	/**
	 * 构造加载索引调用器，指定命令
	 * @param cmd 加载索引命令
	 */
	public DataLoadIndexInvoker(LoadIndex cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		LoadIndex cmd = (LoadIndex) super.getCommand();
		Space space = cmd.getSpace();

		int ret = AccessTrustor.loadIndex(space);
		boolean success = (ret >= 0);
		Logger.note(this, "launch", success, "load '%s', result code %d",
				space, ret);

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
