/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.scan.*;

/**
 * 扫描数据表命令调用器
 * 
 * @author scott.liang
 * @version 1.0 5/29/2021
 * @since laxcus 1.0
 */
public class DesktopScanTableInvoker extends DesktopScanReferenceInvoker {

	/**
	 * 构造扫描数据表命令调用器
	 * @param cmd 扫描数据表命令
	 */
	public DesktopScanTableInvoker(ScanTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanTable getCommand() {
		return (ScanTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanTable cmd = getCommand();

		// 取出全部表名
		List<Space> spaces = getStaffPool().getSpaces();
		// 筛选出匹配的表名
		ArrayList<Space> array = new ArrayList<Space>();
		for (Space space : spaces) {
			if (cmd.contains(space)) {
				array.add(space);
			}
		}

		// 分发给CALL站点
		return distribute(array);
	}

}