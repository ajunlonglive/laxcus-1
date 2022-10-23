/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * “ALTER USER”命令调用器 <br><br>
 * 
 * 修改注册用户的账号密码。密码可以由账号持有人、管理员、等同管理员权限的用户修改。<br>
 * 修改账号密码是一个事务操作，由TOP站点来发起这个事务。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/28/2009
 * @since laxcus 1.0
 */
public class DriverAlterUserInvoker extends DriverInvoker {

	/**
	 * 构造“ALTER USER”命令调用器，指定驱动任务。
	 * @param mission 驱动任务
	 */
	public DriverAlterUserInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AlterUser getCommand() {
		return (AlterUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		AlterUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(AlterUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
			super.fault(e);
			return false;
		}

		boolean success = (product != null);
		if (success) {
			setProduct(product);
		} else {
			AlterUser cmd = getCommand();
			faultX(FaultTip.FAILED_X, cmd); 
		}

		return useful(success);
	}

}
