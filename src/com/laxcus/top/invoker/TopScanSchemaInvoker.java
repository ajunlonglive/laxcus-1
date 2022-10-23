/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.scan.*;
import com.laxcus.top.pool.*;

/**
 * 扫描数据库调用器。<br>
 * 调用器把命令扫描后，转发给HOME站点。
 * 
 * @author scott.liang
 * @version 1.0 12/2/2013
 * @since laxcus 1.0
 */
public class TopScanSchemaInvoker extends TopScanReferenceInvoker {

	/**
	 * 构造扫描数据库调用器，指定命令
	 * @param cmd 扫描数据库命令
	 */
	public TopScanSchemaInvoker(ScanSchema cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanSchema getCommand() {
		return (ScanSchema) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanSchema cmd = getCommand();

		// 取同全部数据表名
		List<Space> spaces = HomeOnTopPool.getInstance().getSpaces();
		// 筛选出符合要求的数据表名
		ArrayList<Space> array = new ArrayList<Space>();
		for (Space space : spaces) {
			if(cmd.contains(space.getSchema())) {
				array.add(space);
			}
		}
		
		// 发送给HOME站点
		return distribute(cmd, array);
	}

}