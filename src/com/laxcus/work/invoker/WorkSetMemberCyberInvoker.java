/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.invoker;

import com.laxcus.command.cyber.*;
import com.laxcus.work.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;

/**
 * 设置成员虚拟空间参数调用器
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public class WorkSetMemberCyberInvoker extends WorkInvoker {

	/**
	 * 构造设置成员虚拟空间参数调用器，指定命令
	 * @param cmd 设置成员虚拟空间参数
	 */
	public WorkSetMemberCyberInvoker(SetMemberCyber cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetMemberCyber getCommand(){
		return (SetMemberCyber)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetMemberCyber cmd = getCommand();
		Node from = cmd.getSourceSite();
		Node hub = getHub();

		// 命令必须来自管理站点
		boolean success = (Laxkit.compareTo(from, hub) == 0);
		// 设置参数
		if (success) {
			WorkLauncher launcher = getLauncher();
			MemberCyber cyber = launcher.getMemberCyber();
			cyber.setPersons(cmd.getPersons());
			cyber.setThreshold(cmd.getThreshold());
		}

		// 要求反馈结果时...
		if (cmd.isReply()) {
			VirtualCyberProduct product = new VirtualCyberProduct();
			product.add(getLocal(), success);
			replyProduct(product);

			// 延时重新注册
			getLauncher().checkin(false);
		}

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