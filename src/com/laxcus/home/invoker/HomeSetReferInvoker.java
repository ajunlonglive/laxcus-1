/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.command.refer.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 设置用户资源引用命令调用器。<br>
 * 
 * 这个命令由TOP站点发出，HOME站点在这里修改配置后，再转发给下属的CALL站点。
 * 这个命令不需要回馈TOP站点
 * 
 * @author scott.liang
 * @version 1.0 4/12/2013
 * @since laxcus 1.0
 */
public class HomeSetReferInvoker extends HomeInvoker {

	/**
	 * 构造设置用户资源引用命令调用器，指定命令
	 * @param cmd 设置用户资源引用命令
	 */
	public HomeSetReferInvoker(SetRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetRefer getCommand() {
		return (SetRefer) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetRefer cmd = getCommand();
		Refer refer = cmd.getRefer();
		// 修改配置
		boolean success = StaffOnHomePool.getInstance().create(refer);

		// 查找CALL站点，修改配置
		Siger siger = refer.getUsername();
		NodeSet set = CallOnHomePool.getInstance().findSites(siger);
		if (set != null) {
			// 以容错模式投递给CALL站点
			directTo(set.show(), cmd, false);
		}

		Logger.debug(this, "launch", success, "set refer");
		// 退出
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
