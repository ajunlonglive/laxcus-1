/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.command.conduct.*;
import com.laxcus.distribute.calculate.command.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.distribute.conduct.command.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.balance.*;
import com.laxcus.task.conduct.init.*;
import com.laxcus.task.meta.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;

/**
 * 分布计算命令调用器。<br><br>
 * 
 * 在CALL站点上，分布计算命令调用器执行INIT/BALANCE阶段任务调度工作，包括对资源检查和会话分派，以及平衡分布数据。
 * FROM操作转到DATA站点，TO操作转到WORK站点
 * 
 * @author scott.liang
 * @version 1.2 9/26/2013
 * @since laxcus 1.0
 */
public class CallConductInvoker extends CallDistributeCommandInvoker {

	/** CONDUCT执行步骤 **/
	private int step;

	/** TO阶段的迭代编号，从0开始 **/
	private int iterateToIndex;

	/** CONDUCT.BALANCE阶段平衡任务实例 **/
	private BalanceTask balanceTask;

	/**
	 * 构造分布计算命令调用器，指定命令
	 * @param conduct 分布计算命令。
	 */
	public CallConductInvoker(Conduct conduct) {
		super(conduct);
		// 从CONDUCT.INIT阶段开始
		setStep(PhaseTag.INIT);
		// TO阶段的迭代序号，从0开始
		iterateToIndex = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Conduct getCommand() {
		return (Conduct) super.getCommand();
	}

	/**
	 * 设置当前阶段
	 * @param who 阶段号
	 */
	private void setStep(int who) {
		if (!PhaseTag.isConduct(who)) {
			throw new IllegalPhaseException("illegal phase: %d", who);
		}
		step = who;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return todo();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return todo();
	}

	/**
	 * 执行CONDUCT各阶段处理
	 * @return 成功返回“真”，否则“假”。
	 */
	private boolean todo() {
		Logger.debug(this, "todo", "this is '%s'", PhaseTag.translate(step));

		boolean success = false;
		switch (step) {
		case PhaseTag.INIT:
			success = doInit();
			if (success) {
				setStep(PhaseTag.FROM);
			}
			break;
		case PhaseTag.FROM:
			success = doFrom();
			break;
		case PhaseTag.TO:
			success = doSubTo();
			break;
		case PhaseTag.PUT:
			success = doPut();
			setQuit(true);
			break;
		}

		Logger.debug(this, "todo", success, "next is '%s'", PhaseTag.translate(step));

		// 在退出或者不成功时，释放元数据和DATA/WORK分布节点数据
		if (isQuit() || !success) {
			release();
		}
		// 不成功，向终端发送错误信息
		if (!success) {
			if (faultText != null) {
				replyFault(Major.FAULTED, Minor.CONDUCT_ERROR, new DefaultEchoHelp(faultText));
			} else {
				replyFault(Major.FAULTED, Minor.CONDUCT_ERROR);
			}
		}

		return success;
	}

	/**
	 * 发送默认数据给FRONT节点
	 * @param b 默认数据
	 * @return 返回真或者假
	 */
	private boolean replyDefault(byte[] b) {
		boolean success = false;
		// 发送原始默认数据给FRONT
		try {
			success = replyPrimitive(b);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return success;
	}
	
	/**
	 * 执行CONDUCT.INIT阶段的参数检查和资源分配
	 * @param job conduct工作实例
	 * @return 成功返回true，否则false。
	 */
	private boolean doInit() {
		Conduct conduct = getCommand();
		// 1. 启动CONDUCT.INIT阶段
		boolean success = false;
		try {
			conduct = initTask(conduct);
			success = (conduct != null); // 判断成功
		} catch (TaskException e) {
			setFaultText(e);
			Logger.error(e);
		} catch (Throwable e) {
			setFaultText(e);
			Logger.fatal(e);
		}

		// 不成功，发送错误应答
		if (!success) {
			Logger.debug(this, "doInit", "cannot be create it");
			return false;
		}
		
		// 取得TO阶段根对象实例
		ToObject to = conduct.getToObject();
		if (to == null) {
			Logger.error(this, "doInit", "not create ToObject!");
			return false;
		}
		// 从0开始，给每个TO阶段对象分配迭代编号
		to.doIterateIndex();

		// 取出FROM阶段资源分配器，和它的连接主机数目
		FromObject from = conduct.getFromObject();
		if (from == null) {
			Logger.error(this, "doInit", "not create FromObject!");
			return false;
		}
		FromDispatcher dispatcher = from.getDispatcher();
		// 设置数据操作人
		dispatcher.setIssuer(getIssuer());
		
		// 如果有默认结果，返回默认值
		if (dispatcher.hasDefaultReturnValue()) {
			byte[] b = dispatcher.getDefaultReturnValue();
			boolean reply = replyDefault(b);
			setQuit(true);
			return reply;
		}

		// 根据INIT实例产生的FROM阶段会话，去执行CONDUCT.FROM阶段处理
		return doFromSession(dispatcher.list(), true);
	}

	/**
	 * 根据TO分派器的要求，发送命令到关联的DATA/WORK节点
	 * @param dispatcher TO阶段分派器
	 * @param next 存在下一次迭代
	 * @return 成功返回真，否则假
	 */
	private boolean doToDispatcher(ToDispatcher dispatcher, boolean next) {
		// 设置数据操作人
		dispatcher.setIssuer(getIssuer());

		// 如果有默认结果，返回默认值
		if (dispatcher.hasDefaultReturnValue()) {
			byte[] b = dispatcher.getDefaultReturnValue();
			boolean reply = replyDefault(b);
			setQuit(true);
			return reply;
		}
		
		// 判断是空集合
		if (dispatcher.isEmpty()) {
			Logger.error(this, "doToDispatcher", "cannot be create session!");
			return false;
		}

		// 判断会话属性。FROM会话连接DATA节点；TO会话连接WORK节点。
		if (dispatcher.isFullFromSession()) {
			// 执行FROM会话操作
			List<FromSession> sessions = dispatcher.getFromSessions();
			return doFromSession(sessions, next);
		} else if (dispatcher.isFullToSession()) {
			return doToSession(dispatcher, next);
		}

		// 以上不成立就是错误
		Logger.error(this, "doToDispatcher", "illegal session");

		return false;
	}

	/**
	 * 接收FROM阶段数据，被BALANCE阶段整合处理后，发送给TO阶段处理
	 * @return 成功返回真，否则假
	 */
	private boolean doFrom() {
		Logger.debug(this, "doFrom", "into...");
		// 如果存在故障
		if (isFaultCompleted()) {
			Logger.error(this, "doFrom", "From Task Failed!");
			return false;
		}

		Conduct conduct = getCommand(); 
		// 1. 分配BALANCE任务实例
		BalanceObject balance = conduct.getBalanceObject();
		if (balanceTask == null && balance != null) {
			balanceTask = BalanceTaskPool.getInstance().create(balance.getPhase());
		}
		// 2. 根据根命名，找到BALANCE任务实例
		if (balanceTask == null) {
			Phase phase = new Phase(getIssuer(), PhaseTag.BALANCE, conduct.getSock());
			balanceTask = BalanceTaskPool.getInstance().create(phase);
		}
		// 3. 以上两个条件不成立时，使用默认的平衡计算实例
		if (balanceTask == null) {
			balanceTask = new DefaultBalanceTask();
		}
		// 设置命令和调用器编号
		balanceTask.setCommand(conduct);
		balanceTask.setInvokerId(getInvokerId());

		// 根据迭代编号，找到对应的TO阶段对象
		ToObject object = conduct.getToObject().next(iterateToIndex);

		// 判断应答数据全部在磁盘文件中
		boolean ondisk = isEchoFiles();
		Logger.debug(this, "doFrom", "data on '%s'", (ondisk ? "DISK":"MEMORY"));

		try {
			if (ondisk) {
				File[] files = getAllFiles();
				object = balanceTask.admix(object, files);
			} else {
				byte[] b = collect();
				balanceTask.admix(object, b, 0, b.length);
			}
		} catch (TaskException e) {
			setFaultText(e);
			Logger.error(e);
			return false;
		} catch (Throwable e) {
			setFaultText(e);
			Logger.fatal(e);
			return false;
		}

		//		// 如果TO对象有子级(SUBTO)，后面是TO阶段，否则是PUT阶段
		//		boolean next = object.hasNext();
		//		// 设置下一个处理阶段
		//		setStep(next ? PhaseTag.TO : PhaseTag.PUT);
		//		if (next) {
		//			iterateToIndex++;
		//		}

		// 如果TO对象以子级对象（SUBTO），后面仍然是TO阶段，否则是PUT阶段。
		boolean next = object.hasNext();
		if (next) {
			// 跨过当前进入下一个
			iterateToIndex++;

			// 判断如果对象自己设置迭代，以它的为准，系统不再自增；否则系统自增1。
			int skip = object.getSkipObjects();
			if (skip > 0) {
				if (!object.hasNext(skip)) {
					setFaultText("skip to objects, failed!");
					Logger.error(this, "doFrom", "skip %d, failed! ", skip);
					return false;
				}
				// 支持跨过
				iterateToIndex += skip;
			}

			// 设置下个阶段
			setStep(PhaseTag.TO);
		} else {
			setStep(PhaseTag.PUT);
		}

		// 取出TO阶段任务分派器
		ToDispatcher dispatcher = object.getDispatcher();
		return doToDispatcher(dispatcher, next);
	}

	/**
	 * 处理TO子级迭代对象
	 * @return 成功返回真，否则假
	 */
	private boolean doSubTo() {
		Logger.debug(this, "doSubTo", "into...");
		// 如果存在故障
		if (isFaultCompleted()) {
			Logger.error(this, "doSubTo", "SUBTO failed!");
			return false;
		}

		Conduct conduct = getCommand(); 
		// 设置命令
		balanceTask.setCommand(conduct);

		// 根据迭代编号，找到对应的TO阶段对象
		ToObject object = conduct.getToObject().next(iterateToIndex);

		// 判断应答数据全部在文件中
		boolean ondisk = isEchoFiles();
		Logger.debug(this, "doSubTo", "ondisk is '%s'", ondisk);

		try {
			if (ondisk) {
				File[] files = getAllFiles();
				object = balanceTask.admix(object, files);
			} else {
				byte[] b = collect();
				balanceTask.admix(object, b, 0, b.length);
			}
		} catch (TaskException e) {
			setFaultText(e);
			Logger.error(e);
			return false;
		} catch (Throwable e) {
			setFaultText(e);
			Logger.fatal(e);
			return false;
		}
		
		//		// 如果继续有TO子级对象(SUBTO)，后面阶段仍是TO，迭代编号加1；否则是PUT
		//		if (object.hasNext()) {
		//			iterateToIndex++;
		//		} else {
		//			setStep(PhaseTag.PUT);
		//		}

		// 如果继续有TO子级对象(SUBTO)，后面阶段仍是TO，迭代编号加1；否则是PUT
		boolean next = object.hasNext();
		if (next) {
			// 自增1，跨过本阶段，进入下个阶段
			iterateToIndex++;

			// 判断有跨过阶段发生
			int skip = object.getSkipObjects();
			if (skip > 0) {
				if (!object.hasNext(skip)) {
					setFaultText("skip to objects, failed!");
					Logger.error(this, "doSubTo", "skip %d, failed! ", skip);
					return false;
				}
				iterateToIndex += skip;
			}
		} else {
			setStep(PhaseTag.PUT);
		}

		// 处理TO阶段任务分派器
		ToDispatcher dispatcher = object.getDispatcher();
		return doToDispatcher(dispatcher, next);
	}

	/**
	 * 执行CONDUCT的INIT阶段操作，包括：<br>
	 * <1> 分配FROM阶段的计算资源，为FROM阶段做准备。<br>
	 * <2> 检查TO阶段的资源有效性(如命名任务是否存在，主机数目等)，分配TO阶段计算需要的迭代链。<br><br>
	 * 
	 * 任何一项要求的条件不能满足，将弹出TaskException异常。<br>
	 * 如果处理正常，资源将分配到conduct句柄里面，返回它分配后的句柄。<br>
	 * 
	 * @param conduct
	 * @return
	 */
	private Conduct initTask(Conduct conduct) throws TaskException {
		// 取初始化对象，如果没有是错误!
		InitObject init = conduct.getInitObject();
		if (init == null) {
			throw new InitTaskException("cannot be find \"INIT\" object!");
		}

		// 根据命名取得任务实例(命名必须存在)
		Phase phase = init.getPhase();
		InitTask initTask = InitTaskPool.getInstance().create(phase);
		if (initTask == null) {
			throw new InitTaskException("cannot be find \"INIT\" task! %s", phase);
		}
		// 设置命令
		initTask.setCommand(conduct);
		initTask.setInvokerId(getInvokerId());

		Logger.debug(this, "initTask", "task class is '%s'", initTask.getClass().getName());

		// 执行初始化，返回完成分配后的CONDUCT对象
		return initTask.init(conduct);
	}

	/**
	 * 执行CONDUCT.FROM阶段会话，返回FROM操作的结果数据的元数据。允许TO阶段操作执行FROM会话。
	 * @param sessions FROM会话集合
	 * @param next 存在下一次的迭代
	 * @return 成功返回真，否则假
	 */
	private boolean doFromSession(List<FromSession> sessions, boolean next) {
		// 确定连接主机数目
		int size = sessions.size();
		// 判断是空集
		if (size == 0) {
			Logger.error(this, "doFromSession", "cannot be create from-session");
			return false;
		}

		// 取出全部目标站点地址
		ArrayList<Node> buddies = new ArrayList<Node>();
		for (int index = 0; index < size; index++) {
			FromSession session = sessions.get(index);
			buddies.add(session.getRemote());
		}

		Logger.debug(this, "doFromSession", "session size:%d", size);
		SHA256Hash master = getListener().sign();
		
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (int index = 0; index < size; index++) {
			// 取出会话
			FromSession session = sessions.get(index);
			// CALL回显地址
			session.setMaster(master);
			// 全部关联站点
			session.addBuddies(buddies);

			// 建立命令
			FromStep step = new FromStep(getIssuer(), session, isMemory());
			step.setLast(!next); // 没有子集，是最后一个！

			// 保存
			Node hub = session.getRemote();
			CommandItem item = new CommandItem(hub, step);
			array.add(item);
		}

		// 发送命令到DATA站点，根据命令要求选择磁盘或者内存做为存取介质
		boolean success = completeTo(array);

		Logger.debug(this, "doFromSession", success, "submit data site size:%d", array.size());
		return success;
	}

	/**
	 * 执行TO会话操作，返回在最后一次前，都是元数据，最后是实际处理结果
	 * @param dispatcher TO阶段任务分派器
	 * @param next 存在下一个
	 * @return 成功返回真，否则假
	 */
	private boolean doToSession(ToDispatcher dispatcher, boolean next) {
		// 确定会话数目
		final int size = dispatcher.size();

		Logger.debug(this, "doToSession", "session size %d", size);

		// 取出全部目标站点地址
		ArrayList<Node> buddies = new ArrayList<Node>();
		for (int index = 0; index < size; index++) {
			ToSession session = dispatcher.getToSession(index);
			buddies.add(session.getRemote());
		}

		// 当前回显地址的数字签名
		SHA256Hash master = getListener().sign();
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (int index = 0; index < size; index++) {
			// 取出目标站点地址
			ToSession session = dispatcher.getToSession(index);
			// 告诉会话，它的反馈地址
			session.setMaster(master);
			session.addBuddies(buddies);

			// 分配TO阶段命令
			ToStep step = new ToStep(getIssuer(), session, isMemory());
			step.setLast(!next); // 没有子集，是最后一个！

			// 保存它
			Node hub = session.getRemote();
			CommandItem item = new CommandItem(hub, step);
			array.add(item);
		}

		// 发送命令到服务器，中间数据选择采用磁盘/内存存取
		boolean success = completeTo(array); 

		Logger.debug(this, "doToSession", success, "submit work site size:%d", array.size());
		return success;
	}

	/**
	 * 向FRONT站点发送计算结果
	 * @return 发送成功返回“真”，否则“假”。
	 */
	private boolean doPut() {
		if (isFaultCompleted()) {
			Logger.error(this, "doPut", "TO failed!");
			return false;
		}

		boolean success = false;
		// 判断数据全部在磁盘文件中
		boolean ondisk = isEchoFiles();
		// 如果全部在磁盘文件中，提供这些文件路径来发送；否则以内存形式发送
		try {
			if (ondisk) {
				File[] files = getAllFiles();
				success = replyFile(files);
			} else {
				byte[] b = collect();
				success = replyPrimitive(b);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		Logger.debug(this, "doPut", success, "send to front site %s", getCommandSource());

		return success;
	}

	/**
	 * 释放在分布计算过程中，在CALL节点缓存的资源数据，同时还通知关联的DATA/WORK节点，释放它们的中间数据
	 */
	private void release() {
		// 从元数据缓存池中读出任务锚点
		ArrayList<FluxDock> docks = new ArrayList<FluxDock>();
		try {
			if (balanceTask != null) {
				List<FluxDock> e = balanceTask.getFluxDocks();
				if (e != null) {
					docks.addAll(e);
				}
			}
		} catch (TaskException e) {
			setFaultText(e);
			Logger.error(e);
		}

		// 生成命令，发送到目标站点
		if (docks.size() > 0) {
			ArrayList<CommandItem> array = new ArrayList<CommandItem>();
			for (FluxDock dock : docks) {
				ReleaseFluxArea cmd = new ReleaseFluxArea(dock.getTaskId());
				cmd.setQuick(true); // 要求快速处理
				CommandItem item = new CommandItem(dock.getNode(), cmd);
				array.add(item);
			}
			// 发送到目标站点
			int size = directTo(array, false);
			Logger.debug(this, "release", "command size:%d, send size:%d", array.size(), size);
		}

		// 判断调用器有效，删除可能存在的元数据缓存，这是最后一步
		long invokerId = getInvokerId();
		if (InvokerIdentity.isValid(invokerId)) {
			MetaPool.getInstance().remove(invokerId);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		Logger.debug(this, "destroy", "%d usedtime:%d", getInvokerId(), getRunTime());

		if (isAlive()) {
			// 释放BALANCE资源句柄
			balanceTask = null;
			faultText = null;
		}

		// 清除上级资源
		super.destroy();
	}

}