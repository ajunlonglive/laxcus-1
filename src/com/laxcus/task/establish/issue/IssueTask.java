/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.issue;

import com.laxcus.command.establish.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.*;

/**
 * 数据构建的“ISSUE”阶段任务。<br><br>
 * 
 * ISSUE是数据构建的资源初始化阶段，检查后续操作需要的参数，为后续SCAN阶段分配会话，功能与CONDUCT.INIT阶段相似。<br>
 * ISSUE阶段任务位于CALL节点，属于串行化任务，一个数据构建任务只调用一次ISSUE任务。<br>
 * 
 * @author scott.liang
 * @version 1.1 1/12/2012
 * @since laxcus 1.0
 */
public abstract class IssueTask extends SerialTask {

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.SerialTask#destroy()
	 */
	@Override
	protected void destroy() {
		super.destroy();
	}

	/**
	 * 生成数据构建的“ISSUE”阶段任务
	 */
	protected IssueTask() {
		super();
	}

	/**
	 * 建立、检查、完善一个ESTABLISH命令的工作。<br>
	 * 工作内容：<br>
	 * 1. 检验ESTABLISH各阶段参数的合法性和有效性。<br>
	 * 2. 为ESTABLISH.SCAN阶段分配参数。<br>
	 * 3. 为ESTABLISH.SCAN之后各阶段预定义参数。<br>
	 * 
	 * @param estab ESTABLISH对象名柄
	 * @return 返回整理和调整后的对象句柄
	 * @throws TaskException - 执行过程发生错误，弹出分布任务异常
	 */
	public abstract Establish create(Establish estab) throws TaskException;

}