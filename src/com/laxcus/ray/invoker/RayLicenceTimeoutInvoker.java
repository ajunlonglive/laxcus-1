/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.missing.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 许可证超时调用器
 * 
 * @author scott.liang
 * @version 1.0 7/18/2020
 * @since laxcus 1.0
 */
public class RayLicenceTimeoutInvoker extends RayInvoker {

	/**
	 * 构造销毁节点调用器，指定命令
	 * @param cmd 销毁节点
	 */
	public RayLicenceTimeoutInvoker(LicenceTimeout cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public LicenceTimeout getCommand() {
		return (LicenceTimeout) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		LicenceTimeout cmd = getCommand();
		int day = cmd.getDay();
		Node node = cmd.getSite();
		// 弹出警告信息
		if (day > 0) {
			warningX(WarningTip.LICENCE_TIMEOUT_X, node, day);
		} else {
			faultX(FaultTip.LICENCE_TIMEOUT_X, node);
		}
		
		Logger.warning(this, "launch", "from %s, days %d", node, day);

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}