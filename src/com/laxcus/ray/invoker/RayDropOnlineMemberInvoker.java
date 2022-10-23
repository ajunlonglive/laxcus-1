/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.site.watch.*;

/**
 * 推送注册用户给WATCH站点调用器
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public class RayDropOnlineMemberInvoker extends RayCastElementInvoker {

	/**
	 * 构造推送注册用户给WATCH站点，设置命令
	 * @param cmd 推送注册用户给WATCH站点
	 */
	public RayDropOnlineMemberInvoker(DropOnlineMember cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropOnlineMember getCommand() {
		return (DropOnlineMember) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropOnlineMember cmd = getCommand();
		// 删除在线用户
		dropOnlineMember(cmd.list());

		//		// 保存和显示
		//		for (Seat seat : cmd.list()) {
		//			// 判断存在
		//			boolean b1 = OnlineMemberBasket.getInstance().contains(seat.getSiger());
		//			// 删除
		//			OnlineMemberBasket.getInstance().remove(seat);
		//			// 判断不存在
		//			boolean b2 = OnlineMemberBasket.getInstance().contains(seat.getSiger());
		//			// 从界面上删除
		//			if (b1 && !b2) {
		//
		//			}
		//		}

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}