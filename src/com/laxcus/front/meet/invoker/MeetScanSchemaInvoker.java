/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.scan.*;

/**
 * 扫描数据库命令调用器
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class MeetScanSchemaInvoker extends MeetScanReferenceInvoker {

	/**
	 * 构造扫描数据库命令调用器，指定命令
	 * @param cmd 扫描数据库命令
	 */
	public MeetScanSchemaInvoker(ScanSchema cmd) {
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

		// 取出全部表名
		List<Space> spaces = getStaffPool().getSpaces();
		// 筛选出匹配的表名
		ArrayList<Space> array = new ArrayList<Space>();
		for (Space space : spaces) {
			if (cmd.contains(space.getSchema())) {
				array.add(space);
			}
		}
		
		// 分发给CALL站点
		return distribute(array);
	}

}