/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.method;

import com.laxcus.log.client.*;
import com.laxcus.tub.command.*;
import com.laxcus.tub.product.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.tub.servlet.pool.*;

/**
 * 检查边缘监听运行器
 * 
 * @author scott.liang
 * @version 1.0 10/18/2020
 * @since laxcus 1.0
 */
public class TubStopServiceRunner extends TubCommandRunner {

	/**
	 * 构造检查边缘监听运行器，指定命令
	 * @param cmd 检查边缘监听
	 */
	public TubStopServiceRunner(TubStopService cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.method.TubCommandRunner#getCommand()
	 */
	@Override
	public TubStopService getCommand() {
		return (TubStopService) super.getCommand();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.method.TubCommandRunner#launch()
	 */
	@Override
	public TubStopServiceProduct launch() {
		TubStopService cmd = getCommand();
		
		long processId = cmd.getProcessId();
		String args = cmd.getArguments();
		
		TubStopResult result = null;
		// 停止它！
		try {
			result = TubPool.getInstance().stop(processId, args);
		} catch (TubException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 判断成功或者否
		boolean success = (result != null && result.isSuccessful());

		TubStopServiceProduct product = new TubStopServiceProduct(success);
		if (success) {
			product.setStatus(result.getStatus());
			product.setNaming(result.getNaming());
			product.setProcessId(result.getProcessId());
		}
		
		return product;
	}

}