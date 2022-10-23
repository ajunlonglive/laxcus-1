/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import java.util.*;

import com.laxcus.command.site.gate.*;
import com.laxcus.gate.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.site.*;

/**
 * 检查GATE站点的注册用户和站点编号的一致性调用器
 * 
 * @author scott.liang
 * @version 1.0 7/18/2019
 * @since laxcus 1.0
 */
public class GateCheckShadowConsistencyInvoker extends GateInvoker {

	/**
	 * 构造检查GATE站点的注册用户和站点编号的一致性调用器，指定命令
	 * @param cmd 检查GATE站点的注册用户和站点编号的一致性
	 */
	public GateCheckShadowConsistencyInvoker(CheckShadowConsistency cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckShadowConsistency getCommand() {
		return (CheckShadowConsistency) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckShadowConsistency cmd = getCommand();
		// 取出全部注册签名和本地站点编号
		List<Siger> sigers = FrontOnGatePool.getInstance().getSigers();

		int members = 0;
		int matchs = 0; // 匹配的数目
		for (Siger siger : sigers) {
			boolean success = match(siger, cmd.getCount());
			// 匹配，统计值加1
			if (success) {
				matchs++;
			}
			// 统计登录用户数目
			int m = FrontOnGatePool.getInstance().findMembers(siger);
			members += m;
		}

		// 检查单元
		Node local = super.getLocal();
		GateUserConsistencyItem item = new GateUserConsistencyItem(local, sigers.size(), members, matchs);

		CheckShadowConsistencyProduct product = new CheckShadowConsistencyProduct();
		product.add(item);

		boolean success = replyProduct(product);
		
		Logger.debug(this, "launch", success, "%s", item);
		
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * 判断匹配
	 * @param siger
	 * @param size
	 * @return
	 */
	private boolean match(Siger siger, int size) {
		int no = siger.mod(size);
		return getLauncher().getNo() == no;
	}

}