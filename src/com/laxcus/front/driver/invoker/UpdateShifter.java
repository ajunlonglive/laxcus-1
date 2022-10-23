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
 * UPDATE命令转义器
 * 
 * @author scott.liang
 * @version 1.0 2/8/2018
 * @since laxcus 1.0
 */
public class UpdateShifter extends DriverShifter {

	/**
	 * 构造UPDATE命令转义器
	 */
	public UpdateShifter() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.driver.invoker.DriverShifter#createInvoker(com.laxcus.front.driver.mission.DriverMission)
	 */
	@Override
	public DriverInvoker createInvoker(DriverMission mission) {
		// 必须是指定命令
		if (!Laxkit.isClassFrom(mission.getCommand(), Update.class)) {
			mission.setException("cannot be cast!");
			return null;
		}

		Update update = (Update) mission.getCommand();

		if (update.hasNested()) {
			return createSubUpdate(mission, update);
		} else {
			return new DriverDirectUpdateInvoker(mission);
		}
	}

	/**
	 * 生成嵌套的CONDUCT命令调用器
	 * @param mission 驱动任务
	 * @param update 更新命令
	 * @return 返回驱动调用器
	 */
	private DriverInvoker createSubUpdate(DriverMission mission, Update update) {
		// SUBUPDATE是系统嵌套更新的根命名
		final String rootText = "SUBUPDATE";
		Sock root = Sock.doSystemSock(rootText);
		
		Phase phase = new Phase(getUsername(), PhaseTag.INIT, root);
		InitObject initObject = new InitObject(phase);
		initObject.addCommand("UPDATE_OBJECT", update);

		// 保存UPDATE命令的事务规则
		initObject.addRules(update.getRules());

		// 构造分布计算实例
		Conduct conduct = new Conduct(root);
		// 设置初始化命名对象，数据资源的处理，如参数分配、数据分片等，到CALL.INIT上执行
		conduct.setInitObject(initObject);

		// 保存原语
		conduct.setPrimitive(update.getPrimitive());
		// 更换命令
		mission.setCommand(conduct);
		
		// 输出驱动调用器
		return new DriverConductInvoker(mission);
	}
}
