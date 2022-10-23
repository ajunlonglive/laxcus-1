/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.util.*;

import com.laxcus.launch.*;
import com.laxcus.log.client.*;

/**
 * 命令/调用器扫描器
 * @author scott.liang
 * @version 1.0 1/15/2019
 * @since laxcus 1.0
 */
class CommandStackTask extends TimerTask {

	/** 站点句柄 **/
	private SiteLauncher launcher;

	/**
	 * 构造默认和私有的命令/调用器扫描器
	 */
	public CommandStackTask(SiteLauncher e) {
		super();
		launcher = e;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		List<String> array = launcher.getCommandPool().printCommands();
		Logger.info(this, "run", "commands size:%d", array.size());
		for (String s : array) {
			Logger.info(this, "run", "%s", s);
		}

		array = launcher.getInvokerPool().printInvokers();
		Logger.info(this, "run", "invokers size:%d", array.size());
		for (String s : array) {
			Logger.info(this, "run", "%s", s);
		}
	}

}
