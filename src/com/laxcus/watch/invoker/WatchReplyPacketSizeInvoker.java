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
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 设置应答FIXP包尺寸调用器。
 * 命令先投递到管理节点，再经过管理节点转发到下属节点
 * 
 * @author scott.liang
 * @version 1.0 1/11/2019
 * @since laxcus 1.0
 */
public class WatchReplyPacketSizeInvoker extends WatchInvoker {

	/**
	 * 构造释放节点内存命令调用器，指定命令
	 * @param cmd 重装加载安全规则命令
	 */
	public WatchReplyPacketSizeInvoker(ReplyPacketSize cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReplyPacketSize getCommand() {
		return (ReplyPacketSize) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ReplyPacketSize cmd = getCommand();
		
		// 判断子包尺寸有范围内
		if (!ReplyTransfer.isSubPacketContentSize(cmd.getSubPacketSize())) {
			faultX(FaultTip.ILLEGAL_PARAMETER);
			return false;
		}
		
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
		ReplyPacketSizeProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(ReplyPacketSizeProduct.class, index);
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
	private void print(ReplyPacketSizeProduct product) {
		// 显示运行时间
		printRuntime();
		// 设置标题
		createShowTitle(new String[] { "REPLY-PACKET-SIZE/STATUS",
				"REPLY-PACKET-SIZE/SITE", "REPLY-PACKET-SIZE/PACKET-SIZE", "REPLY-PACKET-SIZE/SUBPACKET-SIZE" });

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
		for (ReplyPacketSizeItem e : product.list()) {
			ShowItem item = new ShowItem();
			// 图标
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			// 站点地址
			item.add(new ShowStringCell(1, e.getSite()));
			
			// 包尺寸
			if (e.getPacketSize() <= 0) {
				item.add(new ShowStringCell(2, "--"));
			} else {
				String s = ConfigParser.splitCapacity(e.getPacketSize());
				item.add(new ShowStringCell(2, s));
			}
			
			// 子包尺寸
			if (e.getSubPacketSize() <= 0) {
				item.add(new ShowStringCell(3, "--"));
			} else {
				String s = ConfigParser.splitCapacity(e.getSubPacketSize());
				item.add(new ShowStringCell(3, s));
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
		ReplyPacketSize cmd = getCommand();

		// 区分公网/内网，分别设置
		if (cmd.isWide()) {
			ReplyTransfer.setDefaultWidePacketContentSize(cmd.getPacketSize());
			ReplyTransfer.setDefaultWideSubPacketContentSize(cmd.getSubPacketSize());
		} else {
			ReplyTransfer.setDefaultPacketContentSize(cmd.getPacketSize());
			ReplyTransfer.setDefaultSubPacketContentSize(cmd.getSubPacketSize());
		}

		// 设置标题
		createShowTitle(new String[] { "REPLY-PACKET-SIZE/LOCAL/PACKET-SIZE", "REPLY-PACKET-SIZE/LOCAL/SUBPACKET-SIZE" });

		// 显示单元
		ShowItem item = new ShowItem();

		String s1 = ConfigParser.splitCapacity(cmd.getPacketSize());
		String s2 = ConfigParser.splitCapacity(cmd.getSubPacketSize());

		item.add(new ShowStringCell(0, s1));
		item.add(new ShowStringCell(1, s2));
		// 保存单元
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}

}