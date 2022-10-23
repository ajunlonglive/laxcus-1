/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.command.stub.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.data.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.set.*;

/**
 * FilteSelectStub命令调用器。
 * 根据SELECT命令，查找关联的数据块编号。
 * 
 * @author scott.liang
 * @version 1.0 6/23/2013
 * @since laxcus 1.0
 */
public class DataFilteSelectStubInvoker extends DataInvoker {

	/**
	 * 构造FilteSelectStub命令调用器，指定命令
	 * @param cmd FilteSelectStub命令
	 */
	public DataFilteSelectStubInvoker(FilteSelectStub cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FilteSelectStub getCommand() {
		return (FilteSelectStub) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FilteSelectStub cmd = getCommand();

		StubSet set = StaffOnDataPool.getInstance().query(cmd.getSelect());

		StubProduct product = new StubProduct();
		if (set != null) {
			product.addAll(set.list());
		}

		boolean success = replyProduct(product);
		
		Logger.debug(this, "launch", success, "stub size:%d", product.size());

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
