/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.invoker;

import com.laxcus.command.relate.*;
import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;
import com.laxcus.util.*;
import com.laxcus.site.Node;

/**
 * 转发查找授权人CALL站点调用器。<br>
 * 从GATE获得授权人的CALL站点地址。
 * 要求在此之前，注册到授权人的GATE站点。
 * 
 * @author scott.liang
 * @version 1.0 5/31/2019
 * @since laxcus 1.0
 */
public class FrontShiftTakeAuthorizerCallInvoker extends FrontInvoker {
	
	/**
	 * 构造查找授权人CALL站点调用器，指定命令
	 * @param cmd 查找授权人CALL站点
	 */
	public FrontShiftTakeAuthorizerCallInvoker(ShiftTakeAuthorizerCall cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeAuthorizerCall getCommand() {
		return (ShiftTakeAuthorizerCall) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeAuthorizerCall shift = getCommand();
		TakeAuthorizerCall cmd = shift.getCommand();
		Node hub = shift.getHub();
		Siger authorizer = cmd.getAuthorizer();
		// 如果没有指定授权人的GATE节点，根据授权人签名从连接池找到GATE站点，然后把命令发送到GATE站点
		if (hub == null) {
			hub = AuthroizerGateOnFrontPool.getInstance().findSite(authorizer);
		}
		// 判断站点有效发送到指定的地址
		boolean success = (hub != null);
		if (success) {
			success = launchTo(hub, cmd);
		}
		// 不成功，退出
		if(!success) {
			shift.getHook().done();
		}
		
		Logger.debug(this, "launch", success, "check call site [%s] to [%s]", authorizer, hub);
		
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftTakeAuthorizerCall shift = getCommand();
		TakeAuthorizerCallHook hook = shift.getHook();
		
		TakeAuthorizerCallProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeAuthorizerCallProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功或者失败！
		boolean success = (product != null);
		if (success) {
			hook.setResult(product);
		}
		hook.done();

		return useful(success);
		//
		//		if (product == null) {
		//			Logger.error(this, "ending", "cannot find product");
		//			return false;
		//		}
		//
		//		Map<Node, SpaceSet> spaces = product.getSpaces();
		//		
		//		Logger.debug(this, "ending", "[%s] call site count %d", getCommand().getAuthorizer(), spaces.size());
		//		
		//		// 如果在NAT环境，连接CALL节点
		//		checkPocks(spaces.keySet());
		//
		//		// 保存数据表名和它的站点
		//		for (Node node : spaces.keySet()) {
		//			SpaceSet set = spaces.get(node);
		//			for (Space space : set.list()) {
		//				Logger.debug(this, "ending", "save '%s %s'", node, space);
		//				getStaffPool().addSpace(node, space);
		//			}
		//		}
		//		return useful();
	}
	
//	/**
//	 * 如果位于内网，确认NAT地址
//	 * @param set
//	 */
//	private void checkPocks(Set<Node> set) {
//		// 逐一判断
//		for(Node node : set) {
//			checkPock(node);
//		}
//	}
	


}