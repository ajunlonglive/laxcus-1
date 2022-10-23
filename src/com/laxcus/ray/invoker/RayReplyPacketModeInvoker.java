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
 * 设置应答包传输模式调用器。
 * 命令先投递到管理节点，再经过管理节点转发到下属节点
 * 
 * @author scott.liang
 * @version 1.0 9/3/2020
 * @since laxcus 1.0
 */
public class RayReplyPacketModeInvoker extends RayInvoker {

	/**
	 * 构造应答包传输模式调用器，指定命令
	 * @param cmd应答包传输模式命令
	 */
	public RayReplyPacketModeInvoker(ReplyPacketMode cmd) {
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
		ReplyPacketModeProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(ReplyPacketModeProduct.class, index);
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
	private void print(ReplyPacketModeProduct product) {
		// 显示运行时间
		printRuntime();
		// 设置标题
		createShowTitle(new String[] { "REPLY-PACKET-MODE/STATUS",
				"REPLY-PACKET-MODE/SITE", "REPLY-PACKET-MODE/PACKET-MODE", });

		// 时间
		ShowItem title = new ShowItem();
		title.add(new ShowStringCell(0, ""));
		String text = formatDistributeTime(product.getProcessTime());
		title.add(new ShowStringCell(1, text));
		title.add(new ShowStringCell(2, ""));

		// 保存单元
		addShowItem(title);

		// 显示单元
		for (ReplyPacketModeItem e : product.list()) {
			ShowItem item = new ShowItem();
			// 图标
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			// 站点地址
			item.add(new ShowStringCell(1, e.getSite()));
			
			// 包传输模式
			String mode = "--";
			if (e.getPacketMode() == ReplyTransfer.SERIAL_TRANSFER) {
				mode = getXMLContent("REPLY-PACKET-MODE/SERIAL");
			} else if (e.getPacketMode() == ReplyTransfer.PARALLEL_TRANSFER) {
				mode = getXMLContent("REPLY-PACKET-MODE/PARALLEL");
			}
			item.add(new ShowStringCell(2, mode));

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