/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.scan.*;
import com.laxcus.front.driver.mission.*;

/**
 * 扫描数据库命令调用器
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class DriverScanSchemaInvoker extends DriverScanReferenceInvoker {

	/**
	 * 构造扫描数据库命令调用器，指定命令
	 * @param mission 扫描数据库命令
	 */
	public DriverScanSchemaInvoker(DriverMission mission) {
		super(mission);
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