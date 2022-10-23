/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.util.set.*;

/**
 * FindSpacePrimeSite命令调用器。
 * 根据表名，查找关联的DATA主站点。
 * 
 * @author scott.liang
 * @version 1.0 6/23/2013
 * @since 1.0
 */
public class CallFindSpacePrimeSiteInvoker extends CallInvoker {

	/**
	 * 构造FindSpacePrimeSite命令调用器，指定命令
	 * @param cmd FindSpacePrimeSite命令
	 */
	public CallFindSpacePrimeSiteInvoker(FindSpacePrimeSite cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindSpacePrimeSite getCommand() {
		return (FindSpacePrimeSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindSpacePrimeSite cmd = this.getCommand();
		Space space = cmd.getSpace();
		FindSpacePrimeSiteProduct product = new FindSpacePrimeSiteProduct();
		
		// 查询主站点地址
		NodeSet set = DataOnCallPool.getInstance().findPrimeTableSites(space);
		if (set != null) {
			product.addAll(set.show());
		}
		// 发送结果
		boolean success = replyProduct(product);

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
