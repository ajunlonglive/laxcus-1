/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.access.table.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;

/**
 * 导出数据块调用器
 * 
 * @author scott.liang
 * @version 1.0 2/11/2018
 * @since laxcus 1.0
 */
public class CallSingleExportEntityInvoker extends CallInvoker {

	/**
	 * 构造默认的导出数据块调用器，指定命令
	 * @param cmd 导出数据块
	 */
	public CallSingleExportEntityInvoker(SingleExportEntity cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SingleExportEntity getCommand(){
		return (SingleExportEntity)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SingleExportEntity cmd = getCommand();
		Space space = cmd.getSpace();
		long stub = cmd.getStub();

		// 查找主站点
		List<Node> nodes = DataOnCallPool.getInstance().findPrimeSites(space, stub);
		if (nodes.isEmpty()) {
			replyFault(Major.FAULTED, Minor.SITE_NOTFOUND);
			return false;
		}

		Node hub = nodes.get(0);
		boolean success = completeTo(hub, cmd);
		// 不成功，退出
		if (!success) {
			replyFault(Major.FAULTED, Minor.SYSTEM_FAILED);
		}

		Logger.debug(this, "launch", success, "send to %s", hub);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		EchoBuffer buf = findBuffer(index);
		// 不成功显示错误
		if (!buf.isSuccessCompleted()) {
			EchoHead head = buf.getHead();
			// 返回错误
			replyFault(head.getCode(), head.getHelp());
			return false;
		}

		boolean success = false;
		// 判断数据在磁盘或者文件，选择发送
		if (buf.isDisk()) {
			File file = buf.getFile();
			success = replyFile(file);
		} else {
			byte[] b = buf.getMemory();
			success = replyPrimitive(b);
		}
		
		Logger.debug(this, "ending", success, "result is");

		return useful(success);
	}

}
