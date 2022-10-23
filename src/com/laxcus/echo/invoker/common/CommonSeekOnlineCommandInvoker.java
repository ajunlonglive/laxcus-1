/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.command.mix.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 检索站点在线命令调用器
 * 
 * @author scott.liang
 * @version 1.0 4/16/2018
 * @since laxcus 1.0
 */
public class CommonSeekOnlineCommandInvoker extends CommonInvoker {

	/**
	 * 构造检索站点在线命令，指定命令
	 * @param cmd 检索站点在线命令
	 */
	public CommonSeekOnlineCommandInvoker(SeekOnlineCommand cmd) {
		super(cmd);
	}
	
	/**
	 * 判断是自己的命令
	 * 
	 * @param cmd 传入命令
	 * @return 返回真或者假
	 */
	private boolean isSelf(Command cmd) {
		Cabin source = getCommandSource();
		return (Laxkit.compareTo(source, cmd.getSource()) == 0 &&
				cmd.getClass() == SeekOnlineCommand.class);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekOnlineCommandProduct product = new SeekOnlineCommandProduct();
		product.setSite(getLocal());
		
		CommandPool commandPool = getLauncher().getCommandPool();
		InvokerPool invokerPool = getLauncher().getInvokerPool();
		
		// 处于等待状态的命令
		List<Command> array = commandPool.getCommands();
		for (Command cmd : array) {
			SeekOnlineCommandItem e = new SeekOnlineCommandItem(cmd.getClass().getSimpleName(), cmd.getIssuer());
			e.setRunning(false);
			e.setSource(cmd.getSource());
			e.setMemory(cmd.isMemory());
			e.setDirect(cmd.isDirect());
			e.setPriority(cmd.getPriority());
			e.setOnlineTime(System.currentTimeMillis() - cmd.getCreateTime());
			product.add(e);
		}
		
		// 运行状态的命令
		array = invokerPool.getCommands();
		for (Command cmd : array) {
			// 如果是自己，忽略
			if (isSelf(cmd)) {
				continue;
			}
			
			// 如果调用器存在，找到它的线程编号
			long threadId = -1;
			EchoInvoker invoker = invokerPool.findInvoker(cmd.getLocalId());
			if (invoker != null) {
				threadId = invoker.getThreadId();
			}
			
			SeekOnlineCommandItem e = new SeekOnlineCommandItem(cmd.getClass().getSimpleName(), cmd.getIssuer());
			e.setRunning(true);
			e.setSource(cmd.getSource());
			e.setMemory(cmd.isMemory());
			e.setDirect(cmd.isDirect());
			e.setPriority(cmd.getPriority());
			e.setOnlineTime(System.currentTimeMillis() - cmd.getCreateTime());
			e.setThreadId(threadId);
			product.add(e);
		}
		
		// 反馈给WATCH节点
		boolean success = replyProduct(product);
		
		Logger.debug(this, "launch", success, "size is:%d", product.size());

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}