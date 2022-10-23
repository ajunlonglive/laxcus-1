/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.util.*;

import com.laxcus.echo.*;
import com.laxcus.command.attend.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 查找签到器转发命令调用器
 * 
 * @author scott.liang
 * @version 1.0 3/20/2017
 * @since laxcus 1.0
 */
public class CommonShiftSeekAttenderInvoker extends CommonInvoker {

	/**
	 * 构造默认的查找签到器转发命令调用器
	 * @param cmd
	 */
	public CommonShiftSeekAttenderInvoker(ShiftSeekAttender cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftSeekAttender getCommand() {
		return (ShiftSeekAttender) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftSeekAttender shift = getCommand();
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		// 生成命令数组
		for (Cabin cabin : shift.getHubs()) {
			SeekAttender cmd = new SeekAttender(cabin);
			cmd.setTimeout(shift.getTimeout()); // 转发设置命令超时时间
			cmd.setQuick(true); // 要求快速处理
			
			// 命令处理单元
			CommandItem item = new CommandItem(cabin.getNode(), cmd);
			array.add(item);
		}

		// 以容错模式发送
		int count = incompleteTo(array);
		boolean success = (count > 0);
		// 以上不成功，唤醒钩子，退出
		if (!success) {
			shift.getHook().done();
		}

		Logger.debug(this, "launch", success, "send count:%d", count);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		List<Integer> keys = getEchoKeys();
		SeekAttenderTable table = new SeekAttenderTable();
		// 收集反馈结果
		for (int i = 0; i < keys.size(); i++) {
			int index = keys.get(i);
			try {
				if (isSuccessObjectable(index)) {
					SeekAttenderProduct product = getObject(
							SeekAttenderProduct.class, index);
					table.add(product);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 输出处理结果
		ShiftSeekAttender shift = getCommand();
		shift.getHook().setResult(table);

		int count = table.size();
		boolean success = (count > 0);

		Logger.debug(this, "ending", success,
				"Cabin size:%d, SeekInvokeProduct size:%d", shift.getHubSize(), count);

		return useful(success);
	}

}
