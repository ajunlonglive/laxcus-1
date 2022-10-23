/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.gate.pool.*;
import com.laxcus.command.rule.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 绑定事务操作调用器。<br>
 * 
 * 命令由FRONT站点发出，GATE站点在此接受和提交事务请求给管理池。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2014
 * @since laxcus 1.0
 */
public class GateAttachRuleInvoker extends GateInvoker {

	/**
	 * 构造绑定事务操作调用器，指定命令
	 * @param cmd 绑定事务操作命令
	 */
	public GateAttachRuleInvoker(AttachRule cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AttachRule getCommand() {
		return (AttachRule) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AttachRule cmd = getCommand();
		Siger siger = cmd.getIssuer();

		// 如果没有定义用户签名，这是一个错误
		if(siger == null) {
			replyFault(Major.FAULTED, Minor.CLIENT_ERROR);
			return useful(false);
		}
		
		// 判断最大运行任务数目是在允许的范围内
		boolean allow = RuleHouse.getInstance().allow(siger);
		if (!allow) {
			replyFault(Major.FAULTED, Minor.MAX_JOBSOUT);
			return useful(false);
		}

		// 判断与禁止操作发生冲突，禁止操作包括读和写操作
		boolean forbid = ForbidHouse.getInstance().conflict(siger, cmd.list());
		// 判断限制操作发生冲突。限制操作是发生故障后，把“FaultItem”发送给LimitPool，转成“LimitItem”保存。
		if (!forbid) {
			forbid = LimitHouse.getInstance().conflict(siger, cmd.list());
		}
		// 如果冲突，返回冲突错误提示
		if (forbid) {
			replyFault(Major.FAULTED, Minor.LIMIT_FORBID);
			return useful(false);
		}

		// 提交到事务管理池，返回状态结果码
		int status = RuleHouse.getInstance().submit(cmd);
		
		// 显示结果
		Logger.info(this, "launch", "submit %s#%s to RuleManager, result is %s",
				cmd.getIssuer(), cmd.getSource(), RuleSubmit.translate(status));

		// 三种结果
		if(RuleSubmit.isRefuse(status)) {
			replyFault(Major.FAULTED, Minor.REFUSE);
			return useful(false);
		} else if(RuleSubmit.isAccepted(status)) {
			RuleProduct product = new RuleProduct(cmd.getIssuer(), cmd.getTag(), true);
			replyProduct(product);
		} else if(RuleSubmit.isWaiting(status)) {
			// 进行等待，此时不向FRONT节点反馈结果
		}
		
		// 调用器退出及释放
		return useful(true);

//		// 申请成功则发送通知；否则事务进入等待，就不必发送通知。
//		if (RuleSu) {
//			RuleProduct product = new RuleProduct(cmd.getIssuer(), cmd.getTag(), true);
//			replyProduct(product);
//		} else {
//			Logger.info(this, "launch", "%s#%s into waiting...", cmd.getIssuer(), cmd.getSource());
//		}
//
//		// 调用器退出及释放
//		return useful(success);
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