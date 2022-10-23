/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.distribute.calculate.command.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.flux.*;

/**
 * ReleaseFluxArea命令由CALL站点发出，目的是清除DATA/WORK站点上的使用但是没有释放的过期中间数据。
 * 发出的命令是在CallConductInvoker释放的时候。
 * 
 * @author scott.liang
 * @version 1.0 10/09/2014
 * @since laxcus 1.0
 */
public class CommonReleaseFluxAreaInvoker extends CommonInvoker {

	/**
	 * 建立CommonReleaseFluxAreaInvoker调用器，指定命令
	 * @param cmd ReleaseFluxArea命令
	 */
	public CommonReleaseFluxAreaInvoker(ReleaseFluxArea cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReleaseFluxArea getCommand() {
		return (ReleaseFluxArea) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ReleaseFluxArea cmd = getCommand();

		long taskId = cmd.getTaskId();

		// 释放数据
		boolean success = false;
		try {
			if (taskId >= 0) {
				success = FluxTrustorPool.getInstance().deleteStack(getInvokerId(), taskId);
			}
		} catch (TaskException e) {
			Logger.error(e);
		}

		Logger.debug(this, "launch", success, "release %d", taskId);

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
