/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.custom.front.driver;

import com.laxcus.front.driver.invoker.*;
import com.laxcus.front.driver.mission.*;

/**
 * FRONT.DRIVER自定义命令异步调用器 <br>
 * 
 * 根据FRONT的驱动程序的操作模式，提供与用户私有业务相关的异步操作服务和接口。
 * 
 * @author scott.liang
 * @version 1.0 11/2/2017
 * @since laxcus 1.0
 */
public abstract class CustomDriverInvoker extends DriverInvoker {

	/**
	 * 构造默认的FRONT.DRIVER自定义命令调用器
	 * @param mission 驱动任务
	 */
	protected CustomDriverInvoker(DriverMission mission) {
		super(mission);
	}

}
