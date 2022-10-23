/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.access.table.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 复制数据块调用器 <br><br>
 * 
 * CALL做为中继，把命令发给指定DATA地址。
 * 
 * @author scott.liang
 * @version 1.0 11/10/2020
 * @since laxcus 1.0
 */
public class CallCopyEntityInvoker extends CallInvoker {

	/**
	 * 构造默认的复制数据块调用器，指定命令
	 * @param cmd 复制数据块
	 */
	public CallCopyEntityInvoker(CopyEntity cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CopyEntity getCommand(){
		return (CopyEntity)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CopyEntity cmd = getCommand();
		Node to = cmd.getTo();
		Space space = cmd.getSpace();
		
		// 找到节点
		NodeSet hubs = DataOnCallPool.getInstance().findTableSites(space);
		boolean success = (hubs != null && hubs.exists(cmd.getFrom()) && hubs.exists(to));
		if (!success) {
			replyFault(Major.FAULTED, Minor.SITE_NOTFOUND);
			return false;
		}
		
		// 如果两个地址一致时..
		success = (Laxkit.compareTo(cmd.getFrom(), to) == 0);
		if (success) {
			replyFault(Major.FAULTED, Minor.DUPLEX);
			return false;
		}

		// 发送命令给指定节点
		success = completeTo(to, cmd);
		// 不成功，退出
		if (!success) {
			replyFault(Major.FAULTED, Minor.SYSTEM_FAILED);
		}

		Logger.debug(this, "launch", success, "send to %s", to);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 原样返回，不做处理
		return reflect();

		//		int index = findEchoKey(0);
		//		EchoBuffer buf = findBuffer(index);
		//		// 不成功显示错误
		//		if (!buf.isSuccessCompleted()) {
		//			EchoHead head = buf.getHead();
		//			// 返回错误
		//			replyFault(head.getCode(), head.getHelp());
		//			return false;
		//		}
		//
		//		boolean success = false;
		//		// 判断数据在磁盘或者文件，选择发送
		//		if (buf.isDisk()) {
		//			File file = buf.getFile();
		//			success = replyFile(file);
		//		} else {
		//			byte[] b = buf.getMemory();
		//			success = replyPrimitive(b);
		//		}
		//		
		//		Logger.debug(this, "ending", success, "result is");
		//
		//		return useful(success);
	}

}
