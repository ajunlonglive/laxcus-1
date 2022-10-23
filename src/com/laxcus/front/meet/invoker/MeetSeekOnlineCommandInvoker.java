/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.command.mix.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 检索在线命令调用器
 * 
 * @author scott.liang
 * @version 1.0 4/16/2018
 * @since laxcus 1.0
 */
public class MeetSeekOnlineCommandInvoker extends MeetInvoker {

	/**
	 * 构造检索在线命令调用器，指定命令
	 * @param cmd 检索在线命令
	 */
	public MeetSeekOnlineCommandInvoker(SeekOnlineCommand cmd) {
		super(cmd);
	}

	/**
	 * SeekOnlineCommand命令在Meet是本地执行
	 */
	@Override
	public boolean isDistributed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekOnlineCommand getCommand() {
		return (SeekOnlineCommand) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekOnlineCommand cmd = getCommand();

		// 如果只显示自己的任务
		if (!cmd.isMe()) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		}

		// 显示本地参数
		print();
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
	 * 判断是自己的命令
	 * 
	 * @param cmd 传入命令
	 * @return 返回真或者假
	 */
	private boolean isSelf(Command cmd) {
		Cabin source = getCommandSource();
		return (Laxkit.compareTo(source, cmd.getSource()) == 0 && 
				cmd.getClass() == SeekOnlineCommand.class);
	}

	/**
	 * 显示自己的命令
	 */
	private void print() {
		SeekOnlineCommandProduct product = new SeekOnlineCommandProduct();
		product.setSite(getLocal());

		// FRONT站点的命令池和调用器池
		CommandPool commandPool = getLauncher().getCommandPool();
		InvokerPool invokerPool = getLauncher().getInvokerPool();

		// 处于等待状态的命令
		List<Command> array = commandPool.getCommands();
		for (Command cmd : array) {
			SeekOnlineCommandItem e = new SeekOnlineCommandItem(cmd.getClass().getSimpleName(), cmd.getIssuer());
			e.setRunning(false);
			e.setSource(cmd.getSource());
			e.setMemory(cmd.isMemory());
			e.setDirect(cmd.isDirect());
			e.setPriority(cmd.getPriority());
			e.setOnlineTime(System.currentTimeMillis() - cmd.getCreateTime());
			product.add(e);
		}

		// 运行状态的命令
		array = invokerPool.getCommands();
		for (Command cmd : array) {
			// 如果是自己，忽略
			if (isSelf(cmd)) {
				continue;
			}

			// 如果调用器存在，找到它的线程编号
			long threadId = -1;
			EchoInvoker invoker = invokerPool.findInvoker(cmd.getLocalId());
			if (invoker != null) {
				threadId = invoker.getThreadId();
			}

			SeekOnlineCommandItem e = new SeekOnlineCommandItem(cmd.getClass().getSimpleName(), cmd.getIssuer());
			e.setRunning(true);
			e.setSource(cmd.getSource());
			e.setMemory(cmd.isMemory());
			e.setDirect(cmd.isDirect());
			e.setPriority(cmd.getPriority());
			e.setOnlineTime(System.currentTimeMillis() - cmd.getCreateTime());
			e.setThreadId(threadId);
			product.add(e);
		}

		// 显示本地结果
		print(product.list());
	}

	/**
	 * 打印参数
	 * @param array
	 */
	private void print(List<SeekOnlineCommandItem> array) {
		printRuntime();

		createShowTitle(new String[] { "SEEK-ONLINE-COMMAND/COMMAND",
				"SEEK-ONLINE-COMMAND/ONLINE-TIME", "SEEK-ONLINE-COMMAND/SIGER",
				"SEEK-ONLINE-COMMAND/CABIN", "SEEK-ONLINE-COMMAND/STATUS",
				"SEEK-ONLINE-COMMAND/PRIORITY", "SEEK-ONLINE-COMMAND/MODE",
				"SEEK-ONLINE-COMMAND/DIRECT","SEEK-ONLINE-COMMAND/THREAD-ID" });

		String system = getXMLContent("SEEK-ONLINE-COMMAND/SIGER/SYSTEM");
		String selfly = getXMLContent("SEEK-ONLINE-COMMAND/SIGER/MYSELF");
		String running = getXMLContent("SEEK-ONLINE-COMMAND/STATUS/RUNNING"); // 处理中
		String waiting = getXMLContent("SEEK-ONLINE-COMMAND/STATUS/WAITING"); // 等待
		String suspend = getXMLContent("SEEK-ONLINE-COMMAND/STATUS/SUSPEND"); // 等待

		String memory = getXMLContent("SEEK-ONLINE-COMMAND/MODE/MEMORY");
		String disk = getXMLContent("SEEK-ONLINE-COMMAND/MODE/DISK");

		String yes = getXMLContent("SEEK-ONLINE-COMMAND/DIRECT/YES");
		String no = getXMLContent("SEEK-ONLINE-COMMAND/DIRECT/NO");

		for (SeekOnlineCommandItem e : array) {
			ShowItem item = new ShowItem();
			// 命令
			item.add(new ShowStringCell(0, e.getCommand()));
			// 在线时间
			String runtime = doStyleTime(e.getOnlineTime());
			item.add(new ShowStringCell(1, runtime));

			// 签名
			if (e.isSystem()) {
				item.add(new ShowStringCell(2, system));
			} else {
				if (isSelfly(e.getSiger())) {
					item.add(new ShowStringCell(2, selfly));
				} else {
					item.add(new ShowStringCell(2, e.getSiger()));
				}
			}
			// 来源
			if (e.getSource() == null) {
				if (isConsole()) {
					item.add(new ShowStringCell(3, "--"));
				} else {
					item.add(new ShowStringCell(3, " "));
				}
			} else {
				item.add(new ShowStringCell(3, e.getSource()));
			}
			// 状态(运行/等待)

			String msg = waiting;
			if (e.isRunning()) {
				if (e.getThreadId() > 0) {
					msg = running;
				} else {
					msg = suspend;
				}
			}

			// 命令状态...
			item.add(new ShowStringCell(4, msg));
			// 优先级
			item.add(new ShowIntegerCell(5, e.getPriority()));
			// 模式
			item.add(new ShowStringCell(6, (e.isMemory() ? memory : disk)));
			// 单向
			item.add(new ShowStringCell(7, (e.isDirect() ? yes : no)));

			// 线程编号
			if (e.getThreadId() > 0) {
				item.add(new ShowLongCell(8, e.getThreadId()));
			} else {
				// 区别控制台还是终端
				if (isConsole()) {
					item.add(new ShowStringCell(8, "--"));
				} else {
					item.add(new ShowStringCell(8, " "));					
				}
			}

			addShowItem(item);
		}
		// 输出全部记录
		flushTable();

		// 显示行数
		int size = array.size();
		if (size > 0) {
			String jobs = getXMLContent("SEEK-ONLINE-COMMAND/JOBS"); // 在线任务
			String str = String.format(jobs, size);
			setStatusText(str);
		}
	}

}