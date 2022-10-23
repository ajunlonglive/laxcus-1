/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.gate.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 建立用户账号命令调用器。<br>
 * 建立账号只允许由管理员来操作。<br>
 * 
 * 账号具有全网唯一性，为了保证这个唯一性，统一交给BANK站点串行处理。<br>
 * 
 * 完整流程：FRONT -> GATE -> BANK（串行） -> ACCOUNT -> BANK -> TOP -> BANK -> GATE -> FRONT
 * 
 * @author scott.liang
 * @version 1.0 7/6/2018
 * @since laxcus 1.0
 */
public class GateCreateUserInvoker extends GateInvoker {

	/**
	 * 构造建立账号命令调用器，指定命令 
	 * @param cmd 建立用户账号命令
	 */
	public GateCreateUserInvoker(CreateUser cmd) {
		super(cmd);
	}
	
	/**
	 * 将命令转发给BANK站点
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 判断有建立账号权限
		boolean success = canCreateUser();
		if (success) {
			success = transmit(); // 转发给BANK站点
		}
		// 反馈拒绝
		if (!success) {
			refuse();
		}
		return success;
	}

	/**
	 * 将BANK站点的应答转发给前端
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		dissolve();
		// 反馈结果
		return reflect();
	}

	/**
	 * 解除冗余信息
	 */
	private void dissolve() {
		CreateUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null && product.isSuccessful());

		// 判断成功
		if (success) {
			Siger siger = product.getUsername();
			// 冗余操作，把黑名单上的同名账户删除
			BlackOnGatePool.getInstance().remove(siger);
		}
	}

}