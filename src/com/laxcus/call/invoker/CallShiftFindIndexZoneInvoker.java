/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.command.zone.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;
import com.laxcus.visit.*;

/**
 * 查询索引分区调用器
 * 
 * @author scott.liang
 * @version 1.0 5/21/2012
 * @since laxcus 1.0
 */
public class CallShiftFindIndexZoneInvoker extends CallInvoker {

	/**
	 * 构造查询索引分区调用器，指定转发命令
	 * @param shift 转发查询索引分区
	 */
	public CallShiftFindIndexZoneInvoker(ShiftFindIndexZone shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftFindIndexZone getCommand() {
		return (ShiftFindIndexZone) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftFindIndexZone shift = getCommand();
		FindIndexZone cmd = shift.getCommand();

		// 以“不完全”模式发送命令到目标站点
		List<Node> hubs = shift.getHubs();
		int count = incompleteTo(hubs, cmd);

		// 最少一个发送即成功；否则唤醒命令钩子
		boolean success = (count > 0);
		if (!success) {
			shift.getHook().done(); // 唤醒钩子
		}

		Logger.debug(this, "launch", success, "count:%d, size:%d", count, hubs.size());

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 接收异步应答数据
		ShiftFindIndexZone shift = getCommand();
		IndexZoneHook hook = shift.getHook();

		List<Integer> list = getEchoKeys();
		try {
			for(int index : list){	
				if (!isSuccessObjectable(index)) {
					continue;
				}
				IndexZoneProduct product = getObject(IndexZoneProduct.class, index);
				hook.addAll(product.list());
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 发出完成通知
		hook.done();
		
		Logger.debug(this, "ending", "hook is %d", hook.size());

		return useful();
	}

}
