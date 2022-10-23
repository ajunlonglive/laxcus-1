/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import com.laxcus.command.relate.*;
import com.laxcus.log.client.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 查询与指定账号关联的CALL站点。<br>
 * TOP站点向下属的全部HOME站点发出命令，要求检索此账号相关的CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2013
 * @since laxcus 1.0
 */
public class TopFindRelateCallInvoker extends TopInvoker {

	/**
	 * 构造CALL站点调用器，指定命令
	 * @param cmd
	 */
	public TopFindRelateCallInvoker(TakeOwnerCall cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Logger.debug(this, "launch", "test it");
		TakeOwnerCall cmd = (TakeOwnerCall) getCommand();
		Siger username = cmd.getSiger();

		// 命令转发给下属的HOME站点，返回处理结果
		NodeSet set = HomeOnTopPool.getInstance().findSites(username);

//		NodeSet set = HomeOnTopPool.getInstance().list();

		boolean success = (set != null && set.size() > 0);
		// 成功，命令转发给下属HOME站点；否则向请求端返回一个空集合
		if (success) {
			success = super.launchTo(set.show());
		} else {
			super.replyObject(new TakeOwnerCallProduct());
		}

		Logger.debug(this, "launch", success, "send to");

		// 判断完成
		setQuit(!success);
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int size = this.getBufferSize();
		
		Logger.debug(this, "ending", "buffer size %d", size);

		TakeOwnerCallProduct product = new TakeOwnerCallProduct();
		for (int index = 0; index < size; index++) {
			// 不成功，忽略它
			if (!this.isSuccessObjectable(index)) {
				continue;
			}
			// 取出对象，加入集合
			try {
				TakeOwnerCallProduct that = super.getObject(TakeOwnerCallProduct.class, index);
				product.join(that);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 返回处理结果
		boolean success = this.replyObject(product);

		Logger.debug(this, "ending", success, "send to");

		return useful(success);
	}

}
