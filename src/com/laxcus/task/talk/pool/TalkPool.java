/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.talk.pool;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.command.task.talk.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.talk.*;

/**
 * 交互对话管理池。<br><br>
 * 
 * 运行在DATA/WORK/BUILD节点。
 * 
 * @author scott.liang
 * @version 1.0 6/13/2018
 * @since laxcus 1.0
 */
public class TalkPool extends VirtualPool implements TalkTrustor {

	/** 命令管理池  **/
	private CommandPool commandPool;

	/** 调用器管理池 **/
	private InvokerPool invokerPool;

	/** 交互对话管理池句柄 **/
	private static TalkPool selfHandle = new TalkPool();

	/** 对话元组集合 **/
	private ArrayList<TalkTuple> array = new ArrayList<TalkTuple>(100);

	/**
	 * 构造默认和私有的交互对话管理池
	 */
	private TalkPool() {
		super();
	}

	/**
	 * 输出交互对话管理池句柄。
	 * 
	 * @return 交互对话管理池
	 */
	public static TalkPool getInstance() {
		// 安全检查
		VirtualPool.check("getInstance");
		// 输出句柄
		return TalkPool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		commandPool = getLauncher().getCommandPool();
		invokerPool = getLauncher().getInvokerPool();
		return (commandPool != null && invokerPool != null);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.debug(this, "process", "into...");
		while (!isInterrupted()) {
			check();
			sleep();
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	/**
	 * 对命令分布基本参数，保持前后一致
	 * @param cmd 异步命令
	 */
	private void grant(Command cmd) {
		// 没用定义来源调用器编号，忽略
		if (!cmd.hasRelateId()) {
			return;
		}

		long invokerId = cmd.getRelateId();
		EchoInvoker invoker = invokerPool.findInvoker(invokerId);
		if (invoker == null) {
			throw new EchoException("cannot be find invoker, by %d", invokerId);
		}

		// 固定为内存模式
		cmd.setMemory(true);
		// 设置用户签名
		cmd.setIssuer(invoker.getIssuer());
		// 最高等级的优先处理特权
		cmd.setFast(true);

		Logger.debug(this, "grant", "Memory:%s, Issuer:%s", cmd.isMemory(), cmd.getIssuer());
	}

	/**
	 * 处理对话元组
	 * @param tuple
	 */
	private void doTuple(TalkTuple tuple) {
		Command cmd = tuple.getCommand();
		grant(cmd);
		boolean success = false;
		try {
			success = commandPool.admit(cmd);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		tuple.setSuccessful(success);
	}

	/**
	 * 执行异步处理操作
	 */
	private void check() {
		super.lockSingle();
		try {
			while (array.size() > 0) {
				TalkTuple tuple = array.remove(0);
				doTuple(tuple);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 保存一个命令
	 * @param cmd 
	 * @return
	 */
	private boolean add(Command cmd) {
		// 生成命令元组
		TalkTuple tuple = new TalkTuple(cmd);

		// 保存命令
		boolean success = false;
		super.lockSingle();
		try {
			success = array.add(tuple);
		} catch (Throwable e) {
			
		} finally {
			super.unlockSingle();
		}

		// 成功，唤醒线程和等待结果；失败，设置不成功
		if (success) {
			wakeup();
			tuple.await();
		} else {
			tuple.setSuccessful(false);
		}
		// 返回成功或者否
		return tuple.isSuccessful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.talk.TalkTrustor#check(com.laxcus.site.Node, com.laxcus.task.talk.TalkTag)
	 */
	@Override
	public TaskMoment check(long invokerId, Node remote, TalkFalg tag) throws TaskException {
		TalkCheck cmd = new TalkCheck(tag);
		TalkCheckHook hook = new TalkCheckHook();
		ShiftTalkCheck shift = new ShiftTalkCheck(remote, cmd, hook);
		// 如果调用器编号有效，设置它
		if (InvokerIdentity.isValid(invokerId)) {
			shift.setRelateId(invokerId); // 设置调用器编号
		}

		// 提交
		boolean success = add(shift);
		if (!success) {
			throw new TaskException("cannot submit to hub");
		}
		// 进入等待状态
		hook.await();

		// 返回判断状态
		TalkCheckProduct product = hook.getProduct();
		if (product == null) {
			throw new TaskException("implement failed!");
		}
		return product.getStatus();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.talk.TalkTrustor#ask(com.laxcus.site.Node, com.laxcus.task.talk.TalkQuest)
	 */
	@Override
	public TalkReply ask(long invokerId, Node remote, TalkQuest quest) throws TaskException {
		TalkAsk cmd = new TalkAsk(quest);
		TalkAskHook hook = new TalkAskHook();
		ShiftTalkAsk shift = new ShiftTalkAsk(remote, cmd, hook);
		// 如果调用器编号有效，设置它
		if (InvokerIdentity.isValid(invokerId)) {
			shift.setRelateId(invokerId); // 设置调用器关联编号
		}

		// 提交
		boolean success = add(shift);
		if (!success) {
			throw new TaskException("cannot submit to hub");
		}
		// 进入等待状态
		hook.await();

		// 返回判断状态
		TalkAskProduct product = hook.getProduct();
		if(product == null) {
			throw new TaskException("implement failed!");
		}
		return product.getReply();
	}

}