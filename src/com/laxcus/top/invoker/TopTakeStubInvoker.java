/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import com.laxcus.command.stub.*;
import com.laxcus.log.client.*;
import com.laxcus.top.resource.*;

/**
 * 申请数据块编号调用器。<br><br>
 * 
 * 命令由DATA/BUILD站点发起，通过HOME转发到TOP站点上。所有数据块编号由TOP站点统一分配。<br><br>
 * 
 * <b>数据块编号规则规定：数据块编号范围在“Long.MIN_VALUE -> Long.MAX_VALUE”之间，0值被忽略，视为无效。</b><br>
 * 
 * @author scott.liang
 * @version 1.0 05/08/2012
 * @since laxcus 1.0
 */
public class TopTakeStubInvoker extends TopInvoker {

	/**
	 * 构造申请数据块编号调用器，指定命令
	 * @param cmd - 申请数据块编号命令
	 */
	public TopTakeStubInvoker(TakeStub cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeStub cmd = (TakeStub) super.getCommand();

		int size = cmd.getSize();
		StubProduct product = new StubProduct();
		// 必须是管理站点才能分配数据块标识号
		boolean success = isManager();
		if (success) {
			long[] stubs = StubManager.getInstance().allocate(size);
			product.add(stubs);
		}
		// 发送数据块集合
		replyProduct(product);

		Logger.debug(this, "launch", "stub size %d", product.size());

		// 完成，退出
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
