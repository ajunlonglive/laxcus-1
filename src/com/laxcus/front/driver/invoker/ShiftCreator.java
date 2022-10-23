/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.*;
import com.laxcus.command.access.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.util.*;

/**
 * 异步调用器生成器。
 * 
 * @author scott.liang
 * @version 1.0 2/9/2018
 * @since laxcus 1.0
 */
public class ShiftCreator {

	/**
	 * 根据驱动任务，判断命令，生成关联的异步调用器
	 * @param mission 驱动任务
	 * @return 返回异步调用器实例
	 */
	public static DriverInvoker createInvoker(DriverMission mission) {
		DriverShifter shifter = null;
		Command cmd = mission.getCommand();

		// 必须是指定命令
		if (Laxkit.isClassFrom(cmd, Select.class)) {
			shifter = new SelectShifter();
		} else if (Laxkit.isClassFrom(cmd, Delete.class)) {
			shifter = new DeleteShifter();
		} else if (Laxkit.isClassFrom(cmd, Update.class)) {
			shifter = new UpdateShifter();
		} else if (Laxkit.isClassFrom(cmd, Modulate.class)) {
			shifter = new ModulateShifter();
		}

		if (shifter != null) {
			return shifter.createInvoker(mission);
		}

		// 失败提示
		mission.setException("cannot be cast!");
		return null;
	}

}
