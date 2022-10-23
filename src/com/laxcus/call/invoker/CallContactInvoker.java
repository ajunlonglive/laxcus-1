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

import com.laxcus.command.contact.*;
import com.laxcus.distribute.calculate.command.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.contact.*;
import com.laxcus.distribute.contact.command.*;
import com.laxcus.distribute.contact.session.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.contact.merge.*;
import com.laxcus.task.contact.fork.*;
import com.laxcus.task.meta.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;

/**
 * 快速计算命令调用器。<br><br>
 * 
 * 在CALL站点上，快速计算命令调用器执行FORK/MERGE阶段任务调度工作，包括对资源检查和会话分派，以及平衡分布数据。<br>
 * DISTANT操作转到WORK站点。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/10/2020
 * @since laxcus 1.0
 */
public class CallContactInvoker extends CallDistributeCommandInvoker {

	/** CONTACT执行步骤 **/
	private int step;

	/** DISTANT阶段的迭代编号，从0开始 **/
	private int iterateIndex;

	/** CONTACT.MERGE阶段平衡任务实例 **/
	private MergeTask mergeTask;

	/**
	 * 构造快速计算命令调用器，指定命令
	 * @param contact 快速计算命令。
	 */
	public CallContactInvoker(Contact contact) {
		super(contact);
		// 从CONTACT.FORK阶段开始
		setStep(PhaseTag.FORK);
		// DISTANT阶段的迭代序号，从0开始
		iterateIndex = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Contact getCommand() {
		return (Contact) super.getCommand();
	}

	/**
	 * 设置当前阶段
	 * @param who 阶段号
	 */
	private void setStep(int who) {
		if (!PhaseTag.isContact(who)) {
			throw new IllegalPhaseException("illegal phase step: %d", who);
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
	 * 执行CONTACT各阶段处理
	 * @return 成功返回“真”，否则“假”。
	 */
	private boolean todo() {
		Logger.debug(this, "todo", "this is '%s'", PhaseTag.translate(step));

		boolean success = false;
		switch (step) {
		// 1. FORK分裂第一次DISTANT会话
		case PhaseTag.FORK:
			success = doFork();
			break;
		// 2. 执行第二次及此后一系的迭代的DISTANT会话
		case PhaseTag.DISTANT:
			success = doSubDistant();
			break;
		// 3. NEAR阶段，收集数据，返回给FRONT节点
		case PhaseTag.NEAR:
			success = doNear();
			setQuit(true);
			break;
		}

		Logger.debug(this, "todo", success, "next is '%s'", PhaseTag.translate(step));

		// 在退出或者不成功时，释放元数据和WORK分布节点数据
		if (isQuit() || !success) {
			release();
		}
		// 不成功，向终端发送错误信息
		if (!success) {
			if (faultText != null) {
				replyFault(Major.FAULTED, Minor.CONTACT_ERROR, new DefaultEchoHelp(faultText));
			} else {
				replyFault(Major.FAULTED, Minor.CONTACT_ERROR);
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
	 * 执行CONTACT的FORK阶段操作，包括：<br>
	 * <1> 分配FROM阶段的计算资源，为FROM阶段做准备。<br>
	 * <2> 检查DISTANT阶段的资源有效性(如命名任务是否存在，主机数目等)，分配DISTANT阶段计算需要的迭代链。<br><br>
	 * 
	 * 任何一项要求的条件不能满足，将弹出TaskException异常。<br>
	 * 如果处理正常，资源将分配到contact句柄里面，返回它分配后的句柄。<br>
	 * 
	 * @param contact
	 * @return
	 */
	private Contact forkTask(Contact contact) throws TaskException {
		// 取初始化对象，如果没有是错误!
		ForkObject fork = contact.getForkObject();
		if (fork == null) {
			throw new ForkTaskException("cannot be find \"FORK\" object!");
		}

		// 根据命名取得任务实例(命名必须存在)
		Phase phase = fork.getPhase();
		ForkTask forkTask = ForkTaskPool.getInstance().create(phase);
		if (forkTask == null) {
			throw new ForkTaskException("cannot be find \"FORK\" task! %s", phase);
		}
		// 设置命令
		forkTask.setCommand(contact);
		forkTask.setInvokerId(getInvokerId());

		Logger.debug(this, "forkTask", "task class is '%s'", forkTask.getClass().getName());

		// 执行初始化的数据分割，返回完成分配后的CONTACT对象
		return forkTask.fork(contact);
	}

	/**
	 * 执行CONTACT.FORK阶段的参数检查和DISTANT资源分配
	 * @return 成功返回true，否则false。
	 */
	private boolean doFork() {
		Contact contact = getCommand();
		// 1. 启动CONTACT.FORK阶段
		boolean success = false;
		try {
			contact = forkTask(contact);
			success = (contact != null); // 判断成功
		} catch (TaskException e) {
			setFaultText(e);
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 不成功，发送错误应答
		if (!success) {
			Logger.debug(this, "doFork", "cannot be create it");
			return false;
		}

		// 根对象！
		DistantObject object = contact.getDistantObject();
		// 从0开始，给每个DISTANT阶段对象分配迭代编号
		object.doIterateIndex();

		// 如果DISTANT对象有子级(SUBDISTANT)，后面是DISTANT阶段，否则是NEAR阶段
		boolean next = object.hasNext();
		if (next) {
			iterateIndex++;

			// 判断有跨过阶段发生
			int skip = object.getSkipObjects();
			if (skip > 0) {
				if (!object.hasNext(skip)) {
					setFaultText("skip distant objects, failed!");
					Logger.error(this, "doFork", "skip %d, failed! ", skip);
					return false;
				}
				// 支持跨过
				iterateIndex += skip;
			}

			// 设置步骤
			setStep(PhaseTag.DISTANT);
		} else {
			setStep(PhaseTag.NEAR);
		}

		// 根据FORK实例产生的DISTANT阶段分派器，执行第一次处理
		DistantDispatcher dispatcher = object.getDispatcher();
		return doDistantSessions(dispatcher, next);
	}

	/**
	 * 执行DISTANT子级会话处理
	 * @return
	 */
	private boolean doSubDistant() {
		Logger.debug(this, "doSubDistant", "into...");
		// 如果存在故障
		if (isFaultCompleted()) {
			Logger.error(this, "doSubDistant", "Distant Task Failed!");
			return false;
		}

		// 1. 分配MERGE任务实例
		Contact contact = getCommand(); 
		MergeObject merge = contact.getMergeObject();
		if (mergeTask == null && merge != null) {
			mergeTask = MergeTaskPool.getInstance().create(merge.getPhase());
		}
		// 2. 根据根命名，找到MERGE任务实例
		if (mergeTask == null) {
			Phase phase = new Phase(getIssuer(), PhaseTag.MERGE, contact.getSock());
			mergeTask = MergeTaskPool.getInstance().create(phase);
		}
		// 3. 以上两个条件不成立时，使用默认的平衡计算实例
		if (mergeTask == null) {
			mergeTask = new DefaultMergeTask();
		}
		// 设置命令和调用器编号
		mergeTask.setCommand(contact);
		mergeTask.setInvokerId(getInvokerId());

		// 根据迭代编号，找到对应的DISTANT阶段的子级对象
		DistantObject object = contact.getDistantObject().next(iterateIndex);

		// 判断应答数据全部在磁盘文件中
		boolean ondisk = isEchoFiles();
		Logger.debug(this, "doSubDistant", "data on '%s'", (ondisk ? "DISK":"MEMORY"));

		// 分配下一阶段操作
		try {
			if (ondisk) {
				File[] files = getAllFiles();
				object = mergeTask.next(object, files);
			} else {
				byte[] b = collect();
				mergeTask.next(object, b, 0, b.length);
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

		// 如果DISTANT对象有子级(SUBDISTANT)，后面是DISTANT阶段，否则是NEAR阶段
		boolean next = object.hasNext();
		if (next) {
			// 自增1，跨过本阶段，进入下个阶段
			iterateIndex++;

			// 判断有跨过阶段发生
			int skip = object.getSkipObjects();
			if (skip > 0) {
				if (!object.hasNext(skip)) {
					setFaultText("skip distant objects, failed!");
					Logger.error(this, "doSubDistant", "skip %d, failed! ", skip);
					return false;
				}
				iterateIndex += skip;
			}
		} else {
			setStep(PhaseTag.NEAR);
		}

		// 取出DISTANT阶段任务分派器
		DistantDispatcher dispatcher = object.getDispatcher();
		return doDistantSessions(dispatcher, next);
	}

	/**
	 * 执行DISTANT会话，在最后一次前，返回的都是元数据，最后是实体数据
	 * @param dispatcher DISTANT阶段任务分派器
	 * @param next 判断有下一个
	 * @return 成功返回真，否则假
	 */
	private boolean doDistantSessions(DistantDispatcher dispatcher, boolean next) {
		// 设置数据操作人
		dispatcher.setIssuer(getIssuer());

		// 如果有默认结果，返回默认值
		if (dispatcher.hasDefaultReturnValue()) {
			byte[] b = dispatcher.getDefaultReturnValue();
			boolean reply = replyDefault(b);
			setQuit(true);
			return reply;
		}

		// 确定会话数目
		final int size = dispatcher.size();
		// 判断是空集
		if (size == 0) {
			Logger.error(this, "doDistantSessions", "cannot be create distant-session");
			return false;
		}

		Logger.debug(this, "doDistantSessions", "session size %d", size);

		// 取出全部目标站点地址
		ArrayList<Node> buddies = new ArrayList<Node>();
		for (int index = 0; index < size; index++) {
			DistantSession session = dispatcher.getDistantSession(index);
			buddies.add(session.getRemote());
		}

		// 当前回显地址的数字签名
		SHA256Hash master = getListener().sign();
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (int index = 0; index < size; index++) {
			// 取出目标站点地址
			DistantSession session = dispatcher.getDistantSession(index);
			// 告诉会话，它的反馈地址
			session.setMaster(master);
			session.addBuddies(buddies);

			// 分配DISTANT分布阶段命令
			DistantStep step = new DistantStep(getIssuer(), session, isMemory());
			step.setLast(!next); // 没有子集，是最后一个！

			// 保存它
			Node hub = session.getRemote();
			CommandItem item = new CommandItem(hub, step);
			array.add(item);
		}

		// 发送命令到服务器，中间数据选择采用磁盘/内存存取
		boolean success = completeTo(array); 

		Logger.debug(this, "doDistantSessions", success, "submit work site size:%d", array.size());
		return success;
	}

	/**
	 * 向FRONT站点发送计算结果
	 * @return 发送成功返回“真”，否则“假”。
	 */
	private boolean doNear() {
		if (isFaultCompleted()) {
			Logger.error(this, "doNear", "DISTANT failed!");
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

		Logger.debug(this, "doNear", success, "send to front site %s", getCommandSource());

		return success;
	}

	/**
	 * 释放在快速计算过程中，在CALL节点缓存的资源数据，同时还通知关联的WORK节点，释放它们的中间数据
	 */
	private void release() {
		// 从元数据缓存池中读出任务锚点
		ArrayList<FluxDock> docks = new ArrayList<FluxDock>();
		try {
			if (mergeTask != null) {
				List<FluxDock> e = mergeTask.getFluxDocks();
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
			Logger.debug(this, "release", "command size:%d, send count:%d", array.size(), size);
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

		// 如果在活跃状态，释放MERGE资源句柄
		if (isAlive()) {
			mergeTask = null;
			faultText = null;
		}

		// 清除上级资源
		super.destroy();
	}

}