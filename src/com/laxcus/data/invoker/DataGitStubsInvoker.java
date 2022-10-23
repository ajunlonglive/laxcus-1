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
import com.laxcus.command.access.table.*;

/**
 * 获取数据块编号调用器
 * 
 * @author scott.liang
 * @version 1.0 2/10/2018
 * @since laxcus 1.0
 */
public class DataGitStubsInvoker extends DataInvoker {

	/**
	 * 构造获取数据块编号调用器，指定命令
	 * @param cmd 获取数据块编号
	 */
	public DataGitStubsInvoker(GitStubs cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public GitStubs getCommand() {
		return (GitStubs) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		GitStubs cmd = getCommand();
		Space space = cmd.getSpace();

		GitStubsProduct product = new GitStubsProduct(space);

		// 取缓存数据块编号
		long stub = AccessTrustor.getCacheStub(space);
		if (stub != 0) {
			GitStubsItem e = new GitStubsItem(getLocal());
			e.add(stub);
			product.addCacheStub(e);
		}

		// 存储块
		GitStubsItem res = new GitStubsItem(getLocal());
		long[] stubs = AccessTrustor.getChunkStubs(space);
		int size = (stubs != null ? stubs.length : 0);
		for (int i = 0; i < size; i++) {
			res.add(stubs[i]);
		}
		if (res.size() > 0) {
			product.addChunkStub(res);
		}

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
