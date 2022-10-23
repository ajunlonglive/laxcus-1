/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.missing.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 用户虚拟空间耗尽调用器 <br>
 * WATCH站点在图形界面上显示，同时用声音报警。
 * 
 * @author scott.liang
 * @version 1.0 10/20/2019
 * @since laxcus 1.0
 */
public class RayMemberFullInvoker extends RayInvoker {

	/**
	 * 构造用户虚拟空间耗尽调用器，指定命令
	 * @param cmd 用户虚拟空间耗尽
	 */
	public RayMemberFullInvoker(MemberFull cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MemberFull getCommand() {
		return (MemberFull) super.getCommand();
	} 
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		MemberFull cmd = getCommand();
		Node site = cmd.getSite();
		if (site == null) {
			site = getLocal();
		}

		// 判断拒绝显示
		boolean refuse = isRefuseWarning(site);
		if (!refuse) {
			String tip = cmd.toString();
			// 在图形窗口上显示
			warningX(WarningTip.MEMBER_FULL_X, tip);
		}

		// 退出
		return useful();
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
