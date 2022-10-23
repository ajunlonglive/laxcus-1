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
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 设置应答数据流量控制参数调用器。
 * 命令先投递到管理节点，再经过管理节点转发到下属节点
 * 
 * @author scott.liang
 * @version 1.0 9/9/2020
 * @since laxcus 1.0
 */
public class RayReplyFlowControlInvoker extends RayInvoker {

	/**
	 * 构造设置应答数据流量控制参数调用器，指定命令
	 * @param cmd 设置应答数据流量控制参数
	 */
	public RayReplyFlowControlInvoker(ReplyFlowControl cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReplyFlowControl getCommand() {
		return (ReplyFlowControl) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ReplyFlowControl cmd = getCommand();
		
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
		ReplyFlowControlProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(ReplyFlowControlProduct.class, index);
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
	private void print(ReplyFlowControlProduct product) {
		// 显示运行时间
		printRuntime();
		// 设置标题
		createShowTitle(new String[] { "REPLY-FLOW-CONTROL/STATUS", "REPLY-FLOW-CONTROL/SITE" ,
				"REPLY-FLOW-CONTROL/BLOCK", "REPLY-FLOW-CONTROL/TIMESLICE", "REPLY-FLOW-CONTROL/SUBPACKET-CONTENTSIZE"});

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
		for (ReplyFlowControlItem e : product.list()) {
			boolean success = e.isSuccessful();
			ShowItem item = new ShowItem();
			// 图标
			item.add(createConfirmTableCell(0, success));
			// 站点地址
			item.add(new ShowStringCell(1, e.getSite()));

			// 成功失败，不同选择!
			if (success) {
				item.add(new ShowLongCell(2, e.getBlock()));
				item.add(new ShowLongCell(3, e.getTimeslice()));				
				int len = e.getSubPacketContentSize();
				if (len % 1024 != 0) {
					item.add(new ShowIntegerCell(4, len));
				} else {
					String capacity = ConfigParser.splitCapacity(len, 0);
					item.add(new ShowStringCell(4, capacity));
				}
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
		ReplyFlowControl cmd = getCommand();

		ReplyTransfer.setDefaultFlowBlocks(cmd.getBlock());
		ReplyTransfer.setDefaultFlowTimeslice(cmd.getTimeslice());
		// 子包尺寸
		int len = cmd.getSubPacketContentSize();
		// 小于规定的最大子包内容尺寸
		if (!ReplyTransfer.isSubPacketContentSize(len)) {
			len = len - ReplyTransfer.PACKET_HEADSIZE; // 减少包头
		}
		if (ReplyTransfer.isSubPacketContentSize(len)) {
			ReplyTransfer.setDefaultSubPacketContentSize(len);
		}
		
		// 设置标题
		createShowTitle(new String[] { "REPLY-FLOW-CONTROL/LOCAL/BLOCK",
				"REPLY-FLOW-CONTROL/LOCAL/TIMESLICE",
				"REPLY-FLOW-CONTROL/LOCAL/SUBPACKET-CONTENTSIZE" });

		// 显示单元
		ShowItem item = new ShowItem();

		item.add(new ShowIntegerCell(0, ReplyTransfer.getDefaultFlowBlocks()));
		item.add(new ShowIntegerCell(1, ReplyTransfer.getDefaultFlowTimeslice()));
		len = ReplyTransfer.getDefaultSubPacketContentSize();
		if (len % 1024 != 0) {
			item.add(new ShowIntegerCell(2, len));
		} else {
			String capacity = ConfigParser.splitCapacity(len, 0);
			item.add(new ShowStringCell(2, capacity));
		}
		// 保存单元
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}

}