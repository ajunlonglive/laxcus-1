/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 设置FRONT本地异步缓存调用器。<br>
 * 
 * 只在FRONT站点起作用，不影响集群其它节点。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2019
 * @since laxcus 1.0
 */
public class MeetMaxEchoBufferInvoker extends MeetInvoker {

	/**
	 * 构造设置FRONT本地异步缓存调用器，指定命令
	 * @param cmd 设置FRONT本地异步缓存
	 */
	public MeetMaxEchoBufferInvoker(MaxEchoBuffer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MaxEchoBuffer getCommand() {
		return (MaxEchoBuffer) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		MaxEchoBuffer cmd = getCommand();

		// 如果不是本地，显示权限不足
		if (!cmd.isLocal()) {
			faultX(FaultTip.PERMISSION_MISSING); // 权限不足
			return false;
		}

		long capacity = cmd.getCapacity();
		// 设置本地时间
		EchoArchive.setMaxBufferSize(capacity);
		print(capacity);

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
	 * 显示结果
	 * @param capacity
	 */
	private void print(long capacity) {
		createShowTitle(new String[] { "LOCAL-MAX-ECHO-BUFFER/CAPACITY" });

		ShowItem item = new ShowItem();
		if (capacity == 0) {
			String text = getXMLContent("LOCAL-MAX-ECHO-BUFFER/ANY");
			item.add(new ShowStringCell(0, text));
		} else {
			String text = ConfigParser.splitCapacity(capacity, 2);
			item.add(new ShowStringCell(0, text));
		}
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}

}