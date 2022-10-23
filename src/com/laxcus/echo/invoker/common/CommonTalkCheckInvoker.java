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
import com.laxcus.command.task.talk.*;
import com.laxcus.distribute.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.*;
import com.laxcus.util.hash.*;

/**
 * 分布任务组件状态查询调用器
 * 
 * @author scott.liang
 * @version 1.0 6/14/2018
 * @since laxcus 1.0
 */
public class CommonTalkCheckInvoker extends CommonInvoker {

	/**
	 * 构造分布任务组件状态查询调用器，指定命令
	 * @param cmd 分布任务组件状态查询
	 */
	public CommonTalkCheckInvoker(TalkCheck cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TalkCheck getCommand() {
		return (TalkCheck) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TalkCheck cmd = getCommand();
		TalkFalg flag = cmd.getFlag();
		SHA256Hash master = flag.getMaster();

		// 默认没有找到
		int status = TaskStatus.NOTFOUND;
		int number = -1;

		// 在命令池中
		List<Command> commands = getLauncher().getCommandPool().findCommands(DistributedStep.class);
		for (Command e : commands) {
			StepSession session = ((DistributedStep) e).getSession();
			if (Laxkit.compareTo(master, session.getMaster()) == 0) {
				status = TaskStatus.COMMAND;
				number = session.getNumber();
				break;
			}
		}

		// 在调用器中
		if (TaskStatus.isNotFound(status)) {
			// 找到关联的调用器编号
			List<Long> keys = getLauncher().getInvokerPool().findInvokerKeys(DistributedStep.class);
			for (long invokerId : keys) {
				// 找到调用器
				EchoInvoker invoker = getLauncher().getInvokerPool().findInvoker(invokerId);
				if (invoker == null) {
					continue;
				}
				// 从命令中取出
				Command e = invoker.getCommand();
				StepSession session = ((DistributedStep) e).getSession();
				if (Laxkit.compareTo(master, session.getMaster()) == 0) {
					status = TaskStatus.INVOKER;
					number = session.getNumber();
					break;
				}
			}
		}

		Logger.debug(this, "launch", "%s : %s", getCommand().getFlag(),
				TaskStatus.translate(status));
		
		TaskMoment model = new TaskMoment(status, number);

		TalkCheckProduct product = new TalkCheckProduct(model);
		boolean success = replyProduct(product);

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