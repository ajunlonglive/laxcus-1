/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.reload.*;
import com.laxcus.fixp.reply.*;

import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 设置应答数据流量控制参数调用器。
 * 只在本地执行
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopReplyFlowControlInvoker extends DesktopInvoker {

	/**
	 * 构造应答数据流量控制参数调用器，指定命令
	 * @param cmd 应答数据流量控制参数
	 */
	public DesktopReplyFlowControlInvoker(ReplyFlowControl cmd) {
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
		
		if (cmd.isLocal()) {
			reset();
		} else {
			faultX(FaultTip.PERMISSION_MISSING); // 权限不足
		}
		// 投递到HUB站点
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 设置流队列长度
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
		item.add(new ShowStringCell(0, ReplyTransfer.getDefaultFlowBlocks()));
		item.add(new ShowStringCell(1, ReplyTransfer.getDefaultFlowTimeslice()));
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