/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.fork;

import com.laxcus.command.contact.*;
import com.laxcus.task.*;
import com.laxcus.task.contact.*;

/**
 * CONTACT.FORK阶段任务组件。<br><br>
 * 
 * FORK组件位于CALL站点，检查CONTACT命令，和为后续资源分配参数。<br>
 * 
 * @author scott.liang
 * @version 1.2 12/26/2011
 * @since laxcus 1.0
 */
public abstract class ForkTask extends CastTask {

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.swift.DesignTask#destroy()
	 */
	@Override
	protected void destroy() {
		super.destroy();
	}

	/**
	 * 构造默认的CONTACT.FORK任务实例
	 */
	protected ForkTask() {
		super();
	}

	/**
	 * 检查DISTANT阶段计算资源，初始化DISTANT阶段基础参数。
	 * @param contact 分布计算命令
	 * @return 分配参数后的swift命令
	 * @throws TaskException 检查和判断发现错误，弹出分布任务异常
	 */
	public abstract Contact fork(Contact contact) throws TaskException;

}