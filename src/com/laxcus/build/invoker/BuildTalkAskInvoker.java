/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.invoker;

import java.util.*;

import com.laxcus.command.task.talk.*;
import com.laxcus.distribute.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.*;

/**
 * 分布任务组件远程交互调用器
 * 
 * @author scott.liang
 * @version 1.0 6/14/2018
 * @since laxcus 1.0
 */
public class BuildTalkAskInvoker extends BuildInvoker {

	/**
	 * 构造分布任务组件远程交互调用器，指定命令
	 * @param cmd 分布任务组件远程交互
	 */
	public BuildTalkAskInvoker(TalkAsk cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TalkAsk getCommand() {
		return (TalkAsk) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TalkAsk cmd = getCommand();
		TalkQuest quest = cmd.getQuest();
		TalkFalg flag = quest.getFlag();

		// 找到对应的调用器
		EchoInvoker invoker = null;
		List<Long> keys = super.getLauncher().getInvokerPool().findInvokerKeys(DistributedStep.class);
		for (long invokerId : keys) {
			EchoInvoker e = getLauncher().getInvokerPool().findInvoker(invokerId);
			if (e == null) {
				continue;
			}
			StepSession session = ((DistributedStep) e.getCommand()).getSession();
			if (Laxkit.compareTo(flag.getMaster(), session.getMaster()) == 0) {
				invoker = e;
				break;
			}
		}

		TalkReply reply = null;
		if (invoker != null) {
			if (Laxkit.isClassFrom(invoker, BuildEstablishSiftInvoker.class)) {
				BuildEstablishSiftInvoker sub = (BuildEstablishSiftInvoker) invoker;
				reply = sub.ask(quest);
			}
		}

		TalkAskProduct product = new TalkAskProduct(reply);
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