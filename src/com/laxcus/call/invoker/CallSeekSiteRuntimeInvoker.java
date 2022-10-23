/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.call.pool.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.echo.invoker.common.*;

/**
 * 诊断节点运行时状态调用器
 * 
 * @author scott.liang
 * @version 1.0 9/5/2020
 * @since laxcus 1.0
 */
public class CallSeekSiteRuntimeInvoker extends CommonSeekSiteRuntimeInvoker {

	/**
	 * 构造诊断节点运行时状态调用器，指定命令
	 * @param cmd 诊断节点运行时状态
	 */
	public CallSeekSiteRuntimeInvoker(SeekSiteRuntime cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoker.common.CommonSeekSiteRuntimeInvoker#getRegisterMembers()
	 */
	@Override
	protected int getRegisterMembers() {
		return StaffOnCallPool.getInstance().size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoker.common.CommonSeekSiteRuntimeInvoker#getOnlineMembers()
	 */
	@Override
	protected int getOnlineMembers() {
		return FrontOnCallPool.getInstance().getMembers();
	}

}
