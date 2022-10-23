/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.mission;

import com.laxcus.access.parse.*;
import com.laxcus.command.*;
import com.laxcus.front.invoker.*;

/**
 * 前端任务生成器 <br>
 * 
 * @author scott.liang
 * @version 1.0 07/02/2019
 * @since laxcus 1.0
 */
public abstract class MissionCreator {

	/** 语法检查器 **/
	protected SyntaxChecker checker = new SyntaxChecker();

	/**
	 * 构造默认的任务生成器
	 */
	protected MissionCreator() {
		super();
	}

	/**
	 * 将文本语句解析成命令，包装为分布计算的前端任务后输出
	 * 
	 * @param input 输入的命令文本语句
	 * @return 返回分布计算的前端任务
	 * @throws MissionException 分布计算的前端任务异常
	 */
	public abstract Command create(String input) throws MissionException;

	/**
	 * 将一个命令包装到成前端任务后输出。是Mission子类，包括TubMission/EdgeMission/DriverMission
	 * @param cmd 命令
	 * @return 分布计算的前端任务。
	 * @throws MissionException 分布计算的前端任务异常
	 */
	public abstract Mission create(Command cmd) throws MissionException;

	/**
	 * 根据前端任务生成一个调用器。<br>
	 * 前端任务包括：TubMission、EdgeMission、DriverMission。<br>
	 * 
	 * @param mission 前端任务。
	 * @return 返回对应的异步调用器（FrontInvoker的子类），没有返回空指针。
	 * @throws MissionException 
	 */
	public abstract FrontInvoker createInvoker(Mission mission) throws MissionException;

}