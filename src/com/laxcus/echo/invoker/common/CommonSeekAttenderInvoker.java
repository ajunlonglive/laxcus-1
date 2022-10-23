/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.attend.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;

/**
 * 查找调用器的调用器。<br>
 * 
 * 根据命令中提供的调用器监听地址，判断一个调用器处于“活着”状态。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/18/2017
 * @since laxcus 1.0
 */
public class CommonSeekAttenderInvoker extends CommonInvoker {

	/**
	 * 构造查找调用器的调用器，指定命令
	 * @param cmd 环形证明命令
	 */
	public CommonSeekAttenderInvoker(SeekAttender cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekAttender getCommand() {
		return (SeekAttender) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekAttender cmd = getCommand();

		// 根据调用器编号，找到关联的异步调用器
		Cabin cabin = cmd.getCabin();
		EchoInvoker invoker = getLauncher().getInvokerPool().findInvoker(cabin.getInvokerId());

		// 没有找到调用器，返回错误通知。
		boolean success = (invoker != null);
		if (!success) {
			replyProduct(new SeekAttenderProduct(cabin, false));
			return useful(false);
		}
		
		SeekAttenderProduct product = null;
		// 判断继承自InvokerAuditor接口
		success = isInvokerAuditor(invoker);

		// 如果匹配，转成接口调用它；否则生成一个错误
		if(success){
			Attender attender = (Attender) invoker;
			int status = attender.attend(cmd.getSource());
			// 处理以下两种状态，如果是DELAY状态，是由调用器自己处理，这里不忽略。
			switch (status) {
			case AttendTag.CONFORM:
				product = new SeekAttenderProduct(cabin, true);
				break;
			case AttendTag.REFUSE:
				product = new SeekAttenderProduct(cabin, false);
				break;
			}
		} else {
			product = new SeekAttenderProduct(cabin, false);
		}

		// 判断调用器有效，然后发送它
		success = (product != null) ;
		if(success) {
			success = replyProduct(product);
		}
		
		Logger.debug(this, "launch", success, "check %s, from %s", cabin, cmd.getSource());

		return useful(success);
	}

	/**
	 * 判断传入类实现InvokerAuditor接口
	 * @param invoker 调用器
	 * @return 返回真或者假
	 */
	private boolean isInvokerAuditor(Object invoker) {
		Class<?> clazz = invoker.getClass();
		Class<?>[] notes = clazz.getInterfaces();

		int size = (notes == null ? 0 : notes.length);
		// 没有接口，返回假
		if (size == 0) {
			return false;
		}
		// 判断实现InvokerAuditor接口
		for (int i = 0; i < size; i++) {
			if (notes[i] == Attender.class) {
				return true;
			}
		}
		// 判断上级类实现InvokerAuditor接口
		Class<?> parent = clazz.getSuperclass();
		if (parent == null) {
			return false;
		}
		return isInvokerAuditor(parent);
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
