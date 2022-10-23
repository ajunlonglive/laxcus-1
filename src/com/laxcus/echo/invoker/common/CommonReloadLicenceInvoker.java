/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.licence.*;
import com.laxcus.site.*;

/**
 * 重新加载许可证调用器。<br>
 * 当管理员修改节点conf/site.policy文件后，调用方法重置。
 * 
 * @author scott.liang
 * @version 1.0 7/18/2020
 * @since laxcus 1.0
 */
public abstract class CommonReloadLicenceInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造重新加载许可证调用器，指定命令
	 * @param cmd 重新加载许可证
	 */
	protected CommonReloadLicenceInvoker(ReloadLicence cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReloadLicence getCommand() {
		return (ReloadLicence) super.getCommand();
	}

	/**
	 * 加载本地许可证文件
	 * @return 返回加载结果
	 */
	protected ReloadLicenceItem reload() {
		// 来自远程WATCH节点的操作，加载本地许可证
		boolean success = getLauncher().loadLicence(true);

		// 反馈给请求端
		Node local = getLocal();
		return new ReloadLicenceItem(local, success);
	}

}