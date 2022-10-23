/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.util.*;

import com.laxcus.command.account.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * DropArchiveSite命令调用器
 * 
 * 这个调用器被部署了分布任务组件的站点共同使用，包括CALL、WORK、DATA、BUILD。
 * 
 * @author scott.liang
 * @version 1.0 3/23/2013
 * @since laxcus 1.0
 */
public class CommonDropAccountSiteInvoker extends CommonInvoker {

	/**
	 * 构造DropArchiveSite命令调用器，指定命令
	 * @param cmd DropArchiveSite命令
	 */
	public CommonDropAccountSiteInvoker(DropAccountSite cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropAccountSite getCommand() {
		return (DropAccountSite)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropAccountSite cmd = getCommand();
		Node node = cmd.getNode();

		// 删除ACCOUNT地址
		List<Siger> list = AccountOnCommonPool.getInstance().remove(node);
		boolean success = (list != null && list.size() > 0);

		Logger.debug(this, "launch", success, "drop %s", node);

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
