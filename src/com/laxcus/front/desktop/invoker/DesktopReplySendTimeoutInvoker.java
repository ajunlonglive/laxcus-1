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
public class DesktopReplySendTimeoutInvoker extends DesktopInvoker {

	/**
	 * 构造应答包传输模式调用器，指定命令
	 * @param cmd 应答包传输模式
	 */
	public DesktopReplySendTimeoutInvoker(ReplySendTimeout cmd) {
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