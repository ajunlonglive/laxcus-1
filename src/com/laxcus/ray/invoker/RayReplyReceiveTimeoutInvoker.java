/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.reload.*;
import com.laxcus.fixp.reply.*;

import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 设置接收异步数据超时调用器。
 * 命令先投递到管理节点，再经过管理节点转发到下属节点
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class RayReplyReceiveTimeoutInvoker extends RayInvoker {

	/**
	 * 构造释放节点内存命令调用器，指定命令
	 * @param cmd 重装加载安全规则命令
	 */
	public RayReplyReceiveTimeoutInvoker(ReplyReceiveTimeout cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReplyReceiveTimeout getCommand() {
		return (ReplyReceiveTimeout) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ReplyReceiveTimeout cmd = getCommand();
		
		// 判断和清除本地内存
		if (cmd.isLocal()) {
			reset();
			return useful();
		}
		// 投递到HUB站点
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		ReplyReceiveTimeoutProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(ReplyReceiveTimeoutProduct.class, index);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = (product != null);
		// 打印结果
		if (success) {
			print(product);
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}
		return useful(success);
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(ReplyReceiveTimeoutProduct product) {
		// 显示运行时间
		printRuntime();
		// 设置标题
		createShowTitle(new String[] { "REPLY-RECEIVE-TIMEOUT/STATUS",
				"REPLY-RECEIVE-TIMEOUT/SITE" ,"REPLY-RECEIVE-TIMEOUT/DISABLE-TIMEOUT","REPLY-RECEIVE-TIMEOUT/SUBPACKET-TIMEOUT"});

		// 时间
		ShowItem title = new ShowItem();
		title.add(new ShowStringCell(0, ""));
		String text = formatDistributeTime(product.getProcessTime());
		title.add(new ShowStringCell(1, text));
		title.add(new ShowStringCell(2, ""));
		title.add(new ShowStringCell(3, ""));
		// 保存单元
		addShowItem(title);

		// 显示单元
		for (ReplyReceiveTimeoutItem e : product.list()) {
			boolean success = e.isSuccessful();
			ShowItem item = new ShowItem();
			// 图标
			item.add(createConfirmTableCell(0, success));
			// 站点地址
			item.add(new ShowStringCell(1, e.getSite()));

			// 成功失败，不同选择!
			if (success) {
				item.add(new ShowLongCell(2, e.getDisableTimeout() / 1000));
				item.add(new ShowLongCell(3, e.getSubPacketTimeout() / 1000));
			} else {
				item.add(new ShowStringCell(2, "--"));
				item.add(new ShowStringCell(3, "--"));
			}
			// 保存单元
			addShowItem(item);
		}
		
		// 输出全部记录
		flushTable();
	}

	/**
	 * 重置FIXP包尺寸
	 */
	private void reset() {
		ReplyReceiveTimeout cmd = getCommand();

		ReplyHelper.setDisableTimeout((int) cmd.getDisableTimeout());
		ReplyHelper.setSubPacketTimeout((int) cmd.getSubPacketTimeout());

		// 设置标题
		createShowTitle(new String[] { "REPLY-RECEIVE-TIMEOUT/LOCAL/DISABLE-TIMEOUT", "REPLY-RECEIVE-TIMEOUT/LOCAL/SUBPACKET-TIMEOUT" });

		// 显示单元
		ShowItem item = new ShowItem();

		String s1 = String.format("%d", cmd.getDisableTimeout() / 1000);
		String s2 = String.format("%d", cmd.getSubPacketTimeout() / 1000);

		item.add(new ShowStringCell(0, s1));
		item.add(new ShowStringCell(1, s2));
		// 保存单元
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}

}