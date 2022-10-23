/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.access.permit.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 操作权限调用器 <br>
 * 
 * 只有管理员或者等同管理员身份的注册用户才能使用。
 * 
 * @author scott.liang
 * @version 1.1 05/09/2015
 * @since laxcus 1.0
 */
public class DriverCertificateInvoker extends DriverInvoker {

	/**
	 * 构造操作权限调用器，指定驱动任务
	 * @param mission 驱动任务
	 */
	protected DriverCertificateInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Certificate getCommand() {
		return (Certificate) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Node hub = super.getHub();
		Certificate cmd = getCommand();
		// 发送命令到TOP站点，或者经过AID站点发送到TOP站点
		boolean success = completeTo(hub, cmd);
		if (!success) {
			super.fault("cannot be submit to %s", hub);
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CertificateProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CertificateProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
			super.fault(e);
		}

		boolean success = (product != null);
		if (success) {
			setProduct(product);
		} else {
			Certificate cmd = getCommand();
			faultX(FaultTip.FAILED_X , cmd);
		}

		return useful(success);
	}

}