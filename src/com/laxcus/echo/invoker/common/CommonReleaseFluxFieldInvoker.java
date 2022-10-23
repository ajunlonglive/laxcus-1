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
import com.laxcus.task.flux.*;

/**
 * ReleaseFluxField命令由WORK站点发出，CommonReleaseFluxFieldInvoker调用器分别部署在DATA/WORK站点上，
 * WORK站点通知关联的上级站点，释放一段数据。
 * 这个调用器不用反馈结果给WORK站点。
 * 
 * @author scott.liang
 * @version 1.0 7/12/2012
 * @since laxcus 1.0
 */
public class CommonReleaseFluxFieldInvoker extends CommonInvoker {

	/**
	 * 构造ReleaseFluxField命令调用器，指定命令
	 * @param cmd ReleaseFluxField命令
	 */
	public CommonReleaseFluxFieldInvoker(ReleaseFluxField cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReleaseFluxField getCommand() {
		return (ReleaseFluxField) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ReleaseFluxField cmd = getCommand();

		long taskId = cmd.getTaskId();
		long mod = cmd.getMod();

		boolean success = FluxTrustorPool.getInstance().complete(taskId, mod);
		Logger.debug(this, "launch", success, "release %d/%d", taskId, mod);

		// 无论成功或者失败，都要退出
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