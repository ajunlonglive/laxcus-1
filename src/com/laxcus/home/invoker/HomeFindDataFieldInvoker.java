/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.field.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 查找DATA站点元数据命令调用器。<br>
 * 
 * 这个调用器是承接来自CALL站点的请求，查找匹配的DATA站点，向它们发送命令。
 * HOME站点的查找DATA站点元数据命令调用器不向CALL站点发送异步应答。
 * 
 * @author scott.liang
 * @version 1.0 2/23/2013
 * @since laxcus 1.0
 */
public class HomeFindDataFieldInvoker extends HomeInvoker {

	/**
	 * 构造查找DATA站点元数据命令调用器，指定命令。
	 * @param cmd 查找DATA站点元数据
	 */
	public HomeFindDataFieldInvoker(FindDataField cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindDataField getCommand() {
		return (FindDataField) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindDataField cmd = getCommand();
		
//		// 设置本地回显地址
//		cmd.setSource(createSubCabin(0));		
		
		// DATA节点集合
		NodeSet slaves = new NodeSet();

		// 查找数据表名，查找对应的DATA站点
		for (Space space : cmd.getSpaces()) {
			NodeSet set = DataOnHomePool.getInstance().findSites(space);
			if (set != null) {
				slaves.addAll(set.show());
			}
		}
		// 查找注册用户名称
		for (Siger username : cmd.getUsers()) {
			NodeSet set = DataOnHomePool.getInstance().findSites(username);
			if (set != null) {
				slaves.addAll(set.show());
			}
		}

		// 以容错模式向DATA目标站点发送命令，不等待DATA站点的异步回应
		int count = directTo(slaves.toArray(), cmd);
		boolean success = (count > 0);

		Logger.debug(this, "launch", success, "send count %d", count);

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
