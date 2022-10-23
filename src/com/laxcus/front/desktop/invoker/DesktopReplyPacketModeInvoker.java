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

import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 设置应答包传输模式调用器。
 * 只在本地执行
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopReplyPacketModeInvoker extends DesktopInvoker {

	/**
	 * 构造应答包传输模式调用器，指定命令
	 * @param cmd 应答包传输模式
	 */
	public DesktopReplyPacketModeInvoker(ReplyPacketMode cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReplyPacketMode getCommand() {
		return (ReplyPacketMode) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ReplyPacketMode cmd = getCommand();
		
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
		ReplyPacketMode cmd = getCommand();

		// 设置传输模式
		ReplyTransfer.setDefaultTransferMode(cmd.getPacketMode());

		// 设置标题
		createShowTitle(new String[] { "REPLY-PACKET-MODE/LOCAL/PACKET-MODE" });

		// 显示单元
		ShowItem item = new ShowItem();
		
		String mode = "--";
		if (ReplyTransfer.isSerialTransfer()) {
			mode = getXMLContent("REPLY-PACKET-MODE/SERIAL");
		} else if (ReplyTransfer.isParallelTransfer()) {
			mode = getXMLContent("REPLY-PACKET-MODE/PARALLEL");
		}

		item.add(new ShowStringCell(0, mode));
		// 保存单元
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}

}