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
import com.laxcus.command.stub.reflex.*;
import com.laxcus.log.client.*;

/**
 * 设置存储映像数据调用器。<br>
 * 设置存储映像数据调用器的命令从DATA主站点发出，目标是DATA子站点。子站点更新本地保存的固态映像数据
 * 
 * @author scott.liang
 * @version 1.0 1/21/2013
 * @since laxcus 1.0
 */
public class DataSetChunkReflexDataInvoker extends DataInvoker {

	/**
	 * 构造设置存储映像数据调用器调用器，指定命令
	 * @param cmd SetChunkReflexData实例
	 */
	public DataSetChunkReflexDataInvoker(SetChunkReflexData cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetChunkReflexData getCommand() {
		return (SetChunkReflexData) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetChunkReflexData cmd = getCommand();

		Space space = cmd.getSpace();
		long stub = cmd.getStub();
		byte[] reflex = cmd.getData();

		// 写入JNI
		int ret = AccessTrustor.setChunkReflex(space, stub, reflex);
		// 判断结果
		boolean success = (ret >= 0);

		SetReflexDataProduct product = new SetReflexDataProduct(ret);
		replyProduct(product);

		Logger.debug(this, "launch", success, "set %s#%x, code is %d", space, stub, ret);

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
