/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.tub.mission;

import com.laxcus.command.*;
import com.laxcus.mission.*;

/**
 * 边缘容器任务
 * 
 * @author scott.liang
 * @version 1.0 7/4/2019
 * @since laxcus 10
 */
public class TubMission extends Mission {
	
	/**
	 * 构造默认的边缘容器任务
	 */
	private TubMission() {
		super();
	}

	/**
	 * 构造边缘容器任务，指定命令
	 * @param cmd 命令
	 */
	public TubMission(Command cmd) {
		this();
		setCommand(cmd);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.mission.Mission#commit(long, boolean)
	 */
	@Override
	public MissionResult commit(long timeout, boolean memory)
			throws MissionException {
		// 设置超时时间
		command.setTimeout(timeout);
		// 设置内存处理模式
		command.setMemory(memory);

		// 调用产生结果
		return getInvokerPool().launchTub(this);
	}

}