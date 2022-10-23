/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.talk;

import com.laxcus.site.*;
import com.laxcus.task.*;

/**
 * 分布任务组件的实时交互代理。
 * 
 * 实时交互代理是LAXCUS 2.7版本提供的功能，支持运行过程中的分布任务组件进行远程通信，实时更准确的分布式计算。
 * 
 * @author scott.liang
 * @version 1.0 6/13/2018
 * @since laxcus 1.0
 */
public interface TalkTrustor {

	/**
	 * 判断目标节点上的关联命令所处状态，包括以下可能：1.没有找到，2.命令状态（等待中），3.调用器状态（运行中）
	 * @param invokerId 本地调用器编号
	 * @param remote 目标站点地址
	 * @param tag 对话标记。通过这个标记，判断找到并判断目标节点上的命令状态，隐含前提条件是必须继承自DistributeStep。
	 * @return 返回分布任务组件瞬时状态，见TaskMoment定义。
	 * @throws TaskException - 发生分布任务组件异常
	 */
	TaskMoment check(long invokerId, Node remote, TalkFalg tag) throws TaskException;
	
	/**
	 * 执行交互通信
	 * 
	 * @param invokerId 本地调用器编号
	 * @param remote 目标站点
	 * @param quest 请求操作
	 * @return 反馈结果
	 * @throws TaskException - 发生分布任务组件异常
	 */
	TalkReply ask(long invokerId, Node remote, TalkQuest quest) throws TaskException;
}