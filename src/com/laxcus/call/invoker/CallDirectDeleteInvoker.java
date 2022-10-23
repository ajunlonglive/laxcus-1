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
 * 简单DELETE命令调用器。
 * 包含“DELETE FROM DATABASE.TABLE WHERE KEY>=VALUE”语句块的删除命令
 * CALL站点将把命令发送到关联的DATA主站点
 * 
 * @author scott.liang
 * @version 1.1 6/19/2013
 * @since laxcus 1.0
 */
public class CallDirectDeleteInvoker extends CallInvoker {

	/** launch阶段备注 **/
	private DeleteLaunchRemark launchRemark = new DeleteLaunchRemark();

	/** end阶段备注 **/
	private DeleteEndingRemark endingRemark = new DeleteEndingRemark();

	/** ending方法执行阶段，从1开始 **/
	private int step = 1;

	/**
	 * 构造DELETE命令调用器，指定命令
	 * @param cmd 删除命令
	 */
	public CallDirectDeleteInvoker(Delete cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Delete getCommand() {
		return (Delete) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Delete cmd = getCommand();
		Siger issuer = cmd.getIssuer();
		Space space = cmd.getSpace();
		boolean success = allow(issuer, space);
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
			Logger.error(this, "launch", "cannot find %s", space);
			replyFault(Major.FAULTED, Minor.SITE_NOTFOUND);
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
			CastDelete cast = new CastDelete(cmd, stub.list());
			CommandItem item = new CommandItem(hub, cast);
			array.add(item);
		}
		// 统计发送成功数目
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
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
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
		Delete cmd = getCommand();
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
			AssumeDelete submit = new AssumeDelete(cmd.getSpace(), status);
			submit.setRows(endingRemark.getRows());
			// 发送命令到FRONT站点
			replyCommand(cmd.getSource(), submit);
			// 退出标识
			setQuit(true);
		}
		
		return true;
	}

	/**
	 * DELETE操作的ENDING第一阶段：判断全部返回结果，选择是确认或者取消，发送到全部DATA主站点。
	 * @return 命令发送成功返回真，否则假
	 */
	private boolean send() {
		List<Integer> keys = getEchoKeys();
		// 保存
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					AssumeDelete cmd = getObject(AssumeDelete.class, index);
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
		
//		// 测试代码...，正式发布后取消！！！
//		status = ConsultStatus.CANCEL; 
		
//		Logger.debug(this, "send", "组合判断状态：%s", ConsultStatus.translate(status));
		
		// 保存DATA调用器的回显地址
		TreeSet<Cabin> hubs = new TreeSet<Cabin>();
		for (AssumeDelete consult : endingRemark.list()) {
			hubs.add(consult.getSource());
		}

		// 生成反馈命令，发送给DATA站点
		ArrayList<ReplyItem> array = new ArrayList<ReplyItem>();
		for (AssumeDelete consult : endingRemark.list()) {
			Cabin hub = consult.getSource();
			AssertDelete cmd = new AssertDelete(consult.getSpace(), status);
			
			// 如果完全成功，在确认前，让节点之间做相互确认
			if (fullSuccess) {
				cmd.addSeekSites(hubs);		// 保存全部监听地址
				cmd.removeSeekSite(hub);	// 删除自己的监听地址
			}
			
			// 保存
			ReplyItem item = new ReplyItem(hub, cmd);
			array.add(item);
		}

		// 反馈到等待中的DATA站点
		boolean success = replyTo(array);

		Logger.debug(this, "send", success, "commands size %d", array.size());

		return success;
	}

	/**
	 * DELETE操作的ENDING第二阶段：接收全部DATA主站点处理结果。
	 * @return 全部成功返回真，否则假
	 */
	private boolean receive() {
		ArrayList<AssumeDelete> array = new ArrayList<AssumeDelete>();
		List<Integer> keys = getEchoKeys();
		// 保存
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					AssumeDelete cmd = getObject(AssumeDelete.class, index);
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
			for (AssumeDelete cmd : array) {
				if (cmd.isConfirmSuccess()) count++;
			}
			success = (count == array.size());
		}
	
		Logger.debug(this, "receive", success, "key size:%d, commands size:%d, assert result",
				keys.size(), array.size());

		return success;
	}

}