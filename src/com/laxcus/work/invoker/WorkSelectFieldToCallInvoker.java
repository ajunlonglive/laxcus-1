/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.invoker;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.command.field.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.contact.distant.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.work.pool.*;

/**
 * 筛选WORK站点元数据命令调用器。
 * 这个命令由HOME发出，WORK站点选择账号签名匹配的阶段命名，发送给CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 4/19/2013
 * @since laxcus 1.0
 */
public class WorkSelectFieldToCallInvoker extends WorkInvoker {

	/**
	 * 构造筛选元数据命令调用器，指定命令
	 * @param cmd 筛选WORK站点元数据命令。
	 */
	public WorkSelectFieldToCallInvoker(SelectFieldToCall cmd) {
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

		Node local = getLocal();
		PushWorkField field = new PushWorkField(local);
		// 判断内存/磁盘空间不足！
		field.setMemoryMissing(isLocalMemoryMissing());
		field.setDiskMissing(isLocalDiskMissing());
		// 最大优先级
		field.setPriority(CommandPriority.MAX);

		// 选择账号签名匹配的阶段命名
		ArrayList<Phase> phases = new ArrayList<Phase>();
		phases.addAll(ToTaskPool.getInstance().getPhases());
		phases.addAll(DistantTaskPool.getInstance().getPhases());

		for (Phase phase : phases) {
			for (Siger siger : sigers) {
				if (phase.isIssuer(siger)) {
					field.addPhase(phase);
					break;
				}
			}
		}
		
		// 选择匹配的用户签名
		for (Siger siger : sigers) {
			boolean success = StaffOnWorkPool.getInstance().hasRefer(siger);
			if (success) {
				field.addSiger(siger);
			}
		}
		
		// 发送命令给CALL站点
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
