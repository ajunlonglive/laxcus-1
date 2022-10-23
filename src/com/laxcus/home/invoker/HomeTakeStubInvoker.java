/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.command.stub.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 申请数据块编号调用器。<br>
 * 
 * 申请数据块编号命令由DATA站点发出，HOME站点在此转发给TOP站点，并且将TOP站点的反馈原样返回给DATA站点。
 * HOME站点在此只做转发工作。
 * 
 * @author scott.liang
 * @version 1.0 4/23/2009
 * @since laxcus 1.0
 */
public class HomeTakeStubInvoker extends HomeInvoker {

	/**
	 * 构造申请数据块标识号调用器，指定命令
	 * @param cmd 申请数据块尺寸命令
	 */
	public HomeTakeStubInvoker(TakeStub cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 命令转发给TOP站点
		boolean success = launchTo(getHub());
		// 如果不成功，通知请求端拒绝
		if (!success) {
			this.replyFault();
			// 退出运行
			setQuit(true);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		StubProduct product = null;
		// 判断是成功的对象
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = super.getObject(StubProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断有效
		boolean success = (product != null);
		if (success) {
			super.replyObject(product); // 向请求端发送对象
		} else {
			replyFault();
		}

		Logger.debug(this, "ending", success, "apply stub");

		return useful(success);
	}

}