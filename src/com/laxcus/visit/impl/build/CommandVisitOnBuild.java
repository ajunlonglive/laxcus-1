/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.build;

import com.laxcus.build.pool.*;
import com.laxcus.command.*;
import com.laxcus.visit.*;

/**
 * BUILD站点的命令调用接口
 * 
 * @author scott.liang
 * @version 1.0 08/03/2009
 * @since laxcus 1.0
 */
public class CommandVisitOnBuild implements CommandVisit {

	/**
	 * 构造BUILD站点的命令调用接口
	 */
	public CommandVisitOnBuild() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.CommandVisit#submit(com.laxcus.command.Command)
	 */
	@Override
	public boolean submit(Command cmd) throws VisitException {
		return BuildCommandPool.getInstance().accept(cmd);
	}

}