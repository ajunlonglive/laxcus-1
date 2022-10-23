/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.guide;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.task.guide.parameter.*;
import com.laxcus.util.naming.*;

/**
 * 引导任务接口。<br><br>
 * 
 * 根据基础字和录入参数产生分布计算命令。<br>
 * 由用户派生实现。
 * 
 * @author scott.liang
 * @version 1.0 7/25/2020
 * @since laxcus 1.0
 */
public interface CopyOfGuideTask_Interface {

	/**
	 * 返回支持的基础字
	 * @return Sock列表
	 */
	List<Sock> getSocks();

	/**
	 * 判断支持某个基础字
	 * 
	 * @param sock 基础字
	 * @return 返回真或者假
	 */
	boolean isSupport(Sock sock);

	/**
	 * 产生标注参数
	 * 
	 * 实际类输出接口，由用户输入后，调用“create”方法产生分布计算命令
	 * @param sock 基础字
	 * @return 启动参数列表
	 * @throws GuideTaskException
	 */
	InputParameterList markup(Sock sock) throws GuideTaskException;

	/**
	 * 根据基础字和录入参数，产生分布命令
	 * @param sock 基础字
	 * @param list 已经录入的参数
	 * @return 返回生成的DistributeCommand分布命令，包括有“Conduct/Contact/Establish”
	 * @throws GuideTaskException
	 */
	DistributedCommand create(Sock sock, InputParameterList list) throws GuideTaskException;

	/**
	 * 根据输入参数和基础字，产生新分布计算命令，形成迭代。是在“create”方法之后调用。
	 * 
	 * @param predata 上个阶段产生的结果数据
	 * @param sock 基础字
	 * @return 返回生成的DistributeCommand分布命令，包括有“Conduct/Contact/Establish”
	 * @throws GuideTaskException
	 */
	DistributedCommand next(byte[] predata, Sock sock) throws GuideTaskException;


}