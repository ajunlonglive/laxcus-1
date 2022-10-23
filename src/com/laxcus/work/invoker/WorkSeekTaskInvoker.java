/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.invoker;

import java.util.*;

import com.laxcus.command.task.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.contact.distant.*;
import com.laxcus.util.naming.*;

/**
 * 检索分布任务组件站点调用器
 * 
 * @author scott.liang
 * @version 1.0 5/26/2017
 * @since laxcus 1.0
 */
public class WorkSeekTaskInvoker extends CommonSeekTaskInvoker {

	/**
	 * 构造检索分布任务组件站点调用器，指定命令
	 * @param cmd - 检索分布任务组件站点
	 */
	public WorkSeekTaskInvoker(SeekTask cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.CommonSeekTaskInvoker#obtain()
	 */
	@Override
	protected List<Phase> obtain() {
		ArrayList<Phase> array = new ArrayList<Phase>();
		array.addAll(ToTaskPool.getInstance().getPhases());
		array.addAll(DistantTaskPool.getInstance().getPhases());
		return array;
	}

}
