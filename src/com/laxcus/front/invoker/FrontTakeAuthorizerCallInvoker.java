/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.relate.*;
import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.site.Node;

/**
 * 查找授权人CALL站点调用器。<br>
 * 从GATE获得授权人的CALL站点地址。
 * 要求在此之前，注册到授权人的GATE站点。
 * 
 * @author scott.liang
 * @version 1.0 8/6/2018
 * @since laxcus 1.0
 */
public class FrontTakeAuthorizerCallInvoker extends FrontInvoker {
	
	/**
	 * 构造查找授权人CALL站点调用器，指定命令
	 * @param cmd 查找授权人CALL站点
	 */
	public FrontTakeAuthorizerCallInvoker(TakeAuthorizerCall cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeAuthorizerCall getCommand() {
		return (TakeAuthorizerCall) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeAuthorizerCall cmd = getCommand();
		Siger authorizer = cmd.getAuthorizer();
		// 根据授权人签名找到GATE站点，然后把命令发送到GATE站点
		Node hub = AuthroizerGateOnFrontPool.getInstance().findSite(authorizer);
		boolean success = (hub != null);
		if (success) {
			success = launchTo(hub, cmd);
		}
		
		Logger.debug(this, "launch", success, "check call site [%s] to [%s]", authorizer, hub);
		
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		TakeAuthorizerCallProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeAuthorizerCallProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		if (product == null) {
			Logger.error(this, "ending", "cannot find product");
			return false;
		}

		Map<Node, SpaceSet> spaces = product.getSpaces();
		
		Logger.debug(this, "ending", "[%s] call site count %d", getCommand().getAuthorizer(), spaces.size());
		
		// 如果在NAT环境，连接CALL节点
		checkPocks(spaces.keySet());

		// 保存数据表名和它的站点
		for (Node node : spaces.keySet()) {
			SpaceSet set = spaces.get(node);
			for (Space space : set.list()) {
				Logger.debug(this, "ending", "save '%s %s'", node, space);
				getStaffPool().addTableSite(node, space);
			}
		}
		return useful();
	}
	
	/**
	 * 如果位于内网，确认NAT地址
	 * @param set
	 */
	private void checkPocks(Set<Node> set) {
		// 逐一判断
		for(Node node : set) {
			checkPock(node);
		}
	}

}