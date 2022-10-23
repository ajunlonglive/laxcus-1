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
import com.laxcus.util.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * “DROP USER”命令调用器 <br><br>
 * 
 * 删除用户账号是一个事务操作，这个事务由TOP站点发出，在AID受理后删除账号下面的全部资源。
 * 
 * @author scott.liang
 * @version 1.0 8/23/2012
 * @since laxcus 1.0
 */
public class DriverDropUserInvoker extends DriverInvoker {

	/**
	 * 构造“DROP USER”命令调用器，指定驱动任务
	 * @param mission 驱动任务
	 */
	public DriverDropUserInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropUser getCommand() {
		return (DropUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropUser cmd = getCommand();
		Siger username = cmd.getUsername();
		// 不能删除自己
		if (getUsername().compareTo(username) == 0) {
			super.fault("cannot drop self!");
			return false;
		}

		// 判断具备删除用户权限。 如果是管理员，拥有全部操作权限
		boolean success = isAdministrator();
		// 一般用户，必须拥有删除权限
		if (!success) {
			success = getStaffPool().canDropUser();
		}
		if (!success) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		}

		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		DropUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DropUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
			super.fault(e);
			return false;
		}

		// 判断是DROP USER 结果（判断成功或者失败由用户处理）
		boolean success = (product != null);
		if (success) {
			setProduct(product);
		} else {
			DropUser cmd = getCommand();
			faultX(FaultTip.FAILED_X, cmd);
		}

		return useful(success);
	}

}
