/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.command.*;
import com.laxcus.command.field.*;
import com.laxcus.data.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.conduct.from.*;
import com.laxcus.task.establish.rise.*;
import com.laxcus.task.establish.scan.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 筛选DATA站点元数据命令调用器。
 * HOME站点发出命令，DATA站点选择账号签名匹配的数据块索引表和阶段命名，发送给CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 4/19/2013
 * @since laxcus 1.0
 */
public class DataSelectFieldToCallInvoker extends DataInvoker {

	/**
	 * 构造筛选命令调用器，指定筛选命令
	 * @param cmd 筛选DATA站点元数据命令
	 */
	public DataSelectFieldToCallInvoker(SelectFieldToCall cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SelectFieldToCall getCommand() {
		return (SelectFieldToCall) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SelectFieldToCall cmd = getCommand();
		Node call = cmd.getCallSite();
		List<Siger> sigers = cmd.list();

		// 生成命令，记录本地站点地址
		Node local = getLocal();
		PushDataField field = new PushDataField(local);
		// 判断内存/磁盘空间不足！
		field.setMemoryMissing(isLocalMemoryMissing());
		field.setDiskMissing(isLocalDiskMissing());
		// 最大权级
		field.setPriority(CommandPriority.MAX);
		
		// 输出数据块索引表
		for (Siger siger : sigers) {
			List<Space> spaces = StaffOnDataPool.getInstance().findSpaces(siger);

			Logger.debug(this, "launch", "find '%s' space size:%d", siger, spaces.size());

			for (Space space : spaces) {
				StubTable table = StaffOnDataPool.getInstance().createStubTable(space);
				if (table != null) {
					field.add(table);

					Logger.debug(this, "launch", "save StubTable for %s, stub size:%d", space, table.size());
				}
			}
		}

		// 检查与用户签名匹配的阶段命名
		ArrayList<Phase> array = new ArrayList<Phase>();
		array.addAll(FromTaskPool.getInstance().getPhases());
		array.addAll(ScanTaskPool.getInstance().getPhases());
		array.addAll(RiseTaskPool.getInstance().getPhases());
		// 筛选匹配的用户
		for (Phase phase : array) {
			for (Siger siger : sigers) {
				if(phase.isIssuer(siger)) {
					field.addPhase(phase);

					Logger.debug(this, "launch", "save '%s'", phase);
					break;
				}
			}
		}
		// 用户签名
		for (Siger siger : sigers) {
			boolean success = StaffOnDataPool.getInstance().hasRefer(siger);
			if (success) {
				field.addSiger(siger);
			}
		}

		// 提交命令给CALL站点，不需要反馈
		boolean success = directTo(call, field);

		Logger.debug(this, "launch", success, "siger size:%d, send %d phases to %s", 
				sigers.size(), field.getPhases().size(), call);
		
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
