/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import java.util.*;

import com.laxcus.command.site.front.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.gate.pool.*;
import com.laxcus.util.*;

/**
 * 检索FRONT在线用户调用器。
 * 依据用户签名的检索。
 * 
 * @author scott.liang
 * @version 1.0 7/12/2018
 * @since laxcus 1.0
 */
public class GateSeekFrontUserInvoker extends GateInvoker {

	/**
	 * 构造检索FRONT在线用户，指定命令
	 * @param cmd 检索FRONT在线用户命令
	 */
	public GateSeekFrontUserInvoker(SeekFrontUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekFrontUser getCommand() {
		return (SeekFrontUser) super.getCommand();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekFrontUser cmd = getCommand();

		// 取出签名
		ArrayList<Siger> array = new ArrayList<Siger>();
		if (cmd.isAllUser()) {
			array.addAll(FrontOnGatePool.getInstance().getSigers());
		} else {
			array.addAll(cmd.getUsers());
		}
		
		FrontDetail detial = new FrontDetail(getLocal());
		
		for (Siger siger : array) {
			List<Node> slaves = FrontOnGatePool.getInstance().findFronts(siger);
			if (slaves != null) {
				for (Node sub : slaves) {
					// 保存一个单元
					FrontItem item = new FrontItem(siger, sub);
					detial.add(item);
				}
			}
		}

		// 保存结果
		FrontUserProduct product = new FrontUserProduct();
		if (detial.size() > 0) {
			product.add(detial);
		}

		// 反馈报告
		boolean success = replyProduct(product);
		
		Logger.debug(this, "launch", success, "size is %d", product.size());
		
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
