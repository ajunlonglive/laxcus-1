/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.shutdown.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;

/**
 * “SHUTDOWN”转发命令调用器。
 * SHUTDOWN命令由WATCH站点，分发给它监视的全部站点，包括它的上级站点：HOME/TOP。
 * 
 * @author scott.liang
 * @version 1.0 
 * @since laxcus 1.0
 */
public class CommandShiftShutdownInvoker extends CommonInvoker {

	/**
	 * 构造“SHUTDOWN”转发命令调用器
	 * @param cmd “SHUTDOWN”转发命令
	 */
	public CommandShiftShutdownInvoker(ShiftShutdown cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftShutdown shift = (ShiftShutdown)super.getCommand();
		
		Node[] sites = shift.getSites();
		Shutdown cmd = shift.getCommand();
		
		int count = 0;
		// 重置回显缓存数目
		super.resetAllBuffers(sites.length);
		// 建立缓存和发送命令
		for (int index = 0; index < sites.length; index++) {
			Shutdown clone = (Shutdown) cmd.duplicate();
			
			Node hub = sites[index];
			
			Cabin local = createLocalCabin(hub, index);
			clone.setSource(local);

			boolean success = createBuffer(local.getFlag(), false, clone, hub);
			if (success) {
				success = super.submit(sites[index], clone);
			}
			if (success) {
				count++;
				// 打印成功
			} else {
				super.removeBuffer(index);
				// 打印出错
			}
		}
		
		boolean success = (count > 0);
		Logger.debug(this, "launch", success, "send success:%d, failed:%d", count, sites.length - count);
		
		return success;
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
