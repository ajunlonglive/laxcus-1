/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.work;

import com.laxcus.command.*;
import com.laxcus.visit.*;
import com.laxcus.work.pool.*;

/**
 * WORK站点的命令调用接口。<br>
 * 提供资源管理和CONDUCT.TO阶段的命令处理。
 * 
 * @author scott.liang
 * @version 1.0 8/23/2009
 * @since laxcus 1.0
 */
public class CommandVisitOnWork implements CommandVisit {

	/**
	 * 构造WORK站点的命令调用接口
	 */
	public CommandVisitOnWork() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.CommandVisit#submit(com.laxcus.command.Command)
	 */
	@Override
	public boolean submit(Command cmd) throws VisitException {
		return WorkCommandPool.getInstance().accept(cmd);
	}

}