/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.access.permit.*;
import com.laxcus.log.client.*;
import com.laxcus.site.front.*;

/**
 * 检查注册账号的权级（管理员/普通注册用户）
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class GateTakeGradeInvoker extends GateInvoker {

	/**
	 * 构造账号权级调用器，指定命令
	 * @param cmd 注册账号权级命令
	 */
	public GateTakeGradeInvoker(TakeGrade cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeGrade getCommand() {
		return (TakeGrade) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 默认是无效
		int grade = GradeTag.OFFLINE;
		// 判断是管理员或者注册用户，必须是在线状态
		if (isAdministrator()) {
			grade = GradeTag.ADMINISTRATOR;
		} else if (isUser()) {
			grade = GradeTag.USER;
		}

		TakeGradeProduct product = new TakeGradeProduct(grade);

		// 反馈报告
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "%s grade is %s",
				getCommandSource(), GradeTag.translate(grade));

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