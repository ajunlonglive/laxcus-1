/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;

/**
 * 转发删除在线用户命令调用器，指定命令
 * 
 * @author scott.liang
 * @version 1.0 1/12/2020
 * @since laxcus 1.0
 */
public class CallShiftDropOnlineMemberInvoker extends CallInvoker {

	/**
	 * 构造转发删除在线用户命令，指定命令
	 * @param shift 转发删除在线用户命令
	 */
	public CallShiftDropOnlineMemberInvoker(ShiftDropOnlineMember shift) {
		super(shift);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftDropOnlineMember getCommand(){
		return (ShiftDropOnlineMember)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftDropOnlineMember shift = getCommand();

		FrontSeat seat = new FrontSeat(shift.getSiger(), getLocal(), shift.getFront());
		DropOnlineMember cmd = new DropOnlineMember(seat);
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