/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.cyber.*;
import com.laxcus.gate.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;

/**
 * 设置FRONT在线用户空间参数调用器
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public class GateSetFrontCyberInvoker extends GateInvoker {

	/**
	 * 构造设置FRONT在线用户空间参数调用器，指定命令
	 * @param cmd 设置FRONT在线用户空间参数
	 */
	public GateSetFrontCyberInvoker(SetFrontCyber cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetFrontCyber getCommand(){
		return (SetFrontCyber)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetFrontCyber cmd = getCommand();
		Node from = cmd.getSourceSite();
		Node hub = getHub();

		// 命令必须来自管理站点
		boolean success = (Laxkit.compareTo(from, hub) == 0);
		// 设置参数
		if (success) {
			GateLauncher launcher = getLauncher();
			FrontCyber cyber = launcher.getFrontCyber();
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
