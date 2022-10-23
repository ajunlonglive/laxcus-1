/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.mid;

import com.laxcus.site.*;

/**
 * 中间数据存取栈<br>
 * 
 * 存取分布处理过程中产生的中间数据，子接口将分别实现“读”和“写”两种操作。
 * 
 * @author scott.liang
 * @version 1.1 3/2/2015
 * @since laxcus 1.0
 */
public interface MiddleTub {

	/**
	 * 判断数据从内存读出
	 * @return 判断成立返回真，否则假
	 */
	boolean isMemory();

	/**
	 * 判断数据从硬盘读出
	 * @return 判断成立返回真，否则假
	 */
	boolean isDisk();
	
	/**
	 * 返回存取栈的任务编号
	 * @return 任务编号
	 */
	long getTaskId();

	/**
	 * 返回存取堆栈所属的站点地址
	 * @return 存取堆栈所属的站点地址
	 */
	Node getLocal();
}