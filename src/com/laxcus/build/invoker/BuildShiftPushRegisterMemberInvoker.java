/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.build.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 转发推送注册成员命令
 * @author scott.liang
 * @version 1.0 1/18/2020
 * @since laxcus 1.0
 */
public class BuildShiftPushRegisterMemberInvoker extends BuildInvoker {

	/**
	 * 构造转发推送注册成员命令
	 * @param shift 转发推送注册成员
	 */
	public BuildShiftPushRegisterMemberInvoker(ShiftPushRegisterMember shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftPushRegisterMember getCommand() {
		return (ShiftPushRegisterMember) super.getCommand();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftPushRegisterMember shift = getCommand();

		// 生成用户基点，推送给HOME节点
		PushRegisterMember cmd = new PushRegisterMember();
		for (Siger siger : shift.list()) {
			Seat seat = new Seat(siger, getLocal());
			// 查找资源引用
			Refer refer = StaffOnBuildPool.getInstance().findRefer(siger);
			if (refer != null) {
				seat.setPlainText(refer.getUser().getPlainText());
			}
			// 保存!
			cmd.add(seat);
		}
		boolean success = directToHub(cmd);
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
