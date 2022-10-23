/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 转发推送在线用户命令调用器，指定命令
 * 
 * @author scott.liang
 * @version 1.0 1/12/2020
 * @since laxcus 1.0
 */
public class CallShiftPushOnlineMemberInvoker extends CallInvoker {

	/**
	 * 构造转发推送在线用户命令，指定命令
	 * @param shift 转发推送在线用户命令
	 */
	public CallShiftPushOnlineMemberInvoker(ShiftPushOnlineMember shift) {
		super(shift);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftPushOnlineMember getCommand(){
		return (ShiftPushOnlineMember)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftPushOnlineMember shift = getCommand();
		Siger siger = shift.getSiger();

		FrontSeat seat = new FrontSeat(siger, getLocal(), shift.getFront());
		// 找到明文
		Refer refer = StaffOnCallPool.getInstance().findRefer(siger);
		if (refer != null) {
			seat.setPlainText(refer.getUser().getPlainText());
		}
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