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

import com.laxcus.command.establish.*;
import com.laxcus.distribute.establish.*;
import com.laxcus.distribute.establish.command.*;
import com.laxcus.distribute.establish.session.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.assign.*;
import com.laxcus.task.establish.issue.*;
import com.laxcus.task.meta.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;

/**
 * ESTABLISH命令调用器。<br><br>
 * 
 * 在CALL站点上，ESTABLISH执行调度工作。
 * 
 * @author scott.liang
 * @version 1.2 07/23/2015
 * @since laxcus 1.0
 */
public class CallEstablishInvoker extends CallDistributeCommandInvoker {

	/** ESTABLISH阶段执行步骤 **/
	private int step;

	/** SIFT迭代编号 **/
	private int siftIterate;

	/** ASSIGN对象 **/
	private AssignTask assignTask;

	/**
	 * 构造ESTABLISH命令调用器，指定命令
	 * @param cmd ESTABLISH命令
	 */
	public CallEstablishInvoker(Establish  cmd) {
		super(cmd);
		// 初始化为ISSUE阶段
		setStep(PhaseTag.ISSUE);
		// SIFT迭代从0开始
		siftIterate = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Establish getCommand() {
		return (Establish) super.getCommand();
	}

	/**
	 * 设置当前操作步骤
	 * @param who 当前步骤
	 */
	private void setStep(int who) {
		if (!PhaseTag.isEstablish(who)) {
			throw new IllegalPhaseException("illegal family %d", who);
		}
		step = who;
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
	 * 执行ESTABLISH的各阶段处理
	 * @return 成功返回“真”，否则“假”。
	 */
	private boolean todo() {
		Logger.debug(this, "todo", "this is '%s'", PhaseTag.translate(step));

		boolean success = false;
		switch(step) {
		case PhaseTag.ISSUE:
			success = doIssue();
			if(success) {
				setStep(PhaseTag.SCAN);
			}
			break;
		case PhaseTag.SCAN:
			success = doScan();
			break;
		case PhaseTag.SIFT:
			success = doSubSift();
			break;
		case PhaseTag.RISE:
			success = doRise();
			if(success) {
				setStep(PhaseTag.END);
			}
			break;
		case PhaseTag.END:
			success = doEnd();
			setQuit(true);
			break;
		}

		// 在退出或者不成功时，释放元数据和DATA/BUILD数据
		if (isQuit() || !success) {
			release();
		}
		// 不成功，通知FRONT站点
		if(!success) {
			if (faultText != null) {
				replyFault(Major.FAULTED, Minor.ESTABLISH_ERROR, new DefaultEchoHelp(faultText));
			} else {
				replyFault(Major.FAULTED, Minor.ESTABLISH_ERROR);
			}
		}

		// 返回结果
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
	 * ESTABLISH.ISSUE阶段，检查和分配分布资源
	 * @param estab ESTABLISH命令
	 * @return 返回分配后的ESTABLISH命令实例
	 * @throws TaskException
	 */
	private Establish issue(Establish estab) throws TaskException {
		IssueObject issue = estab.getIssueObject();
		// 找到阶段命名
		Phase phase = (issue != null ? issue.getPhase() : null);
		// 生成一个默认的阶段命名
		if (phase == null) {
			phase = new Phase(getIssuer(), PhaseTag.ISSUE, estab.getSock());
		}
		// 查找ISSUE组件
		IssueTask issueTask = IssueTaskPool.getInstance().create(phase);
		if (issueTask == null) {
			throw new IssueTaskException("cannot be find \"ISSUE\" task! %s", phase);
		}
		// 设置命令
		issueTask.setCommand(estab);
		issueTask.setInvokerId(getInvokerId());

		// 由任务实例分配后续的工作
		return issueTask.create(estab);
	}

	/**
	 * 启动数据构建，分派到DATA.SCAN处理，进入CALL.ASSIGN阶段
	 * @return 成功返回真，否则假
	 */
	private boolean doIssue() {
		Logger.debug(this, "doIssue", "into...");

		boolean success = false;
		Establish estab = getCommand();
		// 1. 启动ISSUE检测
		try {
			estab = issue(estab);
			success = (estab != null);
		} catch (TaskException e) {
			setFaultText(e);
			Logger.error(e);
			return false;
		} catch (Throwable e) {
			Logger.fatal(e);
			return false;
		}

		// 不成功，退出
		if (!success) {
			Logger.debug(this, "doIssue", "cannot be create it");
			return false;
		}

		// 发送SCAN阶段会话到DATA主站点
		ScanDispatcher dispatcher = estab.getScanObject().getDispatcher();
		dispatcher.setIssuer(getIssuer());
		
		// 如果有默认结果，返回默认值
		if (dispatcher.hasDefaultReturnValue()) {
			byte[] b = dispatcher.getDefaultReturnValue();
			boolean reply = replyDefault(b);
			setQuit(true);
			return reply;
		}
		
		ArrayList<Node> buddies = new ArrayList<Node>();
		for (ScanSession session : dispatcher.list()) {
			buddies.add(session.getRemote());
		}

		// 建立异步传输单元
		SHA256Hash master = getListener().sign();
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (ScanSession session : dispatcher.list()) {
			// CALL回显地址
			session.setMaster(master);
			// 全部关联站点
			session.addBuddies(buddies);
			
			ScanStep step = new ScanStep(getIssuer(), session);
			CommandItem item = new CommandItem(session.getRemote(), step);
			array.add(item);
		}

		// 发送到DATA主站点，返回的SCAN元数据保存到内存/硬盘里
		success = completeTo(array);

		Logger.debug(this, "doIssue", success, "command size:%d, on disk is %s", array.size(), isDisk());

		return success;
	}

	/**
	 * ASSIGN阶段任务解析SCAN阶段返回的元数据，并分配元数据给SIFT阶段任务。
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean doScan() {
		Logger.debug(this, "doScan", "into...");
		// 如果存在故障
		if(isFaultCompleted()) {
			Logger.error(this, "doScan", "SCAN failed!");
			return false;
		}

		Establish cmd = getCommand();
		// 2. 获得ASSIGN对象
		AssignObject assign = cmd.getAssignObject();
		// 3. 分配ASSIGN任务组件
		if (assignTask == null && assign != null) {
			assignTask = AssignTaskPool.getInstance().create(assign.getPhase());
		}
		// 3. 根据根命名，找到对应的ASSIGN组件（ASSIGN对象可以忽略）
		if (assignTask == null) {
			Phase phase = new Phase(getIssuer(), PhaseTag.ASSIGN, cmd.getSock());
			assignTask = AssignTaskPool.getInstance().create(phase);
		}
		// 4. 以上两个条件不成立，是错误
		if (assignTask == null) {
			Logger.error(this, "doScan", "cannot be assign task");
			return false;
		}
		// 设置命令
		assignTask.setCommand(cmd);
		assignTask.setInvokerId(getInvokerId());

		// 根据迭代编号，找到对应的TO阶段对象
		SiftObject sift = cmd.getSiftObject().next(siftIterate);
		if (sift == null) {
			Logger.error(this, "doScan", "cannot be get sift object");
			return false;
		}

		// 判断应答数据全部在文件中
		boolean ondisk = isEchoFiles();
		Logger.debug(this, "doScan", "ondisk is '%s'", ondisk);

		try {
			if (ondisk) {
				File[] files = getAllFiles();
				sift = assignTask.scan(sift, files);
			} else {
				byte[] b = collect();
				sift = assignTask.scan(sift, b, 0, b.length);
			}
		} catch (TaskException e) {
			setFaultText(e);
			Logger.error(e);
			return false;
		} catch (Throwable e) {
			Logger.fatal(e);
			return false;
		}

		//		// 如果有SIFT子对象，继续SIFT处理，否则转入RISE阶段
		//		boolean next = sift.hasNext();
		//		if (next) {
		//			setStep(PhaseTag.SIFT);
		//			siftIterate++;
		//		} else {
		//			setStep(PhaseTag.RISE);
		//		}

		// 如果SIFT对象有子级对象（SUBSIFT），后面仍然是SUBSIFT阶段，否则是RISE阶段。
		if (sift.hasNext()) {
			// 跨过当前进入下一个
			siftIterate++;

			// 判断如果对象自己设置迭代，以它的为准，系统不再自增；否则系统自增1。
			int skip = sift.getSkipObjects();
			if (skip > 0) {
				if (!sift.hasNext(skip)) {
					setFaultText("skip sift objects, failed!");
					Logger.error(this, "doScan", "skip %d, failed! ", skip);
					return false;
				}
				// 支持跨过
				siftIterate += skip;
			}

			// 设置下个阶段
			setStep(PhaseTag.SIFT);
		} else {
			setStep(PhaseTag.RISE);
		}

		//		Logger.debug(this, "doScan", "next is %s", next);

		// 取出SIFT阶段任务分派器
		return doSiftSessions(sift.getDispatcher());
	}

	/**
	 * 解析SUBSIFT阶段返回的数据
	 * @return 成功返回真，否则假
	 */
	private boolean doSubSift() {
		Logger.debug(this, "doSubSift", "into...");
		// 如果存在故障
		if(isFaultCompleted()) {
			Logger.error(this, "doSubSift", "SUBSIFT failed!");
			return false;
		}

		Establish cmd = getCommand();
		assignTask.setCommand(cmd);
		// 根据迭代编号，找到对应的TO阶段对象
		SiftObject sift = cmd.getSiftObject().next(siftIterate);
		if (sift == null) {
			Logger.error(this, "doSubSift", "cannot be get sift object");
			return false;
		}

		// 判断应答数据全部在文件中
		boolean ondisk = isEchoFiles();
		Logger.debug(this, "doSubSift", "ondisk is '%s'", ondisk);

		try {
			if (ondisk) {
				File[] files = getAllFiles();
				sift = assignTask.sift(sift, files);
			} else {
				byte[] b = collect();
				sift = assignTask.sift(sift, b, 0, b.length);
			}
		} catch (TaskException e) {
			setFaultText(e);
			Logger.error(e);
			return false;
		} catch (Throwable e) {
			Logger.fatal(e);
			return false;
		}

		//		// 继续SIFT迭代，或者转入RISE阶段
		//		boolean next = sift.hasNext();
		//		if (next) {
		//			siftIterate++;
		//		} else {
		//			setStep(PhaseTag.RISE);
		//		}

		// 如果继续有TO子级对象(SUBTO)，后面阶段仍是TO，迭代编号加1；否则是PUT
		if (sift.hasNext()) {
			// 自增1，跨过本阶段，进入下个阶段
			siftIterate++;

			// 判断有跨过阶段发生
			int skip = sift.getSkipObjects();
			if (skip > 0) {
				if (!sift.hasNext(skip)) {
					setFaultText("skip sift objects, failed!");
					Logger.error(this, "doSubSift", "skip %d, failed! ", skip);
					return false;
				}
				siftIterate += skip;
			}
		} else {
			setStep(PhaseTag.RISE);
		}

		// 取出SIFT阶段任务分派器
		return doSiftSessions(sift.getDispatcher());
	}

	/**
	 * 处理发送给SIFT阶段的会话
	 * @param dispatcher SIFT阶段会话分派器
	 * @return 成功返回真，否则假
	 */
	private boolean doSiftSessions(SiftDispatcher dispatcher) {
		// 设置数据操作人
		dispatcher.setIssuer(getIssuer());
		
		// 如果有默认结果，返回默认值
		if (dispatcher.hasDefaultReturnValue()) {
			byte[] b = dispatcher.getDefaultReturnValue();
			boolean reply = replyDefault(b);
			setQuit(true);
			return reply;
		}
		
		List<SiftSession> sessions = dispatcher.list();
		// 如果是空集合，通知FRONT站点
		if (sessions.isEmpty()) {
			Logger.warning(this, "doSiftSessions", "sift session array is empty");
			super.replyFault(Major.FAULTED , Minor.ESTABLISH_ERROR, new DefaultEchoHelp("empty sift session"));
			return useful(true);
		}
		
		// 全部关联节点
		ArrayList<Node> buddies = new ArrayList<Node>();
		for (SiftSession session : sessions) {
			buddies.add(session.getRemote());
		}

		// 取出会话，建立命令保存
		SHA256Hash master = getListener().sign();
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (SiftSession session : sessions) {
			// CALL回显地址
			session.setMaster(master);
			// 全部关联站点
			session.addBuddies(buddies);
			
			SiftStep step = new SiftStep(getIssuer(), session);
			CommandItem item = new CommandItem(session.getRemote(), step);
			array.add(item);
		}

		// 必须全部发送到目标地址
		boolean success = completeTo(array);

		Logger.debug(this, "doSiftSessions", success, "session size is %d", array.size());		
		return success;
	}

	/**
	 * ASSIGN任务分析SIFT/SUBSIFT返回的元数据，生成和分配新的元数据给RISE阶段。
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean doRise() {
		// 如果存在故障
		if(isFaultCompleted()) {
			Logger.error(this, "doRise", "SIFT/SUBSIFT failed!");
			return false;
		}
		
		Establish establish = getCommand(); 
		RiseObject object = establish.getRiseObject();
		assignTask.setCommand(establish);

		boolean ondisk = isEchoFiles();
		Logger.debug(this, "doRise", "ondisk is '%s'", ondisk);

		try {
			if (ondisk) {
				File[] files = getAllFiles();
				object = assignTask.rise(object, files);
			} else {
				byte[] b = collect();
				object = assignTask.rise(object, b, 0, b.length);
			}
		} catch (TaskException e) {
			setFaultText(e);
			Logger.error(e);
			return false;
		} catch (Throwable e) {
			Logger.fatal(e);
			return false;
		}
		
		// 发送RISE会话到DATA主节点
		return doRiseSessions(object.getDispatcher());
	}

	/**
	 * 根据RISE会话发送命令到指定的DATA主节点
	 * @param dispatcher
	 * @return 成功返回真，否则假
	 */
	private boolean doRiseSessions(RiseDispatcher dispatcher) {
		// 设置数据操作人
		dispatcher.setIssuer(getIssuer());
		
		// 如果有默认结果，返回默认值
		if (dispatcher.hasDefaultReturnValue()) {
			byte[] b = dispatcher.getDefaultReturnValue();
			boolean reply = replyDefault(b);
			setQuit(true);
			return reply;
		}

		// 输出会话
		List<RiseSession> sessions = dispatcher.list();
		// 如果是空集合，发送错误信息
		if (sessions.isEmpty()) {
			Logger.warning(this, "doRiseSessions", "rise session array is empty");
			super.replyFault(Major.FAULTED, Minor.ESTABLISH_ERROR,
					new DefaultEchoHelp("empty rise session"));
			return useful(true);
		}

		// 全部关联节点
		ArrayList<Node> buddies = new ArrayList<Node>();
		for (RiseSession session : sessions) {
			buddies.add(session.getRemote());
		}
		
		// 取出会话，建立命令保存
		SHA256Hash master = getListener().sign();
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (RiseSession session : sessions) {
			// CALL回显地址
			session.setMaster(master);
			// 全部关联站点
			session.addBuddies(buddies);
			
			RiseStep step = new RiseStep(getIssuer(), session);
			CommandItem item = new CommandItem(session.getRemote(), step);
			array.add(item);
		}

		// 必须全部发送到目标地址
		boolean success = completeTo(array);

		Logger.debug(this, "doRiseSessions", success, "session size is %d", array.size());		
		return success;
	}
	
	/**
	 * 接收DATA.RISE反馈，转发给FRONT站点
	 * @return
	 */
	private boolean doEnd() {
		// RISE阶段发生故障
		if(isFaultCompleted()) {
			Logger.error(this, "doEnd", "RISE failed!");
			return false;
		}
		
		boolean ondisk = isEchoFiles();
		Logger.debug(this, "doEnd", "ondisk is '%s'", ondisk);

		boolean success = false;
		try {
			if (ondisk) {
				File[] files = getAllFiles();
//				success = doLargeTransfer(files);
				success = replyFile(files);
			} else {
				byte[] b = collect();
//				success = doLargeTransfer(b);
				success = replyPrimitive(b);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		Logger.debug(this, "doEnd", success, "send");

		// 退出
		return useful(success);
	}

	/**
	 * 释放方法执行两项工作：
	 * 1. 释放全部BUILD.SIFT阶段产生的数据表
	 * 2. 释放SCAN/SIFT/SUBSIFT/RISE产生的缓存元数据
	 */
	private void release() {
		// 从元数据缓存池中读出SIFT锚点
		ArrayList<SiftDock> docks = new ArrayList<SiftDock>();
		try {
			if (assignTask != null) {
				List<SiftDock> e = assignTask.getSiftDocks();
				if (e != null) docks.addAll(e);
			}
		} catch (TaskException e) {
			setFaultText(e);
			Logger.error(e);
		}

		// 生成命令，发送到目标站点
		if (docks.size() > 0) {
			ArrayList<CommandItem> array = new ArrayList<CommandItem>();
			for (SiftDock dock : docks) {
				ReleaseSiftSource cmd = new ReleaseSiftSource(dock.getSpace());
				cmd.setQuick(true); // 要求快速处理
				CommandItem item = new CommandItem(dock.getNode(), cmd);
				array.add(item);
			}
			// 发送到目标站点，不等待反馈
			int size = directTo(array, false);
			Logger.debug(this, "release", "%d send size %d", array.size(), size);
		}

		// 判断调用器有效，删除可能存在的元数据缓存，这是最后一步
		long invokerId = super.getInvokerId();
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
		// 释放本地资源
		if (isAlive()) {
			// 释放ASSIGN阶段数据
			assignTask = null;
			faultText = null;
		}

		// 调用上级方法
		super.destroy();
	}

}