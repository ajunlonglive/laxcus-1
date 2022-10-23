/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.command.reload.*;
import com.laxcus.fixp.reply.*;

import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 设置发送异步数据超时调用器。
 * 命令先投递到管理节点，再经过管理节点转发到下属节点
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class WatchReplySendTimeoutInvoker extends WatchInvoker {

	/**
	 * 构造释放节点内存命令调用器，指定命令
	 * @param cmd 重装加载安全规则命令
	 */
	public WatchReplySendTimeoutInvoker(ReplySendTimeout cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReplySendTimeout getCommand() {
		return (ReplySendTimeout) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ReplySendTimeout cmd = getCommand();
		
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
		ReplySendTimeoutProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(ReplySendTimeoutProduct.class, index);
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
	private void print(ReplySendTimeoutProduct product) {
		// 显示运行时间
		printRuntime();
		// 设置标题
		createShowTitle(new String[] { "REPLY-SEND-TIMEOUT/STATUS",
				"REPLY-SEND-TIMEOUT/SITE","REPLY-SEND-TIMEOUT/DISABLE-TIMEOUT",
				"REPLY-SEND-TIMEOUT/SUBPACKET-TIMEOUT","REPLY-SEND-TIMEOUT/INTERVAL", });

		// 时间
		ShowItem title = new ShowItem();
		title.add(new ShowStringCell(0, ""));
		String text = formatDistributeTime(product.getProcessTime());
		title.add(new ShowStringCell(1, text));
		title.add(new ShowStringCell(2, ""));
		title.add(new ShowStringCell(3, ""));
		title.add(new ShowStringCell(4, ""));
		// 保存单元
		addShowItem(title);

		// 显示单元
		for (ReplySendTimeoutItem e : product.list()) {
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
				item.add(new ShowLongCell(4, e.getInterval()));
			} else {
				item.add(new ShowStringCell(2, "--"));
				item.add(new ShowStringCell(3, "--"));
				item.add(new ShowStringCell(4, "--"));
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
		ReplySendTimeout cmd = getCommand();

		ReplyWorker.setDisableTimeout((int) cmd.getDisableTimeout());
		ReplyWorker.setSubPacketTimeout((int) cmd.getSubPacketTimeout());
		ReplyWorker.setSendInterval((int) cmd.getInterval());

		// 设置标题
		createShowTitle(new String[] { "REPLY-SEND-TIMEOUT/LOCAL/DISABLE-TIMEOUT", "REPLY-SEND-TIMEOUT/LOCAL/SUBPACKET-TIMEOUT" , "REPLY-SEND-TIMEOUT/LOCAL/SUBPACKET-INTERVAL" });

		// 显示单元
		ShowItem item = new ShowItem();

		String s1 = String.format("%d", cmd.getDisableTimeout() / 1000);
		String s2 = String.format("%d", cmd.getSubPacketTimeout() / 1000);
		String s3 = String.format("%d", cmd.getInterval());

		item.add(new ShowStringCell(0, s1));
		item.add(new ShowStringCell(1, s2));
		item.add(new ShowStringCell(2, s3));
		// 保存单元
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}

}