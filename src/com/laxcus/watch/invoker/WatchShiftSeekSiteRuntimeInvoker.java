/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.util.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 转发检查被WATCH站点监视的站点状态调用器
 * 
 * @author scott.liang
 * @version 1.0 4/13/2018
 * @since laxcus 1.0
 */
public class WatchShiftSeekSiteRuntimeInvoker extends WatchInvoker {

	/**
	 * 构造转发检查被WATCH站点监视的站点状态，指定转发命令
	 * @param shift 转发检查被WATCH站点监视的站点状态
	 */
	public WatchShiftSeekSiteRuntimeInvoker(ShiftSeekSiteRuntime shift) {
		super(shift);
		// 不要记录!
		setTigger(false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftSeekSiteRuntime getCommand() {
		return (ShiftSeekSiteRuntime) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 判断是登录
		if (!isLogined()) {
			return useful(false);
		}

		ShiftSeekSiteRuntime shift = getCommand();
		
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		// 生成命令数组
		for (Node node : shift.list()) {
			SeekSiteRuntime cmd = new SeekSiteRuntime(true); // 要求极 速处理
			cmd.setTigger(false); // 不要记录到Tigger.command
			
			CommandItem item = new CommandItem(node, cmd);
			array.add(item);
		}

		// 直接投递，不等待反馈
		int count = directTo(array, false);
		boolean success = (count == array.size());
		
		// 如果没有发送成功，要求重新注册到节点
		if (count == 0) {
			getLauncher().checkin(false);
		}

		Logger.debug(this, "launch", success, "sites:%d, send count:%d",
				array.size(), count);

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
