/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;
import com.laxcus.task.*;
import com.laxcus.util.*;

/**
 * 简单SELECT命令调用器。<br>
 * 包含“SELECT * FROM DATABASE.MEDIA WHERE KEY>=VALUE”语句块的SELECT命令。
 * CALL站点将SELECT操作分派到DATA站点执行。
 * 
 * @author scott.liang
 * @version 1.2 1/23/2015
 * @since laxcus 1.0
 */
public class CallDirectSelectInvoker extends CallInvoker {

	/**
	 * 构造SELECT调用器，指定异步命令
	 * @param cmd SELECT命令
	 */
	public CallDirectSelectInvoker(Select cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Select getCommand() {
		return (Select) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Select cmd = getCommand();

		boolean success = launchTo(cmd);
		// 不成功，发送错误通知
		if (!success) {
			replyFault();
		}

		Logger.debug(this, "launch", success, "result is");

		// 返回处理结果
		return success;
	}

	/**
	 * 发送命令到DATA站点
	 * @param select
	 * @return
	 */
	private boolean launchTo(Select select) {
		Space space = select.getSpace();
		Siger siger = select.getIssuer();
		// 不允许
		if(!allow(siger, space)) {
			replyFault(Major.FAULTED, Minor.REFUSE);
			return false;
		}
		
		// 产生数据块分布区列表
		List<StubSector> list = null;
		try {
			list = DataOnCallPool.getInstance().doStubSector(space);
		} catch (TaskException e) {
			Logger.error(e);
		}
		
		// try {
		// list =
		// FromSeekManager.getInstance().createStubSector(select.getIssuer(),
		// space);
		// } catch (TaskException e) {
		// Logger.error(e);
		// }
		
		if (list == null || list.isEmpty()) {
			Logger.error(this, "launchTo", "cannot be find %s", space);
			return false;
		}

		Logger.debug(this, "launchTo", "send sites is :%d", list.size());

		// 站点地址
		int sites = list.size();
		// 分配单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>(sites);
		for (int index = 0; index < sites; index++) {
			StubSector stub = list.get(index);
			// 投递SELECT命令
			CastSelect cmd = new CastSelect(select, stub.list());
			// 目标地址
			Node hub = stub.getRemote();
			CommandItem item = new CommandItem(hub, cmd);
			array.add(item);
		}

		// 以“完成”模式发送命令到DATA站点，任何一个站点出错都返回失败
		boolean success = completeTo(array);

		Logger.debug(this, "launchTo", success, "send size:%d", array.size());

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 处理失败
		if (isFaultCompleted()) {
			Logger.error(this, "ending", "select error");
			replyFault(); // 通知终端错误
			return false;
		}

		boolean success = false;
		// 判断文件全部在磁盘
		boolean ondisk = isEchoFiles();
		// 发送文件给FRONT节点
		if(ondisk) {
			File[] files = getAllFiles();
			success = replyFile(files);
		} else {
			byte[] b = collect();
			success = replyPrimitive(b);
		}

		Logger.note(this, "ending", success, "Send to %s", getCommandSource());

		// 退出
		return useful(success);
	}

}