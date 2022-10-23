/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.to.evaluate;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.system.select.to.*;
import com.laxcus.task.system.select.util.*;

/**
 * “EVALUATE”模式的SELECT检索计算。<br>
 * 所有基于“EVALUATE”模式的检索操作从这里派生。
 * 
 * @author scott.liang
 * @version 1.0 9/23/2013
 * @since laxcus 1.0
 */
public abstract class SelectToEvaluateTask extends SQLToEvaluateTask {

	/** 列属性在显示集合中的排列顺序(包括列属性，和被SQL函数操作的列属性) **/
	protected Sheet indexSheet;

	/** SELECT实例 **/
	protected Select select;

	/**
	 * 构造默认的SELECT计算实例
	 */
	public SelectToEvaluateTask() {
		super();
	}

	/**
	 * 从会话中取出SELECT命令。分为两种情况：
	 * 1. 通过Session.getCommand() 获得。这是标准SELECT查询情况：GROUP BY/ORDER BY/DISTINCT语句块。
	 * 2. 在第1项条件不成立的情况下，通过会话中的自定义参数获得。这是嵌套查询情况下。
	 * 
	 * @return SELECT命令
	 * @throws ToTaskException
	 */
	protected Select fetchSelect() throws ToTaskException {
		// 从会话中取得SELECT命令
		ToSession session = getSession();
		Select cmd = (Select) session.getCommand();
		// 如果SELECT不在会话命令中，就是以自定义参数身份存在（嵌套查询等情况）
		if (cmd == null) {
			if (!session.hasParameter(SQLTaskKit.SELECT_OBJECT)) {
				throw new ToTaskException("cannot be find \"SELECT_OBJECT\"");
			}
			cmd = (Select) session.findCommand(SQLTaskKit.SELECT_OBJECT);
		}
		return cmd;
	}

}