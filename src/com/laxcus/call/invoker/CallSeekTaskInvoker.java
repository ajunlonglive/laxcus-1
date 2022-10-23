/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.command.task.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.task.conduct.balance.*;
import com.laxcus.task.conduct.init.*;
import com.laxcus.task.contact.fork.*;
import com.laxcus.task.contact.merge.*;
import com.laxcus.task.establish.assign.*;
import com.laxcus.task.establish.issue.*;
import com.laxcus.util.naming.*;

/**
 * 检索分布任务组件站点调用器
 * 
 * @author scott.liang
 * @version 1.0 5/26/2017
 * @since laxcus 1.0
 */
public class CallSeekTaskInvoker extends CommonSeekTaskInvoker {

	/**
	 * 构造检索分布任务组件站点调用器，指定命令
	 * @param cmd - 检索分布任务组件站点
	 */
	public CallSeekTaskInvoker(SeekTask cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.CommonSeekTaskInvoker#obtain()
	 */
	@Override
	protected List<Phase> obtain() {
		ArrayList<Phase> array = new ArrayList<Phase>();
		array.addAll(InitTaskPool.getInstance().getPhases());
		array.addAll(BalanceTaskPool.getInstance().getPhases());
		array.addAll(IssueTaskPool.getInstance().getPhases());
		array.addAll(AssignTaskPool.getInstance().getPhases());
		array.addAll(ForkTaskPool.getInstance().getPhases());
		array.addAll(MergeTaskPool.getInstance().getPhases());
		return array;
	}

}
