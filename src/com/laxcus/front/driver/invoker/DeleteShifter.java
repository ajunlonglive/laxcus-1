/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.access.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * DELETE命令转义器
 * 
 * @author scott.liang
 * @version 1.0 2/8/2018
 * @since laxcus 1.0
 */
public class DeleteShifter extends DriverShifter {

	/**
	 * 构造默认的DELETE命令转义器
	 */
	public DeleteShifter() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.driver.invoker.DriverShifter#createInvoker(com.laxcus.front.driver.mission.DriverMission)
	 */
	@Override
	public DriverInvoker createInvoker(DriverMission mission) {
		// 必须是指定命令
		if (!Laxkit.isClassFrom(mission.getCommand(), Delete.class)) {
			mission.setException("cannot be cast!");
			return null;
		}

		Delete delete = (Delete) mission.getCommand();

		// 判断是带嵌套的删除操作
		if (delete.hasNested()) {
			return createSubDelete(mission, delete);
		} else {
			return new DriverDirectDeleteInvoker(mission);
		}
	}
	
	/**
	 * 建立一个嵌套删除的CONDUCT调用器
	 * @param mission 驱动任务
	 * @param delete 删除命令
	 * @return 返回CONDUCT异步调用器
	 */
	private DriverInvoker createSubDelete(DriverMission mission, Delete delete) {
		final String root = "SUBDELETE";
//		Phase phase = new Phase(getUsername(), PhaseTag.INIT, Sock.doSystemSock( root));
		Phase phase = new Phase(getUsername(), PhaseTag.INIT, Sock.doSystemSock(root));
		InitObject initObject = new InitObject(phase);
		initObject.addCommand("DELETE_OBJECT", delete);

		// 保存DELETE独享资源写操作
		initObject.addRules(delete.getRules());

		// 构造分布计算实例
		Conduct conduct = new Conduct(phase.getSock());
		// 设置初始化命名对象，数据资源的处理，如参数分配、数据分片等，到CALL.INIT上执行
		conduct.setInitObject(initObject);

		// 保存原语
		conduct.setPrimitive(delete.getPrimitive());
		// 替换命令
		mission.setCommand(conduct);
		
		// 返回异步调用器
		return new DriverConductInvoker(mission);
	}

}
