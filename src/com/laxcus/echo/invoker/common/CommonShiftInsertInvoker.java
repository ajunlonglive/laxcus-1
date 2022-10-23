/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.access.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 转发INSERT调用器 <br>
 * 
 * 从此发出的INSERT等同于CALL站点的第一阶段处理，包括：
 * 1. 发送一个INSERT PROMPT 命令，确认DATA站点接受
 * 2. 发送INSERT数据，等待DATA写入的反馈，输出INSERT ASSERT命令
 * 
 * @author scott.liang
 * @version 1.21 12/20/2014
 * @since laxcus 1.0
 */
public class CommonShiftInsertInvoker extends CommonInvoker {

	/** 数据操作阶段，从1开始 **/
	private int step = 1;

	/**
	 * 构造转发INSERT调用器，指定转发命令
	 * @param shift INSERT转发命令
	 */
	public CommonShiftInsertInvoker(ShiftInsert shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftInsert getCommand() {
		return (ShiftInsert) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return todo();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return todo();
	}

	/**
	 * 按照顺序执行异步调用
	 * @return 成功返回“真”，否则“假”。
	 */
	private boolean todo() {
		boolean success = false;
		switch (step) {
		case 1:
			success = doFirst();
			break;
		case 2:
			success = doSecond();
			break;
		case 3:
			success = doThird();
			break;
		case 4:
			success = doFourth();
			break;
		}
		// 无论结果，自增1
		step++;

		Logger.debug(this, "todo", success, "step is %d", step - 1);

		return success;
	}

	/**
	 * 发送一个INSERT PROMPT命令到DATA站点
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		ShiftInsert shift = getCommand();
		Insert cmd = shift.getCommand();
		InsertGuide guide = new InsertGuide(cmd.getSpace());
		guide.setIssuer(getIssuer()); // 用户签名

		Node hub = shift.getHub();
		CommandItem item = new CommandItem(hub, guide);

		// 发送到目标站点
		boolean success = completeTo(item);
		if (!success) {
			InsertHook hook = shift.getHook();
			hook.setFault(new EchoException("cannot be send to %s", hub));
		}

		return success;
	}

	/**
	 * 接收DATA反馈，发送INSERT命令
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		ShiftInsert shift = getCommand();
		InsertHook hook = shift.getHook();

		int index = findEchoKey(0);
		// 判断DATA站点反馈成功
		InsertGuide guide = null;
		try {
			if (isSuccessObjectable(index)) {
				guide = getObject(InsertGuide.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
			hook.setFault(e);
			return false;
		}

		// 确认正确
		boolean success = (guide != null);
		if (!success) {
			Logger.error(this, "doSecond", "refused by a data site"); // DATA失败
			hook.setFault(new EchoException("cannot be support!"));
			return false;
		}

		// DATA主站点监听器通讯地址
		Cabin hub = guide.getSource();
		byte[] b = shift.getCommand().build();
		ReplyItem item = new ReplyItem(hub, b);
		// 发送INSERT命令给DATA主站点调用器
		success = replyTo(item);
		// 如果不成功，发送一个错误
		if (!success) {
			hook.setFault(new EchoException("cannot be send to %s", hub));
		}

		Logger.debug(this, "doSecond", success, "reply to %s", hub);

		return success;
	}

	//	/**
	//	 * 第三阶段，接收DATA反馈的INSERT ASSUME命令
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean doThird() {
	//		VisitException excption = null;
	//
	//		int index = findEchoKey(0);
	//		AssumeInsert assume = null;
	//		try {
	//			if (isSuccessObjectable(index)) {
	//				assume = getObject(AssumeInsert.class, index);
	//			}
	//		} catch (VisitException e) {
	//			excption = e;
	//		}
	//
	//		// 判断有效
	//		boolean valid = (assume != null  );
	//		
	//		// 发送反馈结果
	//		if (valid) {
	//			Logger.debug(this, "doThird", "result %s %s", assume.getSpace(), (assume.isSuccess() ? "成功" : "失败"));
	//			// 反馈结果
	//			byte status = (assume.isSuccess() ? ConsultStatus.CONFIRM : ConsultStatus.CANCEL);
	//			AssertInsert reply = new AssertInsert(assume.getSpace(), status);
	//			replyTo(assume.getSource(), reply);
	//		}
	//		
	//		// 唤醒钩子
	//		ShiftInsert shift = getCommand();
	//		InsertHook hook = shift.getHook();
	//		if (valid) {
	//			hook.setResult(assume);
	//		} else {
	//			if (excption != null) {
	//				Logger.error(excption);
	//			} else {
	//				hook.setFault(new VisitException("server error! cannot be insert!"));
	//			}
	//		}
	//		
	//		Logger.debug(this, "doThird", valid, "result is");
	//
	//		return useful(valid);
	//	}

	/**
	 * 第三阶段，接收DATA反馈的INSERT ASSUME命令
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		VisitException excption = null;

		int index = findEchoKey(0);
		AssumeInsert assume = null;
		try {
			if (isSuccessObjectable(index)) {
				assume = getObject(AssumeInsert.class, index);
			}
		} catch (VisitException e) {
			excption = e;
		}

		// 发送反馈结果
		//	Logger.debug(this, "doThird", "result %s %s", cmd.getSpace(), (assume.isSuccess() ? "成功" : "失败"));

		boolean success = false;
		// 反馈结果
		if (assume != null) {
			byte status = (assume.isSuccess() ? ConsultStatus.CONFIRM : ConsultStatus.CANCEL);
			AssertInsert reply = new AssertInsert(assume.getSpace(), status);
			// 投递给目标地址，返回结果!
			success = replyTo(assume.getSource(), reply);
		} 

		// 不成功，通知
		if (!success) {
			ShiftInsert shift = getCommand();
			InsertHook hook = shift.getHook();
			if (excption != null) {
				Logger.error(excption);
			} else {
				hook.setFault(new VisitException("server error! cannot be insert!"));
			}
		}

		Logger.debug(this, "doThird", success, "结果是");

		return success;
	}

	/**
	 * 接受DATA节点再次确认反馈
	 * @return 成功返回真，否则假
	 */
	private boolean doFourth() {
		VisitException excption = null;

		int index = findEchoKey(0);
		AssumeInsert assume = null;
		try {
			if (isSuccessObjectable(index)) {
				assume = getObject(AssumeInsert.class, index);
			}
		} catch (VisitException e) {
			excption = e;
		}
		
		// 确认成功
		boolean success = (assume != null && assume.isConfirmSuccess());
		
		ShiftInsert shift = getCommand();
		InsertHook hook = shift.getHook();
		// 选择反馈结果
		if (success) {
			hook.setResult(assume);
		} else {
			if (excption != null) {
				Logger.error(excption);
			} else {
				hook.setFault(new VisitException("server error! cannot be insert!"));
			}
		}
		
		Logger.debug(this, "doFourth", success, "确认结果是");

		return useful( success);
	}

}
