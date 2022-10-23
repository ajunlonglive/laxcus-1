/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.attend;

import com.laxcus.echo.*;

/**
 * 签到器。<br>
 * 借鉴去中心化的思想，在执行某种数据处理之前，各站点之间进行相互确认的一个操作，证明对方存在且有效。<br>
 * 所有需要相互确认的调用器都要实现这个接口，以实现去中心化的数据处理。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/18/2014
 * @since laxcus 1.0
 */
public interface Attender {

	/**
	 * 执行分布签到。<br>
	 * 
	 * @param from 调用器来源地址
	 * @return 返回签到状态的三种状态之一。见AttendTag中定义。
	 */
	int attend(Cabin from);
}