/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.gate.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 转发推送在线用户命令调用器，指定命令
 * 
 * @author scott.liang
 * @version 1.0 1/12/2020
 * @since laxcus 1.0
 */
public class GateShiftPushOnlineMemberInvoker extends GateInvoker {

	/**
	 * 构造转发推送在线用户命令，指定命令
	 * @param shift 转发推送在线用户命令
	 */
	public GateShiftPushOnlineMemberInvoker(ShiftPushOnlineMember shift) {
		super(shift);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftPushOnlineMember getCommand() {
		return (ShiftPushOnlineMember) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftPushOnlineMember shift = getCommand();
		
		Siger siger = shift.getSiger();
		// 判断是管理员账号，如果是忽略它！
		Administrator admin = StaffOnGatePool.getInstance().getAdministrator();
		if (admin != null) {
			if (Laxkit.compareTo(admin.getUsername(), siger) == 0) {
				return useful(false);
			}
		}

		// 投递给BANK集群
		FrontSeat seat = new FrontSeat(siger, getLocal(), shift.getFront());
		// 找到用户明文
		Account account = StaffOnGatePool.getInstance().findAccount(siger);
		if (account != null) {
			seat.setPlainText(account.getUser().getPlainText());
		}
		// 生成命令，投递给BANK节点
		PushOnlineMember cmd = new PushOnlineMember(seat);
		boolean success = directToHub(cmd);

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}