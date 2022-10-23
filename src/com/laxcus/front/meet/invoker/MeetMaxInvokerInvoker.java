/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;


import com.laxcus.command.mix.*;
import com.laxcus.remote.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 设置调用器数目调用器。
 * 只在本地执行
 * 
 * @author scott.liang
 * @version 1.0 9/12/2020
 * @since laxcus 1.0
 */
public class MeetMaxInvokerInvoker extends MeetInvoker {

	/**
	 * 构造应答包传输模式调用器，指定命令
	 * @param cmd 应答包传输模式
	 */
	public MeetMaxInvokerInvoker(MaxInvoker cmd) {
		super(cmd);
		cmd.setFast(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MaxInvoker getCommand() {
		return (MaxInvoker) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		MaxInvoker cmd = getCommand();
		
		if (cmd.isLocal()) {
			reset();
		} else {
			faultX(FaultTip.PERMISSION_MISSING); // 权限不足
		}
		// 投递到HUB站点
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 调用器数目
	 */
	private void reset() {
		MaxInvoker cmd = getCommand();

		EchoTransfer.setMaxInvokers(cmd.getInvokers());
		EchoTransfer.setMaxConfineTime(cmd.getConfineTime());
		long ms = EchoTransfer.getMaxConfineTime();
		
		// 设置标题
		if (ms < 1 || ms >= 1000) {
			createShowTitle(new String[] { "MAX-INVOKER/LOCAL/INVOKERS",
					"MAX-INVOKER/LOCAL/CONFINE-TIME" });
		} else {
			createShowTitle(new String[] { "MAX-INVOKER/LOCAL/INVOKERS",
					"MAX-INVOKER/LOCAL/CONFINE-TIME-MS" });
		}
		String alway = getXMLContent("MAX-INVOKER/ALWAY");
		
		// 显示单元
		ShowItem item = new ShowItem();

		item.add(new ShowIntegerCell(0, EchoTransfer.getMaxInvokers()));
		if (ms < 1) {
			item.add(new ShowStringCell(1, alway));
		} else {
			if (ms >= 1000) {
				item.add(new ShowLongCell(1, ms / 1000));
			} else {
				item.add(new ShowLongCell(1, ms));
			}
		}
		// 保存单元
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}

}