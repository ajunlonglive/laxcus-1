/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.servlet;

import com.laxcus.command.*;
import com.laxcus.mission.*;

/**
 * 边缘容器信道。
 * 通过这个信道，边缘容器可以访问云端。
 * 
 * @author scott.liang
 * @version 1.0 2019年6月23日
 * @since laxcus 1.0
 */
public interface TubChannel {

	/**
	 * 通过容器信道，边缘容器向云端发送命令，然后等待和输出结果。
	 * 
	 * @param cmd LAXCUS命令
	 * @return 返回任意的对象，边缘容器判断解释
	 * 
	 * @throws TubException 容器错误
	 */
	MissionResult submit(Command cmd) throws MissionException;
	
	/**
	 * 通过容器信道，边缘容器向云端发送命令，然后等待和输出结果。
	 * 
	 * @param input LAXCUS命令
	 * @return 返回任意的对象，边缘容器判断解释
	 * 
	 * @throws TubException 容器错误
	 */
	MissionResult submit(String input) throws MissionException;

}