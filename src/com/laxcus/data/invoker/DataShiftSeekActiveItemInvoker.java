/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.command.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;
import com.laxcus.visit.*;

/**
 * 查询授权单元调用器。
 * 
 * @author scott.liang
 * @version 1.0 8/15/2017
 * @since laxcus 1.0
 */
public class DataShiftSeekActiveItemInvoker extends DataInvoker {

	/**
	 * 构造查询授权单元调用器，指定转发命令
	 * @param cmd 转发查询授权单元命令
	 */
	public DataShiftSeekActiveItemInvoker(ShiftSeekActiveItem cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftSeekActiveItem getCommand() {
		return (ShiftSeekActiveItem) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftSeekActiveItem shift = getCommand();
		SeekActiveItem cmd = shift.getCommand();
		Node hub = getHub();

		// 发送到TOP站点
		boolean success = completeTo(hub, cmd);
		// 不成功，唤醒钩子
		if (!success) {
			shift.getHook().done();
		}

		Logger.debug(this, "launch", success, "result is");

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		SeekActiveItemProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SeekActiveItemProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		ShiftSeekActiveItem shift = getCommand();
		SeekActiveItemHook hook = shift.getHook();
		
		// 成功，设置参数
		boolean success = (product != null);
		if (success) {
			hook.setResult(product);
		}
		// 唤醒
		hook.done();

		Logger.debug(this, "ending", success, "result is");

		return useful(success);
	}

}