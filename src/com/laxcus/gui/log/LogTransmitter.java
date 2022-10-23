/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.log;

import com.laxcus.util.display.*;

/**
 * 日志转发接口，向日志图形界面输出日志
 * 
 * @author scott.liang
 * @version 1.0 5/26/2021
 * @since laxcus 1.0
 */
public interface LogTransmitter {

	/**
	 * 输出日志集
	 * @param logs 日志集
	 */
	void pushLogs(java.util.List<LogItem> logs);

}