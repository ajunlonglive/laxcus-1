/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.invoker;

import java.util.*;

import com.laxcus.access.*;
import com.laxcus.access.schema.*;
import com.laxcus.distribute.establish.command.*;
import com.laxcus.log.client.*;

/**
 * 释放SIFT数据资源命令调用器。<br>
 * 命令从CALL站点发出，目标是BUILD站点，BUILD站点根据命令要求，删除磁盘表空间下的数据资源。
 * 
 * 
 * @author scott.liang
 * @version 1.0 11/2/2012
 * @since laxcus 1.0
 */
public class BuildReleaseSiftSourceInvoker extends BuildInvoker {

	/**
	 * 构造释放SIFT数据资源命令调用器，指定命令
	 * @param cmd 释放SIFT数据资源命令
	 */
	public BuildReleaseSiftSourceInvoker(ReleaseSiftSource cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReleaseSiftSource getCommand() {
		return (ReleaseSiftSource) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ReleaseSiftSource cmd = getCommand();
		List<Space> all = cmd.list();

		// 释放磁盘数据资源
		int count = 0;
		for (Space space : all) {
			int ret = AccessTrustor.deleteSpace(space);
			boolean success = (ret >= 0);
			Logger.note(this, "launch", success, "delete space '%s' is %d", space, ret);
			if (success) {
				count++;
			}
		}

		boolean success = (count == all.size());
		
		Logger.debug(this, "launch", success, "resource size:%d, delete count:%d", all.size(), count);
		
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
