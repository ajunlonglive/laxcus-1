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
 * 设置应答FIXP包尺寸调用器。
 * 命令先投递到管理节点，再经过管理节点转发到下属节点
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopReplyPacketSizeInvoker extends DesktopInvoker {

	/**
	 * 构造释放节点内存命令调用器，指定命令
	 * @param cmd 重装加载安全规则命令
	 */
	public DesktopReplyPacketSizeInvoker(ReplyPacketSize cmd) {
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