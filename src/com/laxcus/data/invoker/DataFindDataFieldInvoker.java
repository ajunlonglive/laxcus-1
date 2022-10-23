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
import com.laxcus.command.field.*;
import com.laxcus.data.*;
import com.laxcus.data.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;
import com.laxcus.task.conduct.from.*;
import com.laxcus.task.establish.rise.*;
import com.laxcus.task.establish.scan.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 查找DATA域数据命令调用器。<br>
 * 
 * 这个命令是来自HOME站点，查找表空间的元数据，发向指定的CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 2/23/2013
 * @since laxcus 1.0
 */
public class DataFindDataFieldInvoker extends DataInvoker {

	/**
	 * 构造查找DATA域数据命令调用器，指定命令
	 * @param cmd 查找DATA域元数据命令调用器
	 */
	public DataFindDataFieldInvoker(FindDataField cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindDataField getCommand() {
		return (FindDataField) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindDataField cmd = getCommand();
		
		// 生成命令，设置回显地址
		DataLauncher launcher = getLauncher();
		PushDataField field = new PushDataField(launcher.getListener());
		// 判断内存/磁盘空间不足！
		field.setMemoryMissing(isLocalMemoryMissing());
		field.setDiskMissing(isLocalDiskMissing());

		// 输出数据块索引表
		for (Space space : cmd.getSpaces()) {
			StubTable table = StaffOnDataPool.getInstance().createStubTable(space);
			if (table != null) {
				field.add(table);
			}
		}
		// 检查与用户签名匹配的阶段命名
		for (Siger siger : cmd.getUsers()) {
			List<Phase> list = FromTaskPool.getInstance().getPhases();
			for (Phase phase : list) {
				if (Laxkit.compareTo(siger, phase.getIssuer()) == 0) {
					field.addPhase(phase);
				}
			}
			list = ScanTaskPool.getInstance().getPhases();
			for (Phase phase : list) {
				if (Laxkit.compareTo(siger, phase.getIssuer()) == 0) {
					field.addPhase(phase);
				}
			}
			list = RiseTaskPool.getInstance().getPhases();
			for (Phase phase : list) {
				if (Laxkit.compareTo(siger, phase.getIssuer()) == 0) {
					field.addPhase(phase);
				}
			}
			// 判断签名存在
			boolean success = StaffOnDataPool.getInstance().hasRefer(siger);
			if (success) {
				field.addSiger(siger);
			}
		}

		// 要求目标站点立即处理这个命令
		field.setQuick(true);
		
		// 向目标站点发送命令，不需要反馈结果
		Node endpoint = cmd.getNode();
		boolean success = directTo(endpoint, field);

		Logger.debug(this, "launch", success, "send to %s, size is %d", endpoint, field.size());

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
