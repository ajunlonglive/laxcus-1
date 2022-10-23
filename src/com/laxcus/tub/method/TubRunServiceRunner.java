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
import com.laxcus.util.naming.*;

/**
 * 检查边缘监听运行器
 * 
 * @author scott.liang
 * @version 1.0 10/18/2020
 * @since laxcus 1.0
 */
public class TubRunServiceRunner extends TubCommandRunner {

	/**
	 * 构造检查边缘监听运行器，指定命令
	 * @param cmd 检查边缘监听
	 */
	public TubRunServiceRunner(TubRunService cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.method.TubCommandRunner#getCommand()
	 */
	@Override
	public TubRunService getCommand() {
		return (TubRunService) super.getCommand();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.method.TubCommandRunner#launch()
	 */
	@Override
	public TubRunServiceProduct launch() {
		TubRunService cmd = this.getCommand();

		Naming naming = cmd.getNaming();
		String args = cmd.getArguments();

		// 启动参数
		TubStartResult result = null;
		try {
			result = TubPool.getInstance().launch(naming, args);
		} catch (TubException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 判断成功
		boolean success = (result != null && result.isSuccessful());
		// 结果
		TubRunServiceProduct product = new TubRunServiceProduct(success);
		if (success) {
			product.setProcessId(result.getProcessId());
			product.setNaming(result.getNaming());
			product.setStatus(result.getStatus());
		}

		return product;
	}

}