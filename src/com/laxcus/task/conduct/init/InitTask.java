/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.init;

import com.laxcus.command.conduct.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.*;

/**
 * CONDUCT.INIT阶段任务组件。<br><br>
 * 
 * INIT组件位于CALL站点，检查CONDUCT命令，和为后续资源分配参数。<br>
 * 
 * @author scott.liang
 * @version 1.2 12/26/2011
 * @since laxcus 1.0
 */
public abstract class InitTask extends DesignTask {

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.DesignTask#destroy()
	 */
	@Override
	protected void destroy() {
		super.destroy();
	}

	/**
	 * 构造默认的CONDUCT.INIT任务实例
	 */
	protected InitTask() {
		super();
	}

	/**
	 * 检查和分配FROM阶段计算资源，初始化TO阶段基础参数。
	 * @param conduct 分布计算命令
	 * @return 分配参数后的conduct命令
	 * @throws TaskException 检查和判断发现错误，弹出分布任务异常
	 */
	public abstract Conduct init(Conduct conduct) throws TaskException;

}

