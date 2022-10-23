/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 简单UPDATE命令调用器。<br>
 * 包含“UPDATE SET FROM DATABASE.TABLE WHERE KEY>=VALUE”这样语句块的命令。<br>
 * 
 * @author scott.liang
 * @version 1.1 6/19/2013
 * @since laxcus 1.0
 */
public class CallDirectUpdateInvoker extends CallInvoker {

	/** launch阶段备注 **/
	private DeleteLaunchRemark launchRemark = new DeleteLaunchRemark();

	/** ending阶段备注 **/
	private UpdateEndingRemark endingRemark = new UpdateEndingRemark();

	/** ending方法执行阶段，从1开始 **/
	private int step = 1;

	/**
	 * 构造UPDATE命令调用器，指定命令
	 * @param cmd - UPDATE命令
	 */
	public CallDirectUpdateInvoker(Update cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Update getCommand() {
		return (Update) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Update cmd = getCommand();
		Siger siger = cmd.getIssuer();
		Space space = cmd.getSpace();
		boolean success = allow(siger, space);
		// 不允许
		if(!success) {
			replyFault(Major.FAULTED, Minor.REFUSE);
			return false;
		}

		// 产生数据块分布区列表
		List<StubSector> list = null;
		try {
			list = DataOnCallPool.getInstance().doPrimeStubSector(space);
		} catch (TaskException e) {
			Logger.error(e);
		}
		if (list == null || list.isEmpty()) {
			Logger.error(this, "launch", "cannot be find %s", space);
			super.replyFault(Major.FAULTED, Minor.SITE_NOTFOUND);
			return false;
		}

		// 保存命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		// 逐一分配
		int sites = list.size();
		for (int index = 0; index < sites; index++) {
			StubSector stub = list.get(index);
			// 目标地址
			Node hub = stub.getRemote();
			// 投递命令
			CastUpdate cast = new CastUpdate(cmd, stub.list());
			CommandItem item = new CommandItem(hub, cast);
			array.add(item);
		}
		// 以容错模式发送命令到DATA主站点，返回发送成功的数目
		int count = incompleteTo(array);

		// 记录参数
		launchRemark.setSites(sites);
		launchRemark.setExpresses(count);
		// 判断是部分成功
		success = launchRemark.isPossibleSuccessful();

		Logger.debug(this, "launch", success, "sites:%d, expresses:%d", launchRemark.getSites(), launchRemark.getExpresses());

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		boolean success = false;
		switch(step) {
		case 1:
			success = send();
			break;
		case 2:
			success = receive();
			break;
		}

		// 删除命令
		Update cmd = getCommand();
		// 不成功，退出
		if (!success) {
			Cabin hub = cmd.getSource();
			replyFault(hub);
			return useful(false);
		}
		// 下一步
		step++;
		// 判断执行结束
		boolean finished = (step > 2);
		if (finished) {
			// 判断完成成功
			boolean fullSuccess = (launchRemark.isAbsoluteSuccessful() && endingRemark.getSuccessCount() == endingRemark.size());
			// 发送反馈命令给FRONT站点
			byte status = (fullSuccess ? ConsultStatus.SUCCESS : ConsultStatus.FAILED);
			AssumeUpdate submit = new AssumeUpdate(cmd.getSpace(), status);
			submit.setRows(endingRemark.getRows());
			// 发送命令到FRONT站点
			replyCommand(cmd.getSource(), submit);
			// 退出标识
			setQuit(true);
		}
		return true;
	}

	/**
	 * UPDATE命令ENDING阶段第一次处理。判断UPDATE操作成功或者失败，选择发送确认或者取消。<br>
	 * 
	 * @return - 命令发送成功返回真，否则假
	 */
	private boolean send() {
		List<Integer> keys = getEchoKeys();
		// 保存
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					AssumeUpdate cmd = getObject(AssumeUpdate.class, index);
					endingRemark.add(cmd);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 判断完全成功（两次都是成功，即完全成功）
		boolean fullSuccess = (launchRemark.isAbsoluteSuccessful() && endingRemark.getSuccessCount() == endingRemark.size());
		// 反馈状态码
		byte status = (fullSuccess ? ConsultStatus.CONFIRM : ConsultStatus.CANCEL);
		
		// 测试代码...
		status = ConsultStatus.CANCEL;

		// 保存DATA调用器的回显地址
		TreeSet<Cabin> hubs = new TreeSet<Cabin>();
		for (AssumeUpdate consult : endingRemark.list()) {
			hubs.add(consult.getSource());
		}

		// 生成反馈命令，发送给DATA站点
		ArrayList<ReplyItem> array = new ArrayList<ReplyItem>();
		for (AssumeUpdate consult : endingRemark.list()) {
			Cabin hub = consult.getSource();
			AssertUpdate cmd = new AssertUpdate(consult.getSpace(), status);

			// 保存全部监听地址
			if (fullSuccess) {
				cmd.addSeekSites(hubs);
				cmd.removeSeekSite(hub);
			}

			// 保存
			ReplyItem item = new ReplyItem(hub, cmd);
			array.add(item);
			
			Logger.debug(this, "send", "command is \"%s\"", cmd);
		}

		// 反馈到等待中的DATA站点
		boolean success = replyTo(array);

		Logger.debug(this, "send", success, "commands size %d", array.size());

		return success;
	}

	/**
	 * UPDATE命令的ENDING第二阶段。接收DATA站点返回的最终处理结果。<br>
	 * @return -  全部成功返回真，否则假
	 */
	private boolean receive() {
		ArrayList<AssumeUpdate> array = new ArrayList<AssumeUpdate>();
		List<Integer> keys = getEchoKeys();
		// 保存
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					AssumeUpdate cmd = getObject(AssumeUpdate.class, index);
					array.add(cmd);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 判断成功
		boolean success = (keys.size() == array.size());
		if (success) {
			int count = 0;
			for (AssumeUpdate cmd : array) {
				if (cmd.isConfirmSuccess()) count++;
			}
			success = (count == array.size());
		}

		Logger.debug(this, "receive", success, "key size:%d, commands size:%d",
				keys.size(), array.size());

		return success;
	}

}
