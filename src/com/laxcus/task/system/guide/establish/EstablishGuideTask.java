/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.guide.establish;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.task.guide.*;
import com.laxcus.task.guide.parameter.*;
import com.laxcus.util.naming.*;

/**
 * 系统级分布数据构建引导任务。<br>
 * 本处提供空方法
 *
 * @author scott.liang
 * @version 1.0 8/31/2020
 * @since laxcus 1.0
 */
public class EstablishGuideTask extends GuideTask {

	/**
	 * 构造默认实例
	 */
	public EstablishGuideTask() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#getSocks()
	 */
	@Override
	public List<Sock> getSocks() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#isSupport(com.laxcus.util.naming.Sock)
	 */
	@Override
	public boolean isSupport(Sock sock) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#markup(com.laxcus.util.naming.Sock)
	 */
	@Override
	public InputParameterList markup(Sock sock) throws GuideTaskException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#create(com.laxcus.util.naming.Sock, com.laxcus.task.guide.parameter.InputParameterList)
	 */
	@Override
	public DistributedCommand create(Sock sock, InputParameterList list)
			throws GuideTaskException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#next(byte[], com.laxcus.util.naming.Sock)
	 */
	@Override
	public DistributedCommand next(byte[] predata, Sock sock)
			throws GuideTaskException {
		// TODO Auto-generated method stub
		return null;
	}

}