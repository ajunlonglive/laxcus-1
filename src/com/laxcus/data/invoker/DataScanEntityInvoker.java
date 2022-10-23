/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.access.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.index.*;
import com.laxcus.command.scan.*;
import com.laxcus.log.client.*;

/**
 * 扫描数据块命令调用器
 * 
 * @author scott.liang
 * @version 1.0 1/18/2018
 * @since laxcus 1.0
 */
public class DataScanEntityInvoker extends DataInvoker {

	/**
	 * 构造扫描数据块命令调用器，指定命令
	 * @param cmd 扫描数据块命令
	 */
	public DataScanEntityInvoker(ScanEntity cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanEntity getCommand() {
		return (ScanEntity) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanEntity cmd = getCommand();
		Space space = cmd.getSpace();

		//		// 查找数据块总容量
		//		StubChartSheet sheet = StaffOnDataPool.getInstance()
		//				.findStubChartSheet(space);
		//
		//		// 返回结果
		//		ScanEntityItem item = new ScanEntityItem(getLocal(), space);
		//		if (sheet != null) {
		//			item.setLength(sheet.getAvailable());
		//			item.setStubs(sheet.getStubs());
		//		}

		ScanEntityItem item = new ScanEntityItem(getLocal(), space);

		// 实时查询
		StubArea area = AccessTrustor.findIndex(space);
		// 统计结果
		if (area != null) {
			long length = 0;
			for (StubItem sub : area.list()) {
				length += sub.getLength();
			}
			item.setLength(length);
			item.setStubs(area.size());
		}

		ScanEntityProduct product = new ScanEntityProduct(item);
		replyProduct(product);

		Logger.debug(this ,"launch", "stubs:%d, length:%d, size:%d", 
				item.getStubs(), item.getLength(), product.size());

		return useful();
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