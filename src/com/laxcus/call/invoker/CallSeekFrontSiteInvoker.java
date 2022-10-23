/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.command.site.front.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.call.pool.*;

/**
 * 检索FRONT在线用户调用器。<br>
 * 依据CALL站点检索
 * 
 * @author scott.liang
 * @version 1.0 09/09/2015
 * @since laxcus 1.0
 */
public class CallSeekFrontSiteInvoker extends CallInvoker {

	/**
	 * 构造检索FRONT在线用户调用器，指定命令
	 * @param cmd 检索FRONT在线用户命令
	 */
	public CallSeekFrontSiteInvoker(SeekFrontSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekFrontSite getCommand() {
		return (SeekFrontSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FrontDetail detial = new FrontDetail(getLocal());
		
		List<Siger> sigers = FrontOnCallPool.getInstance().getSigers();
		for (Siger siger : sigers) {
			List<Node> slaves = FrontOnCallPool.getInstance().findFronts(siger);
			if (slaves != null) {
				for (Node sub : slaves) {
					FrontItem item = new FrontItem(siger, sub);
					detial.add(item);
				}
			}
		}

		// 保存全部
		FrontUserProduct product = new FrontUserProduct();
		product.add(detial);

		// 反馈结果
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "size is %d", detial.size());

		// 反馈结果给BANK站点
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}